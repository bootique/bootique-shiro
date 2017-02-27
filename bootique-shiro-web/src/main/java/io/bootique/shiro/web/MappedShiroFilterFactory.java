package io.bootique.shiro.web;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.jetty.MappedFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@BQConfig("Configures Shiro in a servlet environment.")
public class MappedShiroFilterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappedShiroFilterFactory.class);

    private Map<String, String> urls;
    private Set<String> urlPatterns;

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

    public MappedFilter<ShiroFilter> createShiroFilter(WebSecurityManager securityManager, Map<String, Filter> chainFilters) {

        FilterChainResolver chainResolver = createChainResolver(chainFilters);
        ShiroFilter shiroFilter = createShiroFilter(securityManager, chainResolver);
        return createMappedShiroFilter(shiroFilter);
    }

    protected MappedFilter<ShiroFilter> createMappedShiroFilter(ShiroFilter shiroFilter) {
        Set<String> urlPatterns = this.urlPatterns != null ? this.urlPatterns : Collections.singleton("/*");
        return new MappedFilter<>(shiroFilter, urlPatterns, "bootiqueShiro", 0);
    }

    protected ShiroFilter createShiroFilter(WebSecurityManager securityManager, FilterChainResolver chainResolver) {
        return new ShiroFilter(securityManager, chainResolver);
    }

    protected FilterChainResolver createChainResolver(Map<String, Filter> chainFilters) {
        DefaultFilterChainManager chainManager = new DefaultFilterChainManager();

        // load filters
        chainFilters.forEach((name, filter) -> chainManager.addFilter(name, filter));

        urls.forEach((url, value) -> {
            LOGGER.info("Loading url chain {} -> {}", url, value);
            chainManager.createChain(url, value);
        });

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.setFilterChainManager(chainManager);
        return resolver;
    }

}
