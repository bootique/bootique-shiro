/**
 *    Licensed to ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.shiro.realm;

import io.bootique.BQRuntime;
import io.bootique.test.junit.BQTestFactory;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
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
                .createRuntime();

        Subject subject = new Subject.Builder(bqRuntime.getInstance(SecurityManager.class)).buildSubject();

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
