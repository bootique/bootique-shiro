package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@BQTest
public class JwtTreeNameIT extends ShiroWebJwtModuleIT {
    private static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = getApp(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-tn.yml");

    Map<String, ?> rolesMap(String... roles) {
        Map<String, ?> client = Map.of("roles", Arrays.asList(roles));
        Map<String, ?> access = Map.of("client", client);
        return Map.of("access", access);
    }

    @Override
    JettyTester jetty() {
        return jetty;
    }

}
