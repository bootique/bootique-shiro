package io.bootique.shiro.jwt;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@BQTest
public class JwtRealmMultiServerIT {

    static TestAuthzServer authzServer1;
    static TestAuthzServer authzServer2;
    static TestAuthzServer authzServer3;

    static BQRuntime app;

    final JwtRealm realm = app.getInstance(JwtRealm.class);

    @BeforeAll
    static void setUp(@TempDir Path tempDir) {

        authzServer1 = new TestAuthzServer(tempDir.resolve("jwks1.json"));
        authzServer2 = new TestAuthzServer(tempDir.resolve("jwks2.json"));
        authzServer3 = new TestAuthzServer(tempDir.resolve("jwks3.json"));

        app = Bootique.app()
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.shirojwt.trustedServers.s1.jwkLocation", authzServer1.jwksLocation())
                        .setProperty("bq.shirojwt.trustedServers.s2.jwkLocation", authzServer2.jwksLocation())
                )
                .createRuntime();
    }

    @Test
    public void doGetAuthenticationInfo() {
        for (TestAuthzServer s : List.of(authzServer1, authzServer2)) {
            AuthenticationInfo info = realm.doGetAuthenticationInfo(s.token(Map.of("c1", "x1"), null, null));
            Collection<JwtPrincipal> principals = info.getPrincipals().byType(JwtPrincipal.class);
            assertEquals(1, principals.size());

            JwtPrincipal p = principals.iterator().next();
            assertEquals(s.getKeyId(), p.kid());
            assertEquals("x1", p.claims().get("c1", String.class));
        }
    }

    @Test
    public void doGetAuthenticationInfo_UnknownServer() {
        assertThrows(
                AuthenticationException.class,
                () -> realm.doGetAuthenticationInfo(authzServer3.token(Map.of("c1", "x1"), null, null)));
    }
}

