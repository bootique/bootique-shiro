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

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

/**
 * Servlet filter that should be installed in the web container to intercept web requests and apply Shiro chains.
 *
 * @deprecated in favor of the Jakarta flavor
 */
@Deprecated(since = "3.0", forRemoval = true)
public class ShiroFilter extends AbstractShiroFilter {

    public ShiroFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
        setSecurityManager(securityManager);
        setFilterChainResolver(filterChainResolver);
    }
}
