package org.keycloak.authorization;

import org.keycloak.authorization.model.Resource;
import org.keycloak.models.KeycloakSession;

import java.util.Collections;
import java.util.Set;

public interface ResourceType {

    String getType();
    Set<Scope> getScopes();
    default String getGroupType() {
        return null;
    }
    String resolveResourceId(KeycloakSession session,  String modelId);
    String resolveResourceName(KeycloakSession session, Resource resource);

    class Scope {

        private final String name;
        private final Set<String> aliases;

        public Scope(String name) {
            this(name, null);
        }

        public Scope(String name, Set<String> aliases) {
            this.name = name;
            this.aliases = aliases;
        }

        public String getName() {
            return name;
        }

        public Set<String> getAliases() {
            return Collections.unmodifiableSet(aliases);
        }
    }
}
