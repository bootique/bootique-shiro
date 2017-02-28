package io.bootique.shiro.realm;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
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
    public Realm createRealm(Injector injector) {

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
