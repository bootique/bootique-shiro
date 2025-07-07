package io.bootique.shiro.web.jwt.token.claim;

import java.util.Collections;
import java.util.List;

public class StringListClaim extends JwtClaim<List<String>, List<String>> {

    public StringListClaim(String name) {
        super(name);
    }

    @Override
    protected List<String> parseValue(List<String> claimValue) {
        return claimValue == null ? Collections.emptyList() : claimValue;
    }
}
