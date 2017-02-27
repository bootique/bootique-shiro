package io.bootique.shiro.web;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public class ShiroWebModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ShiroWebModule();
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
}
