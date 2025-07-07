package io.bootique.shiro.web.jwt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.shiro.web.jwt.token.claim.JwtClaim;
import io.bootique.shiro.web.jwt.token.claim.JwtTokenClaimFactory;

import java.util.Arrays;
import java.util.List;

@JsonTypeName("cs")
public class CommaSeparatedClaimFactory extends JwtTokenClaimFactory {

    @Override
    public JwtClaim<?,?> provideClaim() {
        return new CommaSeparatedClaim(getName());
    }

    static class CommaSeparatedClaim extends JwtClaim<List<String>, String> {
        public CommaSeparatedClaim(String name) {
            super(name);
        }

        @Override
        protected List<String> parseValue(String claimValue) {
            return Arrays.asList(claimValue.split(","));
        }
    }
}
