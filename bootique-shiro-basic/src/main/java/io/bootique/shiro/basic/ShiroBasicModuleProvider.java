package io.bootique.shiro.basic;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class ShiroBasicModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroBasicModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides basic Apache Shiro stack based on DefaultSecurityManager.");
    }
}
