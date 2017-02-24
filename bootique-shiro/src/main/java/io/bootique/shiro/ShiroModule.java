package io.bootique.shiro;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;

import java.util.Set;

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
    SubjectManager provideSubjectManager() {
        return new ThreadLocalSubjectManager();
    }

    @Provides
    @Singleton
    SecurityManager provideSecurityManager(Set<Realm> realms) {
        return new DefaultSecurityManager(realms);
    }
}
