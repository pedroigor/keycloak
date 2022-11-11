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

import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.server.core.CurrentRequestManager;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.jboss.resteasy.reactive.server.jaxrs.HttpHeadersImpl;
import org.jboss.resteasy.reactive.server.jaxrs.UriInfoImpl;
import org.jboss.resteasy.reactive.server.spi.ServerHttpRequest;
import org.jboss.resteasy.spi.ResteasyAsynchronousContext;

import io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext;

public class HttpRequest implements org.jboss.resteasy.spi.HttpRequest {
    private ServerHttpRequest serverRequest;

    public HttpRequest(ServerHttpRequest serverRequest) {
        this.serverRequest = serverRequest;
    }

    @Override public HttpHeaders getHttpHeaders() {
        return new HttpHeadersImpl(serverRequest.getAllRequestHeaders());
    }

    @Override public MultivaluedMap<String, String> getMutableHeaders() {
        return null;
    }

    @Override public InputStream getInputStream() {
        return null;
    }

    @Override public void setInputStream(InputStream inputStream) {

    }

    @Override public UriInfo getUri() {
        return CurrentRequestManager.get().getUriInfo();
    }

    @Override public String getHttpMethod() {
        return serverRequest.getRequestMethod();
    }

    @Override public void setHttpMethod(String s) {

    }

    @Override public void setRequestUri(URI uri) throws IllegalStateException {

    }

    @Override public void setRequestUri(URI uri, URI uri1) throws IllegalStateException {

    }

    @Override public MultivaluedMap<String, String> getFormParameters() {
        return null;
    }

    @Override
    public MultivaluedMap<String, String> getDecodedFormParameters() {
        FormData formData = ((QuarkusResteasyReactiveRequestContext) serverRequest).getFormData();
        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();

        for (String name : formData) {
            FormData.FormValue value = formData.getFirst(name);

            if (value != null) {
                params.add(name, value.getValue());
            }
        }

        return params;
    }

    @Override public boolean formParametersRead() {
        return false;
    }

    @Override public Object getAttribute(String s) {
        return null;
    }

    @Override public void setAttribute(String s, Object o) {

    }

    @Override public void removeAttribute(String s) {

    }

    @Override public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override public ResteasyAsynchronousContext getAsyncContext() {
        return null;
    }

    @Override public boolean isInitial() {
        return false;
    }

    @Override public void forward(String s) {

    }

    @Override public boolean wasForwarded() {
        return false;
    }

    @Override public String getRemoteAddress() {
        return null;
    }

    @Override public String getRemoteHost() {
        return null;
    }
}
