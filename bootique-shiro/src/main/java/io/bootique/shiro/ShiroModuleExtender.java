package io.bootique.shiro;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import org.apache.shiro.realm.Realm;

public class ShiroModuleExtender extends ModuleExtender<ShiroModuleExtender> {

    private Multibinder<Realm> realms;

    public ShiroModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public ShiroModuleExtender initAllExtensions() {
        contributeRealms();
        return this;
    }

    public ShiroModuleExtender addRealm(Realm realm) {
        contributeRealms().addBinding().toInstance(realm);
        return this;
    }

    public ShiroModuleExtender addRealm(Class<? extends Realm> realmType) {
        contributeRealms().addBinding().to(realmType);
        return this;
    }

    protected Multibinder<Realm> contributeRealms() {
        return realms != null ? realms : (realms = Multibinder.newSetBinder(binder, Realm.class));
    }
}
