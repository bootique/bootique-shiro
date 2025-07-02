package io.bootique.shiro.web.jwt.keys;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.value.Duration;
import java.net.URL;

@BQConfig("Configuration of Jwk provider")
public class JwksProviderFactory {

    private ResourceFactory keyLocation;
    private Duration expiresIn;

    @BQConfigProperty("Jwks keys target")
    public void setKeyLocation(ResourceFactory keyLocation) {
        this.keyLocation = keyLocation;
    }

    public void setExpiresIn(Duration expiresIn) {
        this.expiresIn = expiresIn;
    }

    private URL getKeyLocation() {
        if (this.keyLocation == null) {
            throw new IllegalStateException("Jwks key location is not defined");
        }
        return keyLocation.getUrl();
    }

    public JwksProvider provideJwk() {
        return new JwksProvider(getKeyLocation(), this.expiresIn);
    }
}
