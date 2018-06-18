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
import com.google.inject.Injector;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;

import java.util.Map;

/**
 * Creates an {@link IniRealm} from user accounts and roles specified in configuration.
 */
@BQConfig("Creates a Realm from user accounts and roles specified in configuration.")
@JsonTypeName("ini")
public class IniRealmFactory extends RealmFactory {

    private Map<String, String> users;
    private Map<String, String> roles;

    @BQConfigProperty("A map of user account names to user password and roles. The values should be in the format "
            + "used by the [users] section of a Shiro .ini file. Namely 'password, roleName1, roleName2, …, roleNameN'.")
    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    @BQConfigProperty("A map of role names to role permissions. The values should be in the format used by the [roles] "
            + "section of a Shiro .ini file. Namely 'permissionDefinition1, permissionDefinition2, ..., "
            + "permissionDefinitionN'.")
    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }

    @Override
    public Realm createRealm(Injector injector) {

        Ini ini = new Ini();

        if (users != null && !users.isEmpty()) {
            ini.addSection("users").putAll(users);
        }

        if (roles != null && !roles.isEmpty()) {
            ini.addSection("roles").putAll(roles);
        }

        IniRealm realm = new IniRealm(ini);
        realm.setIni(ini);

        if (name != null) {
            realm.setName(name);
        }

        return realm;
    }
}
