package io.bootique.shiro.web.oidconnect;

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
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@BQTest
public class AuthorizationCodeHandlerApiIT {

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
            .module(b -> JerseyModule.extend(b).addResource(Api.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void noCodeParam() {
        Response r = appTester.getTarget().path("bq-shiro-oauth-callback").request().get();
        JettyTester.assertBadRequest(r).assertContent("'code' parameter is required");
    }

    @Test
    public void validWithoutOriginalUrl() {
        Response r = appTester.getTarget().path("bq-shiro-oauth-callback")
                .queryParam("code", "000")
                .request()
                .get();
        JettyTester.assertOk(r);
        
        Cookie c = r.getCookies().get("bq-shiro-oid");
        assertNotNull(c);
        assertEquals("123", c.getValue());
    }

    @Test
    public void validWithOriginalUrl() {
        Client client = OidTests.clientNoRedirects();

        Response r1Callback = client.target(appTester.getUrl())
                .path("bq-shiro-oauth-callback")
                .queryParam("code", "000")
                .queryParam(OidpRouter.INITIAL_URI_PARAM, URLEncoder.encode("/public", StandardCharsets.UTF_8))
                .request()
                .get();

        JettyTester.assertTempRedirect(r1Callback);

        Cookie c = r1Callback.getCookies().get("bq-shiro-oid");
        assertNotNull(c, () -> "No access cookie for redirect to: " + r1Callback.getHeaderString("Location"));
        assertEquals("123", c.getValue());

        // test that we got redirected to the right place
        Response r2ResourceAccessCookies = client
                .target(r1Callback.getHeaderString("Location"))
                .request()
                .get();

        JettyTester.assertOk(r2ResourceAccessCookies).assertContent("public");
    }

    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        @POST
        public String auth() {
            return "{\"access_token\":\"123\"}";
        }
    }

    @Path("/")
    public static class Api {

        @GET
        @Path("public")
        public Response getPublic() {
            return Response.ok("public").build();
        }

        @GET
        @Path("private")
        public Response getPrivate() {
            return Response.ok("private").build();
        }
    }
}
