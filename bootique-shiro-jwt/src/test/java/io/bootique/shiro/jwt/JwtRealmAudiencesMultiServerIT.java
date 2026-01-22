package io.bootique.shiro.jwt;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@BQTest
public class JwtRealmAudiencesMultiServerIT {

    static TestAuthzServer authzServer1;
    static TestAuthzServer authzServer2;

    static BQRuntime app;
    final JwtRealm realm = app.getInstance(JwtRealm.class);

    @BeforeAll
    static void setUp(@TempDir Path tempDir) {
        authzServer1 = new TestAuthzServer(tempDir.resolve("jwks1.json"));
        authzServer2 = new TestAuthzServer(tempDir.resolve("jwks2.json"));

        app = Bootique.app()
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.shirojwt.trustedServers.s1.jwkLocation", authzServer1.jwksLocation())
                        .setProperty("bq.shirojwt.trustedServers.s1.audience", "aud-1")

                        .setProperty("bq.shirojwt.trustedServers.s2.jwkLocation", authzServer2.jwksLocation())
                        .setProperty("bq.shirojwt.trustedServers.s2.audience", "aud-2")
                )
                .createRuntime();
    }

    @Test
    public void crossServerAudience() {
        realm.doGetAuthenticationInfo(authzServer1.token(Map.of(), List.of("aud-1"), null));
        realm.doGetAuthenticationInfo(authzServer2.token(Map.of(), List.of("aud-2"), null));

        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer1.token(Map.of(), List.of("aud-2"), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer2.token(Map.of(), List.of("aud-1"), null)));
    }
}

