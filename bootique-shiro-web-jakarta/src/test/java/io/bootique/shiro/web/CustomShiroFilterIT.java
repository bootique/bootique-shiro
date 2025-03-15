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

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.di.Key;
import io.bootique.di.TypeLiteral;
import io.bootique.jetty.MappedFilter;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import io.bootique.shiro.ShiroModule;
import jakarta.servlet.Filter;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Map;

import static org.mockito.Mockito.mock;

@BQTest
public class CustomShiroFilterIT {

    @BQApp(skipRun = true)
    static final BQRuntime app = Bootique
            .app("-c", "classpath:CustomShiroFilterIT.yml")
            .autoLoadModules()
            .module(b -> ShiroModule.extend(b).addRealm(mock(Realm.class)))
            .createRuntime();

    @Test
    public void customFactory() {

        TypeLiteral<MappedFilter<ShiroFilter>> filterKey = new TypeLiteral<MappedFilter<ShiroFilter>>() {
        };
        MappedFilter<ShiroFilter> mappedFilter = app.getInstance(Key.get(filterKey));

        Assertions.assertTrue(mappedFilter.getFilter() instanceof CustomFilter);
    }

    @JsonTypeName("it")
    public static class CustomFactory extends MappedShiroFilterFactory {

        @Inject
        public CustomFactory(WebSecurityManager securityManager, @ShiroFilterBinding  Map<String, Filter> chainFilters) {
            super(securityManager, chainFilters);
        }

        @Override
        protected ShiroFilter createShiroFilter(FilterChainResolver chainResolver) {
            return new CustomFilter(securityManager, chainResolver);
        }
    }

    public static class CustomFilter extends ShiroFilter {
        public CustomFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
            super(securityManager, filterChainResolver);
        }
    }
}
