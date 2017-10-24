package io.bootique.shiro.web;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedFilter;
import io.bootique.shiro.realm.Realms;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;

import javax.servlet.Filter;
import java.util.Map;
import java.util.Set;

public class ShiroWebModule extends ConfigModule {

    public static ShiroWebModuleExtender extend(Binder binder) {
        return new ShiroWebModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {
        extend(binder).initAllExtensions();
        JettyModule.extend(binder).addMappedFilter(new TypeLiteral<MappedFilter<ShiroFilter>>() {
        });
    }

    @Singleton
    @Provides
    RememberMeManager provideRememberMeManager() {
        return new CookieRememberMeManager();
    }

    @Singleton
    @Provides
    WebSecurityManager provideWebSecurityManager(
            SessionManager sessionManager,
            RememberMeManager rememberMeManager,
            Realms realms,
            Set<AuthenticationListener> authListeners) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(realms.getRealms());

        // TODO: from here the code is copied from ShiroModule ... error prone... use factory or something
        ((AbstractAuthenticator) securityManager.getAuthenticator()).setAuthenticationListeners(authListeners);
        securityManager.setSessionManager(sessionManager);
        securityManager.setRememberMeManager(rememberMeManager);
        return securityManager;
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager() {
        return new ServletContainerSessionManager();
    }

    @Singleton
    @Provides
    SecurityManager provideSecurityManager(WebSecurityManager webSecurityManager) {
        return webSecurityManager;
    }

    @Singleton
    @Provides
    MappedFilter<ShiroFilter> provideMappedShiroFilter(ConfigurationFactory configFactory,
                                                       Injector injector,
                                                       WebSecurityManager securityManager,
                                                       @ShiroFilterBinding Map<String, Filter> chainFilters) {
        return configFactory
                .config(MappedShiroFilterFactory.class, configPrefix)
                .createShiroFilter(injector, securityManager, chainFilters);
    }
}
