package io.bootique.shiro.web.jwt.authz;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;

import java.util.List;

/**
 * @since 4.0
 */
@BQConfig("Configuration of JWT Token Claim")
@JsonTypeName("jsonList")
public class JsonListAuthzReaderFactory extends AuthzReaderFactory {

    @Override
    public AuthzReader createReader() {
        return new NamedClaimReader(getClaim(), o -> o != null ? (List<String>) o : List.of());
    }
}
