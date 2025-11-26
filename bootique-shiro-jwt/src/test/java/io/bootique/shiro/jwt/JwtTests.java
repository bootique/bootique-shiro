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
package io.bootique.shiro.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

class JwtTests {

    static ShiroJsonWebToken token(Map<String, ?> claims, List<String> audience, LocalDateTime expiresAt) {
        return new ShiroJsonWebToken("", claims(claims, audience, expiresAt));
    }

    static Claims claims(Map<String, ?> claims, List<String> audience, LocalDateTime expiresAt) {

        ClaimsBuilder builder = Jwts.claims().add(claims);

        if (expiresAt != null) {
            builder.expiration(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (audience != null && !audience.isEmpty()) {
            builder.audience().add(audience);
        }

        return builder.build();
    }
}
