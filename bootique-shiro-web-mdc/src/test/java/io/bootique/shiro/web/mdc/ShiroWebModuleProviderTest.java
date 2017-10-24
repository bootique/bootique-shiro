package io.bootique.shiro.web.mdc;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class ShiroWebModuleProviderTest {

    @Test
    public void testPresentInJar() {
        BQModuleProviderChecker.testPresentInJar(ShiroWebMDCModuleProvider.class);
    }
}
