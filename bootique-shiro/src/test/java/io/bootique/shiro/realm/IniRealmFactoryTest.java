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

import io.bootique.di.Injector;
import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.text.IniRealm;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class IniRealmFactoryTest {

    @Test
    public void testCreateRealm() {
        IniRealmFactory factory = new IniRealmFactory();
        factory.setName("xyz");
        factory.setRoles(Collections.singletonMap("r1", "p1, p2"));
        factory.setUsers(Collections.singletonMap("u1", "up, r1"));

        IniRealm realm = (IniRealm) factory.createRealm(mock(Injector.class));

        assertEquals("xyz", realm.getName());
        assertNull(realm.getResourcePath());

        Ini ini = realm.getIni();
        assertNotNull(realm.getIni());
        assertNotNull(ini.getSection("users"));
        assertNotNull(ini.getSection("roles"));
    }
}
