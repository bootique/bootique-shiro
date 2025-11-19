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
package io.bootique.shiro.web.oidconnect;

import io.bootique.shiro.web.jwt.JwtBearerAuthenticationFilter;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

/**
 * @since 4.0
 */
public class OidConnectFilter extends JwtBearerAuthenticationFilter {

    private final OidpRouter oidpRouter;
    private final String tokenCookie;

    public OidConnectFilter(
            Provider<JwtParser> tokenParser,
            String audience,
            OidpRouter oidpRouter,
            String tokenCookie) {

        super(tokenParser, audience);
        this.oidpRouter = oidpRouter;
        this.tokenCookie = tokenCookie;
    }

    @Override
    protected String getAuthzHeader(ServletRequest request) {
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        return cookies == null ? null : Arrays.stream(cookies)
                .filter(c -> c.getName().equals(tokenCookie))
                .findFirst()

                // TODO: ugly... we take a token from the cookie and pretend it is a "Bearer" auth header. Wish Shiro
                //  inheritance wasn't as deep and limiting
                .map(c -> "Bearer " + c.getValue())
                .orElse(null);
    }

    @Override
    protected void redirectIfNoAuth(ServletRequest request, ServletResponse response, Exception e) throws Exception {
        ((HttpServletResponse) response).sendRedirect(oidpRouter.oidpUrlReturningToCurrentRequest());
    }

    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response) {
        try {
            ((HttpServletResponse) response).sendRedirect(oidpRouter.oidpUrlReturningToCurrentRequest());
            return false;
        } catch (Exception e) {
            return super.sendChallenge(request, response);
        }
    }
}
