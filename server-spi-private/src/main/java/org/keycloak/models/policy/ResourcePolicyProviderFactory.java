package org.keycloak.models.policy;

import org.keycloak.provider.ProviderFactory;

public interface ResourcePolicyProviderFactory extends ProviderFactory<ResourcePolicyProvider> {

    ResourceType getType();
}
