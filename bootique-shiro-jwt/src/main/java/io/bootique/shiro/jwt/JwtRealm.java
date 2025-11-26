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
package io.bootique.shiro.jwt;

import io.bootique.shiro.jwt.authz.AuthzReader;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.util.Set;

/**
 * @since 4.0
 */
public class JwtRealm extends AuthorizingRealm {

    private final AuthzReader rolesReader;
    private final String audience;

    public JwtRealm(AuthzReader rolesReader, String audience) {

        setName(JwtRealm.class.getSimpleName());
        setAuthenticationTokenClass(ShiroJsonWebToken.class);

        this.rolesReader = rolesReader;
        this.audience = audience;
    }

    public String getAudience() {
        return audience;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        principals
                .byType(JwtPrincipal.class)
                .forEach(c -> authorizationInfo.addRoles(rolesReader.readAuthz(c.claims())));
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        Claims claims = ((ShiroJsonWebToken) token).getClaims();
        validateAudience(claims.getAudience());

        JwtPrincipal principal = new JwtPrincipal(claims);
        return new SimpleAuthenticationInfo(
                new SimplePrincipalCollection(principal, getName()),
                token.getCredentials());
    }

    private void validateAudience(Set<String> audienceJwtClaim) {
        if (this.audience != null && !this.audience.isEmpty()) {
            if (audienceJwtClaim == null || audienceJwtClaim.isEmpty()) {
                throw new AuthenticationException("Token has no audience");
            }

            if (!audienceJwtClaim.contains(this.audience)) {
                throw new AuthenticationException("Token has invalid audience: " + audienceJwtClaim);
            }
        }
    }
}
