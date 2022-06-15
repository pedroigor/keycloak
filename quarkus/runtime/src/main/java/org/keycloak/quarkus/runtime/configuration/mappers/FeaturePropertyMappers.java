package org.keycloak.quarkus.runtime.configuration.mappers;

import org.keycloak.config.FeatureOptions;

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
