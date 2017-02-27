package io.bootique.shiro.web;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class MappedShiroFilterFactoryTes {

    private Filter mockFilter1;
    private Filter mockFilter2;
    private FilterChainResolver resolver;

    @Before
    public void before() {
        this.mockFilter1 = mock(Filter.class);
        this.mockFilter2 = mock(Filter.class);

        Map<String, String> urls = new HashMap<>();
        urls.put("/p1/*", "f1, anon");
        urls.put("/p2/*", "f2, roles[r1]");
        urls.put("/p3/*", "anon");

        Map<String, Filter> filters = new HashMap<>();
        filters.put("f1", mockFilter1);
        filters.put("f2", mockFilter2);

        MappedShiroFilterFactory factory = new MappedShiroFilterFactory();
        factory.setUrls(urls);

        this.resolver = factory.createChainResolver(filters);
        assertNotNull(resolver);
    }

    protected void doFilter(HttpServletRequest request) throws IOException, ServletException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);
        this.resolver.getChain(request, mockResponse, mockChain).doFilter(request, mockResponse);
    }

    @Test
    public void testCreateFilterChainResolver1() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/");
        when(request.getRequestURI()).thenReturn("/p1/a");

        doFilter(request);
        verify(mockFilter1)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        verifyZeroInteractions(mockFilter2);
    }

    @Test
    public void testCreateFilterChainResolver2() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/");
        when(request.getRequestURI()).thenReturn("/p2/b");

        doFilter(request);
        verify(mockFilter2)
                .doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        verifyZeroInteractions(mockFilter1);
    }

    @Test
    public void testCreateFilterChainResolver_DefaultResolvers() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/");
        when(request.getRequestURI()).thenReturn("/p3/b");

        doFilter(request);

        verifyZeroInteractions(mockFilter1);
        verifyZeroInteractions(mockFilter2);
    }
}
