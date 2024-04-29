/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.organization.admin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.graphene.page.Page;
import org.junit.Rule;
import org.junit.Test;
import org.keycloak.admin.client.resource.OrganizationResource;
import org.keycloak.common.Profile.Feature;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testsuite.Assert;
import org.keycloak.testsuite.admin.ApiUtil;
import org.keycloak.testsuite.arquillian.annotation.EnableFeature;
import org.keycloak.testsuite.pages.InfoPage;
import org.keycloak.testsuite.util.GreenMailRule;
import org.keycloak.testsuite.util.MailUtils;
import org.keycloak.testsuite.util.UserBuilder;

@EnableFeature(Feature.ORGANIZATION)
public class OrganizationInvitationLinkTest extends AbstractOrganizationTest {

    @Rule
    public GreenMailRule greenMail = new GreenMailRule();

    @Page
    protected InfoPage infoPage;

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
        Map<String, String> smtpConfig = testRealm.getSmtpServer();
        super.configureTestRealm(testRealm);
        testRealm.setSmtpServer(smtpConfig);
    }

    @Test
    public void testInviteExistingUser() throws IOException {
        UserRepresentation user = UserBuilder.create()
                .username("invited")
                .email("invited@myemail.com")
                .password("password")
                .enabled(true)
                .build();
        try (Response response = testRealm().users().create(user)) {
            user.setId(ApiUtil.getCreatedId(response));
        }

        OrganizationResource organization = testRealm().organizations().get(createOrganization().getId());

        organization.members().inviteMember(user).close();

        MimeMessage message = greenMail.getLastReceivedMessage();
        Assert.assertNotNull(message);
        String invitatoinLink = MailUtils.getPasswordResetEmailLink(message);
        driver.manage().timeouts().pageLoadTimeout(1, TimeUnit.DAYS);
        driver.navigate().to(invitatoinLink.trim());
        Assert.assertFalse(organization.members().getAll().stream().anyMatch(actual -> user.getId().equals(actual.getId())));
        infoPage.clickToContinue();
        assertThat(infoPage.getInfo(), containsString("Your account has been updated."));
        Assert.assertTrue(organization.members().getAll().stream().anyMatch(actual -> user.getId().equals(actual.getId())));
    }
}