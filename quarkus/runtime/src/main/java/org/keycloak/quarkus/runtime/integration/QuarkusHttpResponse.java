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

package org.keycloak.quarkus.runtime.integration;

import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.core.LazyResponse;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;

public class QuarkusHttpResponse implements org.keycloak.http.HttpResponse, LazyResponse {

    private Object result;
    private ResteasyReactiveRequestContext requestContext;

    public QuarkusHttpResponse(ResteasyReactiveRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public void setStatus(int statusCode) {
        requestContext.serverResponse().setStatusCode(statusCode);
    }

    @Override
    public void addHeader(String name, String value) {
        requestContext.serverResponse().addResponseHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        requestContext.serverResponse().setResponseHeader(name, value);
    }

    @Override
    public Response get() {
        if (result instanceof Response) {
            return (Response) result;
        }
        return null;
    }

    @Override
    public boolean isCreated() {
        return false;
    }
}
