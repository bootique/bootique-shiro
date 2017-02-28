package io.bootique.shiro.statics;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class ShiroStaticModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroStaticModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Sets up Apache Shiro stack, initializing static singletons in Shiro with " +
                        "Bootique-provided SecurityManager.");
    }
}
