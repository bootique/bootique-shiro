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

package io.bootique.shiro.web;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.shiro.ShiroModule;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@BQTest
public class ShiroWebModule_CustomRealmIT {

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-c", "classpath:ShiroWebModule_CustomRealmIT.yml", "-s")
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(Api.class))
            .module(b -> ShiroModule.extend(b).addRealm(new TestRealm()))
            // overriding standard "perms" filter to avoid being sent to the login form
            .module(b -> ShiroWebModule.extend(b).setFilter("perms", PermissionsFilter.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void anonymousAccess() {
        Response r = jetty.getTarget().path("/anonymous").request().get();
        JettyTester.assertOk(r).assertContent("anon_string_null");
    }

    @Test
    public void login() {
        Response r = jetty.getTarget().path("/login_on_demand").request().get();
        JettyTester.assertOk(r).assertContent("postlogin_string_myuser");
    }

    @Path("/")
    public static class Api {

        @GET
        @Path("anonymous")
        public String getAnonymous() {
            Subject subject = SecurityUtils.getSubject();
            return "anon_string_" + subject.getPrincipal();
        }

        @GET
        @Path("login_on_demand")
        public String getAdmin_Login() {
            Subject subject = SecurityUtils.getSubject();
            subject.login(new UsernamePasswordToken("myuser", "password"));
            subject.checkPermission("admin");

            return "postlogin_string_" + subject.getPrincipal();
        }
    }

    public static class TestRealm extends AuthorizingRealm {

        public TestRealm() {
            setName("TestRealm");
        }

        @Override
        public boolean supports(AuthenticationToken token) {
            return token instanceof UsernamePasswordToken;
        }

        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

            UsernamePasswordToken upToken = (UsernamePasswordToken) token;
            if (!"password".equals(new String(upToken.getPassword()))) {
                throw new AuthenticationException("Invalid password for user: " + upToken.getUsername());
            }

            return new SimpleAuthenticationInfo(upToken.getPrincipal(), upToken.getCredentials(), getName());
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            info.addStringPermission("admin");
            return info;
        }
    }

    public static class PermissionsFilter extends PermissionsAuthorizationFilter {

        @Override
        protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
