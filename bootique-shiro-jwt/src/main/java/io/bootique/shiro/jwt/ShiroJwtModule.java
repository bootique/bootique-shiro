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

package io.bootique.shiro.jwt;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.jwt.authz.AuthzServers;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.inject.Singleton;

/**
 * @since 4.0
 */
public class ShiroJwtModule implements BQModule {

    public static final String CONFIG_PREFIX = "shirojwt";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Provides JWT parser and JWT realm for the Shiro environment")
                .config(CONFIG_PREFIX, AuthzServersFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
        ShiroModule.extend(binder).addRealm(JwtRealm.class);
    }

    @Provides
    @Singleton
    public AuthzServers provideAuthzServers(ConfigurationFactory configFactory) {
        return configFactory
                .config(AuthzServersFactory.class, CONFIG_PREFIX)
                .createAuthzServers();
    }

    @Provides
    @Singleton
    public JwtParser provideJwtParser(AuthzServers servers) {
        return Jwts.parser()
                .keyLocator(h -> servers.getKey(h.getOrDefault("kid", "")))
                .build();
    }

    @Provides
    @Singleton
    public JwtRealm provideRealm(AuthzServers servers) {
        return new JwtRealm(servers);
    }
}
