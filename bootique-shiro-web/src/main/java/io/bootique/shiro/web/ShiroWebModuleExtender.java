/**
 *    Licensed to the ObjectStyle LLC under one
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

package io.bootique.shiro.web;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import io.bootique.ModuleExtender;

import javax.servlet.Filter;

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
        contributeFilters().addBinding(name).toInstance(filter);
        return this;
    }

    public ShiroWebModuleExtender setFilter(String name, Class<? extends Filter> filterType) {
        contributeFilters().addBinding(name).to(filterType);
        return this;
    }

    protected MapBinder<String, Filter> contributeFilters() {
        return newMap(String.class, Filter.class, ShiroFilterBinding.class);
    }
}
