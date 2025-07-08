package org.keycloak.models.policy;

import org.keycloak.models.policy.ResourcePolicy;

public class UserCreationDateResourcePolicy implements ResourcePolicy {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void runActions() {

    }
}
