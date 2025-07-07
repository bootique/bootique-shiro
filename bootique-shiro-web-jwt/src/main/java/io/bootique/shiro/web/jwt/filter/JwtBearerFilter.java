package io.bootique.shiro.web.jwt.filter;

import io.bootique.shiro.web.jwt.JwtBearerToken;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Authenticates request based on a Bearer JWT authorization header. Doesn't check any roles or permissions itself,
 * instead parsing and validating the token, and passing it down to the downstream realms.
 *
 * @since 4.0
 */
public class JwtBearerFilter extends BearerHttpAuthenticationFilter {

    private final Provider<JwtParser> tokenParser;

    public JwtBearerFilter(Provider<JwtParser> tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        BearerToken bearer = (BearerToken) super.createToken(servletRequest, servletResponse);
        return new JwtBearerToken(
                bearer.getToken(),
                bearer.getHost(),
                tokenParser.get().parse(bearer.getToken()).accept(Jws.CLAIMS).getPayload());
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
