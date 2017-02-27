package io.bootique.shiro.web;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

/**
 * Servlet filter that should be installed in the web container to intercept web requests and apply Shiro chains.
 */
public class ShiroFilter extends AbstractShiroFilter {

    public ShiroFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
        setSecurityManager(securityManager);
        setFilterChainResolver(filterChainResolver);
    }
}
