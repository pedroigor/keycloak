package org.keycloak.config;

import org.keycloak.common.Profile;

import java.util.ArrayList;
import java.util.List;

public class FeatureOptions {

    public final static Option features = new OptionBuilder("features", List.class, Profile.Feature.class)
            .category(OptionCategory.FEATURE)
            .description("Enables a set of one or more features.")
            .expectedStringValues(getFeatureValues())
            .buildTime(true)
            .build();

    public final static Option featuresDisabled = new OptionBuilder("features-disabled", List.class, Profile.Feature.class)
            .category(OptionCategory.FEATURE)
            .description("Disables a set of one or more features.")
            .expectedStringValues(getFeatureValues())
            .buildTime(true)
            .build();

    private static List<String> getFeatureValues() {
        List<String> features = new ArrayList<>();

        for (Profile.Feature value : Profile.Feature.values()) {
            features.add(value.name().toLowerCase().replace('_', '-'));
        }

        features.add(Profile.Type.PREVIEW.name().toLowerCase());

        return features;
    }

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(features);
        ALL_OPTIONS.add(featuresDisabled);
    }
}
