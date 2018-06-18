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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.shiro.realm.RealmFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;

@BQConfig("Creates a Realm from user accounts and roles specified in configuration.")
@JsonTypeName("jdbc")
public class JdbcRealmFactory extends RealmFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcRealmFactory.class);

    private String datasource;
    private String authenticationQuery;
    private boolean lookupPermissions;
    private String permissionsQuery;
    private JdbcRealm.SaltStyle saltStyle;
    private String userRolesQuery;

    @BQConfigProperty
    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    @BQConfigProperty("SQL query to return hashed password for user name. The default is 'SELECT password FROM users " +
            "WHERE username = ?'.")
    public void setAuthenticationQuery(String authenticationQuery) {
        this.authenticationQuery = authenticationQuery;
    }

    @BQConfigProperty
    public void setLookupPermissions(boolean lookupPermissions) {
        this.lookupPermissions = lookupPermissions;
    }

    @BQConfigProperty("SQL query to return permissions for role name. The default is 'SELECT permission FROM " +
            "roles_permissions WHERE role_name = ?'")
    public void setPermissionsQuery(String permissionsQuery) {
        this.permissionsQuery = permissionsQuery;
    }

    @BQConfigProperty
    public void setSaltStyle(JdbcRealm.SaltStyle saltStyle) {
        this.saltStyle = saltStyle;
    }

    @BQConfigProperty("SQL query to return roles for user name. The default is 'SELECT role_name FROM user_roles WHERE " +
            "username = ?'.")
    public void setUserRolesQuery(String userRolesQuery) {
        this.userRolesQuery = userRolesQuery;
    }

    @Override
    public Realm createRealm(Injector injector) {

        DataSource ds = findDataSource(injector.getInstance(DataSourceFactory.class));

        JdbcRealm realm = new JdbcRealm();

        if (name != null) {
            realm.setName(name);
        }

        realm.setDataSource(ds);
        realm.setPermissionsLookupEnabled(lookupPermissions);

        if (authenticationQuery != null) {
            realm.setAuthenticationQuery(authenticationQuery);
        }

        if (permissionsQuery != null) {
            realm.setPermissionsQuery(permissionsQuery);
        }

        if (saltStyle != null) {
            realm.setSaltStyle(saltStyle);
        }

        if(userRolesQuery != null) {
            realm.setUserRolesQuery(userRolesQuery);
        }

        return realm;
    }

    protected DataSource findDataSource(DataSourceFactory factory) {
        if (this.datasource == null) {

            Collection<String> allNames = factory.allNames();
            if (allNames.size() == 1) {
                String defaultName = allNames.iterator().next();
                LOGGER.debug("No explicit DataSource name is set, using default '{}'", defaultName);
                return factory.forName(defaultName);
            }

            throw new IllegalStateException("No explicit DataSource name is set, and no default DataSource is defined");
        }

        return factory.forName(datasource);
    }
}
