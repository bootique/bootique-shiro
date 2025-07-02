package io.bootique.shiro.web.jwt.token.claim;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;

import java.util.Map;

public abstract class JwtClaim<T,V> {

    private final String name;

    public JwtClaim(String name) {
        this.name = name;
    }

    protected String getName() {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    public final T parse(Claims claims) {
        String[] path = path();
        Object value = claims.get(path[0]);
        if (path.length == 1) {
            return parseValue((V) value);
        }
        int i = 0;
        while (i < path.length) {
            if (i == path.length - 1) {
                return parseValue((V) value);
            } else {
                if (value instanceof Map) {
                    value = ((Map<?,?>) value).get(path[i + 1]);
                } else {
                    throw new IOException("Unable to parse claim \"" + this.name + "\"");
                }
                i++;
            }
        }
        return emptyValue();
    }

    protected abstract T parseValue(V claimValue);

    protected abstract T emptyValue();

    private String[] path() {
        return this.name.trim().split("\\.");
    }

}
