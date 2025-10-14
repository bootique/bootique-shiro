package io.bootique.shiro.web.oidconnect;

public interface OidConnect {

    String RESPONSE_TYPE_PARAM = "response_type";
    String CLIENT_ID_PARAM = "client_id";
    String REDIRECT_URI_PARAM = "redirect_uri";
    String STATE_PARAM = "state";
    String ORIGINAL_URI_PARAM = "original_uri";
    String CODE_PARAM = "code";
    String CLIENT_SECRET_KEY_PARAM = "client_secret";
    String GRANT_TYPE_PARAM = "grant_type";
    String GRANT_TYPE_AUTH_CODE = "authorization_code";
    String SCOPE_PARAM = "scope";
    String ACCESS_TOKEN_PARAM = "access_token";


    String ERROR_PROPERTY = "error";
    String INVALID_GRANT_ERROR_CODE = "invalid_grant";

    String LOCATION_HEADER_NAME = "Location";
}
