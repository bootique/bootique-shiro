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
package io.bootique.shiro.web.jwt.jjwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

import java.net.URL;
import java.time.Duration;

/**
 * A helper class that creates JWT parser given JWKS location.
 *
 * @since 4.0
 */
public class JwtParserMaker {

    public static JwtParser createParser(URL jwkLocation, Duration jwkExpiresIn) {
        // manager is created once and will be reused by the lambda below to parse every token
        JwksManager jwksManager = new JwksManager(jwkLocation, jwkExpiresIn);
        
        return Jwts.parser()
                .keyLocator(jwksManager::readKey)
                .build();
    }
}
