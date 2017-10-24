package io.bootique.shiro;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ShiroModuleIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    protected Realm mockRealm() {
        Realm mockRealm = mock(Realm.class);
        when(mockRealm.getName()).thenReturn("TestRealm");
        when(mockRealm.supports(any(AuthenticationToken.class))).then(invocation -> {
            AuthenticationToken token = invocation.getArgumentAt(0, AuthenticationToken.class);
            return token instanceof UsernamePasswordToken;
        });

        when(mockRealm.getAuthenticationInfo(any(AuthenticationToken.class))).then(invocation -> {

            UsernamePasswordToken token = invocation.getArgumentAt(0, UsernamePasswordToken.class);
            if (!"password".equals(new String(token.getPassword()))) {
                throw new AuthenticationException("Bad password");
            }

            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "TestRealm");
        });

        return mockRealm;
    }

    @Test
    public void testFullStack() {

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
            Assert.fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            assertFalse(subject.isAuthenticated());
        }

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));

        assertTrue(subject.isAuthenticated());
    }

    @Test
    public void testFullStack_SecurityUtils() {
        Realm mockRealm = mockRealm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule.extend(b).addRealm(mockRealm))
                .autoLoadModules()
                .createRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();

        assertNull(ThreadContext.getSubject());

        // testing Shiro idiom of wrapping lambda in a subject...
        subject.execute(() -> {
            assertSame("Unexpected subject, thread state is disturbed", subject, SecurityUtils.getSubject());
        });
    }

    @Test
    public void testFullStack_AuthListener() {

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
            Assert.fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            verify(mockListener).onFailure(any(AuthenticationToken.class), any(AuthenticationException.class));
        }

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));
        verify(mockListener).onSuccess(any(AuthenticationToken.class), any(AuthenticationInfo.class));
    }
}
