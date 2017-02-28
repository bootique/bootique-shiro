package io.bootique.shiro.jdbc;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

public class ShiroJdbcModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroJdbcModule();
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides a factory to create JDBC Realms based on a set of configurable SQL queries.");
    }
}
