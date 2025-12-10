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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.MappedResource;
import io.bootique.jetty.MappedServlet;
import io.bootique.jetty.servlet.ServletEnvironment;
import io.bootique.shiro.jwt.JwtRealm;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * @since 4.0
 */
@BQConfig("OpenID Connect (OIDC) Configuration")
public class ShiroWebOidcModuleFactory {

    private static final String DEFAULT_TOKEN_COOKIE = "bq-shiro-oidc";
    private static final String DEFAULT_AUTH_HANDLER_PATH = "/bq-shiro-oauth-callback";

    private String oidpUrl;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String tokenCookie;
    private String callbackUri;
    private String scope;

    private final ServletEnvironment servletEnv;
    private final Provider<MappedServlet<ServletContainer>> jerseyServlet;
    private final Provider<JwtParser> tokenParser;
    private final JacksonService jacksonService;
    @Inject
    public ShiroWebOidcModuleFactory(
            ServletEnvironment servletEnv,
            Provider<MappedServlet<ServletContainer>> jerseyServlet,
            JwtRealm realm,
            Provider<JwtParser> tokenParser,
            JacksonService jacksonService) {
        this.servletEnv = servletEnv;
        this.jerseyServlet = jerseyServlet;
        this.tokenParser = tokenParser;
        this.jacksonService = jacksonService;
    }

    @BQConfigProperty("OIDC identity provider login URL")
    public ShiroWebOidcModuleFactory setOidpUrl(String oidpUrl) {
        this.oidpUrl = oidpUrl;
        return this;
    }

    @BQConfigProperty("An OAuth token server used internally to obtain an access token in exchange for an authorization code")
    public ShiroWebOidcModuleFactory setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
        return this;
    }

    @BQConfigProperty("OAuth client id used to request authorization code and then exchange it for an access token")
    public ShiroWebOidcModuleFactory setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @BQConfigProperty("OAuth client secret used to obtain an access token in exchange for an authorization code")
    public ShiroWebOidcModuleFactory setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @BQConfigProperty("Token Cookie Name")
    public ShiroWebOidcModuleFactory setTokenCookie(String tokenCookie) {
        this.tokenCookie = tokenCookie;
        return this;
    }

    @BQConfigProperty("""
            An optional path of the authorization code handler. If specified, the handler will be internally published
            at that path by "bootique-shiro" within the current app. It should be relative to the JAX-RS base "context"
            (the context may be something like "/myapp/api"; 'callbackUri' should be the part that follows that). The
            parameter is optional and by default will be set to '/bq-shiro-oauth-callback'""")
    public ShiroWebOidcModuleFactory setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
        return this;
    }

    @BQConfigProperty("An optional scope. If specified, it will be passed to oidp login page")
    public ShiroWebOidcModuleFactory setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public OidpRouter createOidpRouter() {
        return new OidpRouter(servletEnv, jerseyServlet, getOidpUrl(), getClientId(), getAuthCodeHandlerPath(), scope);
    }

    public OidcFilter createFilter(OidpRouter oidpRouter) {
        return new OidcFilter(tokenParser, oidpRouter, getTokenCookie());
    }

    public MappedResource<AuthorizationCodeHandlerApi> createAuthorizationCodeHandler(OidpRouter oidpRouter) {
        AuthorizationCodeHandlerApi api = new AuthorizationCodeHandlerApi(
                jacksonService.newObjectMapper(),
                oidpRouter,
                getTokenCookie(),
                getTokenUrl(),
                getClientId(),
                getClientSecret());

        return new MappedResource<>(api, getAuthCodeHandlerPath());
    }

    private String getOidpUrl() {

        if (this.oidpUrl == null || this.oidpUrl.isEmpty()) {
            throw new IllegalArgumentException("OIDC 'oidpUrl' property is not defined");
        }

        return this.oidpUrl;
    }

    private String getTokenUrl() {

        if (this.tokenUrl == null || this.tokenUrl.isEmpty()) {
            throw new IllegalArgumentException("OIDC 'tokenUrl' property is not defined");
        }

        return this.tokenUrl;
    }

    private String getTokenCookie() {
        return tokenCookie == null || tokenCookie.isEmpty() ? DEFAULT_TOKEN_COOKIE : tokenCookie;
    }

    private String getClientId() {
        if (this.clientId == null || this.clientId.isEmpty()) {
            throw new IllegalArgumentException("OIDC 'clientId' property is not defined");
        }
        return this.clientId;
    }

    private String getClientSecret() {

        if (this.clientSecret == null || this.clientSecret.isEmpty()) {
            throw new IllegalArgumentException("Client Secret property is not defined");
        }

        return this.clientSecret;
    }

    private String getAuthCodeHandlerPath() {

        if (callbackUri == null || callbackUri.isEmpty()) {
            // callbackUri is expected to be relative to the Jersey servlet, so no need to prepend the default with anything
            return DEFAULT_AUTH_HANDLER_PATH;
        }

        // attempt at URL normalization
        return callbackUri.startsWith("/") ? callbackUri : "/" + callbackUri;
    }
}
