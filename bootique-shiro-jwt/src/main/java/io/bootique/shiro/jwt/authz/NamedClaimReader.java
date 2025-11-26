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
package io.bootique.shiro.jwt.authz;

import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class NamedClaimReader implements AuthzReader {

    private final String claim;
    private final Function<Object, List<String>> parser;

    public NamedClaimReader(String claim, Function<Object, List<String>> parser) {
        this.claim = Objects.requireNonNull(claim);
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public List<String> readAuthz(Claims claims) {
        return parser.apply(claims.get(claim));
    }
}
