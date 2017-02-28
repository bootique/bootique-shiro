package io.bootique.shiro.statics;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.shiro.SubjectManager;
import io.bootique.shiro.ThreadLocalSubjectManager;
import io.bootique.shiro.realm.Realms;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;

public class ShiroStaticModule implements Module {

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton
    SubjectManager provideSubjectManager(Provider<SecurityManager> securityManagerProvider) {
        return new ThreadLocalSubjectManager() {

            @Override
            public Subject subject() {

                // make sure SecurityManager is resolved before returning the subject... Resolving SM initializes
                // static variable via SecurityUtils (see below).
                securityManagerProvider.get();

                return super.subject();
            }
        };
    }

    @Provides
    @Singleton
    SecurityManager provideSecurityManager(SessionManager sessionManager, Realms realms) {
        DefaultSecurityManager manager = new DefaultSecurityManager(realms.getRealms());
        manager.setSessionManager(sessionManager);

        // init static SecurityManager
        SecurityUtils.setSecurityManager(manager);

        return manager;
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager() {
        return new DefaultSessionManager();
    }


}
