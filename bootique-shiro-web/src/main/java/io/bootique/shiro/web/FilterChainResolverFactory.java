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

@BQConfig("Configures Shiro servlet environment.")
public class FilterChainResolverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterChainResolverFactory.class);

    private Map<String, String> urls;

    @BQConfigProperty("A map of Shiro filter chains names against chain definitions. Keys must correspond to" +
            " Shiro filter names mapped via DI.")
    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    public FilterChainResolver createFilterChainResolver(@ShiroFilterBinding Map<String, Filter> shiroFilters) {

        DefaultFilterChainManager chainManager = new DefaultFilterChainManager();

        // load filters
        shiroFilters.forEach((name, filter) -> chainManager.addFilter(name, filter));

        // init chains based on the "[urls]" section of the .ini
        urls.forEach((name, value) -> {
            LOGGER.info("Loading url chain {} -> {}", name, value);
            chainManager.createChain(name, value);
        });

        PathMatchingFilterChainResolver resolver = new PathMatchingFilterChainResolver();
        resolver.setFilterChainManager(chainManager);
        return resolver;
    }

}
