package io.bootique.shiro.realm;

import io.bootique.BQRuntime;
import io.bootique.shiro.subject.SubjectManager;
import io.bootique.test.junit.BQTestFactory;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class IniRealmIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testSubject() {

        BQRuntime bqRuntime = testFactory
                .app("-c", "classpath:io/bootique/shiro/realm/IniRealmIT.yml")
                .autoLoadModules()
                .createRuntime()
                .getRuntime();

        Subject subject = bqRuntime.getInstance(SubjectManager.class).subject();

        subject.login(new UsernamePasswordToken("u11", "u11p"));
        Assert.assertTrue(subject.hasRole("admin"));
        assertArrayEquals(new boolean[]{true, true, true}, subject.isPermitted("do1", "do2", "do3"));
        subject.logout();

        subject.login(new UsernamePasswordToken("u12", "u12p"));
        Assert.assertTrue(subject.hasRole("user"));
        assertArrayEquals(new boolean[]{false, false, true}, subject.isPermitted("do1", "do2", "do3"));
        subject.logout();

        subject.login(new UsernamePasswordToken("u21", "u21p"));
        Assert.assertFalse(subject.hasRole("user"));
        assertArrayEquals(new boolean[]{false, false, false}, subject.isPermitted("do1", "do2", "do3"));
        subject.logout();
    }
}
