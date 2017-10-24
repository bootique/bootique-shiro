package io.bootique.shiro.web.mdc;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class ShiroWebMDCModuleIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testContainerState() {
        BQRuntime runtime = testFactory.app().autoLoadModules().createRuntime();
        PrincipalMDCCleaner cleaner = runtime.getInstance(PrincipalMDCCleaner.class);
        PrincipalMDCInitializer initializer = runtime.getInstance(PrincipalMDCInitializer.class);
        assertSame(cleaner.principalMDC, initializer.principalMDC);
    }
}
