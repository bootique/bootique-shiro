package io.bootique.shiro;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;

/**
 * A foundation module to start an Apache Shiro stack. Defines configurable Shiro realms and a {@link SubjectManager}.
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
    SubjectManager provideSubjectManager() {
        return new ThreadLocalSubjectManager();
    }
}
