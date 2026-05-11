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
