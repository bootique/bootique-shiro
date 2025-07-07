package io.bootique.shiro.web.jwt.realm;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;

/**
 * @since 4.0
 */
@BQConfig("JWT Token Claim parser")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = JsonListAuthzReaderFactory.class)
public abstract class AuthzReaderFactory implements PolymorphicConfiguration {

    private String claim;

    @BQConfigProperty("JWT claim name. If omitted, 'roles' is used as the default")
    public void setClaim(String claim) {
        this.claim = claim;
    }

    public abstract AuthzReader createReader();

    protected String getClaim() {
        return claim != null ? claim : "roles";
    }
}
