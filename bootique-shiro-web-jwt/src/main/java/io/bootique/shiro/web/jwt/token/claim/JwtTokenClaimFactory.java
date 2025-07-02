package io.bootique.shiro.web.jwt.token.claim;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;

@BQConfig("Configuration of JWT Token Claim")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = JwtTokenClaimFactory.class)
@JsonTypeName("default")
public class JwtTokenClaimFactory implements PolymorphicConfiguration {

    private String name;

    @BQConfigProperty("JWT Token claim name")
    public void setName(String name) {
        this.name = name;
    }

    protected String getName() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("Claim name is not defined");
        }
        return name;
    }

    public JwtClaim<?> provideClaim() {
        return new StringListClaim(getName());
    }
}
