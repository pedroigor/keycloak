package org.keycloak.scim.model.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.scim.resource.client.OIDCClient;
import org.keycloak.scim.resource.schema.AbstractModelSchema;
import org.keycloak.scim.resource.schema.attribute.Attribute;

public class OIDCClientModelSchema extends AbstractModelSchema<ClientModel, OIDCClient> {

    public static final String SCHEMA = "urn:keycloak:params:scim:schemas:extension:client:1.0:OIDC";

    protected OIDCClientModelSchema() {
        super(SCHEMA);
    }

    @Override
    protected Set<String> getModelAttributeNames() {
        return Set.of("clientId");
    }

    @Override
    protected Object getAttributeValue(ClientModel model, String name) {
        return model.getClientId();
    }

    @Override
    protected String getAttributeSchemaName(String name) {
        return SCHEMA;
    }

    @Override
    public String getName() {
        return "OIDC";
    }

    @Override
    public String getDescription() {
        return "A schema de defines the attributes for a OIDC client resource type";
    }

    @Override
    protected Map<String, Attribute<ClientModel, OIDCClient>> getAttributeMappers() {
        Map<String, Attribute<ClientModel, OIDCClient>> attributes = new HashMap<>();

        attributes.put("clientId", Attribute.<ClientModel, OIDCClient>simple("clientId")
                .modelAttributeResolver(clientModelOIDCClientAttribute -> "clientId")
                .withModelSetter(ClientModel::setClientId)
                .build().get(0));

        return attributes;
    }
}
