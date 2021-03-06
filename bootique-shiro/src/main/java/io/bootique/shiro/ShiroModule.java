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

package io.bootique.shiro;

import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Provides;
import io.bootique.shiro.mdc.PrincipalMDC;
import io.bootique.shiro.mgt.NoRememberMeManager;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.*;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;

import javax.inject.Singleton;
import java.util.Set;

/**
 * Specifies a generic fully functional Shiro stack.
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
    ShiroConfigurator provideRealms(Injector injector, ConfigurationFactory configFactory, Set<Realm> diRealms) {
        return config(ShiroConfiguratorFactory.class, configFactory).createConfigurator(injector, diRealms);
    }

    @Singleton
    @Provides
    RememberMeManager provideRememberMeManager() {
        return new NoRememberMeManager();
    }

    @Provides
    @Singleton
    SecurityManager provideSecurityManager(
            SessionManager sessionManager,
            RememberMeManager rememberMeManager,
            SubjectDAO subjectDAO,
            ShiroConfigurator configurator,
            Set<AuthenticationListener> authListeners) {

        DefaultSecurityManager manager = new DefaultSecurityManager(configurator.getRealms());
        ((AbstractAuthenticator) manager.getAuthenticator()).setAuthenticationListeners(authListeners);
        manager.setSessionManager(sessionManager);
        manager.setRememberMeManager(rememberMeManager);
        manager.setSubjectDAO(subjectDAO);

        return manager;
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager() {
        return new DefaultSessionManager();
    }

    @Provides
    @Singleton
    PrincipalMDC providePrincipalMDC() {
        return new PrincipalMDC();
    }

    @Provides
    @Singleton
    SubjectDAO provideSubjectDAO(SessionStorageEvaluator sessionStorageEvaluator) {
        DefaultSubjectDAO dao = new DefaultSubjectDAO();
        dao.setSessionStorageEvaluator(sessionStorageEvaluator);
        return dao;
    }

    @Provides
    @Singleton
    SessionStorageEvaluator provideSessionStorageEvaluator(ShiroConfigurator configurator) {
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(!configurator.isSessionStorageDisabled());
        return sessionStorageEvaluator;
    }
}
