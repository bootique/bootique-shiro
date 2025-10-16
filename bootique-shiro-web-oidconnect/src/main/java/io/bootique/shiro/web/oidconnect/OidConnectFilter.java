package io.bootique.shiro.web.oidconnect;

import io.bootique.shiro.web.jwt.JwtBearerAuthenticationFilter;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class OidConnectFilter extends JwtBearerAuthenticationFilter {

    private final String oidpUrl;
    private final String tokenCookie;
    private final String clientId;
    private final String callbackUri;

    public OidConnectFilter(
            Provider<JwtParser> tokenParser,
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
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        return cookies == null ? null : Arrays.stream(cookies)
                .filter(c -> c.getName().equals(tokenCookie))
                .findFirst()

                // TODO: ugly... we take a token from the cookie and pretend it is a "Bearer" auth header. Wish Shiro
                //  inheritance wasn't as deep and limiting
                .map(c -> "Bearer " + c.getValue())
                .orElse(null);
    }

    @Override
    protected void redirectIfNoAuth(ServletRequest request, ServletResponse response, Exception e) throws Exception {
        redirectToOpenIdLoginPage(request, response);
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

    private void redirectToOpenIdLoginPage(ServletRequest request, ServletResponse response) throws Exception {

        // using a map with predictable entry order so we can test the URLs
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(OidConnect.RESPONSE_TYPE_PARAM, OidConnect.CODE_PARAM);
        params.put(OidConnect.CLIENT_ID_PARAM, clientId);
        params.put(OidConnect.REDIRECT_URI_PARAM, redirectUrl((HttpServletRequest) request, callbackUri));

        // TODO: just do raw redirects via the Servlet API, don't rely on non-transparent WebUtils.issueRedirect(..)
        WebUtils.issueRedirect(request, response, oidpUrl, params);
    }

    private static String redirectUrl(HttpServletRequest request, String callbackUri) {

        StringBuffer url = request.getRequestURL();

        // truncate the path from the URL. We'll replace it with a callbac path
        url.setLength(url.length() - request.getRequestURI().length());

        url.append(request.getContextPath());

        // "callbackUri" is relative to the webapp context
        if (!callbackUri.startsWith("/")) {
            url.append("/");
        }

        return url.append(callbackUri).append("?").append(OidConnect.ORIGINAL_URI_PARAM).append("=")
                .append(URLEncoder.encode(postAuthRedirectUrl(request), StandardCharsets.UTF_8)).toString();
    }

    private static String postAuthRedirectUrl(HttpServletRequest request) {

        StringBuffer url = request.getRequestURL();
        String qs = request.getQueryString();

        if (qs != null) {
            url.append('?').append(qs);
        }

        return url.toString();
    }
}
