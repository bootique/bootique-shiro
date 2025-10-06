package io.bootique.shiro.web.oidconnect;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class OidConnectBaseTest {

    public static String token(Map<String, ?> rolesClaim) throws Exception {
        String key = Files.readString(Paths.get(ClassLoader.getSystemResource("io/bootique/shiro/web/oidconnect/jwks-private-key.pem").toURI()));
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        JwtBuilder builder = Jwts.builder()
                .header().add("kid", "xGpTsw0DJs0vbe5CEcKMl5oZc7nKzAC9sF7kx1nQu1I")
                .and()
                .claims(rolesClaim).signWith(SignatureAlgorithm.RS256, privateKey);
        return builder.compact();
    }
}
