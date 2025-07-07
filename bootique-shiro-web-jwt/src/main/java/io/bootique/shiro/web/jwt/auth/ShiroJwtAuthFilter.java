package io.bootique.shiro.web.jwt.auth;

import io.bootique.shiro.web.jwt.token.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;


public class ShiroJwtAuthFilter extends BearerHttpAuthenticationFilter {

    private final Provider<JwtTokenProvider> authenticator;

    public ShiroJwtAuthFilter(Provider<JwtTokenProvider> authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest,
                                              ServletResponse servletResponse) {
        AuthenticationToken bearerToken = super.createToken(servletRequest, servletResponse);
        return new ShiroJwtAuthToken(authenticator.get().getJwtToken(bearerToken.getPrincipal().toString()));
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        try {
            return super.executeLogin(request, response);
        } catch (JwtException e) {
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return false;
        }
    }
}
