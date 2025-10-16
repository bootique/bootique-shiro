package io.bootique.shiro.web.oidconnect;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
            .module(b -> JerseyModule.extend(b).addResource(RedirectApi.class))
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
                .queryParam(OidConnect.CODE_PARAM, "000")
                .request()
                .get();
        JettyTester.assertOk(r);
        Map<String, NewCookie> cookies = r.getCookies();
        assertNotNull(cookies);
        NewCookie tokenCookie = cookies.get("bq-shiro-oid");
        assertNotNull(tokenCookie);
        assertEquals("123", tokenCookie.getValue());
    }

    @Test
    public void validWithOriginalUrl() {
        Response r = appTester.getTarget().path("bq-shiro-oauth-callback")
                .queryParam(OidConnect.CODE_PARAM, "000")
                .queryParam(OidConnect.ORIGINAL_URI_PARAM, Base64.getEncoder().encodeToString(URLEncoder.encode("/public", StandardCharsets.UTF_8).getBytes()))
                .request()
                .get();
        JettyTester.assertOk(r).assertContent("public");
        Map<String, NewCookie> cookies = r.getCookies();
        assertNotNull(cookies);
        NewCookie tokenCookie = cookies.get("bq-shiro-oid");
        assertNotNull(tokenCookie);
        assertEquals("123", tokenCookie.getValue());
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
    public static class RedirectApi {

        @GET
        @Path("public")
        public Response getPublic(@Context HttpServletRequest request) {
            return response(request, "public");
        }

        @GET
        @Path("private")
        public Response getPrivate(@Context HttpServletRequest request) {
            return response(request, "private");
        }

        private Response response(HttpServletRequest request, String entity) {
            List<NewCookie> oidTokenCookie = Arrays.stream(request.getCookies()).map(c -> new NewCookie.Builder(c.getName()).value(c.getValue()).build()).toList();
            Response.ResponseBuilder r = Response.ok(entity);
            oidTokenCookie.forEach(r::cookie);
            return r.build();
        }
    }
}
