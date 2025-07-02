package io.bootique.shiro.web.jwt.keys;

import io.bootique.value.Duration;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class JwksProvider {

    private final Map<Object, Jwk<?>> jwks;

    private final URL target;

    private final Duration expiresIn;

    private LocalDateTime loadTime;

    JwksProvider(URL target, Duration expiresIn) {
        this.target = target;
        this.expiresIn = expiresIn;
        this.jwks = new HashMap<>();
    }

    private synchronized void refresh() throws IOException {
        try (InputStream is = this.target.openStream()) {
            this.jwks.clear();
            Jwks.setParser().build().parse(is).getKeys().forEach(k -> jwks.put(k.getId(), k));
            loadTime = LocalDateTime.now();
        } catch (Exception e) {
            throw new IOException("Unable to load jwks", e);
        }
    }

    public Map<Object, Jwk<?>> getJwks() throws IOException {
        if (jwks.isEmpty() || (expiresIn != null && loadTime.plusSeconds(expiresIn.getDuration().toSeconds()).isAfter(LocalDateTime.now()))) {
            refresh();
        }
        return this.jwks;
    }
}
