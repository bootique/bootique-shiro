package io.bootique.shiro.web.oidconnect;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.web.util.WebUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

class OidConnectUtils {

    static Map<String, Object> getOidpParametersMap(ServletRequest servletRequest, String clientId, String callbackUri) {
        return new HashMap<>() {
            {
                put(OidConnect.RESPONSE_TYPE_PARAMETER_NAME, OidConnect.CODE_PARAMETER_NAME);
                put(OidConnect.CLIENT_ID_PARAMETER_NAME, clientId);
                put(OidConnect.REDIRECT_URI_PARAMETER_NAME, getCallbackUri(servletRequest, callbackUri));
            }
        };
    }

    static String getCallbackUri(String baseUri, String originalUri, String callbackUri) {
        callbackUri = resolveCallbackUri(callbackUri);
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
        return baseUri + resolveCallbackUri(callbackUri) + "?" + OidConnect.ORIGINAL_URI_PARAMETER_NAME + "=" + URLEncoder.encode(
                new String(Base64.getEncoder().encode(originalUri.toString().getBytes())), StandardCharsets.UTF_8);
    }

    private static String resolveCallbackUri(String callbackUri) {
        return callbackUri.startsWith("/") ? callbackUri : "/" + callbackUri;
    }
}
