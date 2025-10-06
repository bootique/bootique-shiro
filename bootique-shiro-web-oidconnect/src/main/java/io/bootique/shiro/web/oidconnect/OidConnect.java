package io.bootique.shiro.web.oidconnect;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public interface OidConnect {

    String RESPONSE_TYPE_PARAMETER_NAME = "response_type";
    String CLIENT_ID_PARAMETER_NAME = "client_id";
    String REDIRECT_URI_PARAMETER_NAME = "redirect_uri";
    String STATE_PARAMETER_NAME = "state";
    String ORIGINAL_URI_PARAMETER_NAME = "original_uri";
    String CODE_PARAMETER_NAME = "code";
    String CLIENT_SECRET_KEY_PARAMETER_NAME = "client_secret";
    String GRANT_TYPE_PARAMETER_NAME = "grant_type";
    String GRANT_TYPE_AUTH_CODE_VALUE = "authorization_code";
    String SCOPE_PARMETER_NAME = "scope";
    String ACCESS_TOKEN_PARAMETER_NAME = "access_token";
    String ERROR_PARAMETER_NAME = "error";
    String INVALID_GRANT_ERROR_CODE = "invalid_grant";
    String LOCATION_HEADER_NAME = "Location";
}
