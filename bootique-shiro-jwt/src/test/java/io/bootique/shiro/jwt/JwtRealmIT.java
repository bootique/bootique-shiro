package io.bootique.shiro.jwt;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@BQTest
public class JwtRealmIT {

    static TestAuthzServer authzServer;
    static BQRuntime app;

    final JwtRealm realm = app.getInstance(JwtRealm.class);

    @BeforeAll
    static void setUp(@TempDir Path tempDir) {
        authzServer = new TestAuthzServer(tempDir.resolve("jwks.json"));
        app = Bootique.app()
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b).setProperty(
                        "bq.shirojwt.trustedServers.s1.jwkLocation",
                        authzServer.jwksLocation())
                )
                .createRuntime();
    }

    @Test
    public void doGetAuthenticationInfo() {
        AuthenticationInfo info = realm.doGetAuthenticationInfo(authzServer.token(Map.of("c1", "x1"), null, null));
        Collection<JwtPrincipal> principals = info.getPrincipals().byType(JwtPrincipal.class);
        assertEquals(1, principals.size());

        JwtPrincipal p = principals.iterator().next();
        assertEquals(authzServer.getKeyId(), p.kid());
        assertEquals("x1", p.claims().get("c1", String.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"r1", "r1,r3", ""})
    public void doGetAuthorizationInfo(String roles) {
        List<String> rl = Stream.of(roles.split(",")).toList();

        AuthenticationInfo auth = realm.doGetAuthenticationInfo(authzServer.token(Map.of("roles", rl), null, null));

        AuthorizationInfo authz = realm.doGetAuthorizationInfo(auth.getPrincipals());
        assertEquals(new HashSet<>(rl), authz.getRoles());
    }
}

