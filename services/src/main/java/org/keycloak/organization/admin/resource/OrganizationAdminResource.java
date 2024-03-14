/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.admin.resource;

import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.utils.StringUtil;

@Provider
public class OrganizationAdminResource {

    private final KeycloakSession session;

    public OrganizationAdminResource() {
        // needed for registering to the JAX-RS stack
        this(null);
    }

    public OrganizationAdminResource(KeycloakSession session) {
        this.session = session;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(OrganizationRepresentation organization) {
        if (organization == null) {
            throw new BadRequestException();
        }
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path("1").build()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Stream<OrganizationRepresentation> get() {
        OrganizationRepresentation organization = new OrganizationRepresentation();

        organization.setId("1");
        organization.setName("acme");

        return Stream.of(organization);
    }

    @Path("{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public OrganizationRepresentation get(@PathParam("id") String id) {
        if (StringUtil.isBlank(id)) {
            throw new BadRequestException();
        }

        OrganizationRepresentation organization = new OrganizationRepresentation();

        organization.setId(id);
        organization.setName("acme");

        return organization;
    }

    @Path("{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        if (StringUtil.isBlank(id)) {
            throw new BadRequestException();
        }
        return Response.noContent().build();
    }

    @Path("{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(OrganizationRepresentation organization) {
        if (organization == null || StringUtil.isBlank(organization.getId())) {
            throw new BadRequestException();
        }
        return Response.noContent().build();
    }
}
