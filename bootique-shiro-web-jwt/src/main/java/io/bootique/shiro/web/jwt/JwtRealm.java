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

import io.bootique.shiro.web.jwt.authz.AuthzReader;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * @since 4.0
 */
public class JwtRealm extends AuthorizingRealm {

    private final AuthzReader rolesReader;

    public JwtRealm(AuthzReader rolesReader) {

        setName(JwtRealm.class.getSimpleName());
        setAuthenticationTokenClass(JwtBearerToken.class);

        this.rolesReader = rolesReader;
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

        JwtPrincipal principal = new JwtPrincipal(((JwtBearerToken) token).getClaims());

        return new SimpleAuthenticationInfo(
                new SimplePrincipalCollection(principal, getName()),
                token.getCredentials());
    }
}
