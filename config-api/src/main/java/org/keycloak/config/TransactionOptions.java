package org.keycloak.config;

import java.util.ArrayList;
import java.util.List;

public class TransactionOptions {

    public final static Option transactionXaEnabled = new OptionBuilder<>("transaction-xa-enabled", Boolean.class)
            .category(OptionCategory.TRANSACTION)
            .description("Manually override the transaction type. Transaction type XA and the appropriate driver is used by default.")
            .buildTime(true)
            .defaultValue(Boolean.TRUE)
            .expectedValues(Boolean.TRUE, Boolean.FALSE)
            .build();

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
         ALL_OPTIONS.add(transactionXaEnabled);
    }
}
