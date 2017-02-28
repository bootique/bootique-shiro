package io.bootique.shiro.realm;

import com.google.inject.Injector;
import org.apache.shiro.realm.Realm;

public abstract class RealmFactory {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Creates a Realm of type specific to the factory and configured from the factory state. Unfortunately we have to
     * pass {@link com.google.inject.Injector} to this method, as different factory implementations rely on different
     * injectable services.
     *
     * @param injector DI injector that a subclass can use to locate any dependencies.
     * @return a new instance of Realm.
     */
    public abstract Realm createRealm(Injector injector);
}