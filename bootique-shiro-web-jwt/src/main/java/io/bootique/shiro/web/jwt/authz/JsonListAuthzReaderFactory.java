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

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;

import java.util.List;

/**
 * @since 4.0
 */
@BQConfig("""
        JWT authorization claim parser expecting authorization information (e.g., roles) to be present as a JSON list \
        in the token""")
@JsonTypeName("jsonList")
public class JsonListAuthzReaderFactory extends AuthzReaderFactory {

    @Override
    public AuthzReader createReader() {
        return new NamedClaimReader(getClaim(), o -> o != null ? (List<String>) o : List.of());
    }
}
