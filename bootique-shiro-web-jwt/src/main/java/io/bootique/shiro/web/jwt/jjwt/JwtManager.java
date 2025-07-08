package io.bootique.shiro.web.jwt.jjwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import org.apache.shiro.authc.AuthenticationException;

import java.util.Collection;

public class JwtManager {

    private static final String AUDIENCE_CLAIM_NAME = "aud";

    private final JwtParser tokenParser;
    private final String audience;

    public JwtManager(JwtParser tokenParser, String audience) {
        this.tokenParser = tokenParser;
        this.audience = audience;
    }

    public Claims parse(String token) throws AuthenticationException {
        Claims claims = tokenParser.parse(token).accept(Jws.CLAIMS).getPayload();
        if (audience != null && !audience.isEmpty()) {
            Object tokenAudience = claims.get(AUDIENCE_CLAIM_NAME);
            if (tokenAudience instanceof Collection<?> && ((Collection<?>) tokenAudience).contains(this.audience)) {
                return claims;
            }
            throw new AuthenticationException("Invalid audience");
        }
        return claims;
    }
}
