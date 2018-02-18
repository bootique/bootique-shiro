package io.bootique.shiro;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ShiroModuleProviderTest {

    @Test
    public void testAutoLoadable() {
        BQModuleProviderChecker.testAutoLoadable(ShiroModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroModuleProvider.class);
    }
}
