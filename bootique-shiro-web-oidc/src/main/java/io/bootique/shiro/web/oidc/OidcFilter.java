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
package io.bootique.shiro.web.oidc;

import io.bootique.shiro.jwt.ShiroJsonWebToken;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @since 4.0
 */
public class OidcFilter extends AuthenticatingFilter {

    private final Provider<JwtParser> tokenParser;
    private final OidpRouter oidpRouter;
    private final String tokenCookie;

    public OidcFilter(
            Provider<JwtParser> tokenParser,
            OidpRouter oidpRouter,
            String tokenCookie) {

        this.tokenParser = tokenParser;
        this.oidpRouter = oidpRouter;
        this.tokenCookie = tokenCookie;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = isLoginAttempt(request, response) ? executeLogin(request, response) : false;

        if (!loggedIn) {
            ((HttpServletResponse) response).sendRedirect(oidpRouter.oidpUrlReturningToCurrentRequest());
        }

        return loggedIn;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {

        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        String authz = cookies == null ? null : Arrays.stream(cookies)
                .filter(c -> c.getName().equals(tokenCookie))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        String token = authz != null && !authz.isEmpty() ? authz : "";
        Jwt<?, ?> jwt = tokenParser.get().parse(token);

        return new ShiroJsonWebToken(
                token,
                (String) jwt.getHeader().get("kid"),
                jwt.accept(Jws.CLAIMS).getPayload());
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        try {
            return super.executeLogin(request, response);
        } catch (JwtException | AuthenticationException e) {
            ((HttpServletResponse) response).sendRedirect(oidpRouter.oidpUrlReturningToCurrentRequest());
            return false;
        }
    }

    private boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        return cookies != null && Stream.of(cookies).anyMatch(c -> c.getName().equals(tokenCookie));
    }
}
