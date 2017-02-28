package io.bootique.shiro.realm;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.inject.Injector;
import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import io.bootique.test.junit.PolymorphicConfigurationChecker;
import org.apache.shiro.realm.Realm;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class RealmFactoryInheritanceIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testCreateRealms() {
        BQRuntime bqRuntime = testFactory
                .app("-c", "classpath:io/bootique/shiro/realm/RealmFactoryInheritanceIT.yml")
                .autoLoadModules()
                .createRuntime()
                .getRuntime();

        Object[] names = bqRuntime.getInstance(Realms.class).getRealms().stream().map(Realm::getName).toArray();
        assertEquals(3, names.length);
        assertEquals("Created by RealmFactory2", names[0]);
        assertEquals("Created by RealmFactory1", names[1]);
        assertEquals("Created by RealmFactory2", names[2]);
    }

    @Test
    public void testMapping() {
        PolymorphicConfigurationChecker.test(RealmFactory.class,
                IniRealmFactory.class,
                // factories introduced by unit tests. Won't exist in production, still need to account for them here
                RealmFactory1.class,
                RealmFactory2.class);
    }

    @JsonTypeName("f1")
    public static class RealmFactory1 extends RealmFactory {

        @Override
        public Realm createRealm(Injector injector) {
            Realm realm = Mockito.mock(Realm.class);
            Mockito.when(realm.getName()).thenReturn("Created by RealmFactory1");
            return realm;
        }
    }

    @JsonTypeName("f2")
    public static class RealmFactory2 extends RealmFactory {

        @Override
        public Realm createRealm(Injector injector) {
            Realm realm = Mockito.mock(Realm.class);
            Mockito.when(realm.getName()).thenReturn("Created by RealmFactory2");
            return realm;
        }
    }
}
