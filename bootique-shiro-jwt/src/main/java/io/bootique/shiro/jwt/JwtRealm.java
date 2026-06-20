/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.shiro.jwt;

import io.bootique.shiro.jwt.authz.AuthzServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.io.Parser;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 4.0
 */
public class JwtRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRealm.class);

    private final JwtParser tokenParser;
    private final Duration expiresIn;
    private final List<AuthzServer> authzServers;

    private volatile Map<Object, KeyAndServers> keyServerIndex;
    private volatile LocalDateTime loadTime;

    public JwtRealm(List<AuthzServer> authzServers, Duration expiresIn) {

        setName(JwtRealm.class.getSimpleName());
        setAuthenticationTokenClass(ShiroJsonWebToken.class);

        this.authzServers = authzServers;
        this.expiresIn = expiresIn;
        this.tokenParser = Jwts.parser()
                .keyLocator(h -> getKey(h.getOrDefault("kid", "")))
                .build();
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        principals
                .byType(JwtPrincipal.class)
                .forEach(p -> info.addRoles(authzServerOrThrow(p.kid(), p.claims()).getRoles(p.claims())));
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {

        String jwtPayload = ((ShiroJsonWebToken) token).getToken();

        Jwt<?, ?> jwt;
        try {
            jwt = tokenParser.parse(jwtPayload);
        }
        // not a trusted authority
        catch (UnsupportedJwtException e) {
            throw new AuthenticationException(e.getMessage());
        }

        Claims claims = jwt.accept(Jws.CLAIMS).getPayload();
        String kid = (String) jwt.getHeader().get("kid");

        // Find and validate the server that matches this token's audience
        AuthzServer server = authzServerOrThrow(kid, claims);

        JwtPrincipal principal = new JwtPrincipal(kid, claims, server.getMdcClaim());
        return new SimpleAuthenticationInfo(
                new SimplePrincipalCollection(principal, getName()),
                token.getCredentials());
    }

    private AuthzServer authzServerOrThrow(String kid, Claims claims) {
        // Must call "keysAndServers()" every time for this request instead of checking the map directly.
        // This way we can refresh the key map as defined by the expiration policy
        KeyAndServers ref = keysAndServers().get(kid);
        if (ref == null) {
            throw new AuthenticationException("JWT 'kid' does not match any known authorization server: " + kid);
        }

        // Try to find a server that accepts this token's audience
        for (AuthzServer server : ref.servers()) {
            if (server.matchesAudience(claims)) {
                return server;
            }
        }

        // No server accepted the audience
        Set<String> claimedAudience = claims.getAudience();
        if (claimedAudience == null || claimedAudience.isEmpty()) {
            throw new AuthenticationException("Token has no audience");
        }
        throw new AuthenticationException("Token has invalid audience: " + claimedAudience);
    }

    private Key getKey(Object kid) {
        // Must call "keysAndServers()" every time for this request instead of checking the map directly.
        // This way we can refresh the key map as defined by the expiration policy
        KeyAndServers ref = keysAndServers().get(kid);
        return ref != null ? ref.key().toKey() : null;
    }

    private Map<Object, KeyAndServers> keysAndServers() {
        if (shouldRefreshIndex()) {
            synchronized (this) {
                if (shouldRefreshIndex()) {
                    this.keyServerIndex = refreshIndex();
                    this.loadTime = LocalDateTime.now();
                }
            }
        }

        return this.keyServerIndex;
    }

    private boolean shouldRefreshIndex() {
        return keyServerIndex == null || LocalDateTime.now().isAfter(loadTime.plus(expiresIn));
    }

    private Map<Object, KeyAndServers> refreshIndex() {

        Map<Object, KeyAndServers> index = new HashMap<>();
        Parser<JwkSet> parser = Jwks.setParser().build();

        for (AuthzServer s : authzServers) {
            for (Jwk<?> k : loadKeys(parser, s.getJwkLocation())) {
                KeyAndServers existing = index.get(k.getId());

                if (existing == null) {
                    List<AuthzServer> servers = new ArrayList<>();
                    servers.add(s);
                    index.put(k.getId(), new KeyAndServers(k, servers));
                } else {
                    // All servers with the same kid must have the same actual key
                    if (!keysAreEqual(existing.key(), k)) {
                        throw new IllegalStateException(
                                "Multiple servers share key ID '" + k.getId() + "' but have different cryptographic keys");
                    }

                    existing.servers().add(s);
                }
            }
        }

        return index;
    }

    static boolean keysAreEqual(Jwk<?> key1, Jwk<?> key2) {
        return key1.toKey().equals(key2.toKey());
    }

    private static Set<Jwk<?>> loadKeys(Parser<JwkSet> parser, URL location) {
        try (InputStream is = location.openStream()) {
            Set<Jwk<?>> keys = parser.parse(is).getKeys();
            LOGGER.info("JWKS (re)loaded from {}", location);
            return keys;
        } catch (Exception e) {
            throw new IOException("Unable to load JWKS from " + location, e);
        }
    }

    record KeyAndServers(Jwk<?> key, List<AuthzServer> servers) {
    }
}
