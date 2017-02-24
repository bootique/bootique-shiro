package io.bootique.shiro.web;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.config.ConfigurationFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;

import javax.servlet.Filter;
import java.util.Map;
import java.util.Set;

public class ShiroWebModule implements Module {

    static final String CONFIG_PREFIX = "shiro";

    public static ShiroWebModuleExtender extend(Binder binder) {
        return new ShiroWebModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {
        extend(binder).initAllExtensions();
    }

    @Singleton
    @Provides
    WebSecurityManager provideWebSecurityManager(Set<Realm> realms) {
        return new DefaultWebSecurityManager(realms);
    }

    @Singleton
    @Provides
    SecurityManager provideSecurityManager(WebSecurityManager webSecurityManager) {
        return webSecurityManager;
    }

    @Singleton
    @Provides
    FilterChainResolver provideFilterChainResolver(
            ConfigurationFactory configurationFactory,
            @ShiroFilterBinding Map<String, Filter> shiroFilters) {

        return configurationFactory
                .config(FilterChainResolverFactory.class, CONFIG_PREFIX)
                .createFilterChainResolver(shiroFilters);
    }

    @Singleton
    @Provides
    ShiroFilter provideShiroFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
        return new ShiroFilter(securityManager, filterChainResolver);
    }
}
