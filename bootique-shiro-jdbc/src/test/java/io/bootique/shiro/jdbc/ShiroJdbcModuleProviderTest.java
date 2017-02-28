package io.bootique.shiro.jdbc;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ShiroJdbcModuleProviderTest {


    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testPresentInJar(ShiroJdbcModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroJdbcModuleProvider.class);
    }
}
