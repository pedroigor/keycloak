package org.keycloak.quarkus.runtime.configuration.mappers;

import io.smallrye.config.ConfigSourceInterceptorContext;
import org.keycloak.config.OptionCategory;
import org.keycloak.config.TransactionOptions;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

public class TransactionPropertyMappers {

    private TransactionPropertyMappers(){}

    public static PropertyMapper[] getTransactionPropertyMappers() {
        return new PropertyMapper[] {
                fromOption(TransactionOptions.transactionXaEnabled)
                        .to("quarkus.datasource.jdbc.transactions")
                        .paramLabel(Boolean.TRUE + "|" + Boolean.FALSE)
                        .transformer(QuarkusTransactionsValue)
                        .build()
        };
    }

    private static BiFunction<String, ConfigSourceInterceptorContext, String> QuarkusTransactionsValue =
        (String txValue, ConfigSourceInterceptorContext context) -> {
            boolean isXaEnabled = Boolean.parseBoolean(txValue);

            if (isXaEnabled) {
                return "xa";
            }

            return "enabled";
        };

}
