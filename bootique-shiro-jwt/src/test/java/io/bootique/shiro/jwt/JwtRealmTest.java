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

import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import org.junit.jupiter.api.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtRealmTest {

    @Test
    public void keysAreEqual_SameKey() {
        KeyPair keyPair = generateKeyPair();
        Jwk<?> jwk1 = Jwks.builder().key(keyPair.getPublic()).id("test-key").build();
        Jwk<?> jwk2 = Jwks.builder().key(keyPair.getPublic()).id("test-key").build();

        assertTrue(JwtRealm.keysAreEqual(jwk1, jwk2));
    }

    @Test
    public void keysAreEqual_DifferentKeys() {
        KeyPair keyPair1 = generateKeyPair();
        KeyPair keyPair2 = generateKeyPair();

        Jwk<?> jwk1 = Jwks.builder().key(keyPair1.getPublic()).id("test-key").build();
        Jwk<?> jwk2 = Jwks.builder().key(keyPair2.getPublic()).id("test-key").build();

        assertFalse(JwtRealm.keysAreEqual(jwk1, jwk2));
    }

    @Test
    public void keysAreEqual_EqualKeysButDifferentInstances() throws Exception {
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey1 = keyPair.getPublic();

        // Create a separate PublicKey instance from the same encoded bytes
        byte[] encoded = publicKey1.getEncoded();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey2 = keyFactory.generatePublic(new X509EncodedKeySpec(encoded));

        Jwk<?> jwk1 = Jwks.builder().key(publicKey1).id("test-key").build();
        Jwk<?> jwk2 = Jwks.builder().key(publicKey2).id("test-key").build();

        assertTrue(JwtRealm.keysAreEqual(jwk1, jwk2));
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
}
