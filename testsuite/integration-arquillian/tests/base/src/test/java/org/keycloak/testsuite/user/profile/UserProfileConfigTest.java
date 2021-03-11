/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.testsuite.user.profile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.testsuite.runonserver.RunOnServer;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.ValidationException;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UserProfileConfigTest extends AbstractUserProfileTest {

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
        // no-op
    }

    @Test
    public void testConfig() {
        getTestingClient().server().run((RunOnServer) UserProfileConfigTest::testConfig);
    }

    private static void testConfig(KeycloakSession session) {
        configureSessionRealm(session);
        RealmModel realm = session.getContext().getRealm();
        DynamicUserProfileProvider provider = getDynamicUserProfileProvider(session);
        ComponentModel component = provider.getComponentModel();

        assertNotNull(component);

        component.put("config", "{\"validateConfigAttribute\": true}");
        realm.updateComponent(component);

        UserProfile profile = provider.create(UserProfileContext.ACCOUNT, Collections.emptyMap());

        try {
            profile.validate();
            fail("Should valid validation");
        } catch (ValidationException ve) {
            assertTrue(ve.isAttributeOnError("validateConfigAttribute"));
        }

        profile = provider.create(UserProfileContext.ACCOUNT, Collections.emptyMap());

        try {
            profile.validate();
            fail("Should valid validation");
        } catch (ValidationException ve) {
            assertTrue(ve.isAttributeOnError("validateConfigAttribute"));
        }

        component.put("config", "{\"validateConfigAttribute\": false}");
        realm.updateComponent(component);

        try {
            profile = provider.create(UserProfileContext.ACCOUNT, Collections.emptyMap());
            profile.validate();
            fail("Should valid validation");
        } catch (ValidationException ve) {
            assertFalse(ve.isAttributeOnError("validateConfigAttribute"));
        }

        component.put("config", "{\"validateConfigAttribute\": true}");
        realm.removeComponent(component);

        try {
            profile = provider.create(UserProfileContext.ACCOUNT, Collections.emptyMap());
            profile.validate();
            fail("Should valid validation");
        } catch (ValidationException ve) {
            assertFalse(ve.isAttributeOnError("validateConfigAttribute"));
        }
    }
}
