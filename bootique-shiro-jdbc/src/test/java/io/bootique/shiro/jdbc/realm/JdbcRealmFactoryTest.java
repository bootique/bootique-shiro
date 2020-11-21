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

package io.bootique.shiro.jdbc.realm;

import io.bootique.di.Injector;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.junit5.PolymorphicConfigurationChecker;
import io.bootique.shiro.realm.ActiveDirectoryRealmFactory;
import io.bootique.shiro.realm.IniRealmFactory;
import io.bootique.shiro.realm.RealmFactory;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcRealmFactoryTest {

    @Test
    public void testMapping() {
        PolymorphicConfigurationChecker.test(RealmFactory.class,
                IniRealmFactory.class,
                ActiveDirectoryRealmFactory.class,
                JdbcRealmFactory.class);
    }

    @Test
    public void testCreateRealm() throws NoSuchFieldException, IllegalAccessException {

        DataSource ds = mock(DataSource.class);

        DataSourceFactory mockDSFactory = mock((DataSourceFactory.class));
        when(mockDSFactory.forName("testDS")).thenReturn(ds);

        Injector injector = mock(Injector.class);
        Mockito.when(injector.getInstance(DataSourceFactory.class)).thenReturn(mockDSFactory);


        JdbcRealmFactory factory = new JdbcRealmFactory();
        factory.setName("testName");
        factory.setDatasource("testDS");

        JdbcRealm realm = (JdbcRealm) factory.createRealm(injector);
        assertEquals("testName", realm.getName());

        Field dsField = JdbcRealm.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertSame(ds, dsField.get(realm));
    }
}
