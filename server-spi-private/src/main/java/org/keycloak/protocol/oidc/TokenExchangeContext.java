/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.protocol.oidc;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;

/**
 * @author <a href="mailto:dmitryt@backbase.com">Dmitry Telegin</a>
 *
 * Token exchange context
 *
 * @author Ben Cresitello-Dittmar
 *
 * Updated to use AbstractOIDCRequestContext abstract class
 */
public class TokenExchangeContext extends AbstractOIDCRequestContext {
    public TokenExchangeContext(KeycloakSession session,
                                 MultivaluedMap<String, String> formParams,
                                 Object cors,
                                 RealmModel realm,
                                 EventBuilder event,
                                 ClientModel client,
                                 ClientConnection clientConnection,
                                 HttpHeaders headers,
                                 Object tokenManager,
                                 Map<String, String> clientAuthAttributes) {
        super(session, formParams, cors, realm, event, client, clientConnection, headers, tokenManager, clientAuthAttributes);
    }

    private final Params params = new Params();

    public Params getParams() {
        return params;
    }

    public class Params {

        public String getActorToken() {
            return formParams.getFirst(OAuth2Constants.ACTOR_TOKEN);
        }

        public String getActorTokenType() {
            return formParams.getFirst(OAuth2Constants.ACTOR_TOKEN_TYPE);
        }

        public String getAudience() {
            return formParams.getFirst(OAuth2Constants.AUDIENCE);
        }

        public String getResource() {
            return formParams.getFirst(OAuth2Constants.RESOURCE);
        }

        public String getRequestedTokenType() {
            return formParams.getFirst(OAuth2Constants.REQUESTED_TOKEN_TYPE);
        }

        public String getScope() {
            return formParams.getFirst(OAuth2Constants.SCOPE);
        }

        public String getSubjectToken() {
            return formParams.getFirst(OAuth2Constants.SUBJECT_TOKEN);
        }

        public String getSubjectTokenType() {
            return formParams.getFirst(OAuth2Constants.SUBJECT_TOKEN_TYPE);
        }

        public String getSubjectIssuer() {
            return formParams.getFirst(OAuth2Constants.SUBJECT_ISSUER);
        }

    }
}
