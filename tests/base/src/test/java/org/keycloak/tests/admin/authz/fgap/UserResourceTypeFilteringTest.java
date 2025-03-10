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

package org.keycloak.tests.admin.authz.fgap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.keycloak.authorization.AdminPermissionsSchema.VIEW;

import java.util.List;
import java.util.Set;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ScopePermissionsResource;
import org.keycloak.authorization.AdminPermissionsSchema;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.UserPolicyRepresentation;
import org.keycloak.testframework.annotations.InjectAdminClient;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.realm.UserConfigBuilder;
import org.keycloak.testframework.util.ApiUtil;

@KeycloakIntegrationTest(config = KeycloakAdminPermissionsServerConfig.class)
public class UserResourceTypeFilteringTest extends AbstractPermissionTest {

    @InjectAdminClient(mode = InjectAdminClient.Mode.MANAGED_REALM, client = "myclient", user = "myadmin")
    Keycloak realmAdminClient;

    private final String usersType = AdminPermissionsSchema.USERS.getType();

    @BeforeEach
    public void onBeforeEach() {
        for (int i = 0; i < 50; i++) {
            realm.admin().users().create(UserConfigBuilder.create().username("user-" + i).build()).close();
        }
    }

    @AfterEach
    public void onAfterEach() {
        ScopePermissionsResource permissions = getScopePermissionsResource(client);

        for (ScopePermissionRepresentation permission : permissions.findAll(null, null, null, -1, -1)) {
            permissions.findById(permission.getId()).remove();
        }
    }

    @Test
    public void testViewUserUsingUserPolicy() {
        UserPolicyRepresentation policy = createUserPolicy(realm, client,"Only My Admin User Policy", realm.admin().users().search("myadmin").get(0).getId());
        createPermission(client, "user-9", usersType, Set.of(VIEW), policy);

        List<UserRepresentation> search = realmAdminClient.realm(realm.getName()).users().search(null, 0, 10);
        assertFalse(search.isEmpty());
        assertEquals(1, search.size());
    }

    @Test
    public void testViewUserUsingGroupPolicy() {
        GroupRepresentation rep = new GroupRepresentation();
        rep.setName("administrators");
        try (Response response = realm.admin().groups().add(rep)) {
            String adminUserId = realm.admin().users().search("myadmin").get(0).getId();
            String groupId = ApiUtil.getCreatedId(response);
            realm.admin().users().get(adminUserId).joinGroup(groupId);
            GroupPolicyRepresentation policy = createGroupPolicy(realm, client, "Admin Group Policy", groupId, Logic.POSITIVE);
            createPermission(client, "user-9", usersType, Set.of(VIEW), policy);

        }

        List<UserRepresentation> search = realmAdminClient.realm(realm.getName()).users().search(null, 0, 10);
        assertFalse(search.isEmpty());
        assertEquals(1, search.size());
    }
}
