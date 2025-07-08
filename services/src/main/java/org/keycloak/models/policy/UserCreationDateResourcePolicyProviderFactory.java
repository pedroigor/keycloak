package org.keycloak.models.policy;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class UserCreationDateResourcePolicyProviderFactory implements ResourcePolicyProviderFactory {


    @Override
    public ResourceType getType() {
        return ResourceType.USERS;
    }

    @Override
    public ResourcePolicyProvider create(KeycloakSession session) {
        return new UserCreationDateResourcePolicyProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String getId() {
        return "user-creation-date-resource-policy";
    }
}
