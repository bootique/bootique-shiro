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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@BQTest
public class ShiroWebModuleIT {

    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-c", "classpath:ShiroWebModuleIT.yml", "-s")
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addApiResource(Api.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void publicAccess() {
        Response r = jetty.getTarget().path("/public").request().get();
        JettyTester.assertOk(r).assertContent("public_string");
    }

    @Test
    public void admin1() {
        Response r = jetty.getTarget().path("/admin1").request().get();
        JettyTester.assertUnauthorized(r);
    }

    @Test
    public void admin2() {
        Response r = jetty.getTarget().path("/admin2")
                .request()
                .header("Authorization", "Basic " + Base64.getUrlEncoder().encodeToString("admin2u:password".getBytes(StandardCharsets.UTF_8)))
                .get();
        JettyTester.assertOk(r).assertContent("admin2_string_admin2u");
    }

    @Path("/")
    public static class Api {

        @GET
        @Path("public")
        public String getPublic() {
            return "public_string";
        }

        @GET
        @Path("admin1")
        public String getAdmin1() {
            Subject subject = SecurityUtils.getSubject();
            return "admin1_string_" + subject.getPrincipal();
        }

        @GET
        @Path("admin2")
        public String getAdmin2() {
            Subject subject = SecurityUtils.getSubject();
            return "admin2_string_" + subject.getPrincipal();
        }
    }
}
