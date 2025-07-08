package org.keycloak.models.policy;

import org.keycloak.component.ComponentFactory;

public interface ResourcePolicyProviderFactory<P extends ResourcePolicyProvider> extends ComponentFactory<P, ResourcePolicyProvider> {

    ResourceType getType();
}
