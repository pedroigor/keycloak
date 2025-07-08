package org.keycloak.models.policy;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class ResourcePolicyManager {

    private KeycloakSession session;

    public ResourcePolicyManager(KeycloakSession session) {
        this.session = session;
    }

    void addPolicy(ResourcePolicy policy) {
        RealmModel realm = session.getContext().getRealm();

    }

    void addAction(ResourcePolicy policy, ResourceAction action) {
        RealmModel realm = session.getContext().getRealm();
        ComponentModel policyModel = realm.getComponent(policy.getComponentId());

        ComponentModel actionModel = new ComponentModel();
        actionModel.setParentId(policyModel.getId());
        actionModel.setProviderId(action.getFactoryId());
//        actionModel.setProviderType()
        // update component
    }

    void runPolicies() {

    }
}
