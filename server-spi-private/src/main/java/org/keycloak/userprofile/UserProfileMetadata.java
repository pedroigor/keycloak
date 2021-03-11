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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class UserProfileMetadata implements Cloneable {

    private final UserProfileContext context;
    private List<AttributeMetadata> attributes;

    public UserProfileMetadata(UserProfileContext context) {
        this.context = context;
    }

    public List<AttributeMetadata> getAttributes() {
        return attributes;
    }

    public void addAttributes(AttributeMetadata... metadata) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        attributes.addAll(Arrays.asList(metadata));
    }

    public void addAttribute(String name, AttributeValidatorMetadata... validator) {
        addAttribute(name, Arrays.asList(validator));
    }

    public void addAttribute(String name, List<AttributeValidatorMetadata> validators) {
        addAttributes(new AttributeMetadata(name).addValidator(validators));
    }

    public void addAttribute(String name, AttributeValidatorMetadata validator, boolean readOnly) {
        addAttributes(new AttributeMetadata(name, readOnly).addValidator(validator));
    }

    public void addAttribute(String name, AttributeValidatorMetadata validator, boolean readOnly, boolean optional) {
        addAttributes(new AttributeMetadata(name, readOnly, optional).addValidator(validator));
    }

    public void addAttribute(String name, List<String> scopeSelector, AttributeValidatorMetadata validator) {
        addAttributes(new AttributeMetadata(name, scopeSelector).addValidator(validator));
    }

    public UserProfileContext getContext() {
        return context;
    }

    @Override
    public UserProfileMetadata clone() {
        UserProfileMetadata metadata = new UserProfileMetadata(this.context);

        metadata.attributes = new ArrayList<>();
        metadata.attributes.addAll(new ArrayList<>(this.attributes));

        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfileMetadata)) return false;

        UserProfileMetadata that = (UserProfileMetadata) o;
        return that.getContext().equals(getContext());
    }

    @Override
    public int hashCode() {
        return getContext().hashCode();
    }
}
