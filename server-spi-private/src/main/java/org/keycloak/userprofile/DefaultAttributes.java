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

package org.keycloak.userprofile;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.representations.userprofile.config.UPConfig.UnmanagedAttributePolicy;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;

/**
 * <p>The default implementation for {@link Attributes}. Should be reused as much as possible by the different implementations
 * of {@link UserProfileProvider}.
 *
 * <p>One of the main aspects of this implementation is to allow normalizing attributes accordingly to the profile
 * configuration and current context. As such, it provides some common normalization to common profile attributes (e.g.: username,
 * email, first and last names, dynamic read-only attributes).
 *
 * <p>This implementation is not specific to any user profile implementation.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DefaultAttributes extends HashMap<String, List<String>> implements Attributes {

    private static Logger logger = Logger.getLogger(DefaultAttributes.class);

    /**
     * To reference dynamic attributes that can be configured as read-only when setting up the provider.
     * We should probably remove that once we remove the legacy provider, because this will come from the configuration.
     */
    public static final String READ_ONLY_ATTRIBUTE_KEY = "kc.read.only";

    protected final UserProfileContext context;
    protected final KeycloakSession session;
    private final Map<String, AttributeMetadata> metadataByAttribute;
    private final UPConfig upConfig;
    protected final UserModel user;
    private final Map<String, List<String>> unmanagedAttributes = new HashMap<>();

    public DefaultAttributes(UserProfileContext context, Map<String, ?> attributes, UserModel user,
            UserProfileMetadata profileMetadata,
            KeycloakSession session) {
        this.context = context;
        this.user = user;
        this.session = session;
        this.metadataByAttribute = configureMetadata(profileMetadata.getAttributes());
        this.upConfig = session.getProvider(UserProfileProvider.class).getConfiguration();
        putAll(Collections.unmodifiableMap(normalizeAttributes(attributes)));
    }

    @Override
    public boolean isReadOnly(String name) {
        if (!isManagedAttribute(name)) {
            return !isAllowEditUnmanagedAttribute();
        }

        if (UserModel.USERNAME.equals(name)) {
            if (isServiceAccountUser()) {
                return true;
            }
        }

        if (UserModel.EMAIL.equals(name)) {
            if (isServiceAccountUser()) {
                return false;
            }
        }

        if (isReadOnlyFromMetadata(name) || isReadOnlyInternalAttribute(name)) {
            return true;
        }

        return getMetadata(name) == null;
    }

    private boolean isAllowEditUnmanagedAttribute() {
        UnmanagedAttributePolicy unmanagedAttributesPolicy = upConfig.getUnmanagedAttributePolicy();

        if (!isAllowUnmanagedAttribute()) {
            return false;
        }

        switch (unmanagedAttributesPolicy) {
            case ENABLED:
                return true;
            case ADMIN_EDIT:
                return UserProfileContext.USER_API.equals(context);
        }

        return false;
    }

    /**
     * Checks whether an attribute is marked as read only by looking at its metadata.
     *
     * @param attributeName the attribute name
     * @return @return {@code true} if the attribute is readonly. Otherwise, returns {@code false}
     */
    protected boolean isReadOnlyFromMetadata(String attributeName) {
        AttributeMetadata attributeMetadata = metadataByAttribute.get(attributeName);

        if (attributeMetadata == null) {
            return false;
        }

        return attributeMetadata.isReadOnly(createAttributeContext(attributeMetadata));
    }

    @Override
    public boolean isRequired(String name) {
        AttributeMetadata attributeMetadata = metadataByAttribute.get(name);

        if (attributeMetadata == null) {
            return false;
        }

        return attributeMetadata.isRequired(createAttributeContext(attributeMetadata));
    }

    @Override
    public boolean validate(String name, Consumer<ValidationError>... listeners) {
        Entry<String, List<String>> attribute = createAttribute(name);
        List<AttributeMetadata> metadatas = new ArrayList<>();

        metadatas.addAll(Optional.ofNullable(this.metadataByAttribute.get(attribute.getKey()))
                .map(Collections::singletonList).orElse(emptyList()));
        metadatas.addAll(Optional.ofNullable(this.metadataByAttribute.get(READ_ONLY_ATTRIBUTE_KEY))
                .map(Collections::singletonList).orElse(emptyList()));

        Boolean result = null;

        for (AttributeMetadata metadata : metadatas) {
            AttributeContext attributeContext = createAttributeContext(attribute, metadata);

            for (AttributeValidatorMetadata validator : metadata.getValidators()) {
                ValidationContext vc = validator.validate(attributeContext);

                if (vc.isValid()) {
                    continue;
                }

                if (user != null && metadata.isReadOnly(attributeContext)
                        && CollectionUtil.collectionEquals(user.getAttributeStream(name).collect(Collectors.toList()), attribute.getValue())) {
                    // allow update if the value was already wrong in the user and is read-only in this context
                    logger.warnf("User '%s' attribute '%s' has previous validation errors %s but is read-only in context %s.",
                            user.getUsername(), name, vc.getErrors(), attributeContext.getContext());
                    continue;
                }

                if (result == null) {
                    result = false;
                }

                if (listeners != null) {
                    for (ValidationError error : vc.getErrors()) {
                        for (Consumer<ValidationError> consumer : listeners) {
                            consumer.accept(error);
                        }
                    }
                }
            }
        }

        return result == null;
    }

    @Override
    public List<String> get(String name) {
        return getOrDefault(name, EMPTY_VALUE);
    }

    @Override
    public boolean contains(String name) {
        return containsKey(name);
    }

    @Override
    public Set<String> nameSet() {
        return keySet();
    }

    @Override
    public Map<String, List<String>> getWritable() {
        Map<String, List<String>> attributes = new HashMap<>(this);

        for (String name : nameSet()) {
            AttributeMetadata metadata = getMetadata(name);
            RealmModel realm = session.getContext().getRealm();

            if ((UserModel.USERNAME.equals(name) && realm.isRegistrationEmailAsUsername())
                || !isManagedAttribute(name)) {
                continue;
            }

            if (metadata == null || !metadata.canEdit(createAttributeContext(metadata))) {
                attributes.remove(name);
            }
        }

        return attributes;
    }

    @Override
    public AttributeMetadata getMetadata(String name) {
        if (unmanagedAttributes.containsKey(name)) {
            return createUnmanagedAttributeMetadata(name);
        }

        return Optional.ofNullable(metadataByAttribute.get(name))
                .map(AttributeMetadata::clone)
                .orElse(null);
    }

    @Override
    public Map<String, List<String>> getReadable() {
        Map<String, List<String>> attributes = new HashMap<>(this);

        for (String name : nameSet()) {
            AttributeMetadata metadata = getMetadata(name);

            if (metadata == null) {
                attributes.remove(name);
                continue;
            }

            AttributeContext attributeContext = createAttributeContext(metadata);

            if (!metadata.canView(attributeContext) || !metadata.isSelected(attributeContext)) {
                attributes.remove(name);
            }
        }

        return attributes;
    }

    @Override
    public Map<String, List<String>> toMap() {
        return Collections.unmodifiableMap(this);
    }

    protected boolean isServiceAccountUser() {
        return user != null && user.getServiceAccountClientLink() != null;
    }

    private AttributeContext createAttributeContext(Entry<String, List<String>> attribute, AttributeMetadata metadata) {
        return new AttributeContext(context, session, attribute, user, metadata, this);
    }

    private AttributeContext createAttributeContext(String attributeName, AttributeMetadata metadata) {
        return new AttributeContext(context, session, createAttribute(attributeName), user, metadata, this);
    }

    protected AttributeContext createAttributeContext(AttributeMetadata metadata) {
        return createAttributeContext(createAttribute(metadata.getName()), metadata);
    }

    private Map<String, AttributeMetadata> configureMetadata(List<AttributeMetadata> attributes) {
        Map<String, AttributeMetadata> metadatas = new HashMap<>();

        for (AttributeMetadata metadata : attributes) {
            // checks whether the attribute is selected for the current profile
            if (metadata.isSelected(createAttributeContext(metadata))) {
                metadatas.put(metadata.getName(), metadata);
            }
        }

        return metadatas;
    }

    private SimpleImmutableEntry<String, List<String>> createAttribute(String name) {
        return new SimpleImmutableEntry<String, List<String>>(name, null) {
            @Override
            public List<String> getValue() {
                List<String> values = get(name);

                if (values == null) {
                    return EMPTY_VALUE;
                }

                return values;
            }
        };
    }

    /**
     * Normalizes the given {@code attributes} (as they were provided when creating a profile) accordingly to the
     * profile configuration and the current context.
     *
     * @param attributes the denormalized map of attributes
     *
     * @return a normalized map of attributes
     */
    private Map<String, List<String>> normalizeAttributes(Map<String, ?> attributes) {
        Map<String, List<String>> newAttributes = new HashMap<>();
        RealmModel realm = session.getContext().getRealm();

        if (attributes != null) {
            for (Map.Entry<String, ?> entry : attributes.entrySet()) {
                String key = entry.getKey();

                if (!isSupportedAttribute(key)) {
                    if (!isManagedAttribute(key) && isAllowUnmanagedAttribute()) {
                        unmanagedAttributes.put(key, (List<String>) entry.getValue());
                    }
                    continue;
                }

                if (key.startsWith(Constants.USER_ATTRIBUTES_PREFIX)) {
                    key = key.substring(Constants.USER_ATTRIBUTES_PREFIX.length());
                }

                Object value = entry.getValue();
                List<String> values;

                if (value instanceof String) {
                    values = Collections.singletonList((String) value);
                } else {
                    values = (List<String>) value;
                }

                values = normalizeAttributeValues(key, values);

                newAttributes.put(key, Collections.unmodifiableList(values));
            }
        }

        // the profile should always hold all attributes defined in the config
        for (String attributeName : metadataByAttribute.keySet()) {
            if (!isSupportedAttribute(attributeName) || newAttributes.containsKey(attributeName)) {
                continue;
            }

            List<String> values = EMPTY_VALUE;
            AttributeMetadata metadata = metadataByAttribute.get(attributeName);

            if (user != null && isIncludeAttributeIfNotProvided(metadata)) {
                values = normalizeAttributeValues(attributeName, user.getAttributes().getOrDefault(attributeName, EMPTY_VALUE));
            }

            newAttributes.put(attributeName, values);
        }

        if (user != null) {
            List<String> username = newAttributes.getOrDefault(UserModel.USERNAME, emptyList());

            if (username.isEmpty() && isReadOnly(UserModel.USERNAME)) {
                setUserName(newAttributes, Collections.singletonList(user.getUsername()));
            }
        }

        List<String> email = newAttributes.getOrDefault(UserModel.EMAIL, emptyList());

        if (!email.isEmpty() && realm.isRegistrationEmailAsUsername()) {
            setUserName(newAttributes, email);

            if (user != null && isReadOnly(UserModel.EMAIL)) {
                newAttributes.put(UserModel.EMAIL, Collections.singletonList(user.getEmail()));
                setUserName(newAttributes, Collections.singletonList(user.getEmail()));
            }
        }

        if (isAllowUnmanagedAttribute()) {
            newAttributes.putAll(unmanagedAttributes);
        }

        return newAttributes;
    }

    private List<String> normalizeAttributeValues(String name, List<String> values) {
        Stream<String> valuesStream = Optional.ofNullable(values).orElse(EMPTY_VALUE).stream();

        if (UserModel.USERNAME.equals(name) || UserModel.EMAIL.equals(name)) {
            valuesStream = valuesStream.map(KeycloakModelUtils::toLowerCaseSafe);
        }

        return valuesStream.filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean isAllowUnmanagedAttribute() {
        UnmanagedAttributePolicy unmanagedAttributePolicy = upConfig.getUnmanagedAttributePolicy();

        if (unmanagedAttributePolicy == null) {
            // unmanaged attributes disabled
            return false;
        }

        switch (unmanagedAttributePolicy) {
            case ADMIN_EDIT:
            case ADMIN_VIEW:
                // unmanaged attributes only available through the admin context
                return UserProfileContext.USER_API.equals(context);
        }

        // allow unmanaged attributes if enabled to all contexts
        return UnmanagedAttributePolicy.ENABLED.equals(unmanagedAttributePolicy);
    }

    private void setUserName(Map<String, List<String>> newAttributes, List<String> lowerCaseEmailList) {
        if (isServiceAccountUser()) {
            return;
        }
        newAttributes.put(UserModel.USERNAME, lowerCaseEmailList);
    }

    protected boolean isIncludeAttributeIfNotProvided(AttributeMetadata metadata) {
        return !metadata.canEdit(createAttributeContext(metadata));
    }

    /**
     * <p>Checks whether an attribute is support by the profile configuration and the current context.
     *
     * <p>This method can be used to avoid unexpected attributes from being added as an attribute because
     * the attribute source is a regular {@link Map} and not normalized.
     *
     * @param name the name of the attribute
     * @return
     */
    protected boolean isSupportedAttribute(String name) {
        if (READ_ONLY_ATTRIBUTE_KEY.equals(name)) {
            return false;
        }

        if (isManagedAttribute(name)) {
            return true;
        }

        if (isServiceAccountUser()) {
            return true;
        }

        return isReadOnlyInternalAttribute(name);
    }

    private boolean isManagedAttribute(String name) {
        return metadataByAttribute.containsKey(name);
    }

    /**
     * <p>Returns whether an attribute is read only based on the provider configuration (using provider config),
     * usually related to internal attributes managed by the server.
     *
     * <p>For user-defined attributes, it should be preferable to use the user profile configuration.
     *
     * @param attributeName the attribute name
     * @return {@code true} if the attribute is readonly. Otherwise, returns {@code false}
     */
    protected boolean isReadOnlyInternalAttribute(String attributeName) {
        // read-only can be configured through the provider so we try to validate global validations
        AttributeMetadata readonlyMetadata = metadataByAttribute.get(READ_ONLY_ATTRIBUTE_KEY);

        if (readonlyMetadata == null) {
            return false;
        }

        AttributeContext attributeContext = createAttributeContext(attributeName, readonlyMetadata);

        for (AttributeValidatorMetadata validator : readonlyMetadata.getValidators()) {
            ValidationContext vc = validator.validate(attributeContext);
            if (!vc.isValid()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<String, List<String>> getUnmanagedAttributes() {
        return unmanagedAttributes;
    }

    private AttributeMetadata createUnmanagedAttributeMetadata(String name) {
        return new AttributeMetadata(name, Integer.MAX_VALUE) {
            final UnmanagedAttributePolicy unmanagedAttributePolicy = upConfig.getUnmanagedAttributePolicy();

            @Override
            public boolean canView(AttributeContext context) {
                return canEdit(context)
                        || (UnmanagedAttributePolicy.ADMIN_VIEW.equals(unmanagedAttributePolicy) && UserProfileContext.USER_API.equals(context.getContext()));
            }

            @Override
            public boolean canEdit(AttributeContext context) {
                return UnmanagedAttributePolicy.ENABLED.equals(unmanagedAttributePolicy)
                        || (UnmanagedAttributePolicy.ADMIN_EDIT.equals(unmanagedAttributePolicy) && UserProfileContext.USER_API.equals(context.getContext()));
            }
        };
    }
}
