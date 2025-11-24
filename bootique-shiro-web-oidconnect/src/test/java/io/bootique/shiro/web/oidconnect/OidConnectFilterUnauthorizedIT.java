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
import org.junit.jupiter.api.Test;

@BQTest
public class OidConnectFilterUnauthorizedIT {

    private static final JettyTester tokenServerTester = JettyTester.create();

    @BQApp
    static final BQRuntime tokenServer = Bootique.app("-s")
            .module(JettyModule.class)
            .module(JerseyModule.class)
            .module(tokenServerTester.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(TokenApi.class))
            .createRuntime();

    private final JettyTester appTester = JettyTester.create();

    @BQApp
    final BQRuntime app = Bootique.app("-c", "classpath:io/bootique/shiro/web/oidconnect/oidconnect.yml", "-s")
            .module(appTester.moduleReplacingConnectors())
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidconnect.tokenUrl", tokenServerTester.getUrl() + "/auth"))
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidconnect.oidpUrl", tokenServerTester.getUrl() + "/auth"))
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void accessDenied() {
        Response r = appTester.getTarget()
                .path("/private")
                .queryParam("aaa", "1").queryParam("bbb", 2)
                .request()
                .get();
        JettyTester.assertUnauthorized(r);
    }

    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        @POST
        public Response authToken() {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"access_denied\"}").build();
        }

        @GET
        public Response authCode(@QueryParam("redirect_uri") String redirectUri) {
            String callbackUrl = redirectUri + "&code=123&state=xyz";
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
}
