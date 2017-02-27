package io.bootique.shiro.basic;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ShiroBasicModuleProviderTest {

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testPresentInJar(ShiroBasicModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroBasicModuleProvider.class);
    }
}
