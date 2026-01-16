package io.bootique.shiro.jwt;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@BQTest
public class JwtRealmAudiencesIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(b -> BQCoreModule.extend(b)
                    .setProperty("bq.shirojwt.trustedServers.default.jwkLocation", JwtTests.AUTHZ1.jwksLocation())
                    .setProperty("bq.shirojwt.trustedServers.default.audience", "aud-1")
            )
            .createRuntime();

    @Test
    public void singleAudience() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of("aud-1"), null));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of("aud-2"), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of("aud-3"), null)));
    }

    @Test
    public void multipleAudiences() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of("aud-1", "aud-2"), null));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of("aud-2", "aud-3"), null)));
        realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of("aud-1", "aud-3"), null));
    }

    @Test
    public void noAudience() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), List.of(), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of(), null, null)));
    }
}

