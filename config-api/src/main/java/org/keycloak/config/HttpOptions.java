package org.keycloak.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class HttpOptions {

    public final static Option httpEnabled = new OptionBuilder<>("http-enabled", Boolean.class)
            .category(OptionCategory.HTTP)
            .description("Enables the HTTP listener.")
            .defaultValue(Boolean.FALSE)
            .expectedValues(Boolean.TRUE, Boolean.FALSE)
            .build();

    public final static Option httpHost = new OptionBuilder<>("http-host", String.class)
            .category(OptionCategory.HTTP)
            .description("The used HTTP Host.")
            .defaultValue("0.0.0.0")
            .build();

    public final static Option httpRelativePath = new OptionBuilder<>("http-relative-path", String.class)
            .category(OptionCategory.HTTP)
            .description("Set the path relative to '/' for serving resources.")
            .defaultValue("/")
            .buildTime(true)
            .build();

    public final static Option httpPort = new OptionBuilder<>("http-port", Integer.class)
            .category(OptionCategory.HTTP)
            .description("The used HTTP port.")
            .defaultValue(8080)
            .build();

    public final static Option httpsPort = new OptionBuilder<>("https-port", Integer.class)
            .category(OptionCategory.HTTP)
            .description("The used HTTPS port.")
            .defaultValue(8443)
            .build();

    public enum ClientAuth {
        none,
        request,
        required
    }

    // TODO: testme from command line
    public final static Option httpsClientAuth = new OptionBuilder<>("https-client-auth", ClientAuth.class)
            .category(OptionCategory.HTTP)
            .description("Configures the server to require/request client authentication. Possible Values: none, request, required.")
            .defaultValue(ClientAuth.none)
            .expectedValues(ClientAuth.values())
            .build();

    public final static Option httpsCipherSuites = new OptionBuilder<>("https-cipher-suites", String.class)
            .category(OptionCategory.HTTP)
            .description("The cipher suites to use. If none is given, a reasonable default is selected.")
            .build();

    public final static Option httpsProtocols = new OptionBuilder<>("https-protocols", String.class)
            .category(OptionCategory.HTTP)
            .description("The list of protocols to explicitly enable.")
            .defaultValue("TLSv1.3")
            .build();

    // TODO: testme, should it be a File?
    public final static Option httpsCertificateFile = new OptionBuilder<>("https-certificate-file", String.class)
            .category(OptionCategory.HTTP)
            .description("The file path to a server certificate or certificate chain in PEM format.")
            .build();

    public final static Option httpsCertificateKeyFile = new OptionBuilder<>("https-certificate-key-file", String.class)
            .category(OptionCategory.HTTP)
            .description("The file path to a private key in PEM format.")
            .build();

    public final static Option httpsKeyStoreFile = new OptionBuilder<>("https-key-store-file", String.class)
            .category(OptionCategory.HTTP)
            .description("The key store which holds the certificate information instead of specifying separate files.")
            .defaultValue("~/conf/server.keystore") // TODO: verify
            .build();

    public final static Option httpsKeyStorePassword = new OptionBuilder<>("https-key-store-password", String.class)
            .category(OptionCategory.HTTP)
            .description("The password of the key store file.")
            .defaultValue("password")
            .build();

    public final static Option httpsKeyStoreType = new OptionBuilder<>("https-key-store-type", String.class)
            .category(OptionCategory.HTTP)
            .description("The type of the key store file. " +
                    "If not given, the type is automatically detected based on the file name.")
            .build();

    public final static Option httpsTrustStoreFile = new OptionBuilder<>("https-trust-store-file", String.class)
            .category(OptionCategory.HTTP)
            .description("The trust store which holds the certificate information of the certificates to trust.")
            .build();

    public final static Option httpsTrustStorePassword = new OptionBuilder<>("https-trust-store-password", String.class)
            .category(OptionCategory.HTTP)
            .description("The password of the trust store file.")
            .build();

    public final static Option httpsTrustStoreType = new OptionBuilder<>("https-trust-store-type", String.class)
            .category(OptionCategory.HTTP)
            .description("The type of the trust store file. " +
                    "If not given, the type is automatically detected based on the file name.")
            // .defaultValue("~/conf/server.keystore") // TODO: is this a bug in the current distribution?
            .build();

    public final static List<Option<?>> ALL_OPTIONS = new ArrayList<>();

    static {
        ALL_OPTIONS.add(httpEnabled);
        ALL_OPTIONS.add(httpHost);
        ALL_OPTIONS.add(httpRelativePath);
        ALL_OPTIONS.add(httpPort);
        ALL_OPTIONS.add(httpsPort);
        ALL_OPTIONS.add(httpsClientAuth);
        ALL_OPTIONS.add(httpsCipherSuites);
        ALL_OPTIONS.add(httpsProtocols);
        ALL_OPTIONS.add(httpsCertificateFile);
        ALL_OPTIONS.add(httpsCertificateKeyFile);
        ALL_OPTIONS.add(httpsKeyStoreFile);
        ALL_OPTIONS.add(httpsKeyStorePassword);
        ALL_OPTIONS.add(httpsKeyStoreType);
        ALL_OPTIONS.add(httpsTrustStoreFile);
        ALL_OPTIONS.add(httpsTrustStorePassword);
        ALL_OPTIONS.add(httpsTrustStoreType);
    }
}
