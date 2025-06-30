package io.bootique.shiro.web.jwt;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.shiro.web.jwt.token.JwtTokenProvider;
import io.bootique.shiro.web.jwt.token.JwtTokenProviderFactory;

@BQConfig("JWT Configuration")
public class ShiroWebJwtModuleFactory {

    private JwtTokenProviderFactory provider;

    @BQConfigProperty("JWT Token Provider Configuration")
    public void setProvider(JwtTokenProviderFactory provider) {
        this.provider = provider;
    }

    public JwtTokenProvider provideJwt() {
        if (provider == null) {
            throw new IllegalStateException("JWT Token Provider configuration is not defined");
        }
        return provider.provideJwt();
    }
}
