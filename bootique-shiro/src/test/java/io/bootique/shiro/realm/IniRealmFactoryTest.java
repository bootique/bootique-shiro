package io.bootique.shiro.realm;

import com.google.inject.Injector;
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
