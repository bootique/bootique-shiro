package io.bootique.shiro;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.shiro.realm.Realms;
import io.bootique.shiro.realm.RealmsFactory;
import org.apache.shiro.realm.Realm;

import java.util.Set;

/**
 * A foundation module to start an Apache Shiro stack. Defines configurable Shiro realms. {@link SubjectManager},
 * {@link org.apache.shiro.mgt.SecurityManager}, filters, etc. are defined in other environment-specific modules.
 */
public class ShiroModule extends ConfigModule {

    public static ShiroModuleExtender extend(Binder binder) {
        return new ShiroModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {
        extend(binder).initAllExtensions();
    }


    @Provides
    @Singleton
    Realms provideRealms(Injector injector, ConfigurationFactory configurationFactory, Set<Realm> diRealms) {
        return configurationFactory
                .config(RealmsFactory.class, configPrefix)
                .createRealms(injector, diRealms);
    }
}
