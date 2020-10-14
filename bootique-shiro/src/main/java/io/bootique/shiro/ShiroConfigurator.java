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

package io.bootique.shiro;

import org.apache.shiro.realm.Realm;

import java.util.List;

/**
 * An injectable immutable holder of Shiro configuration (realms, session storage policy).
 *
 * @since 2.0.B1
 */
public class ShiroConfigurator {

    // the collection is ordered...
    private List<Realm> realms;
    private boolean sessionStorageDisabled;

    public ShiroConfigurator(List<Realm> realms, boolean sessionStorageDisabled) {
        this.realms = realms;
        this.sessionStorageDisabled = sessionStorageDisabled;
    }

    public List<Realm> getRealms() {
        return realms;
    }

    public boolean isSessionStorageDisabled() {
        return sessionStorageDisabled;
    }
}
