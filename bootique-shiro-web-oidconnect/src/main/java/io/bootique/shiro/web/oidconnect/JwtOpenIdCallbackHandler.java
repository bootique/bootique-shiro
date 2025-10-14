package io.bootique.shiro.web.oidconnect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("_this_is_a_jwt_endpoint_placeholder_that_will_be_replaced_dynamically_")
public class JwtOpenIdCallbackHandler {

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

    @GET
    public Response callback(@Context UriInfo uriInfo,
                             @QueryParam(OidConnect.CODE_PARAM) String code,
                             @QueryParam(OidConnect.ORIGINAL_URI_PARAM) String originalUri) {
        // 1. Validate code parameter
        if (code == null || code.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parameter \"" + OidConnect.CODE_PARAM + "\" is required").build();
        }
        // 2. Exchange auth code to token on JWT server
        Response tokenResponse = requestToken(code);
        try {
            if (tokenResponse.getStatus() == Response.Status.OK.getStatusCode()) {
                JsonNode json = mapper.readTree(tokenResponse.readEntity(String.class));
                // 4. Push token to cookie
                String token = json.get(OidConnect.ACCESS_TOKEN_PARAM).asText();
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
                JsonNode error = json.get(OidConnect.ERROR_PROPERTY);
                if (error != null && error.isTextual()) {
                    String errorCode = error.asText();
                    if (OidConnect.INVALID_GRANT_ERROR_CODE.equals(errorCode)) {
                        String oidpUrl = this.oidpUrl + "?" + getOidpParametersString(uriInfo.getBaseUri().toString(), originalUri, clientId, callbackUri);
                        LOGGER.warn("Auth server returns error code {}. Redirection to oidp URL {}", OidConnect.INVALID_GRANT_ERROR_CODE, oidpUrl);
                        return Response.status(Response.Status.FOUND).header(OidConnect.LOCATION_HEADER_NAME, oidpUrl).build();
                    } else {
                        LOGGER.warn("Auth server returns error code {}. Unauthorized", errorCode);
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

    private Response requestToken(String code) {

        // TODO: a more classic form of OAuth request would pass client id / secret as an Authorization header.
        //   Though a parameter flavor should also be ok with most IDPs

        Form form = new Form()
                .param(OidConnect.GRANT_TYPE_PARAM, OidConnect.GRANT_TYPE_AUTH_CODE)
                .param(OidConnect.CLIENT_ID_PARAM, clientId)
                .param(OidConnect.CLIENT_SECRET_KEY_PARAM, clientSecretKey)
                .param(OidConnect.CODE_PARAM, code);

        if (scope != null && !scope.isEmpty()) {
            form = form.param(OidConnect.SCOPE_PARAM, scope);
        }

        Entity<Form> postForm = Entity.form(form);
        return tokenTarget
                .request()
                .post(postForm);
    }

    static String getOidpParametersString(String baseUri, String originalUri, String clientId, String callbackUri) {
        StringBuilder redirectUri = new StringBuilder(baseUri);
        if (!callbackUri.startsWith("/")) {
            redirectUri.append("/");
        }
        redirectUri.append(callbackUri);

        if (originalUri != null && !originalUri.isEmpty()) {

            String originalBase64 = Base64.getEncoder().encodeToString(originalUri.getBytes());

            redirectUri.append("?")
                    .append(OidConnect.ORIGINAL_URI_PARAM)
                    .append("=")
                    .append(URLEncoder.encode(originalBase64, StandardCharsets.UTF_8));
        }

        return OidConnect.RESPONSE_TYPE_PARAM + "=" + OidConnect.CODE_PARAM +
                "&" + OidConnect.CLIENT_ID_PARAM + "=" + clientId +
                "&" + OidConnect.REDIRECT_URI_PARAM + "=" + redirectUri;
    }
}
