package io.bootique.shiro.web;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedFilter;
import io.bootique.shiro.SubjectManager;
import io.bootique.shiro.ThreadLocalSubjectManager;
import io.bootique.shiro.realm.Realms;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;

import javax.servlet.Filter;
import java.util.Map;

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
    WebSecurityManager provideWebSecurityManager(SessionManager sessionManager, Realms realms) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(realms.getRealms());
        securityManager.setSessionManager(sessionManager);
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
                                                       WebSecurityManager securityManager,
                                                       @ShiroFilterBinding Map<String, Filter> chainFilters) {
        return configFactory
                .config(MappedShiroFilterFactory.class, configPrefix)
                .createShiroFilter(securityManager, chainFilters);
    }

    @Provides
    @Singleton
    SubjectManager provideSubjectManager() {
        return new ThreadLocalSubjectManager();
    }
}
