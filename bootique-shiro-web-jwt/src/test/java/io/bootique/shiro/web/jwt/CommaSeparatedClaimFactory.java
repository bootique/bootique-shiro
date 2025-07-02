package io.bootique.shiro.web.jwt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.shiro.web.jwt.token.claim.JwtClaim;
import io.bootique.shiro.web.jwt.token.claim.JwtTokenClaimFactory;
import io.bootique.shiro.web.jwt.token.claim.StringListClaim;

import java.util.Arrays;
import java.util.List;

@JsonTypeName("cs")
public class CommaSeparatedClaimFactory extends JwtTokenClaimFactory {

    @Override
    public JwtClaim<?> provideClaim() {
        return new SpaceSeparatedClaim(getName());
    }

    static class SpaceSeparatedClaim extends StringListClaim {
        public SpaceSeparatedClaim(String name) {
            super(name);
        }

        @Override
        protected List<String> parseValue(Object claimValue) {
            if (claimValue instanceof String) {
                return Arrays.asList(((String) claimValue).split(","));
            }
            throw new IllegalArgumentException("JWT Claim value is not space-separated string");
        }
    }
}
