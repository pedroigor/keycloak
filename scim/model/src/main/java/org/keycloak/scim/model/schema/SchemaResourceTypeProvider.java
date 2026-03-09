package org.keycloak.scim.model.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Model;
import org.keycloak.models.ModelException;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.scim.model.config.ServiceProviderConfigResourceTypeProvider;
import org.keycloak.scim.model.resourcetype.ResourceTypeProviderFactory;
import org.keycloak.scim.protocol.request.SearchRequest;
import org.keycloak.scim.resource.Scim;
import org.keycloak.scim.resource.schema.ModelSchema;
import org.keycloak.scim.resource.schema.Schema;
import org.keycloak.scim.resource.schema.Schema.Attribute;
import org.keycloak.scim.resource.spi.ScimResourceTypeProvider;

/**
 * Provider for SCIM Schema resources. This provider exposes the supported SCIM schemas
 * for discovery by SCIM clients via the /Schemas endpoint.
 * <p>
 * Schemas are read-only resources that describe the structure of SCIM resources.
 * This implementation supports:
 * - Built-in core schemas (User, Group)
 * - Built-in extension schemas (EnterpriseUser)
 * - Custom extension schemas based on user profile configuration (future)
 */
public class SchemaResourceTypeProvider implements ScimResourceTypeProvider<Schema> {

    private final Map<String, Schema> schemas = new HashMap<>();
    private final KeycloakSession session;

    public SchemaResourceTypeProvider(KeycloakSession session) {
        this.session = session;
        initializeSchemas();
    }

    private void initializeSchemas() {
        Stream<ProviderFactory> schemas = session.getKeycloakSessionFactory().getProviderFactoriesStream(ScimResourceTypeProvider.class);

        schemas.filter(providerFactory -> !(providerFactory instanceof SchemaResourceTypeProviderFactory
                        || providerFactory instanceof ResourceTypeProviderFactory
                        || providerFactory instanceof ServiceProviderConfigResourceTypeProvider)
                ).flatMap((Function<ProviderFactory, Stream<ModelSchema>>) factory -> {
                    ScimResourceTypeProvider provider = session.getProvider(ScimResourceTypeProvider.class, factory.getId());
                    List<ModelSchema> modelSchemas = provider.getSchemas();
                    return modelSchemas.stream();
                }).forEach(schema -> {
                    Schema rep = new Schema();

                    rep.setId(schema.getId());
                    rep.setName(schema.getName());
                    rep.setDescription(schema.getDescription());

                    Map<String, Attribute> attributes = new HashMap<>();
                    Map<String, org.keycloak.scim.resource.schema.attribute.Attribute> attributes1 = schema.getAttributes();

                    for (org.keycloak.scim.resource.schema.attribute.Attribute attribute : attributes1.values()) {
                        Attribute attr = new Attribute();
                        String name = attribute.getName();

                        if (name.startsWith("meta.")) {
                            continue;
                        }

                        if (attribute.getParentName() != null) {
                            if (schema.isCore()) {
                                name = attribute.getParentName();
                            } else {
                                name = name.substring(attribute.getParentName().length() + 1);
                            }

                            if (name.indexOf('.') != -1) {
                                name = name.substring(0, name.indexOf('.'));
                            }
                        }

                        attr.setName(name);
                        attr.setType(attribute.getType());
                        attr.setMultiValued(attribute.isMultivalued());

                        attributes.put(name, attr);
                    }

                    rep.setAttributes(attributes.values().stream().toList());

                    this.schemas.put(schema.getId(), rep);
                });
    }

    @Override
    public Schema get(String id) {
        // TODO: Add `view-realm` role check for schema discovery ??
        // Currently accessible to any authenticated user with valid bearer token
        // Should be aligned with other discovery endpoints (ResourceTypes, ServiceProviderConfig)
        return schemas.get(id);
    }

    @Override
    public Stream<Schema> getAll(SearchRequest searchRequest) {
        // Per RFC 7644 Section 4, /Schemas is a discovery endpoint that SHALL return all schemas.
        // Filtering, sorting, and pagination are not supported for discovery endpoints.
        // The searchRequest parameter is ignored.
        return schemas.values().stream();
    }

    @Override
    public Long count(SearchRequest searchRequest) {
        return getAll(null).count();
    }

    @Override
    public Schema create(Schema resource) {
        throw new ModelException("Schemas are read-only and cannot be created");
    }

    @Override
    public Schema update(Schema resource) {
        throw new ModelException("Schemas are read-only and cannot be updated");
    }

    @Override
    public boolean delete(String id) {
        throw new ModelException("Schemas are read-only and cannot be deleted");
    }

    @Override
    public String getSchema() {
        return Scim.SCHEMA_CORE_SCHEMA;
    }

    @Override
    public <M extends Model> List<ModelSchema<M, Schema>> getSchemas() {
        return List.of();
    }

    @Override
    public Class<Schema> getResourceType() {
        return Schema.class;
    }

    @Override
    public void close() {
        // No resources to close
    }
}
