package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.junit.jupiter.api.Test;

import java.util.*;

@BQTest
public class AudienceValidationIT extends ShiroWebJwtModuleIT {

    private static final JettyTester jetty = JettyTester.create();
    @BQApp
    static final BQRuntime app = runtime(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-audience.yml");

    @Override
    protected JettyTester jetty() {
        return jetty;
    }

    @Test
    public void testWithSingleAudienceInToken() {
        Map<String, ?> map = rolesMap(List.of("role1", "role2", "role3"));
        List<String> audience = List.of("aud-1");
        JettyTester.assertOk(getResponse("private-one", map, audience));
        audience = List.of("aud-2");
        JettyTester.assertUnauthorized(getResponse("private-two", map, audience));
        audience = List.of("aud-3");
        JettyTester.assertUnauthorized(getResponse("private-three", map, audience));
    }

    @Test
    public void testWithCollectionOfAudienceInToken() {
        Map<String, ?> map = rolesMap(List.of("role1", "role2", "role3"));
        List<String> audience = List.of("aud-1", "aud-2");
        JettyTester.assertOk(getResponse("private-one", map, audience));
        audience = List.of("aud-2", "aud-3");
        JettyTester.assertUnauthorized(getResponse("private-two", map, audience));
        audience = List.of("aud-1", "aud-3");
        JettyTester.assertOk(getResponse("private-three", map, audience));
    }

    @Test
    public void testWithoutAudienceInToken() {
        Map<String, ?> map = rolesMap(List.of("role1", "role2", "role3"));
        JettyTester.assertUnauthorized(getResponse("private-one", map, Collections.emptyList()));
        JettyTester.assertUnauthorized(getResponse("private-two", map, null));
    }
}

