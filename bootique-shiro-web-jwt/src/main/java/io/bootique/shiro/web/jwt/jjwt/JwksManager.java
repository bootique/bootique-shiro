package io.bootique.shiro.web.jwt.jjwt;

import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 4.0
 */
public class JwksManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwksManager.class);

    private final URL location;
    private final Duration expiresIn;

    private volatile Map<Object, Jwk<?>> jwks;
    private volatile LocalDateTime loadTime;

    public JwksManager(URL location, Duration expiresIn) {
        this.location = location;
        this.expiresIn = expiresIn;
    }

    public Map<Object, Jwk<?>> getJwks() {
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
