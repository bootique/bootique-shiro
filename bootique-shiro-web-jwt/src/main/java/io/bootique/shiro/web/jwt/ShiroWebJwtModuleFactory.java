package io.bootique.shiro.web.jwt;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.shiro.web.jwt.jjwt.JwtParserMaker;
import io.bootique.shiro.web.jwt.realm.AuthzReaderFactory;
import io.bootique.shiro.web.jwt.realm.JsonListAuthzReaderFactory;
import io.bootique.shiro.web.jwt.realm.ShiroJwtAuthRealm;
import io.bootique.value.Duration;
import io.jsonwebtoken.JwtParser;

import java.net.URL;
import java.util.Objects;

/**
 * @since 4.0
 */
@BQConfig("JWT Configuration")
public class ShiroWebJwtModuleFactory {

    private static final java.time.Duration DEFAULT_JWK_EXPIRES_IN = java.time.Duration.ofDays(100 * 365);

    private ResourceFactory jwkLocation;
    private Duration jwkExpiresIn;
    private AuthzReaderFactory roles;

    @BQConfigProperty("Jwks key file location")
    public ShiroWebJwtModuleFactory setJwkLocation(ResourceFactory jwkLocation) {
        this.jwkLocation = jwkLocation;
        return this;
    }

    @BQConfigProperty("Expiration interval when JWKS must be reloaded")
    public ShiroWebJwtModuleFactory setJwkExpiresIn(Duration jwkExpiresIn) {
        this.jwkExpiresIn = jwkExpiresIn;
        return this;
    }

    @BQConfigProperty("Configures JWT roles parser")
    public ShiroWebJwtModuleFactory setRoles(AuthzReaderFactory roles) {
        this.roles = roles;
        return this;
    }

    public JwtParser createTokenParser() {
        return JwtParserMaker.createParser(getJwkLocation(), getJwkExpiresIn());
    }

    public ShiroJwtAuthRealm createRealm() {
        return new ShiroJwtAuthRealm(getRoles().createReader());
    }

    private AuthzReaderFactory getRoles() {
        return roles != null ? roles : new JsonListAuthzReaderFactory();
    }

    private URL getJwkLocation() {
        return Objects.requireNonNull(jwkLocation, "JWKS 'keyLocation' is not specified").getUrl();
    }

    private java.time.Duration getJwkExpiresIn() {
        return jwkExpiresIn != null ? jwkExpiresIn.getDuration() : DEFAULT_JWK_EXPIRES_IN;
    }
}
