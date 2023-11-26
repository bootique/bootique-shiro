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

package io.bootique.shiro.web.mdc;

import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jetty.JettyModule;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.mdc.PrincipalMDC;
import io.bootique.shiro.web.ShiroWebModule;

import javax.inject.Singleton;
import java.util.Collection;

import static java.util.Collections.singletonList;

public class ShiroWebMDCModule implements BQModule, BQModuleProvider {

    @Override
    public ModuleCrate moduleCrate() {
        return ModuleCrate.of(this)
                .description("Integrates MDC logging of Apache Shiro subjects")
                .build();
    }

    @Override
    @Deprecated(since = "3.0", forRemoval = true)
    public Collection<BQModuleProvider> dependencies() {
        return singletonList(new ShiroWebModule());
    }

    @Override
    public void configure(Binder binder) {
        JettyModule.extend(binder).addRequestMDCItem(PrincipalMDC.MDC_KEY, ShiroWebPrincipalMDCItem.class);
        ShiroModule.extend(binder).addAuthListener(ShiroWebPrincipalMDCItem.class);
        ShiroWebModule.extend(binder).setFilter("mdc", SubjectMDCInitializer.class);
    }

    @Singleton
    @Provides
    ShiroWebPrincipalMDCItem providePrincipalMDCCleaner(PrincipalMDC principalMDC) {
        ShiroWebPrincipalMDCItem cleaner = new ShiroWebPrincipalMDCItem(principalMDC);
        return cleaner;
    }

    @Singleton
    @Provides
    SubjectMDCInitializer provideSubjectMDCInitializer(PrincipalMDC principalMDC) {
        return new SubjectMDCInitializer(principalMDC);
    }
}
