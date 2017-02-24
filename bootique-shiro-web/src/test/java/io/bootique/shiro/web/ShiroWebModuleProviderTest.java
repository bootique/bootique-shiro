package io.bootique.shiro.web;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ShiroWebModuleProviderTest {

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testPresentInJar(ShiroWebModuleProvider.class);
    }

    @Test
    public void testMetadata() {
        BQModuleProviderChecker.testMetadata(ShiroWebModuleProvider.class);
    }
}
