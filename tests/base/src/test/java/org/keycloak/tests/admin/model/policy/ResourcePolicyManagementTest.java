package org.keycloak.tests.admin.model.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.policy.DisableUserActionProviderFactory;
import org.keycloak.models.policy.ResourceAction;
import org.keycloak.models.policy.ResourcePolicy;
import org.keycloak.models.policy.ResourcePolicyManager;
import org.keycloak.models.policy.UserCreationDateResourcePolicyProviderFactory;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.remote.providers.runonserver.RunOnServer;
import org.keycloak.testframework.remote.runonserver.InjectRunOnServer;
import org.keycloak.testframework.remote.runonserver.RunOnServerClient;

@KeycloakIntegrationTest
public class ResourcePolicyManagementTest {

    private static final String realmName = "default";

    @InjectRunOnServer(permittedPackages = "org.keycloak.tests")
    RunOnServerClient runOnServer;

    @Test
    public void testCreatePolicy() {
        runOnServer.run((RunOnServer) ResourcePolicyManagementTest::testCreatePolicy);
    }

    private static void testCreatePolicy(KeycloakSession session) {
        RealmModel realm = configureSessionContext(session);
        ResourcePolicyManager manager = new ResourcePolicyManager(session);

        ResourcePolicy created = manager.addPolicy(new ResourcePolicy(UserCreationDateResourcePolicyProviderFactory.ID));
        assertNotNull(created.getId());

        List<ResourcePolicy> policies = manager.getPolicies();

        assertEquals(1, policies.size());

        ResourcePolicy policy = policies.get(0);

        assertNotNull(policy.getId());
        assertEquals(created.getId(), policy.getId());
        assertNotNull(realm.getComponent(policy.getId()));
        assertEquals(UserCreationDateResourcePolicyProviderFactory.ID, policy.getProviderId());
    }

    @Test
    public void testCreateAction() {
        runOnServer.run((RunOnServer) ResourcePolicyManagementTest::testCreateAction);
    }

    private static void testCreateAction(KeycloakSession session) {
        RealmModel realm = configureSessionContext(session);
        ResourcePolicyManager manager = new ResourcePolicyManager(session);
        ResourcePolicy policy = manager.addPolicy(new ResourcePolicy(UserCreationDateResourcePolicyProviderFactory.ID));

        int expectedActionsSize = 5;

        for (int i = 0; i < expectedActionsSize; i++) {
            manager.addAction(policy, new ResourceAction(DisableUserActionProviderFactory.ID));
        }

        List<ResourceAction> actions = manager.getActions(policy);

        assertEquals(expectedActionsSize, actions.size());

        ResourceAction action = actions.get(0);

        assertNotNull(action.getId());
        assertNotNull(realm.getComponent(action.getId()));
        assertEquals(DisableUserActionProviderFactory.ID, action.getProviderId());
    }

    @Test
    public void testRunPolicy() {
        runOnServer.run((RunOnServer) ResourcePolicyManagementTest::testRunPolicy);
    }

    private static void testRunPolicy(KeycloakSession session) {
        RealmModel realm = configureSessionContext(session);
        ResourcePolicyManager manager = new ResourcePolicyManager(session);
        ResourcePolicy policy = manager.addPolicy(new ResourcePolicy(UserCreationDateResourcePolicyProviderFactory.ID));

        manager.addAction(policy, new ResourceAction(DisableUserActionProviderFactory.ID));

        UserModel user = session.users().addUser(realm, "myuser");

        user.setCreatedTimestamp(System.currentTimeMillis() - Duration.ofDays(5).toMillis());
        session.getProvider(JpaConnectionProvider.class).getEntityManager().flush();

        manager.runPolicies();
    }

    private static RealmModel configureSessionContext(KeycloakSession session) {
        RealmModel realm = session.realms().getRealmByName(realmName);
        session.getContext().setRealm(realm);
        return realm;
    }
}
