/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.organization.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.junit.Test;
import org.keycloak.admin.client.resource.OrganizationResource;
import org.keycloak.common.Profile.Feature;
import org.keycloak.representations.idm.OrganizationRepresentation;
import org.keycloak.testsuite.admin.AbstractAdminTest;
import org.keycloak.testsuite.admin.ApiUtil;
import org.keycloak.testsuite.arquillian.annotation.EnableFeature;

@EnableFeature(Feature.ORGANIZATION)
public class OrganizationTest extends AbstractAdminTest {

    @Test
    public void testCreate() {
        OrganizationRepresentation org = new OrganizationRepresentation();

        org.setName("acme");

        String id;

        try (Response response = testRealm().organization().create(org)) {
            assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
            id = ApiUtil.getCreatedId(response);
        }

        OrganizationResource orgResource = testRealm().organization().get(id);
        assertEquals(id, orgResource.toRepresentation().getId());
    }

    @Test
    public void testUpdate() {
        OrganizationRepresentation org = new OrganizationRepresentation();

        org.setId("1");
        org.setName("acme");

        OrganizationResource orgResource = testRealm().organization().get(org.getId());

        try (Response response = orgResource.update(org)) {
            assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }

        org.setId(null);

        try (Response response = orgResource.update(org)) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testGet() {
        OrganizationRepresentation organization = testRealm().organization().get("1").toRepresentation();
        assertNotNull(organization);
        assertEquals("1", organization.getId());
    }

    @Test
    public void testGetAll() {
        List<OrganizationRepresentation> organizations = testRealm().organization().get();
        assertFalse(organizations.isEmpty());
    }

    @Test
    public void testDelete() {
        try (Response response = testRealm().organization().get("1").delete()) {
            assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }
    }
}
