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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

@BQTest
public class OidConnectFilterInvalidGrantIT extends OidConnectBaseTest {

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
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        @POST
        public Response authToken() {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"invalid_grant\"}").build();
        }

        @GET
        public Response authCode(@Context UriInfo uriInfo) {
            String state = uriInfo.getQueryParameters().getFirst(OidConnect.STATE_PARAMETER_NAME);
            if (state != null) {
                if (Response.Status.OK.getReasonPhrase().equals(state)) {
                    final String callbackUrl = uriInfo.getQueryParameters().getFirst("redirect_uri") + "&code=123&state=xyz";
                    return Response.status(Response.Status.FOUND).header("Location", callbackUrl).build();
                } else if (OidConnect.INVALID_GRANT_ERROR_CODE.equals(state)) {
                    return Response.ok(state).build();
                }
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("State parameter does not exist").build();
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
    public void testInvalidGrant() {
        Response r = jetty.getTarget()
                .path("/private")
                .queryParam("aaa", "1").queryParam("bbb", 2)
                .request()
                .get();
        JettyTester.assertOk(r).assertContent(OidConnect.INVALID_GRANT_ERROR_CODE);
    }
}
