package io.bootique.shiro.jdbc.realm;

import com.google.inject.Injector;
import io.bootique.jdbc.DataSourceFactory;
import io.bootique.shiro.realm.ActiveDirectoryRealmFactory;
import io.bootique.shiro.realm.IniRealmFactory;
import io.bootique.shiro.realm.RealmFactory;
import io.bootique.test.junit.PolymorphicConfigurationChecker;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
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
