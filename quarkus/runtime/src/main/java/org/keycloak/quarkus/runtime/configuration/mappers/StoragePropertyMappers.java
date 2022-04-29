package org.keycloak.quarkus.runtime.configuration.mappers;

import io.smallrye.config.ConfigSourceInterceptorContext;

final class StoragePropertyMappers {

    private StoragePropertyMappers(){}

    public static PropertyMapper[] getMappers() {
        return new PropertyMapper[] {
                builder().from("storage")
                        .to("kc.spi-map-storage-provider")
                        .isBuildTimeProperty(true)
                        .description("Sets a storage mechanism.")
                        .expectedValues("concurrenthashmap")
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-realm")
                        .mapFrom("storage")
                        .to("kc.spi-realm-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-client")
                        .mapFrom("storage")
                        .to("kc.spi-client-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-client-scope")
                        .mapFrom("storage")
                        .to("kc.spi-client-scope-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-group")
                        .mapFrom("storage")
                        .to("kc.spi-group-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-role")
                        .mapFrom("storage")
                        .to("kc.spi-role-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-user")
                        .mapFrom("storage")
                        .to("kc.spi-user-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-deployment-state")
                        .mapFrom("storage")
                        .to("kc.spi-deployment-state-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-auth-session")
                        .mapFrom("storage")
                        .to("kc.spi-auth-session-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-user-session")
                        .mapFrom("storage")
                        .to("kc.spi-user-session-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-login-failure")
                        .mapFrom("storage")
                        .to("kc.spi-login-failure-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-authorization-persister")
                        .mapFrom("storage")
                        .to("kc.spi-authorization-persister-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-realm")
                        .mapFrom("storage")
                        .to("kc.spi-realm-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-realm")
                        .mapFrom("storage")
                        .to("kc.spi-realm-provider")
                        .transformer(StoragePropertyMappers::getAreaStorage)
                        .hidden(true)
                        .paramLabel("type")
                        .build(),
                builder().from("storage-dblock")
                        .mapFrom("storage")
                        .to("kc.spi-dblock-provider")
                        .transformer(StoragePropertyMappers::getDbLockProvider)
                        .hidden(true)
                        .paramLabel("type")
                        .build()
        };
    }

    private static String getAreaStorage(String storage, ConfigSourceInterceptorContext context) {
        return "concurrenthashmap".equals(storage) ? "map" : "jpa";
    }

    private static String getDbLockProvider(String storage, ConfigSourceInterceptorContext context) {
        return "concurrenthashmap".equals(storage) ? "none" : "jpa";
    }

    private static PropertyMapper.Builder builder() {
        return PropertyMapper.builder(ConfigCategory.STORAGE);
    }
}
