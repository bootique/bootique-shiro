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
package io.bootique.shiro.web.oidconnect;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.MappedResource;
import io.bootique.shiro.web.ShiroWebModule;
import io.bootique.shiro.web.jwt.ShiroWebJwtModule;
import io.bootique.shiro.web.jwt.ShiroWebJwtModuleFactory;
import jakarta.inject.Singleton;

/**
 * @since 4.0
 */
public class ShiroWebOidConnectModule implements BQModule {

    private static final String CONFIG_PREFIX = "shiroweboidconnect";
    private static final String OID_CONNECT_AUTHENTICATION_FILTER = "oidConnect";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Integrates OpenId Connect to Shiro")
                .config(CONFIG_PREFIX, ShiroWebOidConnectModuleFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        ShiroWebModule.extend(binder).setFilter(OID_CONNECT_AUTHENTICATION_FILTER, OidConnectFilter.class);
        JerseyModule.extend(binder).addMappedResource(new TypeLiteral<MappedResource<AuthorizationCodeHandlerApi>>() {
        });
    }

    @Provides
    @Singleton
    public OidConnectFilter provideOidConnectFilter(ConfigurationFactory configFactory) {
        String audience = configFactory
                .config(ShiroWebJwtModuleFactory.class, ShiroWebJwtModule.CONFIG_PREFIX)
                .provideAudience();

        return configFactory.config(ShiroWebOidConnectModuleFactory.class, CONFIG_PREFIX).createFilter(audience);
    }

    @Provides
    @Singleton
    public MappedResource<AuthorizationCodeHandlerApi> provideAuthorizationCodeHandlerApi(ConfigurationFactory configFactory) {

        String audience = configFactory
                .config(ShiroWebJwtModuleFactory.class, ShiroWebJwtModule.CONFIG_PREFIX)
                .provideAudience();

        return configFactory
                .config(ShiroWebOidConnectModuleFactory.class, CONFIG_PREFIX)
                .createAuthorizationCodeHandler(audience);
    }
}
