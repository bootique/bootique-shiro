package io.bootique.shiro.web.oidconnect;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@BQTest
public class OidConnectFilterIT {

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
            .module(b -> BQCoreModule.extend(b).setProperty("bq.shiroweboidconnect.callbackUri", "custom-oauth-callback"))
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    private WebTarget appTargetNoRedirects() {
        Client client = ClientBuilder.newBuilder()
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .build();

        return client.target(appTester.getUrl());
    }

    @Test
    public void noAuth() {

        Response r = appTargetNoRedirects()
                .path("/private")
                .request()
                .get();

        String expectedOriginalUrl = appTester.getUrl() + "/private";
        String expectedRedirect = tokenServerTester.getUrl() +
                "/auth?response_type=code&client_id=test-client&redirect_uri=" +
                URLEncoder.encode(appTester.getUrl(), StandardCharsets.UTF_8) +
                "%2Fcustom-oauth-callback%3Foriginal_uri%3D" +
                // double URL-encode the origin URL, as it is a parameter of an already URL-encoded URL parameter
                URLEncoder.encode(URLEncoder.encode(expectedOriginalUrl, StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        JettyTester.assertFound(r).assertHeader("Location", expectedRedirect);
    }

    @Test
    public void noAuthWithIDPRedirects() {
        Response r = appTester.getTarget()
                .path("/private")
                .request()
                .get();
        JettyTester.assertOk(r).assertContent("private");
    }

    @Test
    public void authCookie() throws Exception {

        Map<String, ?> map = Map.of("roles", List.of("role1"));
        String authToken = TokenApi.authToken(map);
        Response r = appTargetNoRedirects()
                .path("/private")
                .request()
                .cookie(new NewCookie.Builder("bq-shiro-oid").value(authToken).build())
                .get();
        JettyTester.assertOk(r).assertContent("private");
    }

    @Path("/")
    public static class TestApi {

        @GET
        @Path("private")
        public String getPrivate() {
            return "private";
        }
    }

    @Path("/auth")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public static class TokenApi {

        @POST
        public Response authToken() {
            try {
                Map<String, ?> map = Map.of("roles", List.of("role1"));
                String authToken = authToken(map);
                return Response.ok("{\"access_token\":\"" + authToken + "\"}").build();
            } catch (Exception e) {
                return Response.serverError().entity("Unable to generate auth token: " + e.getMessage()).build();
            }
        }

        @GET
        public Response authCode(@QueryParam("redirect_uri") String redirectUri) {
            String callbackUrl = redirectUri + "&code=123&state=xyz";
            return Response.status(Response.Status.FOUND).header("Location", callbackUrl).build();
        }

        static String authToken(Map<String, ?> rolesClaim) throws Exception {

            String key = Files.readString(Paths.get(ClassLoader.getSystemResource("io/bootique/shiro/web/oidconnect/jwks-private-key.pem").toURI()));
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            JwtBuilder builder = Jwts.builder()
                    .header().add("kid", "xGpTsw0DJs0vbe5CEcKMl5oZc7nKzAC9sF7kx1nQu1I")
                    .and()
                    .claims(rolesClaim).signWith(SignatureAlgorithm.RS256, privateKey);
            return builder.compact();
        }
    }
}
