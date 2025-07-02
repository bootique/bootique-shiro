package io.bootique.shiro.web.jwt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.shiro.web.jwt.token.claim.JwtClaim;
import io.bootique.shiro.web.jwt.token.claim.JwtTokenClaimFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonTypeName("ss")
public class SpaceSeparatedClaimFactory extends JwtTokenClaimFactory {

    @Override
    public JwtClaim<?,?> provideClaim() {
        return new SpaceSeparatedClaim(getName());
    }

    static class SpaceSeparatedClaim extends JwtClaim<List<String>, String> {
        public SpaceSeparatedClaim(String name) {
            super(name);
        }

        @Override
        protected List<String> parseValue(String claimValue) {
            return Arrays.asList(claimValue.split(" "));
        }

        @Override
        protected List<String> emptyValue() {
            return Collections.emptyList();
        }
    }
}
