package io.bootique.shiro.web.jwt.keys;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.value.Duration;
import java.net.URL;

@BQConfig("Configuration of Jwk provider")
public class JwksProviderFactory {

    private ResourceFactory target;
    private Duration expiresIn;

    @BQConfigProperty("Jwks keys target")
    public void setTarget(ResourceFactory target) {
        this.target = target;
    }

    public void setExpiresIn(Duration expiresIn) {
        this.expiresIn = expiresIn;
    }

    private URL getTarget() {
        if (this.target == null) {
            throw new IllegalStateException("Jwks target is not defined");
        }
        return target.getUrl();
    }

    public JwksProvider provideJwk() {
        return new JwksProvider(getTarget(), this.expiresIn);
    }
}
