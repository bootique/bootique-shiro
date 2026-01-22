package io.bootique.shiro.web.jwt;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@BQTest
public class AudiencesIT {

    static final JettyTester jetty = JettyTester.create();

    static TestAuthzServer authzServer;
    static BQRuntime app;

    // TODO: problem connecting @BQApp and @TempDir annotated services, so have to manually
    //  manage the Bootique app lifecycle
    @BeforeAll
    static void start(@TempDir  java.nio.file.Path tempDir) {
        authzServer = new TestAuthzServer(tempDir.resolve("jwks.json"));
        app = Bootique
                .app("-s", "-c", "classpath:io/bootique/shiro/web/jwt/jwt-audience.yml")
                .autoLoadModules()
                .module(jetty.moduleReplacingConnectors())
                .module(b -> JerseyModule.extend(b).addApiResource(TestApi.class))

                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.shirojwt.trustedServers.default.jwkLocation", authzServer.jwksLocation())
                )
                .createRuntime();

        app.run();
    }

    @AfterAll
    static void stop() {
        app.shutdown();
    }

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

        String token = authzServer.token(rolesClaim, audience, null);

        return jetty.getTarget()
                .path(path)
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get();
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
}

