package org.keycloak.quarkus.runtime.configuration.mappers;

import org.keycloak.common.Profile;
import org.keycloak.config.FeatureOptions;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.smallrye.config.ConfigSourceInterceptorContext;

final class FeaturePropertyMappers {

    private FeaturePropertyMappers() {
    }

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                fromOption(FeatureOptions.FEATURES)
                        .paramLabel("feature")
                        .transformer(FeaturePropertyMappers::transformFeatures)
                        .build(),
                fromOption(FeatureOptions.FEATURES_DISABLED)
                        .paramLabel("feature")
                        .build()
        };
    }

    private static String transformFeatures(String features, ConfigSourceInterceptorContext context) {
        if (Boolean.parseBoolean(Configuration.getRawValue("kc.storage-default-persistence-unit-enabled"))) {
            return features;
        }

        Set<String> featureSet = new HashSet<>(List.of(features.split(",")));

        featureSet.add(Profile.Feature.MAP_STORAGE.name().replace('_', '-'));

        return String.join(",", featureSet);
    }
}
