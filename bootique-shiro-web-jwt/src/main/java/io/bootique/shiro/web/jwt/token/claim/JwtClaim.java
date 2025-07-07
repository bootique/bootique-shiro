package io.bootique.shiro.web.jwt.token.claim;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;

public abstract class JwtClaim<T,V> {

    private final String name;

    public JwtClaim(String name) {
        this.name = name;
    }

    protected String getName() {
        return this.name;
    }


    @SuppressWarnings("unchecked")
    public T parse(Claims claims) {
        try {
            V value = (V) claims.get(getName());
            return parseValue(value);
        } catch (Exception e) {
            throw new IOException("Unable to parse claim \"" + getName() + "\"");
        }
    }

    protected abstract T parseValue(V claimValue);
}
