/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.storage.ldap.idm.store.ldap;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.tracing.TracingProvider;
import org.keycloak.truststore.TruststoreProvider;
import org.keycloak.vault.VaultStringSecret;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Optional;

/**
 * A {@link InitialLdapContext} that binds instances of this class with the {@link KeycloakSession} so that any resource
 * acquired during the session lifetime is closed when the session is closed.
 */
public final class SessionBoundInitialLdapContext extends InitialLdapContext implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(SessionBoundInitialLdapContext.class);

    private StartTlsResponse tlsResponse;

    public SessionBoundInitialLdapContext(KeycloakSession session, LDAPConfig ldapConfig) throws NamingException {
        this(session, ldapConfig, null, null);
    }

    public SessionBoundInitialLdapContext(KeycloakSession session, LDAPConfig ldapConfig, LdapName dn, String password) throws NamingException {
        super(getConnectionProperties(session, ldapConfig, dn, password), null);
        var tracing = session.getProvider(TracingProvider.class);
        tracing.startSpan(SessionBoundInitialLdapContext.class, "createLdapContext");
        try {
            if (ldapConfig.isStartTls()) {
                tlsResponse = startTLS(session, ldapConfig);

                // Exception should be already thrown by LDAPContextManager.startTLS if "startTLS" could not be established, but rather do some additional check
                if (tlsResponse == null) {
                    throw new NamingException("Wasn't able to establish LDAP connection through StartTLS");
                }
            }
        } catch (NamingException e) {
            tracing.error(e);
            throw e;
        } finally {
            tracing.endSpan();
        }
        session.enlistForClose(() -> {
                try {
                    close();
                } catch (NamingException e) {
                    failedToCloseLdapContext(e);
                }
        });
    }

    private static Hashtable<Object, Object> getConnectionProperties(KeycloakSession session, LDAPConfig ldapConfig, LdapName dn, String password) {
        String bindDN;
        String bindCredential;
        String authType = ldapConfig.getAuthType();

        if (dn != null) {
            bindDN = dn.toString();
            bindCredential = password;
            authType = "simple";
        } else {
            bindDN = ldapConfig.getBindDN();
            try (VaultStringSecret vaultStringSecret = ldapConfig.getVaultSecret(session)) {
                bindCredential = vaultStringSecret.get().orElse(ldapConfig.getBindCredential());
            }
        }
        boolean disablePool = dn != null;
        return ldapConfig.getConnectionProperties(session, authType, bindDN, bindCredential, disablePool);
    }

    @Override
    public void close() throws NamingException {
        if (tlsResponse != null) {
            try {
                tlsResponse.close();
            } catch (IOException e) {
                logger.error("Could not close Ldap tlsResponse.", e);
            }
        }

        super.close();
    }

    private StartTlsResponse startTLS(KeycloakSession session, LDAPConfig config) throws NamingException {
        StartTlsResponse tls;

        try {
            SSLSocketFactory sslSocketFactory = null;
            if (LDAPUtil.shouldUseTruststoreSpi(config)) {
                TruststoreProvider provider = session.getProvider(TruststoreProvider.class);
                sslSocketFactory = provider.getSSLSocketFactory();
            }
            tls = (StartTlsResponse) extendedOperation(new StartTlsRequest());
            tls.negotiate(sslSocketFactory);

            String authType = (String) getEnvironment().get(Context.SECURITY_AUTHENTICATION);
            addToEnvironment(Context.SECURITY_AUTHENTICATION, authType);

            if (!LDAPConstants.AUTH_TYPE_NONE.equals(authType)) {
                addToEnvironment(Context.SECURITY_PRINCIPAL, getEnvironment().get(Context.SECURITY_PRINCIPAL));
                addToEnvironment(Context.SECURITY_CREDENTIALS, getEnvironment().get(Context.SECURITY_CREDENTIALS));
            }
        } catch (Exception e) {
            logger.error("Could not negotiate TLS", e);
            NamingException ne = new AuthenticationException("Could not negotiate TLS");
            ne.setRootCause(e);
            throw ne;
        }

        // throws AuthenticationException when authentication fails
        lookup("");

        return tls;
    }

    private void failedToCloseLdapContext(NamingException e) {
        throw new RuntimeException("Failed to close LDAP context", e);
    }
}
