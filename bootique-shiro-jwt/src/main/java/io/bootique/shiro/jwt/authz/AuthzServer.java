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

import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;

import java.net.URL;
import java.util.List;
import java.util.Set;

public class AuthzServer {

    private final String audience;
    private final AuthzReader rolesReader;
    private final URL jwkLocation;

    public AuthzServer(String audience, AuthzReader rolesReader, URL jwkLocation) {
        this.audience = audience;
        this.rolesReader = rolesReader;
        this.jwkLocation = jwkLocation;
    }

    URL getJwkLocation() {
        return jwkLocation;
    }

    public List<String> getRoles(Claims claims) {
        return rolesReader.readAuthz(claims);
    }

    public void validateAudience(Claims claims) {
        if (this.audience != null && !this.audience.isEmpty()) {

            Set<String> claimedAudience = claims.getAudience();

            if (claimedAudience == null || claimedAudience.isEmpty()) {
                throw new AuthenticationException("Token has no audience");
            }

            if (!claimedAudience.contains(this.audience)) {
                throw new AuthenticationException("Token has invalid audience: " + claimedAudience);
            }
        }
    }
}
