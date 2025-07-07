package io.bootique.shiro.web.jwt.jjwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import java.net.URL;
import java.time.Duration;

/**
 * A helper class that creates JWT parser given JWKS location.
 *
 * @since 4.0
 */
public class JwtParserMaker {

    public static JwtParser createParser(URL jwkLocation, Duration jwkExpiresIn) {
        // manager is created once and will be reused by the lambda below to parse every token
        JwksManager jwksManager = new JwksManager(jwkLocation, jwkExpiresIn);
        
        return Jwts.parser()
                .keyLocator(jwksManager::readKey)
                .build();
    }
}
