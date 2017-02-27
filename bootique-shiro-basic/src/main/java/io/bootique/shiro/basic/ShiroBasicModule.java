package io.bootique.shiro.basic;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;

import java.util.Set;

public class ShiroBasicModule implements Module {

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton
    SecurityManager provideSecurityManager(SessionManager sessionManager, Set<Realm> realms) {
        DefaultSecurityManager manager = new DefaultSecurityManager(realms);
        manager.setSessionManager(sessionManager);
        return manager;
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager() {
        return new DefaultSessionManager();
    }
}
