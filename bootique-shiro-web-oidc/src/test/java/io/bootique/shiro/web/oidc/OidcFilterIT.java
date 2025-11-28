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
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@BQTest
public class OidcFilterIT {

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
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidc.callbackUri", "cb"))
            .module(b -> JerseyModule.extend(b).addResource(Api.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void noAuth() {

        WebTarget target = appTester.getTarget(false).path("/private");

        String expectedRedirect = tokenServerTester.getUrl() +
                "/auth?response_type=code&client_id=test-client&redirect_uri=" +
                URLEncoder.encode(appTester.getUrl(), StandardCharsets.UTF_8) +
                "%2Fcb%3Finitial_uri%3D" +
                // double URL-encode the origin URL, as it is a parameter of an already URL-encoded URL parameter
                URLEncoder.encode(URLEncoder.encode(target.getUri().toString(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        Response r = target.request().get();
        JettyTester.assertFound(r).assertHeader("Location", expectedRedirect);
    }

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

    @Test
    public void authCookie() {

        String authToken = OidTests.jwt(Map.of("roles", List.of("role1")));
        Response r = appTester.getTarget(false)
                .path("/private")
                .request()
                .cookie(new NewCookie.Builder("bq-shiro-oidc").value(authToken).build())
                .get();
        JettyTester.assertOk(r).assertContent("private");
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

            assertEquals("code", responseType);
            assertEquals("test-client", clientId);
            assertNotNull(redirectUri);

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

            assertEquals("123", data.getFirst("code"));
            assertEquals("authorization_code", data.getFirst("grant_type"));
            assertEquals("test-client", data.getFirst("client_id"));
            assertEquals("test-password", data.getFirst("client_secret"));

            String authToken = OidTests.jwt(Map.of("roles", List.of("role1")));
            return Response.ok("{\"access_token\":\"" + authToken + "\"}").build();
        }
    }
}
