package io.bootique.shiro.web.oidconnect;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@BQTest
public class OidConnectFilterIT extends OidConnectBaseTest {

    private final JettyTester jetty = JettyTester.create();

    private static final JettyTester serverJetty = JettyTester.create();


    @BQApp
    static final BQRuntime tokenServerApp = Bootique.app("-s")
            .module(JettyModule.class)
            .module(JerseyModule.class)
            .module(serverJetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(TokenApi.class))
            .createRuntime();

    @BQApp
    final BQRuntime app = Bootique.app("-c", "classpath:io/bootique/shiro/web/oidconnect/oidconnect.yml", "-s")
            .module(jetty.moduleReplacingConnectors())
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidconnect.tokenUrl", serverJetty.getUrl() + "/auth"))
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidconnect.oidpUrl", serverJetty.getUrl() + "/auth"))
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidconnect.callbackUri", "custom-oauth-callback"))
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        @POST
        public Response authToken() {
            try {
                Map<String, ?> map = Map.of("roles", List.of("role1"));
                String authToken = token(map);
                return Response.ok("{\"access_token\":\"" + authToken + "\"}").build();
            } catch (Exception e) {
                return Response.serverError().entity("Unable to generate auth token: " + e.getMessage()).build();
            }
        }

        @GET
        public Response authCode(@Context UriInfo uriInfo) {
            final String callbackUrl = uriInfo.getQueryParameters().getFirst("redirect_uri") + "&code=123&state=xyz";
            return Response.status(Response.Status.FOUND).header("Location", callbackUrl).build();
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

    @Test
    public void testValidWithoutCookie() {
        Response r = jetty.getTarget()
                .path("/private")
                .queryParam("aaa", "1").queryParam("bbb", 2)
                .request()
                .get();
        JettyTester.assertOk(r);
    }

    @Test
    public void testValidWithCookie() {
        try {
            Map<String, ?> map = Map.of("roles", List.of("role1"));
            String authToken = token(map);
            Response r = jetty.getTarget()
                    .path("/private")
                    .queryParam("aaa", "1").queryParam("bbb", 2)
                    .request()
                    .cookie(new NewCookie.Builder("bq-shiro-oid").value(authToken).build())
                    .get();
            JettyTester.assertOk(r);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testInvalidGrantWithoutCookie() {
        Response r = jetty.getTarget()
                .path("/private")
                .queryParam("aaa", "1").queryParam("bbb", 2)
                .request()
                .get();
        JettyTester.assertOk(r);
    }
}
