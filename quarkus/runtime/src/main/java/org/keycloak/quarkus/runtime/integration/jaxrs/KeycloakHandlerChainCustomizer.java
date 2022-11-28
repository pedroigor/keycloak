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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.common.model.ResourceClass;
import org.jboss.resteasy.reactive.server.core.LazyResponse;
import org.jboss.resteasy.reactive.server.core.ResteasyReactiveRequestContext;
import org.jboss.resteasy.reactive.server.handlers.FormBodyHandler;
import org.jboss.resteasy.reactive.server.handlers.InputHandler;
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer;
import org.jboss.resteasy.reactive.server.model.ServerResourceMethod;
import org.jboss.resteasy.reactive.server.spi.ServerRestHandler;
import org.keycloak.quarkus.runtime.integration.QuarkusHttpResponse;

public class KeycloakHandlerChainCustomizer implements HandlerChainCustomizer {

    @Override
    public List<ServerRestHandler> handlers(Phase phase, ResourceClass resourceClass,
            ServerResourceMethod resourceMethod) {
        if (Phase.BEFORE_METHOD_INVOKE.equals(phase)) {
            if ("post".equalsIgnoreCase(resourceMethod.getHttpMethod())) {
                return List.of(new FormBodyHandler(true, new Supplier<Executor>() {
                    @Override
                    public Executor get() {
                        // we always run in blocking mode and never run in a event loop thread
                        // we don't need to provide an executor to dispatch to a worker thread to parse the body
                        return null;
                    }
                }));
            }
        }
        if (Phase.AFTER_METHOD_INVOKE.equals(phase)) {
            return List.of(new ServerRestHandler() {
                @Override
                public void handle(ResteasyReactiveRequestContext requestContext) throws Exception {
                    Object result = requestContext.getResult();

                    if (result != null) {
                        String[] produces = resourceMethod.getProduces();

                        if (produces.length > 0) {
                            requestContext.setResponseContentType(MediaType.valueOf(produces[0]));
                        }
                    }
                }
            });
        }
        return List.of(new ServerRestHandler() {
            @Override
            public void handle(ResteasyReactiveRequestContext requestContext) throws Exception {
                if (requestContext.getResponse() == null) {
                    QuarkusHttpResponse response = new QuarkusHttpResponse(requestContext);
                    requestContext.setResponse(response);
                }
            }
        });
    }
}
