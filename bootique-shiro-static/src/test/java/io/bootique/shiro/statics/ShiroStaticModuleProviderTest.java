package io.bootique.shiro.statics;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ShiroStaticModuleProviderTest {

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testPresentInJar(ShiroStaticModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroStaticModuleProvider.class);
    }
}
