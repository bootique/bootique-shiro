package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public abstract class ShiroWebJwtModuleIT {

    protected abstract JettyTester jetty();

    protected static BQRuntime runtime(JettyTester jetty, String yml) {
        return Bootique
                .app("-c", yml, "-s")
                .module(jetty.moduleReplacingConnectors())
                .module(b -> JerseyModule.extend(b).addResource(TestApi.class))
                .autoLoadModules()
                .createRuntime();
    }

    protected Map<String, ?> rolesMap(String claimName, List<String> roles) {
        return Map.of(claimName, roles);
    }

    protected Map<String, ?> rolesMap(List<String> roles) {
        return Map.of("roles", roles);
    }

    protected Response getResponse(String resource, Map<String, ?> rolesClaim) {
        return getResponse(resource, rolesClaim, null, null);
    }

    protected Response getResponse(String resource, Map<String, ?> rolesClaim, List<String> audience) {
        return getResponse(resource, rolesClaim, null, audience);
    }

    protected Response getResponse(String resource, Map<String, ?> rolesClaim, Integer expirationSec, List<String> audience) {
        try {
            Date expiration = null;
            if (expirationSec != null) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.SECOND, -expirationSec);
                expiration = c.getTime();
            }
            String token = TokenGenerator.token(rolesClaim, expiration, audience);
            return jetty().getTarget()
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
            return token(rolesClaim, null, null);
        }

        static String token(Map<String, ?> rolesClaim, Date expiration, List<String> audience) throws Exception {

            String key = Files.readString(Paths.get(ClassLoader.getSystemResource("io/bootique/shiro/web/jwt/jwks-private-key.pem").toURI()));
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return build(privateKey, rolesClaim, expiration, audience);
        }

        private static String build(PrivateKey privateKey, Map<String, ?> rolesClaim, Date expiration, List<String> audience) {
            JwtBuilder builder = Jwts.builder()
                    .header().add("kid", "xGpTsw0DJs0vbe5CEcKMl5oZc7nKzAC9sF7kx1nQu1I")
                    .and()
                    .claims(rolesClaim).signWith(SignatureAlgorithm.RS256, privateKey);
            if (expiration != null) {
                builder.expiration(expiration);
            }
            if (audience != null && !audience.isEmpty()) {
                if (audience.size() == 1) {
                    builder.audience().add(audience.get(0));
                } else {
                    builder.audience().add(audience);
                }
            }
            return "Bearer " + builder.compact();

        }
    }
}
