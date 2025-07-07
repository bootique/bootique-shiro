package io.bootique.shiro.web.jwt;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.shiro.web.jwt.token.claim.JwtClaim;
import io.bootique.shiro.web.jwt.token.claim.JwtTokenClaimFactory;
import io.bootique.shiro.web.jwt.token.claim.StringListClaim;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonTypeName("tn")
public class TreeNameStringListClaimFactory extends JwtTokenClaimFactory {

    @Override
    public JwtClaim<?,?> provideClaim() {
        return new TreeNameStringListClaim(getName());
    }

    static class TreeNameStringListClaim extends StringListClaim {
        public TreeNameStringListClaim(String name) {
            super(name);
        }

        @SuppressWarnings("unchecked")
        public List<String> parse(Claims claims) {
            String[] path = path();
            Object value = claims.get(path[0]);
            if (path.length == 1) {
                return parseValue((List<String>) value);
            }
            int i = 0;
            while (i < path.length) {
                if (i == path.length - 1) {
                    return parseValue((List<String>) value);
                } else {
                    if (value instanceof Map<?,?>) {
                        value = ((Map<?, ?>) value).get(path[i + 1]);
                    } else {
                        throw new IOException("Unable to parse claim \"" + this.getName() + "\"");
                    }
                    i++;
                }
            }
            return Collections.emptyList();
        }

        protected final String[] path() {
            return this.getName().trim().split("\\.");
        }
    }
}
