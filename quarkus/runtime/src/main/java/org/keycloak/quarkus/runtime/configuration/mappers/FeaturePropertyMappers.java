package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.keycloak.common.Profile;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.smallrye.config.ConfigSourceInterceptorContext;

final class FeaturePropertyMappers {

    private FeaturePropertyMappers() {
    }

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                builder()
                        .from("features")
                        .description("Enables a set of one or more features.")
                        .expectedValues(getFeatureValues())
                        .defaultValue("")
                        .transformer(FeaturePropertyMappers::transformFeatures)
                        .paramLabel("feature")
                        .build(),
                builder()
                        .from("features-disabled")
                        .expectedValues(getFeatureValues())
                        .paramLabel("feature")
                        .description("Disables a set of one or more features.")
                        .build()
        };
    }

    private static List<String> getFeatureValues() {
        List<String> features = new ArrayList<>();

        for (Profile.Feature value : Profile.Feature.values()) {
            features.add(value.name().toLowerCase().replace('_', '-'));
        }

        features.add(Profile.Type.PREVIEW.name().toLowerCase());

        return features;
    }

    private static PropertyMapper.Builder builder() {
        return PropertyMapper.builder(ConfigCategory.FEATURE).isBuildTimeProperty(true);
    }

    private static String transformFeatures(String features, ConfigSourceInterceptorContext context) {
        if (Boolean.parseBoolean(Configuration.getRawValue("kc.db-enabled"))) {
            return features;
        }

        Set<String> featureSet = new HashSet<>(List.of(features.split(",")));

        featureSet.add(Profile.Feature.MAP_STORAGE.name().replace('_', '-'));

        return String.join(",", featureSet);
    }
}
