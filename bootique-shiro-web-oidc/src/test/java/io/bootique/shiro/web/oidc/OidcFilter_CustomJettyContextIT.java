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
package io.bootique.shiro.web.oidc;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@BQTest
public class OidcFilter_CustomJettyContextIT {

    static final JettyTester tokenServerTester = JettyTester.create();

    @BQApp
    static final BQRuntime tokenServer = Bootique.app("-s")
            .module(JettyModule.class)
            .module(JerseyModule.class)
            .module(tokenServerTester.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(AuthApi.class).addResource(TokenApi.class))
            .createRuntime();

    static final JettyTester appTester = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique.app("-c", "classpath:io/bootique/shiro/web/oidc/oidc-filter.yml", "-s")
            .module(appTester.moduleReplacingConnectors())
            .module(b -> BQCoreModule.extend(b).setPropertyProvider("bq.shiroweboidc.tokenUrl", () -> tokenServerTester.getUrl() + "/token"))
            .module(b -> BQCoreModule.extend(b).setPropertyProvider("bq.shiroweboidc.oidpUrl", () -> tokenServerTester.getUrl() + "/auth"))
            .module(b -> JerseyModule.extend(b).addResource(Api.class))

            // map Jetty to a custom path
            .module(b -> BQCoreModule.extend(b).setProperty("bq.jetty.context", "/app"))

            .autoLoadModules()
            .createRuntime();

    @Test
    public void noAuthWithIDPRedirects() {

        Response r1ResourceNoAccess = appTester.getTarget(false)
                .path("/private")
                .queryParam("pq", "X")
                .request()
                .get();
        JettyTester.assertFound(r1ResourceNoAccess);

        try (Client client = OidTests.clientNoRedirects()) {
            Response r2Login = client
                    .target(r1ResourceNoAccess.getHeaderString("Location"))
                    .request()
                    .get();
            JettyTester.assertFound(r2Login);

            Response r3Callback = client
                    .target(r2Login.getHeaderString("Location"))
                    .request()
                    .get();
            JettyTester.assertTempRedirect(r3Callback);

            Cookie c = r3Callback.getCookies().get("bq-shiro-oidc");
            assertNotNull(c, () -> "No access cookie for redirect to: " + r3Callback.getHeaderString("Location"));

            Response r4ResourceAccessCookies = client
                    .target(r3Callback.getHeaderString("Location"))
                    .request()
                    .cookie(c)
                    .get();

            JettyTester.assertOk(r4ResourceAccessCookies).assertContent("private:pq=X");
        }
    }

    @Path("/")
    public static class Api {

        @GET
        @Path("private")
        public String getPrivate(@QueryParam("pq") String pq) {
            return "private" + (pq != null ? ":pq=" + pq : "");
        }
    }

    @Path("/auth")
    public static class AuthApi {

        @GET
        public Response authCode(
                @QueryParam("response_type") String responseType,
                @QueryParam("client_id") String clientId,
                @QueryParam("redirect_uri") String redirectUri) {
            String callbackUrl = redirectUri + "&code=123&state=xyz";
            return Response.status(Response.Status.FOUND).header("Location", callbackUrl).build();
        }
    }

    @Path("/token")
    public static class TokenApi {

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_JSON)
        public Response token(MultivaluedMap<String, String> data) {
            String authToken = OidTests.jwt(Map.of("roles", List.of("role1")));
            return Response.ok("{\"access_token\":\"" + authToken + "\"}").build();
        }
    }
}
