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
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@BQTest
public class NoRealmIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app()
            .autoLoadModules()
            .createRuntime();

    @Test
    public void testSubject() {

        try {
            Subject subject = new Subject.Builder(app.getInstance(SecurityManager.class)).buildSubject();
            subject.login(new UsernamePasswordToken("uname", "password"));
            fail("Must have failed on a placeholder Realm");

        } catch (UnsupportedTokenException e) {
            // expected
            assertEquals("Realm 'do_nothing_realm' is a placeholder and does not support authentication. You need to configure a real Realm"
                    , e.getMessage());
        }
    }
}
