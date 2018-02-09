package io.bootique.shiro.jdbc;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jdbc.JdbcModuleProvider;
import io.bootique.shiro.ShiroModuleProvider;

import java.util.Collection;

import static java.util.Arrays.asList;

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

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new JdbcModuleProvider(),
                new ShiroModuleProvider()
        );
    }
}
