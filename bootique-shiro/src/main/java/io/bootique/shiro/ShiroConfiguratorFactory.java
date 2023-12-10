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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.di.Injector;
import io.bootique.shiro.realm.RealmFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @since 2.0
 */
@BQConfig
public class ShiroConfiguratorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroConfiguratorFactory.class);

    private final Set<Realm> diRealms;

    private List<RealmFactory> realms;
    private boolean sessionStorageDisabled;

    @Inject
    public ShiroConfiguratorFactory(Set<Realm> diRealms) {
        this.diRealms = diRealms;
    }

    @BQConfigProperty
    public void setRealms(List<RealmFactory> realms) {
        this.realms = realms;
    }

    @BQConfigProperty("Whether to disable Subject session storage, ensuring a session-less app. " +
            "Default is 'false' to match Shiro defaults")
    public void setSessionStorageDisabled(boolean sessionStorageDisabled) {
        this.sessionStorageDisabled = sessionStorageDisabled;
    }

    public ShiroConfigurator create() {
        return new ShiroConfigurator(createRealms(), sessionStorageDisabled);
    }

    protected List<Realm> createRealms() {
        List<Realm> allRealms = new ArrayList<>();
        loadConfiguredRealms(allRealms);

        if (allRealms.isEmpty()) {
            loadDiRealms(allRealms);

            // ignoring DI Realms if at least one config realm exists. This allows to fully override and/or order realms
            // without recompiling

        } else if (!diRealms.isEmpty() && LOGGER.isInfoEnabled()) {
            String realmNames = diRealms.stream().map(this::realmName).collect(joining(", "));
            LOGGER.info("Ignoring DI-originated Realms: " + realmNames + ". Using Realms from configuration instead.");
        }

        if (allRealms.isEmpty()) {
            LOGGER.warn("No Realms configured for Shiro");
            loadPlaceholderRealm(allRealms);
        }

        return allRealms;
    }

    void loadConfiguredRealms(List<Realm> collector) {
        if (realms != null) {
            realms.forEach(rf -> collector.add(rf.createRealm()));
        }
    }

    void loadDiRealms(List<Realm> collector) {
        collector.addAll(diRealms);
    }

    void loadPlaceholderRealm(List<Realm> collector) {

        collector.add(new Realm() {

            @Override
            public String getName() {
                return "do_nothing_realm";
            }

            @Override
            public boolean supports(AuthenticationToken token) {
                throw new UnsupportedTokenException("Realm '" + getName()
                        + "' is a placeholder and does not support authentication. You need to configure a real Realm");
            }

            @Override
            public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
                throw new AuthenticationException("This is a placeholder Realm that does not support authentication");
            }
        });
    }

    private String realmName(Realm r) {
        return r.getName() != null ? r.getName() : r.getClass().getSimpleName();
    }
}
