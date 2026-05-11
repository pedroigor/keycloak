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
package org.keycloak.testsuite.broker;

import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.testsuite.broker.oidc.TestKeycloakOidcIdentityProviderFactory;

import org.junit.Test;

import static org.keycloak.testsuite.broker.BrokerTestConstants.IDP_OIDC_ALIAS;
import static org.keycloak.testsuite.broker.BrokerTestTools.createIdentityProvider;
import static org.keycloak.testsuite.broker.BrokerTestTools.waitForPage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests that a proper error is shown when the OIDC identity provider does not return a nonce in the ID token,
 * while nonce validation is enabled on the consumer side.
 *
 * Reproduces: https://github.com/keycloak/keycloak/issues/48694
 */
public class KcOidcBrokerMissingNonceTest extends AbstractInitializedBaseBrokerTest {

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcMissingNonceBrokerConfiguration();
    }

    @Test
    public void testMissingNonceShowsInvalidResponseError() {
        oauth.client("broker-app");
        loginPage.open(bc.consumerRealmName());

        waitForPage(driver, "sign in to", true);
        loginPage.clickSocial(bc.getIDPAlias());
        waitForPage(driver, "sign in to", true);
        loginPage.login(bc.getUserLogin(), bc.getUserPassword());

        errorPage.assertCurrent();
        assertThat(errorPage.getError(), is("Invalid response from identity provider."));
    }

    private static class KcOidcMissingNonceBrokerConfiguration extends KcOidcBrokerConfiguration {

        @Override
        public IdentityProviderRepresentation setUpIdentityProvider(IdentityProviderSyncMode syncMode) {
            IdentityProviderRepresentation idp = createIdentityProvider(IDP_OIDC_ALIAS, TestKeycloakOidcIdentityProviderFactory.ID);

            applyDefaultConfiguration(idp.getConfig(), syncMode);
            idp.getConfig().put(TestKeycloakOidcIdentityProviderFactory.STRIP_NONCE, Boolean.TRUE.toString());

            return idp;
        }
    }
}
