package io.bootique.shiro;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.ModuleExtender;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.realm.Realm;

public class ShiroModuleExtender extends ModuleExtender<ShiroModuleExtender> {

    private Multibinder<Realm> realms;
    private Multibinder<AuthenticationListener> listeners;

    public ShiroModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public ShiroModuleExtender initAllExtensions() {
        contributeRealms();
        contributeListeners();
        return this;
    }

    /**
     * @param listener a listener instance.
     * @return this extender instance.
     * @since 0.25
     */
    public ShiroModuleExtender addAuthListener(AuthenticationListener listener) {
        contributeListeners().addBinding().toInstance(listener);
        return this;
    }

    /**
     * @param listenerType a class of the auth listener
     * @return this extender instance.
     * @since 0.25
     */
    public ShiroModuleExtender addAuthListener(Class<? extends AuthenticationListener> listenerType) {
        contributeListeners().addBinding().to(listenerType);
        return this;
    }

    /**
     * Adds a new Realm. If any Realms are configured under "shiro.realms" configuration node, this and other extender
     * Realms will be ignored.
     *
     * @param realm a Realm to add.
     * @return this extender instance.
     */
    public ShiroModuleExtender addRealm(Realm realm) {
        contributeRealms().addBinding().toInstance(realm);
        return this;
    }

    /**
     * Adds a new unnamed Realm of a given type. If any Realms are configured under "shiro.realms" configuration node,
     * this and other extender Realms will be ignored.
     *
     * @param realmType a Realm type. The type is resolved via DI, so users can additionally specify a DI provider for
     *                  it, customizing initialization.
     * @return
     */
    public ShiroModuleExtender addRealm(Class<? extends Realm> realmType) {
        contributeRealms().addBinding().to(realmType);
        return this;
    }

    protected Multibinder<Realm> contributeRealms() {
        return realms != null ? realms : (realms = newSet(Realm.class));
    }

    protected Multibinder<AuthenticationListener> contributeListeners() {
        return listeners != null ? listeners : (listeners = newSet(AuthenticationListener.class));
    }

}
