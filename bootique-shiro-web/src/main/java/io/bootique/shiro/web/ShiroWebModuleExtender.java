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

    protected MapBinder<String, Filter> contributeFilters() {
        return MapBinder.newMapBinder(binder, String.class, Filter.class, ShiroFilterBinding.class);
    }
}
