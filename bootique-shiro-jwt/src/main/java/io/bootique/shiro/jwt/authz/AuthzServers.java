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
package io.bootique.shiro.jwt.authz;

import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.io.Parser;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;
import io.jsonwebtoken.security.Jwks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since 4.0
 */
public class AuthzServers {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthzServers.class);

    private final Duration expiresIn;
    private final List<AuthzServer> authzServers;

    private volatile Map<Object, KeyAndServer> keyServerIndex;
    private volatile LocalDateTime loadTime;

    public AuthzServers(List<AuthzServer> authzServers, Duration expiresIn) {
        this.authzServers = authzServers;
        this.expiresIn = expiresIn;
    }

    public AuthzServer getServer(String kid) {
        // Must call "keyServerRefs()" every time for this request instead of checking the map directly.
        // This way we can refresh the key map as defined by the expiration policy
        KeyAndServer ref = keysAndServers().get(kid);
        return ref != null ? ref.server() : null;
    }

    public Key getKey(Object kid) {
        // Must call "keysAndServers()" every time for this request instead of checking the map directly.
        // This way we can refresh the key map as defined by the expiration policy
        KeyAndServer ref = keysAndServers().get(kid);
        return ref != null ? ref.key().toKey() : null;
    }

    private Map<Object, KeyAndServer> keysAndServers() {
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

    private Map<Object, KeyAndServer> refreshIndex() {

        Map<Object, KeyAndServer> index = new HashMap<>();
        Parser<JwkSet> parser = Jwks.setParser().build();

        for (AuthzServer s : authzServers) {
            // TODO: does it make sense to check for duplicate key ids across multiple authz servers?
            loadKeys(parser, s.getJwkLocation()).forEach(k -> index.put(k.getId(), new KeyAndServer(k, s)));
        }

        return index;
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

    record KeyAndServer(Jwk<?> key, AuthzServer server) {
    }
}
