package io.bootique.shiro.web.jwt.jjwt;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.value.Duration;

import java.net.URL;
import java.util.Objects;

/**
 * @since 4.0
 */
@BQConfig("Configuration of JWKS manager")
public class JwksManagerFactory {

    private static final java.time.Duration DEFAULT_EXPIRES_IN = java.time.Duration.ofDays(100 * 365);

    private ResourceFactory keyLocation;
    private Duration expiresIn;

    @BQConfigProperty("Jwks keys location")
    public void setKeyLocation(ResourceFactory keyLocation) {
        this.keyLocation = keyLocation;
    }

    @BQConfigProperty("Expiration interval when JWKS must be reloaded")
    public void setExpiresIn(Duration expiresIn) {
        this.expiresIn = expiresIn;
    }

    public JwksManager createManager() {
        return new JwksManager(getKeyLocation(), getExpiresIn());
    }

    private URL getKeyLocation() {
        return Objects.requireNonNull(keyLocation.getUrl(), "JWKS 'keyLocation' is not specified");
    }

    private java.time.Duration getExpiresIn() {
        return expiresIn != null ? expiresIn.getDuration() : DEFAULT_EXPIRES_IN;
    }
}
