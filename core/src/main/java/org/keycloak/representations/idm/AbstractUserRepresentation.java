package org.keycloak.representations.idm;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.keycloak.json.StringListMapDeserializer;

public abstract class AbstractUserRepresentation {

    protected String id;
    protected String username;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected Boolean emailVerified;
    @JsonDeserialize(using = StringListMapDeserializer.class)
    protected Map<String, List<String>> attributes;
    private UserProfileMetadata userProfileMetadata;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        if (firstName == null) {
            firstName = getFirstAttribute("firstName");
        }
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        if (lastName == null) {
            lastName = getFirstAttribute("lastName");
        }
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        if (email == null) {
            email = getFirstAttribute("email");
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getUsername() {
        if (username == null) {
            username = getFirstAttribute("username");
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns all the attributes set to this user except the root attributes.
     *
     * @return the user attributes.
     */
    public Map<String, List<String>> getAttributes() {
        if (attributes == null) {
            return null;
        }

        attributes = new HashMap<>(attributes);

        // make sure root attributes are not exposed as a regular attribute as they have their own field in the representation
        attributes.remove("username");
        attributes.remove("email");
        attributes.remove("firstName");
        attributes.remove("lastName");

        List<String> locale = attributes.getOrDefault("locale", Collections.emptyList());

        if (locale.isEmpty()) {
            // locale attribute only available if there is a value for it
            attributes.remove("locale");
        }

        return attributes;
    }

    /**
     * Returns all the user attributes including the root attributes.
     *
     * @return all the user attributes.
     */
    @JsonIgnore
    public Map<String, List<String>> getRawAttributes() {
        Map<String, List<String>> attrs = new HashMap<>(Optional.ofNullable(attributes).orElse(new HashMap<>()));

        if (username != null)
            attrs.put("username", Collections.singletonList(getUsername()));
        else
            attrs.remove("username");

        if (email != null)
            attrs.put("email", Collections.singletonList(getEmail()));
        else
            attrs.remove("email");

        if (lastName != null)
            attrs.put("lastName", Collections.singletonList(getLastName()));

        if (firstName != null)
            attrs.put("firstName", Collections.singletonList(getFirstName()));

        return attrs;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }

    @SuppressWarnings("unchecked")
    public <R extends AbstractUserRepresentation> R singleAttribute(String name, String value) {
        if (this.attributes == null) this.attributes=new HashMap<>();
        attributes.put(name, (value == null ? Collections.emptyList() : Arrays.asList(value)));
        return (R) this;
    }

    public String firstAttribute(String key) {
        return this.attributes == null ? null : this.attributes.get(key) == null ? null : this.attributes.get(key).isEmpty()? null : this.attributes.get(key).get(0);
    }

    public void setUserProfileMetadata(UserProfileMetadata userProfileMetadata) {
        this.userProfileMetadata = userProfileMetadata;
    }

    public UserProfileMetadata getUserProfileMetadata() {
        return userProfileMetadata;
    }

    private String getFirstAttribute(String name) {
        List<String> values = ofNullable(attributes).orElse(Collections.emptyMap()).get(name);

        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
}
