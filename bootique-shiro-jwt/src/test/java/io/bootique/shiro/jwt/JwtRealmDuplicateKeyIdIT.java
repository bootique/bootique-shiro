/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
public class JwtRealmDuplicateKeyIdIT {

    private static final String SHARED_KEY_ID = "shared-key-id";

    static TestAuthzServer authzServer1;
    static TestAuthzServer authzServer2;

    static BQRuntime app;
    final JwtRealm realm = app.getInstance(JwtRealm.class);

    @BeforeAll
    static void setUp(@TempDir Path tempDir) {
        authzServer1 = new TestAuthzServer(tempDir.resolve("jwks1.json"), SHARED_KEY_ID);
        authzServer2 = new TestAuthzServer(tempDir.resolve("jwks2.json"), SHARED_KEY_ID);

        app = Bootique.app()
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.shirojwt.trustedServers.s1.jwkLocation", authzServer1.jwksLocation())
                        .setProperty("bq.shirojwt.trustedServers.s1.audience", "api://tenant-1")

                        .setProperty("bq.shirojwt.trustedServers.s2.jwkLocation", authzServer2.jwksLocation())
                        .setProperty("bq.shirojwt.trustedServers.s2.audience", "api://tenant-2")
                )
                .createRuntime();
    }

    @Test
    public void duplicateKeyId_BothAudiencesShouldBeAccepted() {
        // Both audiences should be accepted since the realm trusts both servers
        realm.doGetAuthenticationInfo(authzServer1.token(Map.of("c", "C"), List.of("api://tenant-1"), null));
        realm.doGetAuthenticationInfo(authzServer1.token(Map.of("c", "C"), List.of("api://tenant-2"), null));
        realm.doGetAuthenticationInfo(authzServer2.token(Map.of("c", "C"), List.of("api://tenant-1"), null));
        realm.doGetAuthenticationInfo(authzServer2.token(Map.of("c", "C"), List.of("api://tenant-2"), null));
    }

    @Test
    public void duplicateKeyId_WrongAudience() {
        // Tokens with untrusted audiences should fail
        assertThrows(AuthenticationException.class,
                () -> realm.doGetAuthenticationInfo(authzServer1.token(Map.of("c", "C"), List.of("api://wrong"), null)));

        assertThrows(AuthenticationException.class,
                () -> realm.doGetAuthenticationInfo(authzServer2.token(Map.of("c", "C"), List.of("api://wrong"), null)));
    }

    @Test
    public void duplicateKeyId_MultipleAudiences() {
        // Tokens with multiple audiences including a trusted one should succeed
        realm.doGetAuthenticationInfo(authzServer1.token(Map.of("c", "C"), List.of("api://tenant-1", "api://other"), null));
        realm.doGetAuthenticationInfo(authzServer2.token(Map.of("c", "C"), List.of("api://tenant-2", "api://other"), null));

        // Tokens with multiple audiences but none trusted should fail
        assertThrows(AuthenticationException.class,
                () -> realm.doGetAuthenticationInfo(authzServer1.token(Map.of("c", "C"), List.of("api://other", "api://wrong"), null)));
    }

    @Test
    public void duplicateKeyId_NoAudience() {
        // Tokens without audience should fail
        assertThrows(AuthenticationException.class,
                () -> realm.doGetAuthenticationInfo(authzServer1.token(Map.of("c", "C"), List.of(), null)));

        assertThrows(AuthenticationException.class,
                () -> realm.doGetAuthenticationInfo(authzServer2.token(Map.of("c", "C"), null, null)));
    }
}
