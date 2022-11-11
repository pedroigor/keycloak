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
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.core.LazyResponse;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;

public class HttpResponse implements org.jboss.resteasy.spi.HttpResponse, LazyResponse {

    private MultivaluedMap<String, Object> headers;
    private int statusCode;
    private Object result;

    public HttpResponse(ResteasyReactiveRequestContext requestContext) {
        headers = new MultivaluedMap<String, Object>() {
            @Override public int size() {
                return 0;
            }

            @Override public boolean isEmpty() {
                return false;
            }

            @Override public boolean containsKey(Object key) {
                return false;
            }

            @Override public boolean containsValue(Object value) {
                return false;
            }

            @Override public List<Object> get(Object key) {
                return null;
            }

            @Override public List<Object> put(String key, List<Object> value) {
                return null;
            }

            @Override public List<Object> remove(Object key) {
                return null;
            }

            @Override public void putAll(Map<? extends String, ? extends List<Object>> m) {

            }

            @Override public void clear() {

            }

            @Override public Set<String> keySet() {
                return null;
            }

            @Override public Collection<List<Object>> values() {
                return null;
            }

            @Override public Set<Entry<String, List<Object>>> entrySet() {
                return null;
            }

            @Override public void putSingle(String key, Object value) {

            }

            @Override public void add(String key, Object value) {
                requestContext.serverResponse().addResponseHeader(key, value.toString());
            }

            @Override public Object getFirst(String key) {
                return null;
            }

            @Override public void addAll(String key, Object... newValues) {

            }

            @Override public void addAll(String key, List<Object> valueList) {

            }

            @Override public void addFirst(String key, Object value) {

            }

            @Override public boolean equalsIgnoreValueOrder(MultivaluedMap<String, Object> otherMap) {
                return false;
            }
        };
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public void setStatus(int i) {
        statusCode = i;
    }

    @Override
    public MultivaluedMap<String, Object> getOutputHeaders() {
        return headers;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {

    }

    @Override
    public void addNewCookie(NewCookie newCookie) {

    }

    @Override
    public void sendError(int i) throws IOException {

    }

    @Override
    public void sendError(int i, String s) throws IOException {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void flushBuffer() throws IOException {

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

    public void setResult(Object result) {
        this.result = result;
    }
}
