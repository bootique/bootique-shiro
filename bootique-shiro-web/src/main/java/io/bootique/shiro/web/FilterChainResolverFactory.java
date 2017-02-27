package io.bootique.shiro.web;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import java.util.Map;

@BQConfig("Configures Shiro in a servlet environment.")
public class FilterChainResolverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterChainResolverFactory.class);

    private Map<String, String> urls;

    @BQConfigProperty("A map of URL patterns to Shiro filter chain definitions. Names in the definitions must correspond"
            + " to the Shiro filter names mapped via DI. Corresponds to the [url] section in a traditional Shiro config.")
    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    public FilterChainResolver createFilterChainResolver(Map<String, Filter> shiroFilters) {

        DefaultFilterChainManager chainManager = new DefaultFilterChainManager();

        // load filters
        shiroFilters.forEach((name, filter) -> chainManager.addFilter(name, filter));

        urls.forEach((url, value) -> {
            LOGGER.info("Loading url chain {} -> {}", url, value);
            chainManager.createChain(url, value);
        });

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.setFilterChainManager(chainManager);
        return resolver;
    }

}
