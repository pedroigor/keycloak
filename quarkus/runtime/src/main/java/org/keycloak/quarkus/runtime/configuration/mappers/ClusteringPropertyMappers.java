package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.keycloak.config.ClusteringOptions;
import org.keycloak.config.OptionCategory;
import org.keycloak.quarkus.runtime.Environment;

import io.smallrye.config.ConfigSourceInterceptorContext;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

final class ClusteringPropertyMappers {

    private ClusteringPropertyMappers() {
    }

    public static PropertyMapper[] getClusteringPropertyMappers() {
        return new PropertyMapper[] {
                fromOption(ClusteringOptions.cache)
                        .paramLabel("type")
                        .build(),
                fromOption(ClusteringOptions.cacheStack)
                        .to("kc.spi-connections-infinispan-quarkus-stack")
                        .paramLabel("stack")
                        .build(),
                fromOption(ClusteringOptions.cacheConfigFile)
                        .mapFrom("cache")
                        .to("kc.spi-connections-infinispan-quarkus-config-file")
                        .transformer(new BiFunction<String, ConfigSourceInterceptorContext, String>() {
                            @Override
                            public String apply(String value, ConfigSourceInterceptorContext context) {
                                if ("local".equals(value)) {
                                    return "cache-local.xml";
                                } else if ("ispn".equals(value)) {
                                    return "cache-ispn.xml";
                                }

                                String pathPrefix;
                                String homeDir = Environment.getHomeDir();

                                if (homeDir == null) {
                                    pathPrefix = "";
                                } else {
                                    pathPrefix = homeDir + "/conf/";
                                }

                                return pathPrefix + value;
                            }
                        })
                        .paramLabel("file")
                        .build()
        };
    }
}
