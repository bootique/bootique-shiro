package io.bootique.shiro.web.mdc;

import io.bootique.BQRuntime;
import io.bootique.shiro.web.ShiroWebModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class ShiroWebModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(ShiroWebMDCModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new ShiroWebMDCModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                ShiroWebModule.class,
                ShiroWebMDCModule.class
        );
    }
}
