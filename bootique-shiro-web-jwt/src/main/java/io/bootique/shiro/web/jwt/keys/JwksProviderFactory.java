package io.bootique.shiro.web.jwt.keys;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.value.Duration;
import java.net.URL;

@BQConfig("Configuration of Jwk provider")
public class JwksProviderFactory {

    private static final Duration DEFAULT_EXPIRES_IN = new Duration((100*356) + " days");

    private ResourceFactory keyLocation;
    private Duration expiresIn;

    @BQConfigProperty("Jwks keys key localtion")
    public void setKeyLocation(ResourceFactory keyLocation) {
        this.keyLocation = keyLocation;
    }

    @BQConfigProperty("Expiration interval when jwks must be reloaded")
    public void setExpiresIn(Duration expiresIn) {
        this.expiresIn = expiresIn;
    }

    private URL getKeyLocation() {
        if (this.keyLocation == null) {
            throw new IllegalStateException("Jwks key location is not defined");
        }
        return keyLocation.getUrl();
    }

    private Duration getExpiresIn() {
        return this.expiresIn == null ? DEFAULT_EXPIRES_IN : this.expiresIn;
    }

    public JwksProvider provideJwk() {
        return new JwksProvider(getKeyLocation(), getExpiresIn());
    }
}
