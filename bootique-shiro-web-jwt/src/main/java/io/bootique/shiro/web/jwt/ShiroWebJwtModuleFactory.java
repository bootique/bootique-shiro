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
import io.bootique.shiro.web.jwt.jjwt.JwtManager;
import io.bootique.shiro.web.jwt.jjwt.JwtParserMaker;
import io.bootique.shiro.web.jwt.authz.AuthzReaderFactory;
import io.bootique.shiro.web.jwt.authz.JsonListAuthzReaderFactory;
import io.bootique.value.Duration;
import io.jsonwebtoken.JwtParser;

import java.net.URL;
import java.util.Objects;
import java.util.Set;

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

    @BQConfigProperty("Jwks key file location")
    public ShiroWebJwtModuleFactory setJwkLocation(ResourceFactory jwkLocation) {
        this.jwkLocation = jwkLocation;
        return this;
    }

    @BQConfigProperty("Expiration interval when JWKS must be reloaded")
    public ShiroWebJwtModuleFactory setJwkExpiresIn(Duration jwkExpiresIn) {
        this.jwkExpiresIn = jwkExpiresIn;
        return this;
    }

    @BQConfigProperty("Configures JWT roles parser")
    public ShiroWebJwtModuleFactory setRoles(AuthzReaderFactory roles) {
        this.roles = roles;
        return this;
    }

    @BQConfigProperty("Configures audience")
    public ShiroWebJwtModuleFactory setAudience(String audience) {
        this.audience = audience;
        return this;
    }

    public JwtManager createTokenManager() {
        return new JwtManager(JwtParserMaker.createParser(getJwkLocation(), getJwkExpiresIn()), this.audience);
    }

    public JwtRealm createRealm() {
        return new JwtRealm(getRoles().createReader());
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
