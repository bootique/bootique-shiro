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
public class JwtRealmAudiencesIT {

    static TestAuthzServer authzServer;
    static BQRuntime app;

    final JwtRealm realm = app.getInstance(JwtRealm.class);

    @BeforeAll
    static void setUp(@TempDir Path tempDir) {
        authzServer = new TestAuthzServer(tempDir.resolve("jwks.json"));
        app = Bootique.app()
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.shirojwt.trustedServers.s1.jwkLocation", authzServer.jwksLocation())
                        .setProperty("bq.shirojwt.trustedServers.s1.audience", "aud-1")
                )
                .createRuntime();
    }

    @Test
    public void singleAudience() {
        realm.doGetAuthenticationInfo(authzServer.token(Map.of(), List.of("aud-1"), null));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer.token(Map.of(), List.of("aud-2"), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer.token(Map.of(), List.of("aud-3"), null)));
    }

    @Test
    public void multipleAudiences() {
        realm.doGetAuthenticationInfo(authzServer.token(Map.of(), List.of("aud-1", "aud-2"), null));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer.token(Map.of(), List.of("aud-2", "aud-3"), null)));
        realm.doGetAuthenticationInfo(authzServer.token(Map.of(), List.of("aud-1", "aud-3"), null));
    }

    @Test
    public void noAudience() {
        // no audience may result in the empty token payload which is not allows, so add a few meaningless claims to fill the token instead
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer.token(Map.of("c", "C"), List.of(), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(authzServer.token(Map.of("c", "C"), null, null)));
    }
}

