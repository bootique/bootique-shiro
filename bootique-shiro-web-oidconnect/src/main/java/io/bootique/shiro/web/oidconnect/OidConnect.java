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
package io.bootique.shiro.web.oidconnect;

/**
 * @since 4.0
 */
public interface OidConnect {

    String RESPONSE_TYPE_PARAM = "response_type";
    String CLIENT_ID_PARAM = "client_id";
    String CLIENT_SECRET_KEY_PARAM = "client_secret";
    String REDIRECT_URI_PARAM = "redirect_uri";
    String CODE_PARAM = "code";

    /**
     * The URI of the application request that originated the OID sequence. Not a part of the OAuth standard, it is
     * appended to the "redirect_uri" when OID authentication flow is initially triggered. It is passed through all the
     * redirects, and is finally used by the {@link AuthorizationCodeHandlerApi} to send the user to where they started from.
     */
    String START_URI_PARAM = "start_uri";
}
