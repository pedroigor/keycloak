package org.keycloak.config;

import java.util.ArrayList;
import java.util.List;

public class ClusteringOptions {

    public enum Mechanism {
        ispn,
        local
    }

    public final static Option cache = new OptionBuilder<>("cache", Mechanism.class)
            .category(OptionCategory.CLUSTERING)
            .description("Defines the cache mechanism for high-availability. "
                    + "By default, a 'ispn' cache is used to create a cluster between multiple server nodes. "
                    + "A 'local' cache disables clustering and is intended for development and testing purposes.")
            .defaultValue(Mechanism.ispn)
            .buildTime(true)
            .build();

    public enum Stack {
        tcp,
        udp,
        kubernetes,
        ec2,
        azure,
        google;
    }

    public final static Option cacheStack = new OptionBuilder<>("cache-stack", Stack.class)
            .category(OptionCategory.CLUSTERING)
            .description("Define the default stack to use for cluster communication and node discovery. This option only takes effect "
                    + "if 'cache' is set to 'ispn'. Default: udp.")
            .buildTime(true)
            .expectedValues(Stack.values())
            .build();

    public final static Option cacheConfigFile = new OptionBuilder<>("cache-config-file", String.class)
            .category(OptionCategory.CLUSTERING)
            .description("Defines the file from which cache configuration should be loaded from. "
                    + "The configuration file is relative to the 'conf/' directory.")
            .buildTime(true)
            .build();

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(cache);
        ALL_OPTIONS.add(cacheStack);
        ALL_OPTIONS.add(cacheConfigFile);
    }
}
