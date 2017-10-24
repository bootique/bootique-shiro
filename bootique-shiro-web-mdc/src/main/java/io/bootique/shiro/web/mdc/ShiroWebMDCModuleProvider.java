package io.bootique.shiro.web.mdc;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;

public class ShiroWebMDCModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroWebMDCModule();
    }
}
