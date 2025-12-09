/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.shiro.web.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * A callback endpoint that processes authorization code sent back by an IDP after a user login.
 *
 * @since 4.0
 */
@Path("_oauth_authorization_code_handler_url_placeholder_that_will_be_replaced_dynamically_")
public class AuthorizationCodeHandlerApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationCodeHandlerApi.class);

    private final ObjectMapper mapper;
    private final OidpRouter oidpRouter;
    private final WebTarget tokenTarget;
    private final String tokenCookie;
    private final String clientIdEnc;
    private final String clientSecretKeyEnc;
    private final String callbackUri;

    public AuthorizationCodeHandlerApi(
            ObjectMapper mapper,
            OidpRouter oidpRouter,
            String tokenCookie,
            String tokenUrl,
            String clientId,
            String clientSecretKey,
            String callbackUri) {

        this.mapper = mapper;
        this.oidpRouter = oidpRouter;
        this.tokenCookie = tokenCookie;
        this.clientIdEnc = clientId;
        this.clientSecretKeyEnc = clientSecretKey;
        this.callbackUri = callbackUri;

        // TODO: client must originate in Bootique
        this.tokenTarget = JerseyClientBuilder.createClient().target(tokenUrl);
    }

    @GET
    public Response onAuthCodeCallback(@Context UriInfo uriInfo,
            @QueryParam("code") String code,
            @QueryParam("state") String initialUrl) {

        if (code == null || code.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("'code' parameter is required").build();
        }

        // Exchange auth code to token on JWT server
        Response tokenResponse = requestToken(uriInfo, code);
        JsonNode tokenJson;

        try {
            tokenJson = mapper.readTree(tokenResponse.readEntity(String.class));
        } catch (Exception e) {
            LOGGER.error("Error parsing token response", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal error").build();
        }

        return tokenResponse.getStatus() == Response.Status.OK.getStatusCode()
                ? onTokenSuccess(tokenJson, initialUrl)
                : onTokenFailure(tokenResponse, tokenJson, initialUrl);
    }

    private Response onTokenSuccess(JsonNode tokenJson, String originalUri) {

        String token = tokenJson.get("access_token").asText();
        NewCookie cookie = new NewCookie.Builder(tokenCookie).value(token).build();

        return originalUri != null && !originalUri.isEmpty()
                ? Response.temporaryRedirect(decodeUri(originalUri)).cookie(cookie).build()
                : Response.ok().cookie(cookie).build();
    }

    private Response onTokenFailure(Response tokenResponse, JsonNode tokenJson, String initialUrl) {
        JsonNode error = tokenJson.get("error");
        if (error != null) {
            String errorCode = error.asText();
            if ("invalid_grant".equals(errorCode)) {
                String oidpUrl = oidpRouter.oidpUrlReturningToUrl(initialUrl);
                LOGGER.warn("Auth server returned 'invalid_grant'. Redirecting back to OIDP at {}", oidpUrl);
                return Response.status(Response.Status.FOUND).header("Location", oidpUrl).build();
            } else {
                LOGGER.warn("Auth server returned '{}'. Unauthorized", errorCode);
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode(), "Auth server error: " + errorCode).build();
            }
        }

        return tokenResponse;
    }

    private static URI decodeUri(String encodedUri) {
        return URI.create(URLDecoder.decode(encodedUri, StandardCharsets.UTF_8));
    }

    private Response requestToken(UriInfo uriInfo, String code) {

        // TODO: a more classic form of OAuth request would pass client id / secret as an Authorization header.
        //   Though a parameter flavor should also be ok with most IDPs

        String redirectUri = uriInfo.getBaseUriBuilder().path(this.callbackUri).build().toString();
        Form form = new Form()
                .param("grant_type", "authorization_code")
                .param("client_id", clientIdEnc)
                .param("client_secret", clientSecretKeyEnc)
                .param("code", code)
                .param("redirect_uri", redirectUri);

        Entity<Form> postForm = Entity.form(form);
        return tokenTarget
                .request()
                .post(postForm);
    }
}
