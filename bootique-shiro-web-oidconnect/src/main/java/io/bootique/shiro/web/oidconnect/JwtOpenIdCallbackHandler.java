package io.bootique.shiro.web.oidconnect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.*;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Path("/bq-shiro-oauth-callback")
public class JwtOpenIdCallbackHandler implements OidConnect {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtOpenIdCallbackHandler.class);

    private final ObjectMapper mapper;

    private final JerseyClient webClient;
    private final WebTarget tokenTarget;
    private final String tokenCookie;
    private final String clientId;
    private final String clientSecretKey;
    private final String oidpUrl;
    private final String callbackUri;
    private final String scope;

    public JwtOpenIdCallbackHandler(ObjectMapper objectMapper,
                                    String tokenCookie,
                                    String tokenUrl,
                                    String clientId,
                                    String clientSecretKey,
                                    String scope,
                                    String oidpUrl,
                                    String callbackUri) {
        this.mapper = objectMapper;
        this.tokenCookie = tokenCookie;
        this.clientId = clientId;
        this.clientSecretKey = URLEncoder.encode(clientSecretKey, StandardCharsets.UTF_8);
        this.scope = scope;
        this.webClient = JerseyClientBuilder.createClient();
        this.tokenTarget = webClient.target(tokenUrl);
        this.oidpUrl = oidpUrl;
        this.callbackUri = callbackUri;
    }

    private Form form(String code) {
        Form form = new Form()
                .param(GRANT_TYPE_PARAMETER_NAME, GRANT_TYPE_AUTH_CODE_VALUE)
                .param(CLIENT_ID_PARAMETER_NAME, clientId)
                .param(CLIENT_SECRET_KEY_PARAMETER_NAME, clientSecretKey)
                .param(CODE_PARAMETER_NAME, code);
        if (scope != null && !scope.isEmpty()) {
            form = form.param(SCOPE_PARMETER_NAME, scope);
        }
        return form;
    }

    @GET
    public Response callback(@Context UriInfo uriInfo) {
        // 1. Read query parameters
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        String code = params.getFirst(CODE_PARAMETER_NAME);
        String originalUri = params.getFirst(ORIGINAL_URI_PARAMETER_NAME);
        String state = params.getFirst(STATE_PARAMETER_NAME);
        // 2. Validate parameters
        ErrorHandler errorHandler = validateRequiredParameters(code, state);
        if (errorHandler.hasError()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(errorHandler.get()).build();
        }
        // 3. Exchange auth code to token on JWT server
        Response tokenResponse = exchange(code);
        try {
            if (tokenResponse.getStatus() == HttpStatus.OK_200) {
                JsonNode json = mapper.readTree(tokenResponse.readEntity(String.class));
                // 4. Push token to cookie
                String token = json.get(ACCESS_TOKEN_PARAMETER_NAME).asText();
                // 5. Redirect to "redirectUrl" if defined
                if (originalUri != null && !originalUri.isEmpty()) {
                    WebTarget redirectTarget = prepareOriginalTarget(uriInfo.getBaseUri(), originalUri);
                    return redirectTarget.request().cookie(tokenCookie, token).get();
                } else {
                    return Response.ok()
                            .cookie(new NewCookie.Builder(tokenCookie).value(token).build())
                            .build();
                }
            } else {
                JsonNode json = mapper.readTree(tokenResponse.readEntity(String.class));
                JsonNode error = json.get(ERROR_PARAMETER_NAME);
                if (error != null && error.isTextual()) {
                    String errorCode = error.asText();
                    if (INVALID_GRANT_ERROR_CODE.equals(errorCode)) {
                        String oidpUrl = this.oidpUrl + "?" + OidConnectUtils.getOidpParametersString(uriInfo.getBaseUri().toString(), originalUri, clientId, callbackUri, true);
                        LOGGER.warn("Auth server returns error code " + INVALID_GRANT_ERROR_CODE + ". Redirection to oidp URL " + oidpUrl);
                        return Response.status(Response.Status.FOUND).header(LOCATION_HEADER_NAME, oidpUrl).build();
                    } else {
                        LOGGER.warn("Auth server returns error code " + errorCode + ". Unauthorized");
                        return Response.status(Response.Status.UNAUTHORIZED.getStatusCode(), "Auth server error: " + errorCode).build();
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("Some internal error is happened", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Some internal error is happened").build();
        }
        return tokenResponse;
    }

    private WebTarget prepareOriginalTarget(URI baseUri, String encodedOriginalUri) {
        // 1. Decode original uri
        URI originalUri = URI.create(Base64Coder.decodeString(URLDecoder.decode(encodedOriginalUri, StandardCharsets.UTF_8)));
        // 2. Parse path
        WebTarget redirectTarget =
                webClient.target(baseUri).path(originalUri.getPath());
        // 3. Parse params
        String query = originalUri.getQuery();
        if (query != null && !query.isEmpty()) {
            String[] params = query.split("&");
            for (String p : params) {
                String[] pair = p.split("=");
                redirectTarget = redirectTarget.queryParam(pair[0], pair[1]);
            }
        }
        return redirectTarget;
    }

    private ErrorHandler validateRequiredParameters(String code, String state) {
        ErrorHandler errorHandler = new ErrorHandler();
        boolean hasCode = code != null && !code.isEmpty();
        boolean hasState = state != null && !state.isEmpty();
        if (!hasCode && !hasState) {
            errorHandler.append("Parameters \"code\" and \"state\" are required");
        } else if (!hasCode) {
            errorHandler.append("Parameter \"code\" is required");
        } else if (!hasState) {
            errorHandler.append("Parameter \"state\" is required");
        }
        return errorHandler;
    }

    private Response exchange(String code) {
        Entity<Form> postForm = Entity.form(form(code));
        return tokenTarget
                .request()
                .post(postForm);
    }

    private static class ErrorHandler {

        private String error;

        void append(String error) {
            if (this.error == null || this.error.isEmpty()) {
                this.error = error;
            } else {
                this.error += "\n" + error;
            }
        }

        String get() {
            return this.error;
        }

        boolean hasError() {
            return this.error != null && !this.error.isEmpty();
        }
    }
}
