package io.bootique.shiro;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.shiro.realm.RealmsFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

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

    @Override
    public Map<String, Type> configs() {
        // TODO: config prefix is hardcoded. Refactor away from ConfigModule, and make provider
        // generate config prefix, reusing it in metadata...
        return Collections.singletonMap("shiro", RealmsFactory.class);
    }
}
