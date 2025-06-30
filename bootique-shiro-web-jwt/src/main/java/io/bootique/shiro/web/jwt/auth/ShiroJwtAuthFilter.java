package io.bootique.shiro.web.jwt.auth;

import io.bootique.shiro.web.jwt.token.JwtTokenProvider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;


public class ShiroJwtAuthFilter extends BearerHttpAuthenticationFilter {

    private final JwtTokenProvider authenticator;

    public ShiroJwtAuthFilter(JwtTokenProvider authenticator) {
        this.authenticator = authenticator;
    }

    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        request.setAttribute(DefaultSubjectContext.SESSION_CREATION_ENABLED, Boolean.FALSE);
        return super.onPreHandle(request, response, mappedValue);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest,
                                              ServletResponse servletResponse) {
        AuthenticationToken bearerToken = super.createToken(servletRequest, servletResponse);
        return new ShiroJwtAuthToken(authenticator.getJwtToken(bearerToken.getPrincipal().toString()));
    }

    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (!this.isLoginRequest(request, response)) {
            throw new UnauthenticatedException("Request is not authenticated");
        } else {
            this.executeLogin(request, response);
            return true;
        }
    }
}
