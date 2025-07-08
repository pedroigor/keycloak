package org.keycloak.models.policy;

import org.keycloak.provider.ProviderFactory;

public interface ResourceActionProviderFactory extends ProviderFactory<ResourceActionProvider> {

    ResourceType getType();
}
