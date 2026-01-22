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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

class JwtTests {

    public static final String KEY_ID = "test_jwk1";

    public static String jwksLocation() {
        return "classpath:io/bootique/shiro/jwt/jwks1.json";
    }

    public static ShiroJsonWebToken token(Map<String, ?> claims, List<String> audience, LocalDateTime expiresAt) {
        PrivateKey privateKey = privateKey();

        JwtBuilder builder = Jwts.builder()
                .header().add("kid", KEY_ID)
                .and()
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256);

        if (expiresAt != null) {
            builder.expiration(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (audience != null && !audience.isEmpty()) {
            builder.audience().add(audience);
        }

        return new ShiroJsonWebToken(builder.compact());
    }

    private static PrivateKey privateKey() {
        try (InputStream keyIn = JwtTests.class.getResourceAsStream("jwks-private-key.pem")) {
            String privateKeyPEM = new String(keyIn.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
