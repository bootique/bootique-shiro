package io.bootique.shiro.jdbc;

import io.bootique.BQRuntime;
import io.bootique.jdbc.JdbcModule;
import io.bootique.shiro.ShiroModule;
import io.bootique.test.junit.BQModuleProviderChecker;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.collect.ImmutableList.of;

public class ShiroJdbcModuleProviderTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testPresentInJar(ShiroJdbcModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroJdbcModuleProvider.class);
    }

    @Test
    public void testModuleDeclaresDependencies() {
        final BQRuntime bqRuntime = testFactory.app().module(new ShiroJdbcModuleProvider()).createRuntime();
        BQModuleProviderChecker.testModulesLoaded(bqRuntime, of(
                JdbcModule.class,
                ShiroModule.class,
                ShiroJdbcModule.class
        ));
    }
}
