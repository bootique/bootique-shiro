package io.bootique.shiro.web;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

public class ShiroFilter extends AbstractShiroFilter {

    public ShiroFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
        setSecurityManager(securityManager);
        setFilterChainResolver(filterChainResolver);
    }
}
