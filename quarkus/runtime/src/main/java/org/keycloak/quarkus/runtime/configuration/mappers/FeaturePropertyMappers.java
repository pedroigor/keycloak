package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.ArrayList;
import java.util.List;
import org.keycloak.common.Profile;
import org.keycloak.config.FeatureOptions;
import org.keycloak.config.OptionCategory;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

final class FeaturePropertyMappers {

    private FeaturePropertyMappers() {
    }

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                fromOption(FeatureOptions.features)
                        .paramLabel("feature")
                        .build(),
                fromOption(FeatureOptions.featuresDisabled)
                        .paramLabel("feature")
                        .build()
        };
    }

}
