package org.keycloak.tests.admin.model.policy;

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.policy.ResourceAction;
import org.keycloak.models.policy.ResourcePolicy;
import org.keycloak.models.policy.ResourcePolicyManager;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyBuilder {

    public static PolicyBuilder of(String policyId) {
        return new PolicyBuilder(policyId);
    }


    private final String providerId;
    private ResourceAction[] actions;

    public PolicyBuilder(String providerId) {
        this.providerId = providerId;
    }

    public PolicyBuilder withActions(ResourceAction... actions) {
        this.actions = actions;
        return this;
    }

    public ResourcePolicyManager build(KeycloakSession session) {
        ResourcePolicyManager manager = new ResourcePolicyManager(session);

        ResourcePolicy policy = manager.addPolicy(providerId);
        manager.updateActions(policy, List.of(actions));

        return manager;
    }
}
