package io.bootique.shiro.jwt;

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
    static final BQRuntime app = Bootique
            .app("-c", "classpath:io/bootique/shiro/jwt/jwt-audience.yml")
            .autoLoadModules()
            .createRuntime();

    @Test
    public void singleAudience() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of("aud-1"), null));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of("aud-2"), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of("aud-3"), null)));
    }

    @Test
    public void multipleAudiences() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of("aud-1", "aud-2"), null));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of("aud-2", "aud-3"), null)));
        realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of("aud-1", "aud-3"), null));
    }

    @Test
    public void noAudience() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), List.of(), null)));
        assertThrows(AuthenticationException.class, () -> realm.doGetAuthenticationInfo(JwtTests.token(Map.of(), null, null)));
    }
}

