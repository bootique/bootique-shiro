package io.bootique.shiro.web.jwt.keys;

import io.bootique.value.Duration;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class JwksProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwksProvider.class);

    private final Map<Object, Jwk<?>> jwks;

    private final URL location;

    private final Duration expiresIn;

    private LocalDateTime loadTime;

    JwksProvider(URL location, Duration expiresIn) {
        this.location = location;
        this.expiresIn = expiresIn;
        this.jwks = new HashMap<>();
    }

    private synchronized void refresh() throws IOException {
        LOGGER.info("Loading jwks from " + location.toString() + "...");
        try (InputStream is = this.location.openStream()) {
            this.jwks.clear();
            Jwks.setParser().build().parse(is).getKeys().forEach(k -> jwks.put(k.getId(), k));
            loadTime = LocalDateTime.now();
        } catch (Exception e) {
            throw new IOException("Unable to load jwks", e);
        }
        LOGGER.info("Jwks has been loaded from " + location.toString() + " at " + loadTime);
    }

    public Map<Object, Jwk<?>> getJwks() throws IOException {
        if (jwks.isEmpty() || LocalDateTime.now().isAfter(LocalDateTime.from(loadTime).plusSeconds(expiresIn.getDuration().toSeconds()))) {
            refresh();
        }
        return this.jwks;
    }
}
