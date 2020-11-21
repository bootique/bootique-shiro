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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@BQTest
public class ShiroWebModuleIT {

    private static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-c", "classpath:ShiroWebModuleIT.yml", "-s")
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(Api.class))
            .module(b -> ShiroModule.extend(b).addRealm(new TestRealm()))
            // overriding standard "perms" filter to avoid being sent to the login form
            .module(b -> ShiroWebModule.extend(b).setFilter("perms", PermissionsFilter.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testPublic() {
        Response r = jetty.getTarget().path("/public").request().get();
        JettyTester.assertOk(r).assertContent("public_string");
    }

    @Test
    public void testAnonymous() {
        Response r = jetty.getTarget().path("/anonymous").request().get();
        JettyTester.assertOk(r).assertContent("anon_string_null");
    }

    @Test
    public void testLogin() {
        Response r = jetty.getTarget().path("/login_on_demand").request().get();
        JettyTester.assertOk(r).assertContent("postlogin_string_myuser");
    }

    @Test
    public void testAdmin() {
        Response r = jetty.getTarget().path("/admin").request().get();
        JettyTester.assertUnauthorized(r);
    }

    @Path("/")
    public static class Api {

        @GET
        @Path("public")
        public String getPublic() {
            return "public_string";
        }

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

        @GET
        @Path("admin")
        public String getAdminNoLogin() {
            Subject subject = SecurityUtils.getSubject();
            throw new IllegalStateException("Should have been filtered: " + subject.getPrincipal());
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
            WebUtils.toHttp(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
