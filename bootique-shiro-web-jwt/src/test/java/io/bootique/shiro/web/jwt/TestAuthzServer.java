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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: a duplicate of TestAuthzServer from shiro-jwt.
//  Seems cleaner to keep a per-module dupe than using a "test-jar" ?
public class TestAuthzServer {

    private final String keyId;
    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final Path jwksPath;

    public TestAuthzServer(Path jwksPath) {
        this.keyId = "test_jwk_" + UUID.randomUUID().toString().replace("-", "");
        KeyPair keyPair = generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.jwksPath = jwksPath;
        createJwksFile();
    }

    public String jwksLocation() {
        return "file:" + jwksPath.toAbsolutePath();
    }

    public String token(Map<String, ?> claims, List<String> audience, LocalDateTime expiresAt) {
        JwtBuilder builder = Jwts.builder()
                .header().add("kid", keyId)
                .and()
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256);

        if (expiresAt != null) {
            builder.expiration(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        if (audience != null && !audience.isEmpty()) {
            builder.audience().add(audience);
        }

        return builder.compact();
    }

    public String getKeyId() {
        return keyId;
    }

    private void createJwksFile() {
        try {
            String jwks = generateJwks();
            Files.writeString(jwksPath, jwks);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create JWKS file", e);
        }
    }

    private String generateJwks() {
        try {
            // Extract RSA public key components
            BigInteger modulus = publicKey.getModulus();
            BigInteger exponent = publicKey.getPublicExponent();

            // Base64 URL encode without padding
            String n = base64UrlEncode(modulus.toByteArray());
            String e = base64UrlEncode(exponent.toByteArray());

            // Create JWKS structure
            Map<String, Object> jwk = Map.of(
                    "alg", "RS256",
                    "e", e,
                    "kid", keyId,
                    "kty", "RSA",
                    "n", n,
                    "use", "sig"
            );

            Map<String, Object> jwks = Map.of("keys", List.of(jwk));

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jwks);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWKS", e);
        }
    }

    private static String base64UrlEncode(byte[] data) {
        // Remove leading zero bytes that Java adds for positive BigInteger values
        int start = 0;
        while (start < data.length - 1 && data[start] == 0) {
            start++;
        }

        byte[] trimmed = new byte[data.length - start];
        System.arraycopy(data, start, trimmed, 0, trimmed.length);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(trimmed);
    }

    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
}
