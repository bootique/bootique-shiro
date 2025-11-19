/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.shiro.web.oidconnect;

import io.bootique.shiro.web.jwt.JwtBearerAuthenticationFilter;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @since 4.0
 */
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
        redirectToOpenIdLoginPage((HttpServletRequest) request, (HttpServletResponse) response);
    }

    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        try {
            redirectToOpenIdLoginPage((HttpServletRequest) request, (HttpServletResponse) response);
            return false;
        } catch (Exception e) {
            return super.sendChallenge(request, response);
        }
    }

    private void redirectToOpenIdLoginPage(HttpServletRequest request, HttpServletResponse response) throws Exception {

        StringBuilder redirectUrl = new StringBuilder();
        if (oidpUrl.startsWith("/")) {
            redirectUrl.append(request.getContextPath());
        }

        redirectUrl.append(oidpUrl)
                .append('?').append(OidConnect.RESPONSE_TYPE_PARAM).append('=').append(OidConnect.CODE_PARAM)
                .append('&').append(OidConnect.CLIENT_ID_PARAM).append('=').append(URLEncoder.encode(clientId, StandardCharsets.UTF_8))
                .append('&').append(OidConnect.REDIRECT_URI_PARAM).append('=').append(URLEncoder.encode(redirectUrl(request, callbackUri), StandardCharsets.UTF_8));

        response.sendRedirect(redirectUrl.toString());
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

        return url.append(callbackUri).append("?").append(OidConnect.START_URI_PARAM).append("=")
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
