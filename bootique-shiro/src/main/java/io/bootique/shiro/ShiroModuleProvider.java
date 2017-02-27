package io.bootique.shiro;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class ShiroModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides integration of Apache Shiro realms. Serves as a foundation for " +
                        "other environment-specific Shiro modules.");
    }
}
