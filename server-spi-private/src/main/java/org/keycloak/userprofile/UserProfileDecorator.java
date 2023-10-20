package org.keycloak.userprofile;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface UserProfileDecorator {

    void decorate(UserProfileMetadata metadata);
}
