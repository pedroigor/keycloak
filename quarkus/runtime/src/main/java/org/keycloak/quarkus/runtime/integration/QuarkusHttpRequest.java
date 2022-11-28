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

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.keycloak.http.FormPart;
import org.keycloak.http.HttpRequest;

import io.vertx.ext.web.RoutingContext;

public class QuarkusHttpRequest implements HttpRequest {

    private ResteasyReactiveRequestContext context;

    public <R> QuarkusHttpRequest(ResteasyReactiveRequestContext context) {
        this.context = context;
    }

    @Override
    public String getHttpMethod() {
        return context.getMethod();
    }

    @Override
    public MultivaluedMap<String, String> getDecodedFormParameters() {
        FormData formData = context.getFormData();

        if (formData == null) {
            return new MultivaluedHashMap<>();
        }

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();

        for (String name : formData) {
            Deque<FormData.FormValue> value = formData.get(name);

            if (value != null) {
                Iterator<FormData.FormValue> iterator = value.iterator();

                while (iterator.hasNext()) {
                    params.add(name, iterator.next().getValue());
                }
            }
        }

        return params;
    }

    @Override
    public MultivaluedMap<String, FormPart> getMultiPartFormParameters() {
        FormData formData = context.getFormData();

        if (formData == null) {
            return new MultivaluedHashMap<>();
        }

        MultivaluedMap<String, FormPart> params = new MultivaluedHashMap<>();

        for (String name : formData) {
            Deque<FormData.FormValue> formValues = formData.get(name);

            if (formValues != null) {
                Iterator<FormData.FormValue> iterator = formValues.iterator();

                while (iterator.hasNext()) {
                    FormData.FormValue formValue = iterator.next();

                    if (formValue.isFileItem()) {
                        try {
                            params.add(name, new FormPart(formValue.getFileItem().getInputStream()));
                        } catch (IOException cause) {
                            throw new RuntimeException("Failed to parse multipart file parameter", cause);
                        }
                    } else {
                        params.add(name, new FormPart(formValue.getValue()));
                    }
                }
            }
        }

        return params;
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        return context.getHttpHeaders();
    }

    @Override
    public X509Certificate[] getClientCertificateChain() {
        Instance<RoutingContext> instances = CDI.current().select(RoutingContext.class);

        if (instances.isResolvable()) {
            RoutingContext context = instances.get();

            try {
                SSLSession sslSession = context.request().sslSession();

                if (sslSession == null) {
                    return null;
                }

                return (X509Certificate[]) sslSession.getPeerCertificates();
            } catch (SSLPeerUnverifiedException ignore) {
                // client not authenticated
            }
        }

        return null;
    }

    @Override
    public UriInfo getUri() {
        return context.getUriInfo();
    }
}
