package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@BQTest
public class AudiencesIT extends ShiroWebJwtModuleIT {

    private static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = runtime(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-audience.yml");

    @Test
    public void singleAudience() {
        Map<String, ?> map = Map.of("roles", List.of("role1", "role2", "role3"));
        List<String> audience = List.of("aud-1");
        JettyTester.assertOk(requestWithToken("/private-one", map, audience));
        audience = List.of("aud-2");
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, audience));
        audience = List.of("aud-3");
        JettyTester.assertUnauthorized(requestWithToken("/private-three", map, audience));
    }

    @Test
    public void multipleAudiences() {
        Map<String, ?> map = Map.of("roles", List.of("role1", "role2", "role3"));
        List<String> audience = List.of("aud-1", "aud-2");
        JettyTester.assertOk(requestWithToken("/private-one", map, audience));
        audience = List.of("aud-2", "aud-3");
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, audience));
        audience = List.of("aud-1", "aud-3");
        JettyTester.assertOk(requestWithToken("/private-three", map, audience));
    }

    @Test
    public void noAudience() {
        Map<String, ?> map = Map.of("roles", List.of("role1", "role2", "role3"));
        JettyTester.assertUnauthorized(requestWithToken("/private-one", map, Collections.emptyList()));
        JettyTester.assertUnauthorized(requestWithToken("/private-two", map, null));
    }

    private Response requestWithToken(String path, Map<String, ?> rolesClaim, List<String> audience) {

        String token = WebJwtTests.jwt(rolesClaim, audience, null);

        return jetty.getTarget()
                .path(path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get();
    }
}

