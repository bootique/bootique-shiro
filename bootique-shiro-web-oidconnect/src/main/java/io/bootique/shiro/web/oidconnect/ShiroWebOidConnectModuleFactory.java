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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jackson.JacksonService;
import io.bootique.jersey.MappedResource;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * @since 4.0
 */
@BQConfig("OpenID Connect Configuration")
public class ShiroWebOidConnectModuleFactory {

    private static final String DEFAULT_TOKEN_COOKIE = "bq-shiro-oid";
    private static final String DEFAULT_CALLBACK_URL = "/bq-shiro-oauth-callback";

    private String oidpUrl;
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String tokenCookie;
    private String callbackUri;

    private final Provider<JwtParser> tokenParser;
    private final JacksonService jacksonService;

    @Inject
    public ShiroWebOidConnectModuleFactory(Provider<JwtParser> tokenParser, JacksonService jacksonService) {
        this.tokenParser = tokenParser;
        this.jacksonService = jacksonService;
    }

    @BQConfigProperty("OpenId Connect Login Url")
    public void setOidpUrl(String oidpUrl) {
        this.oidpUrl = oidpUrl;
    }

    @BQConfigProperty("JWT Token Url")
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    @BQConfigProperty("Client Id")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @BQConfigProperty("Client Secret")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @BQConfigProperty("Token Cookie Name")
    public void setTokenCookie(String tokenCookie) {
        this.tokenCookie = tokenCookie;
    }

    @BQConfigProperty("""
            A URI of the authorization code handler. The handler itself is internally published by Bootique at this URL.
            It should be relative to the application URL "context". The parameter is optional and if not specified 
            it will be set to '/bq-shiro-oauth-callback'""")
    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }

    public OidConnectFilter createFilter(String audience) {
        return new OidConnectFilter(tokenParser, audience, getOidpUrl(), getTokenCookie(), getClientId(), getCallbackUri());
    }

    public MappedResource<AuthorizationCodeHandlerApi> createAuthorizationCodeHandler(String audience) {
        AuthorizationCodeHandlerApi api = new AuthorizationCodeHandlerApi(
                jacksonService.newObjectMapper(),
                getTokenCookie(),
                getTokenUrl(),
                getClientId(),
                getClientSecret(),
                audience,
                getOidpUrl(),
                getCallbackUri());

        return new MappedResource<>(api, getCallbackUri());
    }

    private String getOidpUrl() {

        if (this.oidpUrl == null || this.oidpUrl.isEmpty()) {
            throw new IllegalArgumentException("OpenId Connect Login Url property is not defined");
        }

        return this.oidpUrl;
    }

    private String getTokenUrl() {

        if (this.tokenUrl == null || this.tokenUrl.isEmpty()) {
            throw new IllegalArgumentException("Token Url property is not defined");
        }

        return this.tokenUrl;
    }

    private String getTokenCookie() {
        return tokenCookie == null || tokenCookie.isEmpty() ? DEFAULT_TOKEN_COOKIE : tokenCookie;
    }

    private String getClientId() {
        if (this.clientId == null || this.clientId.isEmpty()) {
            throw new IllegalArgumentException("Client Id property is not defined");
        }
        return this.clientId;
    }

    private String getClientSecret() {

        if (this.clientSecret == null || this.clientSecret.isEmpty()) {
            throw new IllegalArgumentException("Client Secret property is not defined");
        }

        return this.clientSecret;
    }

    private String getCallbackUri() {
        // TODO: the default will only work if the JAX-RS context is "/"... Append Jersey servlet context to the default
        //  to make it work universally
        return callbackUri == null || callbackUri.isEmpty() ? DEFAULT_CALLBACK_URL : callbackUri;
    }
}
