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
import org.apache.shiro.authc.*;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@BQTest
public class ShiroModuleIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    protected Realm mockRealm() {
        Realm mockRealm = mock(Realm.class);
        when(mockRealm.getName()).thenReturn("TestRealm");
        when(mockRealm.supports(any(AuthenticationToken.class))).then(invocation -> {
            AuthenticationToken token = invocation.getArgument(0);
            return token instanceof UsernamePasswordToken;
        });

        when(mockRealm.getAuthenticationInfo(any(AuthenticationToken.class))).then(invocation -> {

            UsernamePasswordToken token = invocation.getArgument(0);
            if (!"password".equals(new String(token.getPassword()))) {
                throw new AuthenticationException("Bad password");
            }

            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "TestRealm");
        });

        return mockRealm;
    }

    @Test
    public void fullStack() {

        Realm mockRealm = mockRealm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule.extend(b).addRealm(mockRealm))
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
        Realm mockRealm = mockRealm();

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

        Realm mockRealm = mockRealm();
        AuthenticationListener mockListener = mock(AuthenticationListener.class);

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule
                        .extend(b)
                        .addRealm(mockRealm)
                        .addAuthListener(mockListener))
                .autoLoadModules()
                .createRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();
        assertFalse(subject.isAuthenticated());

        // try bad login
        try {
            subject.login(new UsernamePasswordToken("uname", "badpassword"));
            fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            verify(mockListener).onFailure(any(AuthenticationToken.class), any(AuthenticationException.class));
        }

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));
        verify(mockListener).onSuccess(any(AuthenticationToken.class), any(AuthenticationInfo.class));
    }

    @Test
    public void fullStack_AuthListenerType() {

        TestAuthListener.reset();

        Realm mockRealm = mockRealm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule
                        .extend(b)
                        .addRealm(mockRealm)
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
