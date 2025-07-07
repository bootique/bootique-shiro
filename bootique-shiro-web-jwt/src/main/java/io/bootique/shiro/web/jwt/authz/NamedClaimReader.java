package io.bootique.shiro.web.jwt.authz;


import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

class NamedClaimReader implements AuthzReader {

    private final String claim;
    private final Function<Object, List<String>> parser;

    public NamedClaimReader(String claim, Function<Object, List<String>> parser) {
        this.claim = Objects.requireNonNull(claim);
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public List<String> readAuthz(Claims claims) {
        return parser.apply(claims.get(claim));
    }
}
