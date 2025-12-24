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

import io.bootique.shiro.realm.RealmFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class ShiroConfiguratorFactoryTest {

    @Test
    public void createRealms_NoConfig() {

        Realm r1 = new SimpleAccountRealm();
        Realm r2 = new SimpleAccountRealm();

        Set<Realm> diRealms = new HashSet<>(asList(r1, r2));

        ShiroConfiguratorFactory shiroConfiguratorFactory = new ShiroConfiguratorFactory(diRealms);

        ShiroConfigurator configurator = shiroConfiguratorFactory.create();
        assertNotNull(configurator);
        assertEquals(2, configurator.getRealms().size());
        assertTrue(configurator.getRealms().contains(r1));
        assertTrue(configurator.getRealms().contains(r2));
    }

    @Test
    public void createRealms_NoDi() {

        Realm r1 = new SimpleAccountRealm();
        Realm r2 = new SimpleAccountRealm();

        RealmFactory rf1 = new RealmFactory() {
            @Override
            public Realm createRealm() {
                return r1;
            }
        };

        RealmFactory rf2 = new RealmFactory() {
            @Override
            public Realm createRealm() {
                return r2;
            }
        };

        List<RealmFactory> configFactories = asList(rf1, rf2);

        ShiroConfiguratorFactory shiroConfiguratorFactory = new ShiroConfiguratorFactory(Set.of());
        shiroConfiguratorFactory.setRealms(configFactories);

        ShiroConfigurator configurator = shiroConfiguratorFactory.create();
        assertNotNull(configurator);
        assertEquals(2, configurator.getRealms().size());
        assertEquals(r1, configurator.getRealms().get(0), "Realm ordering got lost");
        assertEquals(r2, configurator.getRealms().get(1), "Realm ordering got lost");
    }

    @Test
    public void createRealms_DiAndConfig() {

        Realm rdi1 = new SimpleAccountRealm();
        Realm rdi2 = new SimpleAccountRealm();

        Set<Realm> diRealms = new HashSet<>(asList(rdi1, rdi2));

        Realm r1 = new SimpleAccountRealm();
        Realm r2 = new SimpleAccountRealm();

        RealmFactory rf1 = new RealmFactory() {
            @Override
            public Realm createRealm() {
                return r1;
            }
        };

        RealmFactory rf2 = new RealmFactory() {
            @Override
            public Realm createRealm() {
                return r2;
            }
        };

        List<RealmFactory> configFactories = asList(rf1, rf2);

        ShiroConfiguratorFactory shiroConfiguratorFactory = new ShiroConfiguratorFactory(diRealms);
        shiroConfiguratorFactory.setRealms(configFactories);

        ShiroConfigurator configurator = shiroConfiguratorFactory.create();
        assertNotNull(configurator);
        assertEquals(2, configurator.getRealms().size());
        assertEquals(r1, configurator.getRealms().get(0), "Wrong Realms or Realm ordering got lost");
        assertEquals(r2, configurator.getRealms().get(1), "Wrong Realms or Realm ordering got lost");
    }
}
