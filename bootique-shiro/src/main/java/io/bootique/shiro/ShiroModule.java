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

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.shiro.mdc.PrincipalMDC;
import io.bootique.shiro.mgt.NoRememberMeManager;
import io.bootique.shiro.realm.Realms;
import io.bootique.shiro.realm.RealmsFactory;
import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;

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
    Realms provideRealms(Injector injector, ConfigurationFactory configurationFactory, Set<Realm> diRealms) {
        return configurationFactory
                .config(RealmsFactory.class, configPrefix)
                .createRealms(injector, diRealms);
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
            Realms realms,
            Set<AuthenticationListener> authListeners) {


        DefaultSecurityManager manager = new DefaultSecurityManager(realms.getRealms());
        ((AbstractAuthenticator) manager.getAuthenticator()).setAuthenticationListeners(authListeners);
        manager.setSessionManager(sessionManager);
        manager.setRememberMeManager(rememberMeManager);

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
}
