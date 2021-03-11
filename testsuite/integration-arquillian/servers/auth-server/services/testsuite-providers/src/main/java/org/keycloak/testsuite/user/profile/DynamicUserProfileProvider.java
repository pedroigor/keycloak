/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.testsuite.user.profile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.component.AmphibianProviderFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileMetadata;
import org.keycloak.userprofile.AttributeMetadata;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.legacy.AbstractUserProfileProvider;
import org.keycloak.userprofile.AttributeValidatorMetadata;
import org.keycloak.util.JsonSerialization;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DynamicUserProfileProvider extends AbstractUserProfileProvider<DynamicUserProfileProvider>
        implements AmphibianProviderFactory<DynamicUserProfileProvider> {

    public static final String ID = "dynamic-userprofile-provider";
    private static final String PARSED_CONFIG_COMPONENT_KEY = "kc.user.profile.metadata";

    public DynamicUserProfileProvider() {
        // for reflection
    }

    public DynamicUserProfileProvider(KeycloakSession session, Map<UserProfileContext, UserProfileMetadata> metadataRegistry) {
        super(session, metadataRegistry);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected DynamicUserProfileProvider create(KeycloakSession session, Map<UserProfileContext, UserProfileMetadata> metadataRegistry) {
        return new DynamicUserProfileProvider(session, metadataRegistry);
    }

    @Override
    protected UserProfileMetadata configureUserProfile(UserProfileMetadata metadata) {
        // adds an attribute config for any context
        metadata.addAttribute("address", createStaticValueValidator("invalidAddress", "fixed-address"));

        // adds an attribute config where the config only takes effect when a scope is requested
        // this only works when performing actions during authentication
        metadata.addAttribute("business.address", Arrays.asList("customer"),
                createStaticValueValidator("invalidBusinessAddress", "fixed-business-address"));

        if (UserProfileContext.ACCOUNT.equals(metadata.getContext())) {
            // department not mandatory when in account and only readonly
            metadata.addAttribute("department", createRequiredValidator("departmentRequired"), true, true);
        }

        if (UserProfileContext.USER_API.equals(metadata.getContext())) {
            // department is set by admin and mandatory
            metadata.addAttribute("department", createRequiredValidator("departmentRequired"));
        }

        return metadata;
    }

    @Override
    protected UserProfileMetadata configureUserProfile(UserProfileMetadata metadata, KeycloakSession session) {
        return decorateUserProfile(metadata, getComponentModelOrCreate(session));
    }

    @Override
    public String getHelpText() {
        return null;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel model)
            throws ComponentValidationException {
        // apply the configuration to all user profile contexts
        for (UserProfileMetadata metadata : contextualMetadataRegistry.values()) {
            decorateUserProfile(metadata, model);
        }
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    public ComponentModel getComponentModel() {
        return getComponentModelOrCreate(session);
    }

    private UserProfileMetadata decorateUserProfile(UserProfileMetadata metadata, ComponentModel model) {
        if (model == null) {
            return metadata;
        }

        String config = model.getConfig().getFirst("config");

        // no config, just return the default profile config
        if (config == null) {
            return metadata;
        }

        Map<UserProfileContext, UserProfileMetadata> metadataMap = model.getNote(PARSED_CONFIG_COMPONENT_KEY);

        // not cached, create a note
        if (metadataMap == null) {
            metadataMap = new HashMap<>();
            model.setNote(PARSED_CONFIG_COMPONENT_KEY, metadataMap);
        }

        return metadataMap.computeIfAbsent(metadata.getContext(), context -> {
            // need to clone otherwise changes to profile config are going to be reflected in the default config
            UserProfileMetadata decoratedMetadata = metadata.clone();

            try {
                Object attribute = JsonSerialization.readValue(config, Map.class).get("validateConfigAttribute");

                if (attribute != null && Boolean.parseBoolean(attribute.toString())) {
                    decoratedMetadata.addAttribute("validateConfigAttribute", createRequiredValidator("required_dynamic_attribute"));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse config", e);
            }

            return decoratedMetadata;
        });
    }

    private ComponentModel getComponentModelOrCreate(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        ComponentModel model = realm.getComponentsStream(realm.getId(), UserProfileProvider.class.getName())
                .findAny()
                .orElseGet(() -> {
                    ComponentModel configModel = new DynamicUserProfileModel();

                    realm.addComponentModel(configModel);

                    return configModel;
                });

        return model;
    }

    private AttributeValidatorMetadata createRequiredValidator(String message) {
        return new AttributeValidatorMetadata(message, context -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();
            AttributeMetadata metadata = context.getMetadata();

            if (!metadata.isOptional() && values.isEmpty()) {
                return false;
            }

            return true;
        });
    }

    private AttributeValidatorMetadata createStaticValueValidator(String message, String value) {
        return new AttributeValidatorMetadata(message, context -> {
            Map.Entry<String, List<String>> attribute = context.getAttribute();
            List<String> values = attribute.getValue();

            if (values.isEmpty()) {
                return false;
            }

            return values.get(0).equals(value);
        });
    }
}
