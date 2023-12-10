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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.jetty.MappedFilter;
import jakarta.servlet.Filter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@BQConfig("Configures Shiro in a servlet environment.")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = MappedShiroFilterFactory.class)
public class MappedShiroFilterFactory implements PolymorphicConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappedShiroFilterFactory.class);

    protected final WebSecurityManager securityManager;
    protected final Map<String, Filter> chainFilters;

    private Map<String, String> urls;
    private Set<String> urlPatterns;

    @Inject
    public MappedShiroFilterFactory(
            WebSecurityManager securityManager,
            @ShiroFilterBinding Map<String, Filter> chainFilters) {
        this.securityManager = securityManager;
        this.chainFilters = chainFilters;
    }

    @BQConfigProperty("A map of URL patterns to filter chain definitions. Names in the definitions must correspond"
            + " to the Shiro filter names mapped via DI. Corresponds to the [url] section in a traditional Shiro config."
            + " Filter chains are executed inside ShiroFilter mapped using 'urlPatterns' property.")
    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    @BQConfigProperty("Servlet spec-compatible URL patterns for resources covered by the Shiro filter. Default is '/*'.")
    public void setUrlPatterns(Set<String> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }

    // passing injector for the sake of subclasses
    public MappedFilter<ShiroFilter> create() {
        FilterChainResolver chainResolver = createChainResolver();
        ShiroFilter shiroFilter = createShiroFilter(chainResolver);
        return createMappedShiroFilter(shiroFilter);
    }

    protected MappedFilter<ShiroFilter> createMappedShiroFilter(ShiroFilter shiroFilter) {
        Set<String> urlPatterns = this.urlPatterns != null ? this.urlPatterns : Collections.singleton("/*");
        return new MappedFilter<>(shiroFilter, urlPatterns, "bootiqueShiro", 0);
    }

    protected ShiroFilter createShiroFilter(FilterChainResolver chainResolver) {
        return new ShiroFilter(securityManager, chainResolver);
    }

    protected FilterChainResolver createChainResolver() {
        DefaultFilterChainManager chainManager = new DefaultFilterChainManager();

        // load filters
        chainFilters.forEach((name, filter) -> chainManager.addFilter(name, filter));

        if (urls != null) {
            urls.forEach((url, value) -> {
                LOGGER.info("Loading url chain {} -> {}", url, value);
                chainManager.createChain(url, value);
            });
        }

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.setFilterChainManager(chainManager);
        return resolver;
    }

}
