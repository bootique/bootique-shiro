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

package io.bootique.shiro.web;

import io.bootique.ModuleExtender;
import io.bootique.di.Binder;
import io.bootique.di.MapBuilder;
import jakarta.servlet.Filter;


public class ShiroWebModuleExtender extends ModuleExtender<ShiroWebModuleExtender> {

    public ShiroWebModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public ShiroWebModuleExtender initAllExtensions() {
        contributeFilters();
        return this;
    }

    public ShiroWebModuleExtender setFilter(String name, Filter filter) {
        contributeFilters().putInstance(name, filter);
        return this;
    }

    public ShiroWebModuleExtender setFilter(String name, Class<? extends Filter> filterType) {
        contributeFilters().put(name, filterType);
        return this;
    }

    protected MapBuilder<String, Filter> contributeFilters() {
        return newMap(String.class, Filter.class, ShiroFilterBinding.class);
    }
}
