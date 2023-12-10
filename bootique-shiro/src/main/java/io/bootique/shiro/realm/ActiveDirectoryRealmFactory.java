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
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;

import java.util.Map;
import java.util.Objects;

@BQConfig("Creates a Realm that connects to MS ActiveDirectory.")
@JsonTypeName("activeDirectory")
public class ActiveDirectoryRealmFactory extends RealmFactory {

    private String url;
    private String searchBase;
    private String searchFilter;
    private String systemUserName;
    private String systemPassword;
    private Map<String, String> groupsToRoles;

    @BQConfigProperty
    public void setUrl(String url) {
        this.url = url;
    }

    @BQConfigProperty("A map of AD group names (e.g. 'CN=Group,OU=Company,DC=MyDomain,DC=local') to Shiro roles.")
    public void setGroupsToRoles(Map<String, String> groupsToRoles) {
        this.groupsToRoles = groupsToRoles;
    }

    @BQConfigProperty
    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    @BQConfigProperty
    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

    @BQConfigProperty
    public void setSystemPassword(String systemPassword) {
        this.systemPassword = systemPassword;
    }

    @BQConfigProperty
    public void setSystemUserName(String systemUserName) {
        this.systemUserName = systemUserName;
    }

    @Override
    public Realm createRealm() {

        ActiveDirectoryRealm realm = new ActiveDirectoryRealm();

        if (name != null) {
            realm.setName(name);
        }

        realm.setUrl(Objects.requireNonNull(url, "'url' property is required"));

        if (groupsToRoles != null) {
            realm.setGroupRolesMap(groupsToRoles);
        }

        if (searchBase != null) {
            realm.setSearchBase(searchBase);
        }

        if (searchFilter != null) {
            realm.setSearchFilter(searchFilter);
        }

        if (systemPassword != null) {
            realm.setSystemPassword(systemPassword);
        }

        if (systemUserName != null) {
            realm.setSystemUsername(systemUserName);
        }

        return realm;
    }
}
