package io.bootique.shiro.web.oidconnect;

import io.bootique.shiro.web.jwt.JwtBearerAuthenticationFilter;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;

import java.util.Arrays;

public class OidConnectFilter extends JwtBearerAuthenticationFilter implements OidConnect {

    private final String oidpUrl;
    private final String tokenCookie;
    private final String clientId;
    private final String callbackUri;

    public OidConnectFilter(Provider<JwtParser> tokenParser,
                            String audience,
                            String oidpUrl,
                            String tokenCookie,
                            String clientId,
                            String callbackUri) {
        super(tokenParser, audience);
        this.oidpUrl = oidpUrl;
        this.tokenCookie = tokenCookie;
        this.clientId = clientId;
        this.callbackUri = callbackUri;
    }

    @Override
    protected String getAuthzHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        Cookie[] cookies = httpRequest.getCookies();
        return cookies == null ? null : Arrays.stream(cookies)
                .filter(c -> c.getName().equals(tokenCookie))
                .findFirst()
                .map(c -> "Bearer " + c.getValue())
                .orElse(null);
    }

    protected void redirectIfNoAuth(ServletRequest request, ServletResponse response, Exception e) throws Exception {
        redirectToOpenIdLoginPage(request, response);
    }

    private void redirectToOpenIdLoginPage(ServletRequest request, ServletResponse response) throws Exception {
        WebUtils.issueRedirect(request, response, oidpUrl, OidConnectUtils.getOidpParametersMap(request, clientId, callbackUri));
    }

    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        try {
            redirectToOpenIdLoginPage(request, response);
            return false;
        } catch (Exception e) {
            return super.sendChallenge(request, response);
        }
    }
}
