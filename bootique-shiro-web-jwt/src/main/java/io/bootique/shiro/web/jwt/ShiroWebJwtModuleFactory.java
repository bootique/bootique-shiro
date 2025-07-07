package io.bootique.shiro.web.jwt;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.shiro.web.jwt.jjwt.JwksManager;
import io.bootique.shiro.web.jwt.realm.ShiroJwtAuthRealm;
import io.bootique.shiro.web.jwt.jjwt.JwksManagerFactory;
import io.bootique.shiro.web.jwt.realm.AuthzReaderFactory;
import io.bootique.shiro.web.jwt.realm.JsonListAuthzReaderFactory;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Jwk;

import java.security.Key;
import java.util.Objects;

/**
 * @since 4.0
 */
@BQConfig("JWT Configuration")
public class ShiroWebJwtModuleFactory {

    private JwksManagerFactory jwk;
    private AuthzReaderFactory roles;

    @BQConfigProperty("Configured JWK")
    public void setJwk(JwksManagerFactory jwk) {
        this.jwk = jwk;
    }

    @BQConfigProperty("Configures JWT roles parser")
    public void setRoles(AuthzReaderFactory roles) {
        this.roles = roles;
    }

    public JwtParser createTokenParser() {
        JwksManager jwksManager = getJwk().createManager();
        return Jwts.parser()
                .keyLocator(h -> locateKey(jwksManager, h))
                .build();
    }

    public ShiroJwtAuthRealm createRealm() {
        return new ShiroJwtAuthRealm(getRoles().createReader());
    }

    private JwksManagerFactory getJwk() {
        return Objects.requireNonNull(jwk, "'jwk' configuration is not specified");
    }

    private AuthzReaderFactory getRoles() {
        return roles != null ? roles : new JsonListAuthzReaderFactory();
    }

    private Key locateKey(JwksManager manager, Header header) {
        // must call "getJwks()" every time we process a header. This way manager can refresh the list of keys
        // if needed
        Jwk<?> jwk = manager.getJwks().get(header.getOrDefault("kid", ""));
        return jwk != null ? jwk.toKey() : null;
    }
}
