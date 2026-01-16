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
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class JwtRealmRolesIT {


    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app()
            .autoLoadModules()
            .module(b -> BQCoreModule.extend(b).setProperty(
                    "bq.shirojwt.trustedServers.default.jwkLocation",
                    JwtTests.AUTHZ1.jwksLocation()))
            .createRuntime();

    @ParameterizedTest
    @ValueSource(strings = {"r1", "r1,r3", ""})
    public void roles1(String roles) {
        JwtRealm realm = app.getInstance(JwtRealm.class);
        List<String> rl = Stream.of(roles.split(",")).toList();

        AuthenticationInfo auth = realm.doGetAuthenticationInfo(JwtTests.AUTHZ1.token(Map.of("roles", rl), null, null));

        AuthorizationInfo authz = realm.doGetAuthorizationInfo(auth.getPrincipals());
        assertEquals(new HashSet<>(rl), authz.getRoles());
    }
}
