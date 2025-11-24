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
package io.bootique.shiro.web.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import jakarta.inject.Provider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;

import java.util.Set;

/**
 * Authenticates request based on a Bearer JWT authorization header. Doesn't check any roles or permissions itself,
 * instead parsing and validating the token, and passing it down to the downstream realms.
 *
 * @since 4.0
 */
public class JwtBearerAuthenticationFilter extends BearerHttpAuthenticationFilter {

    private final Provider<JwtParser> tokenParser;
    private final String audience;

    public JwtBearerAuthenticationFilter(Provider<JwtParser> tokenParser, String audience) {
        this.tokenParser = tokenParser;
        this.audience = audience;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        BearerToken bearer = (BearerToken) super.createToken(servletRequest, servletResponse);
        Claims claims = tokenParser.get().parse(bearer.getToken()).accept(Jws.CLAIMS).getPayload();

        validateAudience(claims.getAudience());

        return new JwtBearerToken(
                bearer.getToken(),
                bearer.getHost(),
                claims);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        try {
            return super.executeLogin(request, response);
        } catch (JwtException | AuthenticationException e) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return false;
        }
    }

    private void validateAudience(Set<String> audienceJwtClaim) {
        if (this.audience != null && !this.audience.isEmpty()) {
            if (audienceJwtClaim == null || !audienceJwtClaim.contains(this.audience)) {
                throw new AuthenticationException("Invalid audience");
            }
        }
    }
}
