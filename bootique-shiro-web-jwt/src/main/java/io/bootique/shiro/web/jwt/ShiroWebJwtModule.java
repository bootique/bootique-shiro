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
import io.bootique.shiro.web.ShiroWebModule;
import jakarta.inject.Singleton;

/**
 * @since 4.0
 */
public class ShiroWebJwtModule implements BQModule {

    private static final String JWT_BEARER_AUTHENTICATION_FILTER = "jwtBearer";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Provides 'jwtBearer' Shiro filter")
                .build();
    }

    @Override
    public void configure(Binder binder) {
        ShiroWebModule.extend(binder).setFilter(JWT_BEARER_AUTHENTICATION_FILTER, JwtBearerAuthenticationFilter.class);
    }

    @Provides
    @Singleton
    public JwtBearerAuthenticationFilter provideBearerFilter(ConfigurationFactory configFactory) {
        return new JwtBearerAuthenticationFilter();
    }
}
