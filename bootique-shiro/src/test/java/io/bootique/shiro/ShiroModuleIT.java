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

package io.bootique.shiro;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class ShiroModuleIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @BeforeEach
    public void beforeEach() {
        TestAuthListener.reset();
    }

    protected Realm realm() {
        SimpleAccountRealm realm = new SimpleAccountRealm("TestRealm") {
            @Override
            protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
                if (!"password".equals(new String(((UsernamePasswordToken) token).getPassword()))) {
                    throw new AuthenticationException("Bad password");
                }

                return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "TestRealm");
            }
        };
        realm.setAuthenticationTokenClass(UsernamePasswordToken.class);

        return realm;
    }

    @Test
    public void fullStack() {

        Realm realm = realm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule.extend(b).addRealm(realm))
                .autoLoadModules()
                .createRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();
        assertFalse(subject.isAuthenticated());

        // try bad login
        try {
            subject.login(new UsernamePasswordToken("uname", "badpassword"));
            fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            assertFalse(subject.isAuthenticated());
        }

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));

        assertTrue(subject.isAuthenticated());
    }

    @Test
    public void fullStack_SecurityUtils() {
        Realm mockRealm = realm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule.extend(b).addRealm(mockRealm))
                .autoLoadModules()
                .createRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();

        assertNull(ThreadContext.getSubject());

        // testing Shiro idiom of wrapping lambda in a subject...
        subject.execute(() -> assertSame(subject, SecurityUtils.getSubject(), "Unexpected subject, thread state is disturbed"));
    }

    @Test
    public void fullStack_AuthListener() {

        Realm mockRealm = realm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule
                        .extend(b)
                        .addRealm(mockRealm)
                        .addAuthListener(new TestAuthListener()))
                .autoLoadModules()
                .createRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();
        assertFalse(subject.isAuthenticated());

        // try bad login
        try {
            subject.login(new UsernamePasswordToken("uname", "badpassword"));
            fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            assertFalse(TestAuthListener.onSuccess);
            assertFalse(TestAuthListener.onLogout);
            assertTrue(TestAuthListener.onFailure);
        }

        TestAuthListener.reset();

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));
        assertTrue(TestAuthListener.onSuccess);
        assertFalse(TestAuthListener.onLogout);
        assertFalse(TestAuthListener.onFailure);
    }

    @Test
    public void fullStack_AuthListenerType() {

        Realm realm = realm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule
                        .extend(b)
                        .addRealm(realm)
                        .addAuthListener(TestAuthListener.class))
                .autoLoadModules()
                .createRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();
        assertFalse(subject.isAuthenticated());

        // try bad login
        try {
            subject.login(new UsernamePasswordToken("uname", "badpassword"));
            fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            assertTrue(TestAuthListener.onFailure);
        }

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));
        assertTrue(TestAuthListener.onSuccess);
    }

    public static class TestAuthListener implements AuthenticationListener {

        static boolean onSuccess;
        static boolean onFailure;
        static boolean onLogout;

        public static void reset() {
            onSuccess = false;
            onLogout = false;
            onFailure = false;
        }

        @Override
        public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
            onSuccess = true;
        }

        @Override
        public void onFailure(AuthenticationToken token, AuthenticationException ae) {
            onFailure = true;
        }

        @Override
        public void onLogout(PrincipalCollection principals) {
            onLogout = true;
        }
    }

}
