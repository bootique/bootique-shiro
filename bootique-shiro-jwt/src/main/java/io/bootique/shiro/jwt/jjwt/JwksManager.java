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
package io.bootique.shiro.jwt.jjwt;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 4.0
 */
class JwksManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwksManager.class);

    private final URL location;
    private final Duration expiresIn;

    private volatile Map<Object, Jwk<?>> jwks;
    private volatile LocalDateTime loadTime;

    public JwksManager(URL location, Duration expiresIn) {
        this.location = location;
        this.expiresIn = expiresIn;
    }

    public Key readKey(Header header) {
        // must call "getKeys()" every time we process a header. This way the manager can refresh keys as defined
        // by the expiration policy
        Jwk<?> jwk = getKeys().get(header.getOrDefault("kid", ""));
        return jwk != null ? jwk.toKey() : null;
    }

    private Map<Object, Jwk<?>> getKeys() {
        if (needRefresh()) {
            synchronized (this) {
                if (needRefresh()) {
                    this.jwks = loadKeys();
                    this.loadTime = LocalDateTime.now();
                }
            }
        }

        return this.jwks;
    }

    private boolean needRefresh() {
        return jwks == null || LocalDateTime.now().isAfter(loadTime.plus(expiresIn));
    }

    private Map<Object, Jwk<?>> loadKeys() {
        try (InputStream is = this.location.openStream()) {

            Map<Object, Jwk<?>> jwks = new HashMap<>();

            Jwks.setParser().build().parse(is).getKeys().forEach(k -> jwks.put(k.getId(), k));
            LOGGER.info("JWKS (re)loaded from {}", location);

            return jwks;
        } catch (Exception e) {
            throw new IOException("Unable to load JWKS", e);
        }
    }
}
