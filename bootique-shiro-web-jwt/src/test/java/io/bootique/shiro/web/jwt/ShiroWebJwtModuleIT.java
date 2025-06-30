package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@BQTest
public class ShiroWebJwtModuleIT {

    private static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("-c", "classpath:io/bootique/shiro/web/jwt/ShiroWebJwtModuleIT.yml", "-s")
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testPublicAccess() {
        JettyTester.assertOk(getResponse("public", Collections.emptyList())).assertContent("public");
    }

    @Test
    public void testRole1() {
        List<String> roles = List.of("role1");
        JettyTester.assertOk(getResponse("private-one", roles)).assertContent("private-one");
        JettyTester.assertUnauthorized(getResponse("private-two", roles));
        JettyTester.assertUnauthorized(getResponse("private-three", roles));
    }

    @Test
    public void testRole2() {
        List<String> roles = List.of("role2");
        JettyTester.assertUnauthorized(getResponse("private-one", roles));
        JettyTester.assertOk(getResponse("private-two", roles)).assertContent("private-two");
        JettyTester.assertUnauthorized(getResponse("private-three", roles));
    }

    @Test
    public void testRole3() {
        List<String> roles = List.of("role3");
        JettyTester.assertUnauthorized(getResponse("private-one", roles));
        JettyTester.assertUnauthorized(getResponse("private-two", roles));
        JettyTester.assertOk(getResponse("private-three", roles)).assertContent("private-three");
    }

    private Response getResponse(String resource, List<String> roles) {
        try {
            Response r = jetty.getTarget()
                    .path("/" + resource)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, TokenGenerator.token(roles))
                    .get();
            return r;
        } catch (Exception e) {
            Assertions.fail(e);
        }
        return null;
    }

    @Path("/")
    public static class TestApi {

        @GET
        @Path("public")
        public String getPublic() {
            return "public";
        }

        @GET
        @Path("private-one")
        public String getPrivateOne() {
            return "private-one";
        }

        @GET
        @Path("private-two")
        public String getPrivateTwo() {
            return "private-two";
        }

        @GET
        @Path("private-three")
        public String getPrivateThree() {
            return "private-three";
        }
    }

    static class TokenGenerator {

        static String token(List<String> roles) throws Exception {
            Map<String, List<String>> map = Map.of("roles", roles);

            String key = Files.readString(Paths.get(ClassLoader.getSystemResource("io/bootique/shiro/web/jwt/jwks-private-key.pem").toURI()));
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            Header kidHeader = Jwts.header().build();
            return "Bearer " + Jwts.builder().header().add("kid", "xGpTsw0DJs0vbe5CEcKMl5oZc7nKzAC9sF7kx1nQu1I").and().claims(map).signWith(SignatureAlgorithm.RS256, privateKey).compact();
        }
    }
}
