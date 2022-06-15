package org.keycloak.quarkus.runtime.configuration.mappers;

import io.quarkus.datasource.common.runtime.DatabaseKind;
import io.smallrye.config.ConfigSourceInterceptorContext;
import io.smallrye.config.ConfigValue;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.config.database.Database;

import java.util.Optional;
import java.util.function.BiFunction;

import static org.keycloak.quarkus.runtime.Messages.invalidDatabaseVendor;
import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;
import static org.keycloak.quarkus.runtime.integration.QuarkusPlatform.addInitializationException;

final class DatabasePropertyMappers {

    private DatabasePropertyMappers(){}

    public static PropertyMapper[] getDatabasePropertyMappers() {
        return new PropertyMapper[] {
                fromOption(DatabaseOptions.dbDialect)
                        .mapFrom("db")
                        .to("quarkus.hibernate-orm.dialect")
                        .transformer(DatabasePropertyMappers.transformDialect())
                        .build(),
                fromOption(DatabaseOptions.dbDriver)
                        .mapFrom("db")
                        .to("quarkus.datasource.jdbc.driver")
                        .transformer(DatabasePropertyMappers.getXaOrNonXaDriver())
                        .build(),
                fromOption(DatabaseOptions.db)
                        .to("quarkus.datasource.db-kind")
                        .transformer(toDatabaseKind())
                        .paramLabel("vendor")
                        .build(),
                fromOption(DatabaseOptions.dbUrl)
                        .to("quarkus.datasource.jdbc.url")
                        .mapFrom("db")
                        .transformer(DatabasePropertyMappers.getDatabaseUrl())
                        .paramLabel("jdbc-url")
                        .build(),
                fromOption(DatabaseOptions.dbUrlHost)
                        .to("kc.db-url-host")
                        .paramLabel("hostname")
                        .build(),
                fromOption(DatabaseOptions.dbUrlDatabase)
                        .to("kc.db-url-database")
                        .paramLabel("dbname")
                        .build(),
                fromOption(DatabaseOptions.dbUrlPort)
                        .to("kc.db-url-port")
                        .paramLabel("port")
                        .build(),
                fromOption(DatabaseOptions.dbUrlProperties)
                        .to("kc.db-url-properties")
                        .paramLabel("properties")
                        .build(),
                fromOption(DatabaseOptions.dbUsername)
                        .to("quarkus.datasource.username")
                        .mapFrom("db")
                        .transformer(DatabasePropertyMappers.resolveUsername())
                        .paramLabel("username")
                        .build(),
                fromOption(DatabaseOptions.dbPassword)
                        .to("quarkus.datasource.password")
                        .mapFrom("db")
                        .transformer(DatabasePropertyMappers.resolvePassword())
                        .paramLabel("password")
                        .isMasked(true)
                        .build(),
                fromOption(DatabaseOptions.dbSchema)
                        .to("quarkus.hibernate-orm.database.default-schema")
                        .paramLabel("schema")
                        .build(),
                fromOption(DatabaseOptions.dbPoolInitialSize)
                        .to("quarkus.datasource.jdbc.initial-size")
                        .paramLabel("size")
                        .build(),
                fromOption(DatabaseOptions.dbPoolMinSize)
                        .to("quarkus.datasource.jdbc.min-size")
                        .paramLabel("size")
                        .build(),
                fromOption(DatabaseOptions.dbPoolMaxSize)
                        .to("quarkus.datasource.jdbc.max-size")
                        .paramLabel("size")
                        .build()
        };
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> getDatabaseUrl() {
        return (s, c) -> Database.getDefaultUrl(s).orElse(s);
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> getXaOrNonXaDriver() {
        return (String db, ConfigSourceInterceptorContext context) -> {
            ConfigValue xaEnabledConfigValue = context.proceed("kc.transaction-xa-enabled");

            boolean isXaEnabled = xaEnabledConfigValue == null || Boolean.parseBoolean(xaEnabledConfigValue.getValue());

            return Database.getDriver(db, isXaEnabled).orElse(db);
        };
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> toDatabaseKind() {
        return (db, context) -> {
            Optional<String> databaseKind = Database.getDatabaseKind(db);

            if (databaseKind.isPresent()) {
                return databaseKind.get();
            }

            addInitializationException(invalidDatabaseVendor(db, Database.getAliases()));

            return "h2";
        };
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> resolveUsername() {
        return (String value, ConfigSourceInterceptorContext context) -> {
            if (isDevModeDatabase(context)) {
                return "sa";
            }

            return Database.getDatabaseKind(value).isEmpty() ? value : null;
        };
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> resolvePassword() {
        return (String value, ConfigSourceInterceptorContext context) -> {
            if (isDevModeDatabase(context)) {
                return "password";
            }

            return Database.getDatabaseKind(value).isEmpty() ? value : null;
        };
    }

    private static boolean isDevModeDatabase(ConfigSourceInterceptorContext context) {
        String db = context.proceed("kc.db").getValue();
        return Database.getDatabaseKind(db).get().equals(DatabaseKind.H2);
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> transformDialect() {
        return (db, configSourceInterceptorContext) -> {
            Optional<String> databaseKind = Database.getDatabaseKind(db);

            if (databaseKind.isEmpty()) {
                return db;
            }

            return Database.getDialect(db).orElse(Database.getDialect("dev-file").get());
        };
    }

}
