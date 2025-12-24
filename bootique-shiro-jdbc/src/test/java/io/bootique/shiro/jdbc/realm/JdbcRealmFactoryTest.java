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

import io.bootique.jdbc.DataSourceFactory;
import io.bootique.junit5.PolymorphicConfigurationChecker;
import io.bootique.shiro.realm.ActiveDirectoryRealmFactory;
import io.bootique.shiro.realm.IniRealmFactory;
import io.bootique.shiro.realm.RealmFactory;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class JdbcRealmFactoryTest {

    @Test
    public void mapping() {
        PolymorphicConfigurationChecker.test(RealmFactory.class,
                IniRealmFactory.class,
                ActiveDirectoryRealmFactory.class,
                JdbcRealmFactory.class);
    }

    @Test
    public void createRealm() throws NoSuchFieldException, IllegalAccessException {

        DataSource ds = new TestDataSource();

        DataSourceFactory dsFactory = new DataSourceFactory() {
            @Override
            public DataSource forName(String name) {
                if (name.equals("testDS")) {
                    return ds;
                }

                throw new IllegalArgumentException(name);
            }

            @Override
            public Collection<String> allNames() {
                return List.of("testDS");
            }

            @Override
            public boolean isStarted(String name) {
                return false;
            }
        };

        JdbcRealmFactory factory = new JdbcRealmFactory(dsFactory);
        factory.setName("testName");
        factory.setDatasource("testDS");

        JdbcRealm realm = (JdbcRealm) factory.createRealm();
        assertEquals("testName", realm.getName());

        Field dsField = JdbcRealm.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertSame(ds, dsField.get(realm));
    }

    static class TestDataSource implements DataSource {
        @Override
        public Connection getConnection() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Connection getConnection(String username, String password) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrintWriter getLogWriter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLogWriter(PrintWriter out) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLoginTimeout(int seconds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLoginTimeout() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Logger getParentLogger() {
            throw new UnsupportedOperationException();
        }
    }
}
