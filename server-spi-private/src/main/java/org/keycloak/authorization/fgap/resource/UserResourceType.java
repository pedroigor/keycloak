package org.keycloak.authorization.fgap.resource;

import org.keycloak.authorization.ResourceType;
import org.keycloak.authorization.model.Resource;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Optional;
import java.util.Set;

public class UserResourceType implements ResourceType {

    private static final UserResourceType INSTANCE = new UserResourceType();

    public static UserResourceType getInstance() {
        return INSTANCE;
    }

    private final Set<Scope> scopes = Set.of(
            new Scope("view", Set.of("view-members")),
            new Scope("manage", Set.of("manage-members"))
    );

    @Override
    public String getType() {
        return "Users";
    }

    @Override
    public Set<Scope> getScopes() {
        return scopes;
    }

    @Override
    public String getGroupType() {
        return "Groups";
    }

    @Override
    public String resolveResourceId(KeycloakSession session, String modelId) {
        return resolveUser(session, modelId).map(UserModel::getId).orElse(null);
    }

    @Override
    public String resolveResourceName(KeycloakSession session, Resource resource) {
        return resolveUser(session, resource.getName()).map(UserModel::getUsername).orElse(null);
    }

    private Optional<UserModel> resolveUser(KeycloakSession session, String modelId) {
        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, modelId);

        if (user == null) {
            user = session.users().getUserByUsername(realm, modelId);
        }

        return Optional.ofNullable(user);
    }
}
