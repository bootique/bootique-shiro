package io.bootique.shiro.web.mdc;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.shiro.web.ShiroWebModuleProvider;

import java.util.Collection;

import static java.util.Collections.singletonList;

public class ShiroWebMDCModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroWebMDCModule();
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return singletonList(
                new ShiroWebModuleProvider()
        );
    }
}
