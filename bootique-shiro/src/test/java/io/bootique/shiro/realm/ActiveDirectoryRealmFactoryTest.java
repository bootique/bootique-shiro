package io.bootique.shiro.realm;

import com.google.inject.Injector;
import org.apache.shiro.realm.activedirectory.ActiveDirectoryRealm;
import org.apache.shiro.realm.ldap.AbstractLdapRealm;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class ActiveDirectoryRealmFactoryTest {

    @Test
    public void testCreateRealm() throws NoSuchFieldException, IllegalAccessException {
        ActiveDirectoryRealmFactory factory = new ActiveDirectoryRealmFactory();
        factory.setName("xyz");
        factory.setUrl("ldap://example.org");
        factory.setSearchBase("sb");

        ActiveDirectoryRealm realm = (ActiveDirectoryRealm) factory.createRealm(mock(Injector.class));

        Assert.assertEquals("xyz", realm.getName());

        Field urlField = AbstractLdapRealm.class.getDeclaredField("url");
        urlField.setAccessible(true);
        assertSame("ldap://example.org", urlField.get(realm));

        Field sbField = AbstractLdapRealm.class.getDeclaredField("searchBase");
        sbField.setAccessible(true);
        assertSame("sb", sbField.get(realm));
    }
}
