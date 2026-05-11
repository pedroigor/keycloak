package org.keycloak.scim.model.client;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.scim.model.filter.ScimAttributeJpaExpressionResolver;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.client.OIDCClient;
import org.keycloak.scim.resource.schema.attribute.Attribute;
import org.keycloak.scim.resource.spi.AbstractScimResourceTypeProvider;

public class OIDCClientResourceType extends AbstractScimResourceTypeProvider<ClientModel, OIDCClient> implements ScimAttributeJpaExpressionResolver {

    public OIDCClientResourceType(KeycloakSession session) {
        super(session, new OIDCClientModelSchema());
    }

    @Override
    public Expression<?> getAttributeExpression(Attribute<?, ?> attribute, CriteriaBuilder cb, Root<?> root, BiFunction<Class<?>, Supplier<Join<?, ?>>, Join<?, ?>> joinResolver) {
        return null;
    }

    @Override
    protected OIDCClient onCreate(OIDCClient resource) {
        return null;
    }

    @Override
    protected OIDCClient onUpdate(ClientModel model, OIDCClient resource) {
        return null;
    }

    @Override
    protected boolean onDelete(String id) {
        return false;
    }

    @Override
    protected Stream<ClientModel> getModels(SearchRequest searchRequest) {
        return Stream.empty();
    }

    @Override
    protected ClientModel getModel(String id) {
        return null;
    }

    @Override
    protected String getRealmResourceType() {
        return "";
    }

    @Override
    public Class<OIDCClient> getResourceType() {
        return OIDCClient.class;
    }

    @Override
    public Long count(SearchRequest searchRequest) {
        return 0L;
    }

    @Override
    public void close() {

    }
}
