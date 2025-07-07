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

package io.bootique.shiro.web.jwt;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.web.ShiroWebModule;
import io.bootique.shiro.web.jwt.auth.ShiroJwtAuthFilter;
import io.bootique.shiro.web.jwt.auth.ShiroJwtAuthRealm;
import io.bootique.shiro.web.jwt.token.JwtTokenProvider;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

public class ShiroWebJwtModule implements BQModule {

    private static final String CONFIG_PREFIX = "shirowebjwt";
    private static final String JWT_BEARER_FILTER_IDENTIFIER = "jwtBearer";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Integrates JWT to Shiro")
                .config(CONFIG_PREFIX, ShiroWebJwtModuleFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        ShiroWebModule.extend(binder).setFilter(JWT_BEARER_FILTER_IDENTIFIER, ShiroJwtAuthFilter.class);
        ShiroModule.extend(binder).addRealm(ShiroJwtAuthRealm.class);
    }

    @Provides
    @Singleton
    public JwtTokenProvider provideJwtTokenProvider(ConfigurationFactory configFactory) {
        return configFactory.config(ShiroWebJwtModuleFactory.class, CONFIG_PREFIX).provideJwt();
    }

    @Provides
    @Singleton
    public ShiroJwtAuthFilter provideFilter(Provider<JwtTokenProvider> tokenProvider) {
        return new ShiroJwtAuthFilter(tokenProvider);
    }

    @Provides
    @Singleton
    public ShiroJwtAuthRealm provideRealm() {
        return new ShiroJwtAuthRealm();
    }
}
