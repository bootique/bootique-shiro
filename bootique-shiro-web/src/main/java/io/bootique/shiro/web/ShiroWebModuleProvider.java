package io.bootique.shiro.web;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.jetty.JettyModuleProvider;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.ShiroModuleProvider;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;

public class ShiroWebModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroWebModule();
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.singleton(ShiroModule.class);
    }

    @Override
    public BQModule.Builder moduleBuilder() {
        return BQModuleProvider.super
                .moduleBuilder()
                .description("Provides integration with Apache Shiro for Java servlet applications.");
    }

    @Override
    public Map<String, Type> configs() {
        return Collections.singletonMap("shiroweb", MappedShiroFilterFactory.class);
    }

    @Override
    public Collection<BQModuleProvider> dependencies() {
        return asList(
                new JettyModuleProvider(),
                new ShiroModuleProvider()
        );
    }
}
