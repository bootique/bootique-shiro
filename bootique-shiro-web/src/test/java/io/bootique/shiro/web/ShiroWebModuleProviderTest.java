package io.bootique.shiro.web;

import io.bootique.BQRuntime;
import io.bootique.jetty.JettyModule;
import io.bootique.shiro.ShiroModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQRuntimeChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

public class ShiroWebModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testPresentInJar(ShiroWebModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroWebModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new ShiroWebModuleProvider()).createRuntime();
        BQRuntimeChecker.testModulesLoaded(bqRuntime,
                JettyModule.class,
                ShiroModule.class,
                ShiroWebModule.class
        );
    }
}
