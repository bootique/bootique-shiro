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

package io.bootique.shiro.realm;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.di.Injector;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.PolymorphicConfigurationChecker;
import io.bootique.shiro.ShiroConfigurator;
import org.apache.shiro.realm.Realm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class RealmFactoryInheritanceIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app("-c", "classpath:io/bootique/shiro/realm/RealmFactoryInheritanceIT.yml")
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testCreateRealms() {

        Object[] names = app.getInstance(ShiroConfigurator.class).getRealms().stream().map(Realm::getName).toArray();
        assertEquals(3, names.length);
        assertEquals("Created by RealmFactory2", names[0]);
        assertEquals("Created by RealmFactory1", names[1]);
        assertEquals("Created by RealmFactory2", names[2]);
    }

    @Test
    public void testMapping() {
        PolymorphicConfigurationChecker.test(
                RealmFactory.class,
                IniRealmFactory.class,
                ActiveDirectoryRealmFactory.class,
                // factories introduced by unit tests. Won't exist in production, still need to account for them here
                RealmFactory1.class,
                RealmFactory2.class);
    }

    @JsonTypeName("f1")
    public static class RealmFactory1 extends RealmFactory {

        @Override
        public Realm createRealm(Injector injector) {
            Realm realm = Mockito.mock(Realm.class);
            Mockito.when(realm.getName()).thenReturn("Created by RealmFactory1");
            return realm;
        }
    }

    @JsonTypeName("f2")
    public static class RealmFactory2 extends RealmFactory {

        @Override
        public Realm createRealm(Injector injector) {
            Realm realm = Mockito.mock(Realm.class);
            Mockito.when(realm.getName()).thenReturn("Created by RealmFactory2");
            return realm;
        }
    }
}
