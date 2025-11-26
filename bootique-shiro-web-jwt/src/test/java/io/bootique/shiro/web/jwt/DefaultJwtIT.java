package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

@BQTest
public class DefaultJwtIT extends ShiroWebJwtModuleIT {

    private static final JettyTester jetty = JettyTester.create();
    @BQApp
    static final BQRuntime app = runtime(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-default.yml");

    @Test
    public void publicAccess() {
        JettyTester.assertOk(requestWithToken("/public", Collections.emptyMap(), null, null)).assertContent("public");
    }

    @Test
    public void role1() {
        Map<String, ?> map = Map.of("roles", List.of("role1"));
        JettyTester.assertOk(requestWithToken("/private-one", map, null, null)).assertContent("private-one");
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, null, null));
        JettyTester.assertUnauthorized(requestWithToken("/private-three", map, null, null));
    }

    @Test
    public void role2() {
        Map<String, ?> map = Map.of("roles", List.of("role2"));
        JettyTester.assertUnauthorized(requestWithToken("/private-one", map, null, null));
        JettyTester.assertOk(requestWithToken("/private-two", map, null, null)).assertContent("private-two");
        JettyTester.assertUnauthorized(requestWithToken("/private-three", map, null, null));
    }

    @Test
    public void role3() {
        Map<String, ?> map = Map.of("roles", List.of("role3"));
        JettyTester.assertUnauthorized(requestWithToken("/private-one", map, null, null));
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, null, null));
        JettyTester.assertOk(requestWithToken("/private-three", map, null, null)).assertContent("private-three");
    }

    @Test
    public void role1And3() {
        Map<String, ?> map = Map.of("roles", List.of("role1", "role3"));
        JettyTester.assertOk(requestWithToken("/private-one", map, null, null)).assertContent("private-one");
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, null, null));
        JettyTester.assertOk(requestWithToken("/private-three", map, null, null)).assertContent("private-three");
    }

    @Test
    public void expiration() {
        Map<String, ?> map = Map.of("roles", List.of("role1"));
        JettyTester.assertUnauthorized(requestWithToken("/private-one", map, null, LocalDateTime.now().minusSeconds(15)));
    }

    @Test
    public void noRolesClaim() {
        Map<String, ?> map = Map.of("no-roles", List.of("role1"));
        JettyTester.assertUnauthorized(requestWithToken("/private-one", map, null, null));
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, null, null));
        JettyTester.assertUnauthorized(requestWithToken("/private-three", map, null, null));
    }

    @Test
    public void noAudienceValidation() {
        Map<String, ?> map = Map.of("roles", List.of("role1"));
        List<String> audience = List.of("aud-1", "aud-2");
        JettyTester.assertOk(requestWithToken("/private-one", map, audience, null)).assertContent("private-one");
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, audience, null));
        JettyTester.assertUnauthorized(requestWithToken("/private-three", map, audience, null));
    }

    private Response requestWithToken(String path, Map<String, ?> rolesClaim, List<String> audience, LocalDateTime expiresAt) {

        String token = WebJwtTests.jwt(
                rolesClaim,
                audience,
                expiresAt
        );

        return jetty.getTarget()
                .path(path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get();
    }
}
