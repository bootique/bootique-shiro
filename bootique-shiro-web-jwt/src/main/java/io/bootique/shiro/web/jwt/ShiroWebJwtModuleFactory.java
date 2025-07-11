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
package io.bootique.shiro.web.jwt;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;
import io.bootique.shiro.web.jwt.jjwt.JwtParserMaker;
import io.bootique.shiro.web.jwt.authz.AuthzReaderFactory;
import io.bootique.shiro.web.jwt.authz.JsonListAuthzReaderFactory;
import io.bootique.value.Duration;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;

import java.net.URL;
import java.util.Objects;

/**
 * @since 4.0
 */
@BQConfig("JWT Configuration")
public class ShiroWebJwtModuleFactory {

    private static final java.time.Duration DEFAULT_JWK_EXPIRES_IN = java.time.Duration.ofDays(100 * 365);

    private ResourceFactory jwkLocation;
    private Duration jwkExpiresIn;
    private AuthzReaderFactory roles;
    private String audience;

    @BQConfigProperty("JWKS key file location")
    public ShiroWebJwtModuleFactory setJwkLocation(ResourceFactory jwkLocation) {
        this.jwkLocation = jwkLocation;
        return this;
    }

    @BQConfigProperty("Expiration interval when JWKS must be reloaded")
    public ShiroWebJwtModuleFactory setJwkExpiresIn(Duration jwkExpiresIn) {
        this.jwkExpiresIn = jwkExpiresIn;
        return this;
    }

    @BQConfigProperty("JWT-originated roles parser configuration")
    public ShiroWebJwtModuleFactory setRoles(AuthzReaderFactory roles) {
        this.roles = roles;
        return this;
    }

    @BQConfigProperty("An optional audience. If specified, it will be compared with the 'aud' JWT claim, and fail the request if the two do not match")
    public ShiroWebJwtModuleFactory setAudience(String audience) {
        this.audience = audience;
        return this;
    }

    public JwtParser createTokenParser() {
        return JwtParserMaker.createParser(getJwkLocation(), getJwkExpiresIn());
    }

    public JwtRealm createRealm() {
        return new JwtRealm(getRoles().createReader());
    }

    public JwtBearerAuthenticationFilter createFilter(Provider<JwtParser> tokenParser) {
        return new JwtBearerAuthenticationFilter(tokenParser, this.audience);
    }

    private AuthzReaderFactory getRoles() {
        return roles != null ? roles : new JsonListAuthzReaderFactory();
    }

    private URL getJwkLocation() {
        return Objects.requireNonNull(jwkLocation, "JWKS 'keyLocation' is not specified").getUrl();
    }

    private java.time.Duration getJwkExpiresIn() {
        return jwkExpiresIn != null ? jwkExpiresIn.getDuration() : DEFAULT_JWK_EXPIRES_IN;
    }
}
