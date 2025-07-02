package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@BQTest
public class JwtSpaceSeparatedIT extends ShiroWebJwtModuleIT {
    private static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = getApp(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-ss.yml");

    Map<String, ?> rolesMap(String... roles) {
        String s = String.join(" ", roles);
        return Map.of("scope", s);
    }

    @Override
    JettyTester jetty() {
        return jetty;
    }

}
