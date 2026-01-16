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
package io.bootique.shiro.jwt.authz;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.resource.ResourceFactory;

import java.net.URL;
import java.util.Objects;

/**
 * @since 4.0
 */
@BQConfig("""
        Configuration of a token-issuing authorization server including location of public keys, token audiences, role \
        parsing mechanisms.""")
public class AuthzServerFactory {

    private ResourceFactory jwkLocation;
    private String audience;
    private AuthzReaderFactory roles;

    @BQConfigProperty("""
            An optional audience. If specified, it will be compared with the 'aud' JWT claim, and fail authentication \
            if the two do not match""")
    public AuthzServerFactory setAudience(String audience) {
        this.audience = audience;
        return this;
    }

    @BQConfigProperty("JWKS key file location")
    public AuthzServerFactory setJwkLocation(ResourceFactory jwkLocation) {
        this.jwkLocation = jwkLocation;
        return this;
    }

    @BQConfigProperty("JWT-originated roles parser configuration")
    public AuthzServerFactory setRoles(AuthzReaderFactory roles) {
        this.roles = roles;
        return this;
    }

    public AuthzServer createAuthzServer() {
        return new AuthzServer(this.audience, getRoles().createReader(), getJwkLocation());
    }

    private AuthzReaderFactory getRoles() {
        return roles != null ? roles : new JsonListAuthzReaderFactory();
    }

    private URL getJwkLocation() {
        return Objects.requireNonNull(jwkLocation, "'jwkLocation' is not specified").getUrl();
    }
}
