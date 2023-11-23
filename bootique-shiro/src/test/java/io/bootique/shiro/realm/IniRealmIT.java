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

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class IniRealmIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app("-c", "classpath:io/bootique/shiro/realm/IniRealmIT.yml")
            .autoLoadModules()
            .createRuntime();

    @Test
    public void subject() {

        Subject subject = new Subject.Builder(app.getInstance(SecurityManager.class)).buildSubject();

        subject.login(new UsernamePasswordToken("u11", "u11p"));
        assertTrue(subject.hasRole("admin"));
        assertArrayEquals(new boolean[]{true, true, true}, subject.isPermitted("do1", "do2", "do3"));
        subject.logout();

        subject.login(new UsernamePasswordToken("u12", "u12p"));
        assertTrue(subject.hasRole("user"));
        assertArrayEquals(new boolean[]{false, false, true}, subject.isPermitted("do1", "do2", "do3"));
        subject.logout();

        subject.login(new UsernamePasswordToken("u21", "u21p"));
        assertFalse(subject.hasRole("user"));
        assertArrayEquals(new boolean[]{false, false, false}, subject.isPermitted("do1", "do2", "do3"));
        subject.logout();
    }
}
