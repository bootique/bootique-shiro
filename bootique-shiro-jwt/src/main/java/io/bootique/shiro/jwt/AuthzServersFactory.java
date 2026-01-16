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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.shiro.jwt.authz.AuthzServer;
import io.bootique.shiro.jwt.authz.AuthzServerFactory;
import io.bootique.shiro.jwt.authz.AuthzServers;
import io.bootique.value.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @since 4.0
 */
@BQConfig("Configuration of one or more token-issuing trusted authorization servers.")
public class AuthzServersFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthzServersFactory.class);

    private static final java.time.Duration DEFAULT_JWK_EXPIRES_IN = java.time.Duration.ofDays(100 * 365);

    private Duration jwkExpiresIn;
    private Map<String, AuthzServerFactory> trustedServers;

    @BQConfigProperty("A map of authorization servers whose tokens will be trusted by the app")
    public AuthzServersFactory setTrustedServers(Map<String, AuthzServerFactory> trustedServers) {
        this.trustedServers = trustedServers;
        return this;
    }

    @BQConfigProperty("Expiration interval when JWKS must be reloaded")
    public AuthzServersFactory setJwkExpiresIn(Duration jwkExpiresIn) {
        this.jwkExpiresIn = jwkExpiresIn;
        return this;
    }

    public AuthzServers createAuthzServers() {
        java.time.Duration expiresIn = jwkExpiresIn != null ? jwkExpiresIn.getDuration() : DEFAULT_JWK_EXPIRES_IN;

        List<AuthzServer> servers;
        if (trustedServers == null || trustedServers.isEmpty()) {
            LOGGER.warn("""
                    No trusted authorization servers were configured under "shirojwt.trustedServers". \
                    JWT realm will always fail authentication.""");
            servers = List.of();
        } else {

            // notice that here we lose trusted server symbolic names. Those names are only used for simplicity
            // in addressing configuration nodes and are not needed in runtime. In runtime, servers are looked up
            // by JWK "kid" (key id).
            servers = trustedServers.values().stream().map(AuthzServerFactory::createAuthzServer).toList();
        }

        return new AuthzServers(servers, expiresIn);
    }
}
