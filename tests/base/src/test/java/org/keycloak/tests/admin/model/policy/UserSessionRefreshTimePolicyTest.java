/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.tests.admin.model.policy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.keycloak.common.util.Time;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.policy.DisableUserActionProviderFactory;
import org.keycloak.models.policy.NotifyUserActionProviderFactory;
import org.keycloak.models.policy.ResourceAction;
import org.keycloak.models.policy.ResourcePolicy;
import org.keycloak.models.policy.ResourcePolicyManager;
import org.keycloak.models.policy.UserActionBuilder;
import org.keycloak.models.policy.UserLastAuthTimeResourcePolicyProviderFactory;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.InjectUser;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.oauth.OAuthClient;
import org.keycloak.testframework.oauth.annotations.InjectOAuthClient;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.realm.ManagedUser;
import org.keycloak.testframework.realm.UserConfig;
import org.keycloak.testframework.realm.UserConfigBuilder;
import org.keycloak.testframework.remote.runonserver.InjectRunOnServer;
import org.keycloak.testframework.remote.runonserver.RunOnServerClient;
import org.keycloak.testframework.ui.annotations.InjectPage;
import org.keycloak.testframework.ui.annotations.InjectWebDriver;
import org.keycloak.testframework.ui.page.LoginPage;
import org.openqa.selenium.WebDriver;

@KeycloakIntegrationTest
public class UserSessionRefreshTimePolicyTest {

    private static final String REALM_NAME = "default";

    @InjectRunOnServer(permittedPackages = "org.keycloak.tests")
    RunOnServerClient runOnServer;

    @InjectUser(ref = "alice", config = DefaultUserConfig.class)
    private ManagedUser userAlice;

    @InjectWebDriver
    WebDriver driver;

    @InjectPage
    LoginPage loginPage;

    @InjectOAuthClient
    OAuthClient oauth;

    @Test
    public void testDeleteUsersBasedOnSessionRefreshTime() {
        // setup
        runOnServer.run(session -> {
            RealmModel realm = configureSessionContext(session);
            ResourcePolicyManager manager = new ResourcePolicyManager(session);
            ResourcePolicy policy = manager.addPolicy(UserLastAuthTimeResourcePolicyProviderFactory.ID);
            ResourceAction notifyAction = UserActionBuilder.builder(NotifyUserActionProviderFactory.ID)
                    .after(Duration.ofDays(5))
                    .build();
            ResourceAction disableAction = UserActionBuilder.builder(DisableUserActionProviderFactory.ID)
                    .after(Duration.ofDays(10))
                    .build();
            manager.updateActions(policy, List.of(notifyAction, disableAction));
            UserModel user = session.users().getUserByUsername(realm, "alice");
            session.sessions().removeUserSessions(realm, user);
        });

        oauth.realm("default");
        oauth.openLoginForm();
        loginPage.fillLogin("alice", "alice");
        loginPage.submit();
        assertTrue(driver.getPageSource().contains("Happy days"));

        // test run policy
        runOnServer.run(session -> {
            RealmModel realm = configureSessionContext(session);

            UserModel user = session.users().getUserByUsername(realm, "alice");
            EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
            UserEntity entity = em.find(UserEntity.class, user.getId());
            assertNotNull(entity.getLastSessionRefreshTime());
            assertTrue(user.isEnabled());
            assertNull(user.getAttributes().get("notification_sent"));

            ResourcePolicyManager manager = new ResourcePolicyManager(session);

            try {
                Time.setOffset(Math.toIntExact(Duration.ofDays(7).toSeconds()));

                // running runPolicy multiple times within the same date should not disable the user
                manager.runPolicies();
                user = session.users().getUserByUsername(realm, "alice");
                assertTrue(user.isEnabled());
                assertNotNull(user.getAttributes().get("notification_sent"));

                Time.setOffset(Math.toIntExact(Duration.ofDays(11).toSeconds()));
                manager.runPolicies();
                user = session.users().getUserByUsername(realm, "alice");
                assertFalse(user.isEnabled());
            } finally {
                Time.setOffset(0);
            }
        });
    }

    @Test
    public void testDeleteUsersBasedOnSessionRefreshTimeReAuth() {
        // setup
        runOnServer.run(session -> {
            configureSessionContext(session);
            ResourcePolicyManager manager = new ResourcePolicyManager(session);
            ResourcePolicy policy = manager.addPolicy(UserLastAuthTimeResourcePolicyProviderFactory.ID);
            ResourceAction notifyAction = UserActionBuilder.builder(NotifyUserActionProviderFactory.ID)
                    .after(Duration.ofDays(5))
                    .build();
            ResourceAction disableAction = UserActionBuilder.builder(DisableUserActionProviderFactory.ID)
                    .after(Duration.ofDays(10))
                    .build();

            manager.updateActions(policy, List.of(notifyAction, disableAction));
        });

        oauth.realm("default");
        oauth.openLoginForm();
        loginPage.fillLogin("alice", "alice");
        loginPage.submit();
        assertTrue(driver.getPageSource().contains("Happy days"));

        // test run policy
        runOnServer.run(session -> {
            RealmModel realm = configureSessionContext(session);

            UserModel user = session.users().getUserByUsername(realm, "alice");
            EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
            UserEntity entity = em.find(UserEntity.class, user.getId());
            assertNotNull(entity.getLastSessionRefreshTime());
            assertTrue(user.isEnabled());
            assertNull(user.getAttributes().get("notification_sent"));

            ResourcePolicyManager manager = new ResourcePolicyManager(session);

            Time.setOffset(Math.toIntExact(Duration.ofDays(7).toSeconds()));

            manager.runPolicies();
            user = session.users().getUserByUsername(realm, "alice");
            assertTrue(user.isEnabled());
            assertNotNull(user.getAttributes().get("notification_sent"));

            Time.setOffset(0);
        });

        oauth.openLoginForm();

        runOnServer.run(session -> {
            RealmModel realm = configureSessionContext(session);
            ResourcePolicyManager manager = new ResourcePolicyManager(session);

            Time.setOffset(Math.toIntExact(Duration.ofDays(11).toSeconds()));

            manager.runPolicies();
            UserModel user = session.users().getUserByUsername(realm, "alice");
            assertTrue(user.isEnabled());
        });

        runOnServer.run(session -> {
            RealmModel realm = configureSessionContext(session);
            UserModel user = session.users().getUserByUsername(realm, "alice");
            ResourcePolicyManager manager = new ResourcePolicyManager(session);

            try {
                Time.setOffset(Math.toIntExact(Duration.ofDays(20).toSeconds()));
                manager.runPolicies();
                user = session.users().getUserByUsername(realm, "alice");
                assertFalse(user.isEnabled());
            } finally {
                Time.setOffset(0);
            }
        });
    }

    private static RealmModel configureSessionContext(KeycloakSession session) {
        RealmModel realm = session.realms().getRealmByName(REALM_NAME);
        session.getContext().setRealm(realm);
        return realm;
    }

    private static class DefaultUserConfig implements UserConfig {

        @Override
        public UserConfigBuilder configure(UserConfigBuilder user) {
            user.username("alice");
            user.password("alice");
            user.name("alice", "alice");
            user.email("master-admin@email.org");
            return user;
        }
    }
}
