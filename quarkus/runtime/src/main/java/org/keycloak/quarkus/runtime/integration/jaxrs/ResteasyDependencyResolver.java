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

package org.keycloak.quarkus.runtime.integration.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Resteasy;
import org.keycloak.models.KeycloakSession;

import io.quarkus.arc.Unremovable;

@ApplicationScoped
@Unremovable
public class ResteasyDependencyResolver {

    @Produces
    @RequestScoped
    public KeycloakSession getKeycloakSession() {
        return Resteasy.getContextData(KeycloakSession.class);
    }

    @Produces
    @RequestScoped
    public ClientConnection getClientConnection() {
        return Resteasy.getContextData(ClientConnection.class);
    }

    @Produces
    @RequestScoped
    public HttpRequest getHttpRequest() {
        return Resteasy.getContextData(HttpRequest.class);
    }

    @Produces
    @RequestScoped
    public HttpResponse getHttpResponse() {
        return Resteasy.getContextData(HttpResponse.class);
    }
}
