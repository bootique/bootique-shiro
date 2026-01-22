package io.bootique.shiro.jwt;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@BQTest
public class JwtRealmIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique.app()
            .autoLoadModules()
            .module(b -> BQCoreModule.extend(b).setProperty(
                    "bq.shirojwt.trustedServers.default.jwkLocation",
                    JwtTests.jwksLocation())
            )
            .createRuntime();

    @Test
    public void doGetAuthenticationInfo() {
        JwtRealm realm = app.getInstance(JwtRealm.class);

        AuthenticationInfo info = realm.doGetAuthenticationInfo(JwtTests.token(Map.of("c1", "x1"), null, null));
        Collection<JwtPrincipal> principals = info.getPrincipals().byType(JwtPrincipal.class);
        assertEquals(1, principals.size());

        JwtPrincipal p = principals.iterator().next();
        assertEquals(JwtTests.KEY_ID, p.kid());
        assertEquals("x1", p.claims().get("c1", String.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"r1", "r1,r3", ""})
    public void doGetAuthorizationInfo(String roles) {
        JwtRealm realm = app.getInstance(JwtRealm.class);
        List<String> rl = Stream.of(roles.split(",")).toList();

        AuthenticationInfo auth = realm.doGetAuthenticationInfo(JwtTests.token(Map.of("roles", rl), null, null));

        AuthorizationInfo authz = realm.doGetAuthorizationInfo(auth.getPrincipals());
        assertEquals(new HashSet<>(rl), authz.getRoles());
    }
}

