package org.keycloak.models.policy;

import org.keycloak.provider.Provider;

public interface ResourceActionProvider extends Provider {

    ResourceAction create();

    void update(ResourceAction action);

    boolean delete(ResourceAction action);

    ResourceAction findById(String id);
}
