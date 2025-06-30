package io.bootique.shiro.web.jwt.auth;

import io.bootique.shiro.web.jwt.token.JwtToken;
import org.apache.shiro.authc.AuthenticationToken;

public class ShiroJwtAuthToken implements AuthenticationToken {

    private final JwtToken token;

    public ShiroJwtAuthToken(JwtToken jwtToken) {
        this.token = jwtToken;
    }

    public Object getPrincipal() {
        return this.token;
    }

    public Object getCredentials() {
        return this.token.getRoles();
    }
}
