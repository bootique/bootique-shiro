package io.bootique.shiro.web.oidc;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

@BQTest
public class OidcFilterInvalidGrantIT {

    static final JettyTester tokenServerTester = JettyTester.create();

    @BQApp
    static final BQRuntime tokenServer = Bootique.app("-s")
            .module(JettyModule.class)
            .module(JerseyModule.class)
            .module(tokenServerTester.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(TokenApi.class))
            .createRuntime();

    static final JettyTester appTester = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique.app("-c", "classpath:io/bootique/shiro/web/oidc/oidc.yml", "-s")
            .module(appTester.moduleReplacingConnectors())
            .module(b -> BQCoreModule.extend(b).setPropertyProvider("bq.shiroweboidc.tokenUrl", () -> tokenServerTester.getUrl() + "/auth"))
            .module(b -> BQCoreModule.extend(b).setPropertyProvider("bq.shiroweboidc.oidpUrl", () -> tokenServerTester.getUrl() + "/auth"))
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testInvalidGrant() {
        Response r = appTester.getTarget()
                .path("/private")
                .queryParam("aaa", "1").queryParam("bbb", 2)
                .request()
                .get();
        JettyTester.assertOk(r).assertContent("invalid_grant");
    }

    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        private boolean authCodeAlreadyUsed = false;

        @POST
        public Response authToken() {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"invalid_grant\"}").build();
        }

        @GET
        public Response authCode(@Context UriInfo uriInfo) {
            if (!authCodeAlreadyUsed) {
                final String callbackUrl = uriInfo.getQueryParameters().getFirst("redirect_uri") + "?code=123&state=xyz";
                authCodeAlreadyUsed = true;
                return Response.status(Response.Status.FOUND).header("Location", callbackUrl).build();
            } else {
                return Response.ok("invalid_grant").build();
            }
        }
    }


    @Path("/")
    public static class TestApi {

        @GET
        @Path("private")
        public String getPrivate() {
            return "private";
        }
    }
}
