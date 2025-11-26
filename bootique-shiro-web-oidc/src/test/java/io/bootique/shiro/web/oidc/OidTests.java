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

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientProperties;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

class OidTests {

    public static Client clientNoRedirects() {
        return ClientBuilder.newBuilder()
                .property(ClientProperties.FOLLOW_REDIRECTS, false)
                .build();
    }

    static String jwt(Map<String, ?> claims) {
        PrivateKey privateKey = privateKey();

        JwtBuilder builder = Jwts.builder()
                .header().add("kid", "xGpTsw0DJs0vbe5CEcKMl5oZc7nKzAC9sF7kx1nQu1I")
                .and()
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256);

        return builder.compact();
    }

    private static PrivateKey privateKey() {
        try (InputStream keyIn = OidTests.class.getResourceAsStream("jwks-private-key.pem")) {
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
