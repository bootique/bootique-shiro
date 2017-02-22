package io.bootique.shiro;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;

public class ShiroModule extends ConfigModule {

    @Provides
    @Singleton
    SubjectManager provideSubjectManager() {
        return new ThreadLocalSubjectManager();
    }

   
}
