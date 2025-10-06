package io.bootique.shiro.web.oidconnect;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.apache.shiro.web.util.WebUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OidConnectUtils {

    static String getOidpParametersString(String baseUri, String originalUri, String clientId, String callbackUri, boolean invalidGrant) {
        StringBuilder params = new StringBuilder()
                .append(OidConnect.RESPONSE_TYPE_PARAMETER_NAME).append("=").append(OidConnect.CODE_PARAMETER_NAME)
                .append("&").append(OidConnect.CLIENT_ID_PARAMETER_NAME).append("=").append(clientId)
                .append("&").append(OidConnect.REDIRECT_URI_PARAMETER_NAME).append("=").append(getCallbackUri(baseUri, originalUri, callbackUri))
                .append("&").append(OidConnect.STATE_PARAMETER_NAME).append("=").append(getState(invalidGrant));
        return params.toString();
    }

    static Map<String, Object> getOidpParametersMap(String baseUri, String originalUri, String clientId, String callbackUri) {
        return new HashMap<>() {
            {
                put(OidConnect.RESPONSE_TYPE_PARAMETER_NAME, OidConnect.CODE_PARAMETER_NAME);
                put(OidConnect.CLIENT_ID_PARAMETER_NAME, clientId);
                put(OidConnect.REDIRECT_URI_PARAMETER_NAME, getCallbackUri(baseUri, originalUri, callbackUri));
                put(OidConnect.STATE_PARAMETER_NAME, getState(false));
            }
        };
    }

    private static String getState(boolean invalidGrant) {
        // TODO: How do we generate it
        if (invalidGrant) {
            return OidConnect.INVALID_GRANT_ERROR_CODE;
        }
        return Response.Status.OK.getReasonPhrase();
    }

    static Map<String, Object> getOidpParametersMap(ServletRequest servletRequest, String clientId, String callbackUri) {
        return new HashMap<>() {
            {
                put(OidConnect.RESPONSE_TYPE_PARAMETER_NAME, OidConnect.CODE_PARAMETER_NAME);
                put(OidConnect.CLIENT_ID_PARAMETER_NAME, clientId);
                put(OidConnect.REDIRECT_URI_PARAMETER_NAME, getCallbackUri(servletRequest, callbackUri));
                put(OidConnect.STATE_PARAMETER_NAME, getState(false));
            }
        };
    }

    static String getCallbackUri(String baseUri, String originalUri, String callbackUri) {
        if (originalUri != null && !originalUri.isEmpty()) {
            return baseUri + callbackUri + "?" + OidConnect.ORIGINAL_URI_PARAMETER_NAME + "=" + URLEncoder.encode(
                    new String(Base64.getEncoder().encode(originalUri.getBytes())), StandardCharsets.UTF_8);
        } else {
            return baseUri + callbackUri;
        }
    }

    static String getCallbackUri(ServletRequest request, String callbackUri) {
        HttpServletRequest servletRequest = WebUtils.toHttp(request);
        String baseUri = servletRequest.getRequestURL()
                .substring(0, servletRequest.getRequestURL().length() - servletRequest.getRequestURI().length())
                + servletRequest.getContextPath();
        StringBuilder originalUri = new StringBuilder();
        Enumeration<String> parameters = servletRequest.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            if (originalUri.isEmpty()) {
                originalUri.append(servletRequest.getRequestURI()).append("?");
            } else {
                originalUri.append("&");
            }
            originalUri.append(parameter).append("=").append(servletRequest.getParameter(parameter));
        }
        if (originalUri.isEmpty()) {
            originalUri.append(servletRequest.getRequestURI());
        }
        return baseUri + callbackUri + "?" + OidConnect.ORIGINAL_URI_PARAMETER_NAME + "=" + URLEncoder.encode(
                new String(Base64.getEncoder().encode(originalUri.toString().getBytes())), StandardCharsets.UTF_8);
    }
}
