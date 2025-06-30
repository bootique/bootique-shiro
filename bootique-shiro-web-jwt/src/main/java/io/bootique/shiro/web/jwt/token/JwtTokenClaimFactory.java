package io.bootique.shiro.web.jwt.token;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

@BQConfig("Configuration of JWT Token Claim")
public class JwtTokenClaimFactory {

    private String name;

    private String regexp;

    @BQConfigProperty("JWT Token claim name")
    public void setName(String name) {
        this.name = name;
    }

    @BQConfigProperty("JWT Token claim value parsing regexp")
    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    private String getName() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("Claim name is not defined");
        }
        return name;
    }

    JwtTokenClaim provideClaim() {
        return new JwtTokenClaim(getName(), this.regexp);
    }
}
