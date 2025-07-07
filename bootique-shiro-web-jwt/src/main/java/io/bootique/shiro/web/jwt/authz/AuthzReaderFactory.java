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
package io.bootique.shiro.web.jwt.authz;

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
