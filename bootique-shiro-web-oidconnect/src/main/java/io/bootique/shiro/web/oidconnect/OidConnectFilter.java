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
import java.util.Enumeration;
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
        WebUtils.issueRedirect(request, response, oidpUrl, oidpParams(request));
    }

    private Map<String, Object> oidpParams(ServletRequest request) {

        // using a map with predictable entry order so we can test the URLs
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(OidConnect.RESPONSE_TYPE_PARAM, OidConnect.CODE_PARAM);
        params.put(OidConnect.CLIENT_ID_PARAM, clientId);
        params.put(OidConnect.REDIRECT_URI_PARAM, redirectUri((HttpServletRequest) request, callbackUri));
        return params;
    }

    private static String redirectUri(HttpServletRequest request, String callbackUri) {
        StringBuffer requestUrl = request.getRequestURL();
        int requestUrlLen = requestUrl.length();

        //
        String baseUri = requestUrl
                .substring(0, requestUrlLen - request.getRequestURI().length())
                + request.getContextPath();

        StringBuilder postAuthRedirectUri = new StringBuilder();
        Enumeration<String> parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            if (postAuthRedirectUri.isEmpty()) {
                postAuthRedirectUri.append(request.getRequestURI()).append("?");
            } else {
                postAuthRedirectUri.append("&");
            }
            postAuthRedirectUri.append(parameter).append("=").append(request.getParameter(parameter));
        }
        if (postAuthRedirectUri.isEmpty()) {
            postAuthRedirectUri.append(request.getRequestURI());
        }

        return baseUri + resolveCallbackUri(callbackUri) + "?" + OidConnect.ORIGINAL_URI_PARAM + "=" + URLEncoder.encode(
                postAuthRedirectUri.toString(), StandardCharsets.UTF_8);
    }

    private static String resolveCallbackUri(String callbackUri) {
        return callbackUri.startsWith("/") ? callbackUri : "/" + callbackUri;
    }
}
