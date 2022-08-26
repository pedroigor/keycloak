/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.protocol.oidc;

import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;

/**
 * @author Ben Cresitello-Dittmar
 *
 * This abstract class defines the context in which an OIDC authentication flow takes place. This code was originally
 * located in the TokenExchangeContext class written by Dmitry Telegin.
 */
public abstract class AbstractOIDCRequestContext {

    protected final KeycloakSession session;
    protected final MultivaluedMap<String, String> formParams;

    // TODO: resolve deps issue and use correct types
    protected final Object cors;
    protected final Object tokenManager;

    protected final ClientModel client;
    protected final RealmModel realm;
    protected final EventBuilder event;

    protected ClientConnection clientConnection;
    protected HttpHeaders headers;
    protected Map<String, String> clientAuthAttributes;

    public AbstractOIDCRequestContext(KeycloakSession session,
                                      MultivaluedMap<String, String> formParams,
                                      Object cors,
                                      RealmModel realm,
                                      EventBuilder event,
                                      ClientModel client,
                                      ClientConnection clientConnection,
                                      HttpHeaders headers,
                                      Object tokenManager,
                                      Map<String, String> clientAuthAttributes) {
        this.session = session;
        this.formParams = formParams;
        this.cors = cors;
        this.client = client;
        this.realm = realm;
        this.event = event;
        this.clientConnection = clientConnection;
        this.headers = headers;
        this.tokenManager = tokenManager;
        this.clientAuthAttributes = clientAuthAttributes;
    }

    public KeycloakSession getSession() {
        return session;
    }

    public MultivaluedMap<String, String> getFormParams() {
        return formParams;
    }

    public Object getCors() {
        return cors;
    }

    public RealmModel getRealm() {
        return realm;
    }

    public ClientModel getClient() {
        return client;
    }

    public EventBuilder getEvent() {
        return event;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public Object getTokenManager() {
        return tokenManager;
    }

    public Map<String, String> getClientAuthAttributes() {
        return clientAuthAttributes;
    }
}
