package io.bootique.shiro.web.jwt.token.claim;

import java.util.Collections;
import java.util.List;

public class StringListClaim extends JwtClaim<List<String>> {

    public StringListClaim(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<String> parseValue(Object claimValue) {
        if (claimValue instanceof List) {
            return (List<String>) claimValue;
        }
        throw new IllegalArgumentException("JWT Claim value is not list");
    }

    @Override
    protected List<String> emptyValue() {
        return Collections.emptyList();
    }
}
