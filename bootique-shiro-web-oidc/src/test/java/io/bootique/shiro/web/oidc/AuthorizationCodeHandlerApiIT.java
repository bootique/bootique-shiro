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

    static final JettyTester tokenServerTester = JettyTester.create();

    @BQApp
    static final BQRuntime tokenServer = Bootique.app("-s")
            .module(JettyModule.class)
            .module(JerseyModule.class)
            .module(tokenServerTester.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addApiResource(TokenApi.class))
            .createRuntime();

    static final JettyTester appTester = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique.app("-c", "classpath:io/bootique/shiro/web/oidc/oidc.yml", "-s")
            .module(appTester.moduleReplacingConnectors())
            .module(b -> BQCoreModule.extend(b).setPropertyProvider("bq.shiroweboidc.tokenUrl", () -> tokenServerTester.getUrl() + "/auth"))
            .module(b -> JerseyModule.extend(b).addApiResource(Api.class))
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

        Cookie c = r.getCookies().get("bq-shiro-oidc");
        assertNotNull(c);
        assertEquals("123", c.getValue());
    }

    @Test
    public void validWithOriginalUrl() {

        Response r1Callback = appTester.getTarget(false)
                .path("bq-shiro-oauth-callback")
                .queryParam("code", "000")
                .queryParam("state", URLEncoder.encode("/public", StandardCharsets.UTF_8))
                .request()
                .get();

        JettyTester.assertTempRedirect(r1Callback);

        Cookie c = r1Callback.getCookies().get("bq-shiro-oidc");
        assertNotNull(c, () -> "No access cookie for redirect to: " + r1Callback.getHeaderString("Location"));
        assertEquals("123", c.getValue());

        try (Client client = OidTests.clientNoRedirects()) {
            // test that we got redirected to the right place
            Response r2ResourceAccessCookies = client
                    .target(r1Callback.getHeaderString("Location"))
                    .request()
                    .get();

            JettyTester.assertOk(r2ResourceAccessCookies).assertContent("public");
        }
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
