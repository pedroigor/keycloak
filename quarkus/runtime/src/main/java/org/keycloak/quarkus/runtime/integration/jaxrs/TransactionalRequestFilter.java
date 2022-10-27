/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.integration.jaxrs;

import java.io.IOException;
import java.util.stream.Stream;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.ext.Provider;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Resteasy;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.transaction.TransactionalSessionHandler;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

@Provider
public class TransactionalRequestFilter implements ContainerRequestFilter, TransactionalSessionHandler {

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        configureContextualData(Resteasy.getContextData(RoutingContext.class));
    }

    private void configureContextualData(RoutingContext context) {
        KeycloakSession session = create();

        Resteasy.pushContext(KeycloakSession.class, session);
        context.put(KeycloakSession.class.getName(), session);

        ClientConnection connection = createClientConnection(context.request());

        Resteasy.pushContext(ClientConnection.class, connection);
        context.put(ClientConnection.class.getName(), connection);
    }

    private ClientConnection createClientConnection(HttpServerRequest request) {
        return new ClientConnection() {
            @Override
            public String getRemoteAddr() {
                return request.remoteAddress().host();
            }

            @Override
            public String getRemoteHost() {
                return request.remoteAddress().host();
            }

            @Override
            public int getRemotePort() {
                return request.remoteAddress().port();
            }

            @Override
            public String getLocalAddr() {
                return request.localAddress().host();
            }

            @Override
            public int getLocalPort() {
                return request.localAddress().port();
            }
        };
    }
}
