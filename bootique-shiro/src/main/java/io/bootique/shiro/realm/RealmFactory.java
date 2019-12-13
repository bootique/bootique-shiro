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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.di.Injector;
import org.apache.shiro.realm.Realm;

@BQConfig
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = IniRealmFactory.class)
public abstract class RealmFactory implements PolymorphicConfiguration {

    protected String name;

    @BQConfigProperty
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Creates a Realm of type specific to the factory and configured from the factory state. Unfortunately we have to
     * pass {@link io.bootique.di.Injector} to this method, as different factory implementations rely on different
     * injectable services.
     *
     * @param injector DI injector that a subclass can use to locate any dependencies.
     * @return a new instance of Realm.
     */
    public abstract Realm createRealm(Injector injector);
}
