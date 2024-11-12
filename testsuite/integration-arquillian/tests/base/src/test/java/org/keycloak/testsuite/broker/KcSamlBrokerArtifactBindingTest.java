package org.keycloak.testsuite.broker;

import org.junit.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.broker.saml.SAMLIdentityProviderConfig;
import org.keycloak.protocol.saml.SamlConfigAttributes;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

public final class KcSamlBrokerArtifactBindingTest extends AbstractInitializedBaseBrokerTest {

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return KcSamlBrokerConfiguration.INSTANCE;
    }


    @Test
    public void testLogin() {
        // configure artifact binding to the broker
        IdentityProviderRepresentation idpRep = identityProviderResource.toRepresentation();
        String baseSamlUrl = idpRep.getConfig().get(SAMLIdentityProviderConfig.ARTIFACT_RESOLUTION_SERVICE_URL);
        idpRep.getConfig().put(SAMLIdentityProviderConfig.ARTIFACT_RESOLUTION_SERVICE_URL, baseSamlUrl + "/resolve");
        idpRep.getConfig().put(SAMLIdentityProviderConfig.ARTIFACT_BINDING_RESPONSE, Boolean.TRUE.toString());
        identityProviderResource.update(idpRep);

        // configure artifact binding to the broker client
        RealmResource providerRealm = realmsResouce().realm(bc.providerRealmName());
        ClientRepresentation brokerClient = providerRealm.clients().findByClientId(bc.getIDPClientIdInProviderRealm()).get(0);
        brokerClient.getAttributes().put(SamlConfigAttributes.SAML_ARTIFACT_BINDING, Boolean.TRUE.toString());
        providerRealm.clients().get(brokerClient.getId()).update(brokerClient);

        // login using artifact binding
        oauth.clientId("broker-app");
        loginPage.open(bc.consumerRealmName());
        logInWithBroker(bc);
        updateAccountInformationPage.assertCurrent();
        updateAccountInformationPage.updateAccountInformation("f", "l");
        appPage.assertCurrent();
    }

    @Test
    public void testIdpInitiatedUsingSamlArtifactBinding() {
        // configure artifact binding to the broker
        IdentityProviderRepresentation idpRep = identityProviderResource.toRepresentation();
        String baseSamlUrl = idpRep.getConfig().get(SAMLIdentityProviderConfig.ARTIFACT_RESOLUTION_SERVICE_URL);
        idpRep.getConfig().put(SAMLIdentityProviderConfig.ARTIFACT_RESOLUTION_SERVICE_URL, baseSamlUrl + "/resolve");
        idpRep.getConfig().put(SAMLIdentityProviderConfig.ARTIFACT_BINDING_RESPONSE, Boolean.TRUE.toString());
        identityProviderResource.update(idpRep);

        // configure artifact binding to the broker client
        RealmResource providerRealm = realmsResouce().realm(bc.providerRealmName());
        ClientRepresentation brokerClient = providerRealm.clients().findByClientId(bc.getIDPClientIdInProviderRealm()).get(0);
        brokerClient.getAttributes().put(SamlConfigAttributes.SAML_ARTIFACT_BINDING, Boolean.TRUE.toString());
        // make sure the broker redirectes to the IDP-Initiated URL for a specific client in the consumer realm
        String idpInitiatedSsoName = "broker-client";
        String brokerRedirectUri = brokerClient.getAttributes().get(SamlProtocol.SAML_ASSERTION_CONSUMER_URL_POST_ATTRIBUTE) + "/clients/" + idpInitiatedSsoName;
        brokerClient.getAttributes().put(SamlProtocol.SAML_ASSERTION_CONSUMER_URL_POST_ATTRIBUTE, brokerRedirectUri);
        // configure the IDP-Initiated URL Name to the client in the provider realm
        brokerClient.getAttributes().put(SamlProtocol.SAML_IDP_INITIATED_SSO_URL_NAME, idpInitiatedSsoName);
        providerRealm.clients().get(brokerClient.getId()).update(brokerClient);

        brokerClient.setId(null);
        // creates a new client that supports IDP-Initiated SSO
        brokerClient.getAttributes().put(SamlProtocol.SAML_IDP_INITIATED_SSO_URL_NAME, idpInitiatedSsoName);
        // creates a new client in the consumer realm to redirect to the target application
        brokerClient.getAttributes().put(SamlProtocol.SAML_ASSERTION_CONSUMER_URL_POST_ATTRIBUTE, oauth.getRedirectUri());
        brokerClient.setProtocolMappers(null);
        realmsResouce().realm(bc.consumerRealmName()).clients().create(brokerClient);

        // login using artifact binding
        driver.navigate().to(getAuthServerRoot() + "/realms/" +  bc.providerRealmName()+ "/protocol/" + SamlProtocol.LOGIN_PROTOCOL + "/clients/" + idpInitiatedSsoName);
        loginPage.login("testuser", "password");
        updateAccountInformationPage.assertCurrent();
        updateAccountInformationPage.updateAccountInformation("f", "l");
        appPage.assertCurrent();
    }
}
