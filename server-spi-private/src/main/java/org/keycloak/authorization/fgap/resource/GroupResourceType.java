package org.keycloak.authorization.fgap.resource;

import org.keycloak.authorization.ResourceType;
import org.keycloak.authorization.model.Resource;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.util.Optional;
import java.util.Set;

public class GroupResourceType implements ResourceType {

    private static final GroupResourceType INSTANCE = new GroupResourceType();

    public static GroupResourceType getInstance() {
        return INSTANCE;
    }

    private final Set<Scope> scopes = Set.of(
            new Scope("view"),
            new Scope("manage")
    );

    @Override
    public String getType() {
        return "Groups";
    }

    @Override
    public Set<Scope> getScopes() {
        return scopes;
    }

    @Override
    public String resolveResourceId(KeycloakSession session, String modelId) {
        return resolveGroup(session, modelId).map(GroupModel::getId).orElse(null);
    }

    @Override
    public String resolveResourceName(KeycloakSession session, Resource resource) {
        return resolveGroup(session, resource.getName()).map(GroupModel::getName).orElse(null);
    }

    private Optional<GroupModel> resolveGroup(KeycloakSession session, String modelId) {
        RealmModel realm = session.getContext().getRealm();
        return Optional.ofNullable(session.groups().getGroupById(realm, modelId));
    }
}
