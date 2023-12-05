/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.shiro.web;

import io.bootique.ConfigModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedFilter;
import io.bootique.shiro.ShiroConfigurator;
import io.bootique.shiro.ShiroModule;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.*;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.DefaultWebSessionStorageEvaluator;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.ServletContainerSessionManager;

import javax.inject.Singleton;
import javax.servlet.Filter;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated in favor of the Jakarta flavor
 */
@Deprecated(since = "3.0", forRemoval = true)
public class ShiroWebModule extends ConfigModule {

    public static ShiroWebModuleExtender extend(Binder binder) {
        return new ShiroWebModuleExtender(binder);
    }

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Deprecated, can be replaced with 'bootique-shiro-web-jakarta'.")
                .config("shiroweb", MappedShiroFilterFactory.class)
                .overrides(ShiroModule.class)
                .build();
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
            SubjectDAO subjectDAO,
            ShiroConfigurator realms,
            Set<AuthenticationListener> authListeners) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(realms.getRealms());

        // TODO: from here the code is copied from ShiroModule ... error prone... use factory or something
        ((AbstractAuthenticator) securityManager.getAuthenticator()).setAuthenticationListeners(authListeners);
        securityManager.setSessionManager(sessionManager);
        securityManager.setRememberMeManager(rememberMeManager);
        securityManager.setSubjectDAO(subjectDAO);
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
    MappedFilter<ShiroFilter> provideMappedShiroFilter(
            ConfigurationFactory configFactory,
            Injector injector,
            WebSecurityManager securityManager,
            @ShiroFilterBinding Map<String, Filter> chainFilters) {

        return config(MappedShiroFilterFactory.class, configFactory)
                .createShiroFilter(injector, securityManager, chainFilters);
    }

    @Provides
    @Singleton
    SessionStorageEvaluator provideSessionStorageEvaluator(ShiroConfigurator configurator) {
        DefaultWebSessionStorageEvaluator sessionStorageEvaluator = new DefaultWebSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(!configurator.isSessionStorageDisabled());
        return sessionStorageEvaluator;
    }
}
