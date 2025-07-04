package io.bootique.shiro.web.jwt;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.jersey.JerseyModule;
import io.bootique.jetty.junit5.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
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
public class JwtDefaultIT extends ShiroWebJwtModuleIT {

    private static final JettyTester jetty = JettyTester.create();
    @BQApp
    static final BQRuntime app = getApp(jetty, "classpath:io/bootique/shiro/web/jwt/jwt-default.yml");

    Map<String, ?> rolesMap(String... roles) {
        return Map.of("roles", Arrays.asList(roles));
    }

    @Override
    JettyTester jetty() {
        return jetty;
    }

    @Test
    public void testNoRolesInToken() {
        Map<String, ?> map = Map.of("noroles", "role1");
        JettyTester.assertUnauthorized(getResponse("private-one", map));
        JettyTester.assertUnauthorized(getResponse("private-two", map));
        JettyTester.assertUnauthorized(getResponse("private-three", map));
    }
}
