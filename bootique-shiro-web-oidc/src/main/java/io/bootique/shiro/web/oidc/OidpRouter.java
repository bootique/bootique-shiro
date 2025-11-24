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
package io.bootique.shiro.web.oidc;

import io.bootique.jetty.MappedServlet;
import io.bootique.jetty.servlet.ServletEnvironment;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Generates URLs for OID Connect authorization flow.
 *
 * @since 4.0
 */
public class OidpRouter {

    /**
     * The URI of the application request that originated the OID sequence. Not a part of the OAuth standard, it is
     * appended to the "redirect_uri" when OID authentication flow is initially triggered. It is passed through all the
     * redirects, and is finally used by the {@link AuthorizationCodeHandlerApi} to send the user to where they started from.
     */
    static final String INITIAL_URI_PARAM = "initial_uri";

    private final ServletEnvironment env;
    private final Provider<MappedServlet<ServletContainer>> jerseyServlet;
    private final String oidpBaseUrl;
    private final String clientIdEnc;
    // a path relative to the Jersey servlet context
    private final String authCodeHandlerPath;

    public OidpRouter(
            ServletEnvironment env,
            Provider<MappedServlet<ServletContainer>> jerseyServlet,
            String oidpBaseUrl,
            String clientId,
            String authCodeHandlerPath) {

        this.env = env;
        this.jerseyServlet = jerseyServlet;
        this.oidpBaseUrl = oidpBaseUrl;
        this.clientIdEnc = URLEncoder.encode(clientId, StandardCharsets.UTF_8);
        this.authCodeHandlerPath = authCodeHandlerPath;
    }

    public String oidpUrlReturningToCurrentRequest() {
        return oidpUrlReturningToUrl(currentRequestUrl());
    }

    public String oidpUrlReturningToUrl(String initialUrl) {

        // "authCodeHandlerUrl" assumes that auth code callback endpoint is deployed in the same webapp as the filter
        String authCodeHandlerUrlEnc = URLEncoder.encode(authCodeHandlerUrl(initialUrl), StandardCharsets.UTF_8);

        return new StringBuilder(oidpBaseUrl)
                .append("?response_type=code")
                .append("&client_id=").append(clientIdEnc)
                .append("&redirect_uri=").append(authCodeHandlerUrlEnc)
                .toString();
    }

    private String authCodeHandlerUrl(String initialUrl) {

        String initialUrlEnc = URLEncoder.encode(initialUrl, StandardCharsets.UTF_8);

        HttpServletRequest request = requestInProgress();
        StringBuffer url = request.getRequestURL();

        // Extract the (protocol + host + port) from the current request URL. Generated URL is based on the assumption
        // that auth code callback endpoint is deployed in the same webapp as the filter and hence shares the
        // (protocol + host + port)

        url.setLength(url.length() - request.getRequestURI().length());

        return url
                .append(request.getContextPath())
                .append(getJerseyServletPrefix())
                .append(authCodeHandlerPath)
                .append("?")
                .append(OidpRouter.INITIAL_URI_PARAM).append("=").append(initialUrlEnc)
                .toString();
    }

    private String getJerseyServletPrefix() {
        // TODO: cache after the first access?
        return jerseyServlet.get().
                getUrlPatterns()
                .stream()
                .map(OidpRouter::toJerseyServletPrefix)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to map auth code handler endpoint."));
    }

    static String toJerseyServletPrefix(String pattern) {
        if (pattern == null) {
            return null;
        }

        // TODO: this is a valid mapping, but Jersey servlet seems to be confused by it and not able to find a
        //  matching resource. So we are letting it through, but it will likely not work at all and not because of us.
        if (pattern.startsWith("*.")) {
            return "";
        }

        if (pattern.endsWith("/*")) {
            return pattern.substring(0, pattern.length() - 2);
        }

        return pattern;
    }

    private String currentRequestUrl() {

        HttpServletRequest request = requestInProgress();

        // In theory, we don't need the "https://<host>" part here, as current request URL is based off of the
        //  same server as the auth code handler that will ultimately send the user to this URL. Still, providing the
        //  full host name feels more robust
        StringBuffer url = request.getRequestURL();

        String qs = request.getQueryString();
        if (qs != null) {
            url.append('?').append(qs);
        }

        return url.toString();
    }

    private HttpServletRequest requestInProgress() {
        return env
                .request()
                .orElseThrow(() -> new RuntimeException("Called outside of a web request"));
    }
}
