package io.bootique.shiro.web;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import io.bootique.ModuleExtender;

import javax.servlet.Filter;

public class ShiroWebModuleExtender extends ModuleExtender<ShiroWebModuleExtender> {

    public ShiroWebModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public ShiroWebModuleExtender initAllExtensions() {
        contributeFilters();
        return this;
    }

    public ShiroWebModuleExtender setFilter(String name, Filter filter) {
        contributeFilters().addBinding(name).toInstance(filter);
        return this;
    }

    public ShiroWebModuleExtender setFilter(String name, Class<? extends Filter> filterType) {
        contributeFilters().addBinding(name).to(filterType);
        return this;
    }

    protected MapBinder<String, Filter> contributeFilters() {
        return newMap(String.class, Filter.class, ShiroFilterBinding.class);
    }
}
