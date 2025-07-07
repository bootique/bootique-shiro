package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.jsonwebtoken.JwtBuilder;
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
            .app("-c", "classpath:io/bootique/shiro/web/jwt/jwt-default.yml", "-s")
            .module(jetty.moduleReplacingConnectors())
            .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
            .autoLoadModules()
            .createRuntime();

    private Map<String, ?> rolesMap(String claimName, List<String> roles) {
        return Map.of(claimName, roles);
    }

    private Map<String, ?> rolesMap(List<String> roles) {
        return Map.of("roles", roles);
    }

    @Test
    public void testPublicAccess() {
        JettyTester.assertOk(getResponse("public", Collections.emptyMap())).assertContent("public");
    }

    @Test
    public void testRole1() {
        Map<String, ?> map = rolesMap(List.of("role1"));
        JettyTester.assertOk(getResponse("private-one", map)).assertContent("private-one");
        JettyTester.assertUnauthorized(getResponse("private-two", map));
        JettyTester.assertUnauthorized(getResponse("private-three", map));
    }

    @Test
    public void testRole2() {
        Map<String, ?> map = rolesMap(List.of("role2"));
        JettyTester.assertUnauthorized(getResponse("private-one", map));
        JettyTester.assertOk(getResponse("private-two", map)).assertContent("private-two");
        JettyTester.assertUnauthorized(getResponse("private-three", map));
    }

    @Test
    public void testRole3() {
        Map<String, ?> map = rolesMap(List.of("role3"));
        JettyTester.assertUnauthorized(getResponse("private-one", map));
        JettyTester.assertUnauthorized(getResponse("private-two", map));
        JettyTester.assertOk(getResponse("private-three", map)).assertContent("private-three");
    }

    @Test
    public void testRole1And3() {
        Map<String, ?> map = rolesMap(List.of("role1", "role3"));
        JettyTester.assertOk(getResponse("private-one", map)).assertContent("private-one");
        JettyTester.assertUnauthorized(getResponse("private-two", map));
        JettyTester.assertOk(getResponse("private-three", map)).assertContent("private-three");
    }

    @Test
    public void testExpiration() {
        Map<String, ?> map = rolesMap(List.of("role1"));
        JettyTester.assertUnauthorized(getResponse("private-one", map, 15));
    }

    @Test
    public void testWithoutRolesClaim() {
        Map<String, ?> map = rolesMap("no-roles", List.of("role1"));
        JettyTester.assertUnauthorized(getResponse("private-one", map));
        JettyTester.assertUnauthorized(getResponse("private-two", map));
        JettyTester.assertUnauthorized(getResponse("private-three", map));
    }

    protected Response getResponse(String resource, Map<String, ?> rolesClaim) {
        return getResponse(resource, rolesClaim, null);
    }

    protected Response getResponse(String resource, Map<String, ?> rolesClaim, Integer expirationSec) {
        try {
            Date expiration = null;
            if (expirationSec != null) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.SECOND, -expirationSec);
                expiration = c.getTime();
            }
            String token = TokenGenerator.token(rolesClaim, expiration);
            return jetty.getTarget()
                    .path("/" + resource)
                    .request()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .get();
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

        static String token(Map<String, ?> rolesClaim) throws Exception {
            return token(rolesClaim, null);
        }

        static String token(Map<String, ?> rolesClaim, Date expiration) throws Exception {

            String key = Files.readString(Paths.get(ClassLoader.getSystemResource("io/bootique/shiro/web/jwt/jwks-private-key.pem").toURI()));
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return build(privateKey, rolesClaim, expiration);
        }

        private static String build(PrivateKey privateKey, Map<String, ?> rolesClaim, Date expiration) {
            JwtBuilder builder = Jwts.builder()
                    .header().add("kid", "xGpTsw0DJs0vbe5CEcKMl5oZc7nKzAC9sF7kx1nQu1I")
                    .and()
                    .claims(rolesClaim).signWith(SignatureAlgorithm.RS256, privateKey);
            if (expiration != null) {
                builder = builder.expiration(expiration);
            }
            return "Bearer " + builder.compact();

        }
    }
}
