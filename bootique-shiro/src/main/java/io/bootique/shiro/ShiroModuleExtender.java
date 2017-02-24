package io.bootique.shiro;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import org.apache.shiro.realm.Realm;

public class ShiroModuleExtender extends ModuleExtender<ShiroModuleExtender> {

    public ShiroModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public ShiroModuleExtender initAllExtensions() {
        contributeRealms();
        return this;
    }

    protected  Multibinder<Realm> contributeRealms() {
        return Multibinder.newSetBinder(binder, Realm.class);
    }
}
