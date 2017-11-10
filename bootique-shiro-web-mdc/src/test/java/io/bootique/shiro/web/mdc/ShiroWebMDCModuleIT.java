package io.bootique.shiro.web.mdc;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.bootique.BQRuntime;
import io.bootique.jetty.MappedListener;
import io.bootique.shiro.ShiroModule;
import io.bootique.test.junit.BQTestFactory;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ShiroWebMDCModuleIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testContainerState() {
        BQRuntime runtime = testFactory.app().autoLoadModules().createRuntime();
        MappedListener<ShiroWebMDCCleaner> cleaner = runtime.getInstance(Key.get(new TypeLiteral<MappedListener<ShiroWebMDCCleaner>>() {
        }));
        OnAuthMDCInitializer initializer = runtime.getInstance(OnAuthMDCInitializer.class);
        assertSame(cleaner.getListener().principalMDC, initializer.principalMDC);
    }

    @Test
    public void testContainerState_InitializerListener() {
        Realm mockRealm = mock(Realm.class);
        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule.extend(b).addRealm(mockRealm))
                .autoLoadModules()
                .createRuntime();

        DefaultSecurityManager securityManager = (DefaultSecurityManager) runtime.getInstance(SecurityManager.class);
        AbstractAuthenticator authenticator = (AbstractAuthenticator) securityManager.getAuthenticator();
        assertEquals(1, authenticator.getAuthenticationListeners().size());
        assertTrue(authenticator.getAuthenticationListeners().iterator().next() instanceof OnAuthMDCInitializer);
    }
}
