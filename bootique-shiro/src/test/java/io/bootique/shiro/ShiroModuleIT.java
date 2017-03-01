package io.bootique.shiro;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
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
                .createRuntime()
                .getRuntime();

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
                .createRuntime()
                .getRuntime();

        Subject subject = new Subject.Builder(runtime.getInstance(SecurityManager.class)).buildSubject();

        assertNull(ThreadContext.getSubject());

        // testing Shiro idiom of wrapping lambda in a subject...
        subject.execute(() -> {
            assertSame("Unexpected subject, thread state is disturbed", subject, SecurityUtils.getSubject());
        });
    }
}
