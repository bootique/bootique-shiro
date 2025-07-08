package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.junit.jupiter.api.Test;
import java.util.*;

@BQTest
public class DefaultJwtIT extends ShiroWebJwtModuleIT {

    private static final JettyTester jetty = JettyTester.create();
    @BQApp
    static final BQRuntime app = runtime(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-default.yml");

    @Override
    protected JettyTester jetty() {
        return jetty;
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
        JettyTester.assertUnauthorized(getResponse("private-one", map, 15, null));
    }

    @Test
    public void testWithoutRolesClaim() {
        Map<String, ?> map = rolesMap("no-roles", List.of("role1"));
        JettyTester.assertUnauthorized(getResponse("private-one", map));
        JettyTester.assertUnauthorized(getResponse("private-two", map));
        JettyTester.assertUnauthorized(getResponse("private-three", map));
    }

    @Test
    public void testWithoutAudienceValidation() {
        Map<String, ?> map = rolesMap(List.of("role1"));
        List<String> audience = List.of("aud-1", "aud-2");
        JettyTester.assertOk(getResponse("private-one", map, audience)).assertContent("private-one");
        JettyTester.assertUnauthorized(getResponse("private-two", map, audience));
        JettyTester.assertUnauthorized(getResponse("private-three", map, audience));
    }
}
