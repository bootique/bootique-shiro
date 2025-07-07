package io.bootique.shiro.web.jwt;

import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.BearerToken;

/**
 * @since 4.0
 */
public class JwtBearerToken extends BearerToken {

    private final Claims claims;

    public JwtBearerToken(String token, String host, Claims claims) {
        super(token, host);
        this.claims = claims;
    }

    @Override
    public Object getPrincipal() {
        return claims;
    }

    @Override
    public Object getCredentials() {
        return "";
    }
}
