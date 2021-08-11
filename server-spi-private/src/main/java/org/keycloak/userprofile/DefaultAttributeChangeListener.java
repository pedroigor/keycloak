package org.keycloak.userprofile;

import java.util.function.BiConsumer;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * A default listener implementation that handles changes to user attributes.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DefaultAttributeChangeListener implements BiConsumer<String, UserModel> {

    private static final String UPDATED_AT_TIME_STAMP_UPDATED = "org.keycloak.user.updatedAtTimeStampUpdated";
    private final KeycloakSession session;

    public DefaultAttributeChangeListener(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void accept(String s, UserModel userModel) {
        setLastTimeUpdatedTimestamp(userModel);
    }

    private void setLastTimeUpdatedTimestamp(UserModel userModel) {
        if (!session.getAttributeOrDefault(UPDATED_AT_TIME_STAMP_UPDATED, Boolean.FALSE)) {
            userModel.setLastUpdatedTimestamp(Time.currentTimeMillis());
            session.setAttribute(UPDATED_AT_TIME_STAMP_UPDATED, Boolean.TRUE);
        }
    }
}
