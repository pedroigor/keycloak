package org.keycloak.models.policy;

import java.util.Date;
import java.util.List;

import org.keycloak.provider.Provider;

public interface ResourceActionProvider extends Provider {

    void run(List<Object> resources);

    boolean isRunnable();
}
