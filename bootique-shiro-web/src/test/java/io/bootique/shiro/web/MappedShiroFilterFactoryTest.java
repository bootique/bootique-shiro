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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class MappedShiroFilterFactoryTest {

    private TestFilter filter1;
    private TestFilter filter2;
    private FilterChainResolver resolver;

    @BeforeEach
    public void beforeEach() {
        this.filter1 = new TestFilter();
        this.filter2 = new TestFilter();

        Map<String, String> urls = new HashMap<>();
        urls.put("/p1/*", "f1, anon");
        urls.put("/p2/*", "f2, roles[r1]");
        urls.put("/p3/*", "anon");

        Map<String, Filter> filters = new HashMap<>();
        filters.put("f1", filter1);
        filters.put("f2", filter2);

        MappedShiroFilterFactory factory = new MappedShiroFilterFactory(new DefaultWebSecurityManager(), filters);
        factory.setUrls(urls);

        this.resolver = factory.createChainResolver();
        assertNotNull(resolver);
    }

    protected void doFilter(HttpServletRequest request) throws IOException, ServletException {
        FilterChain chain = (rq, rs) -> {
        };
        this.resolver.getChain(request, null, chain).doFilter(request, null);
    }

    @Test
    public void createFilterChainResolver1() throws IOException, ServletException {

        HttpServletRequest request = new TestRequest("/", "/p1", "/a", "/p1/a");

        doFilter(request);

        assertEquals(1, filter1.count);
        assertEquals(0, filter2.count);
    }

    @Test
    public void createFilterChainResolver2() throws IOException, ServletException {

        HttpServletRequest request = new TestRequest("/", "/p2", "/b", "/p2/b");

        doFilter(request);
        assertEquals(1, filter2.count);
        assertEquals(0, filter1.count);
    }

    @Test
    public void createFilterChainResolver_DefaultResolvers() throws IOException, ServletException {

        HttpServletRequest request = new TestRequest("/", "/p3", "/b", "/p3/b");

        doFilter(request);

        assertEquals(0, filter1.count);
        assertEquals(0, filter2.count);
    }

    static class TestFilter implements Filter {

        int count;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            count++;
        }
    }

    static class TestRequest implements  HttpServletRequest {
        private final String contextPath;
        private final String servletPath;
        private final String pathInfo;
        private final String requestURI;
        private final Map<String, Object> attributes;

        public TestRequest(String contextPath, String servletPath, String pathInfo, String requestURI) {
            this.contextPath = contextPath;
            this.servletPath = servletPath;
            this.pathInfo = pathInfo;
            this.requestURI = requestURI;
            this.attributes = new HashMap<>();
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }

        @Override
        public String getPathInfo() {
            return pathInfo;
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object o) {
            attributes.put(name, o);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public boolean authenticate(HttpServletResponse response) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Cookie[] getCookies() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getDateHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getIntHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getQueryString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isUserInRole(String role) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpSession getSession(boolean create) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String changeSessionId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void login(String username, String password) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void logout() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Part> getParts() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Part getPart(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCharacterEncoding(String env) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getContentLengthLong() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletInputStream getInputStream() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getParameter(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getParameterValues(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BufferedReader getReader() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRemoteAddr() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncContext startAsync() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRequestId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getProtocolRequestId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletConnection getServletConnection() {
            throw new UnsupportedOperationException();
        }
    }
}
