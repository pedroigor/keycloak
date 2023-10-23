/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.forms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import com.google.common.base.Strings;

import jakarta.ws.rs.core.MultivaluedMap;

public class RegistrationRecaptcha implements FormAction, FormActionFactory {
    public static final String PROVIDER_ID = "registration-recaptcha-action";

    public static final String G_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    public static final String RECAPTCHA_REFERENCE_CATEGORY = "recaptcha";

    // option keys
    public static final String PROJECT_ID = "project.id";
    public static final String SITE_KEY = "site.key";
    public static final String API_KEY = "api.key";
    public static final String ACTION = "action";
    public static final String INVISIBLE = "recaptcha.v3";
    public static final String SCORE_THRESHOLD = "score.threshold";

    private static final Logger LOGGER = Logger.getLogger(RegistrationRecaptcha.class);

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
            .property()
            .name(PROJECT_ID)
            .label("Recaptcha Project ID")
            .helpText("Google Recaptcha Enterprise Project ID the Site Key belongs to")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name(SITE_KEY)
            .label("Recaptcha Site Key")
            .helpText("Google Recaptcha Enterprise Site Key")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name(API_KEY)
            .label("Google API Key")
            .helpText("An API key with the reCAPTCHA Enterprise API enabled in the given project ID")
            .type(ProviderConfigProperty.STRING_TYPE)
            .secret(true)
            .add()
            .property()
            .name(ACTION)
            .label("Action Name")
            .helpText("A meaningful name for this reCAPTCHA context (e.g. login, register). "
                    + "An action name can only contain alphanumeric characters, "
                    + "slashes and underscores and is not case-sensitive.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("register")
            .add()
            .property()
            .name(INVISIBLE)
            .label("ReCAPTCHA v3")
            .helpText("Whether this API Key is configured for invisible, score-based reCAPTCHA (v3, true) "
                    + "or visible, checkbox-based reCAPTCHA (v2, false)")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue(false)
            .add()
            .property()
            .name(SCORE_THRESHOLD)
            .label("Min. Score Threshold")
            .helpText("The minimum score threshold for considering the reCAPTCHA valid (inclusive). "
                    + "Must be a valid double between 0.0 and 1.0.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue("0.7")
            .add()
            .build();

    @Override
    public String getDisplayType() {
        return "Recaptcha Enterprise";
    }

    @Override
    public String getHelpText() {
        return "Adds Google ReCAPTCHA Enterprise to the form. "
                + "Requires a Google API key and a reCAPTCHA Enterprise Key (invisible, score-base or checkbox-based).";
    }

    @Override
    public String getReferenceCategory() {
        return RECAPTCHA_REFERENCE_CATEGORY;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {
        LOGGER.trace("Building page with reCAPTCHA");

        Map<String, String> config = context.getAuthenticatorConfig().getConfig();
        if (config == null
                || Stream.of(PROJECT_ID, SITE_KEY, API_KEY, ACTION)
                        .anyMatch(key -> Strings.isNullOrEmpty(config.get(key)))
                || parseDoubleFromConfig(config, SCORE_THRESHOLD) == null) {
            form.addError(new FormMessage(null, Messages.RECAPTCHA_NOT_CONFIGURED));
            return;
        }

        String userLanguageTag = context.getSession().getContext().resolveLocale(context.getUser())
                .toLanguageTag();
        boolean invisible = Boolean.parseBoolean(config.getOrDefault(INVISIBLE, "true"));

        form.setAttribute("recaptchaRequired", true);
        form.setAttribute("recaptchaSiteKey", config.get(SITE_KEY));
        form.setAttribute("recaptchaAction", config.get(ACTION));
        form.setAttribute("recaptchaVisible", !invisible);
        form.addScript("https://www.google.com/recaptcha/enterprise.js?hl=" + userLanguageTag);
    }

    @Override
    public void validate(ValidationContext context) {
        LOGGER.trace("Validating form with ReCAPTCHA enterprise");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String captcha = formData.getFirst(G_RECAPTCHA_RESPONSE);
        LOGGER.trace("Got captcha: " + captcha);

        if (!Validation.isBlank(captcha) && validateRecaptcha(context, captcha)) {
            context.success();
        } else {
            List<FormMessage> errors = new ArrayList<>();
            errors.add(new FormMessage(null, Messages.RECAPTCHA_FAILED));
            formData.remove(G_RECAPTCHA_RESPONSE);
            context.error(Errors.INVALID_REGISTRATION);
            context.validationError(formData, errors);
            context.excludeOtherErrors();
        }
    }

    protected boolean validateRecaptcha(ValidationContext context, String captcha) {
        LOGGER.trace("Requesting assessment of Google reCAPTCHA Enterprise");
        Map<String, String> config = context.getAuthenticatorConfig().getConfig();

        try {
            HttpPost request = buildAssessmentRequest(captcha, config);
            HttpClient httpClient = context.getSession().getProvider(HttpClientProvider.class).getHttpClient();
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                LOGGER.errorf("Could not create reCAPTCHA assessment: %s", response.getStatusLine());
                EntityUtils.consumeQuietly(response.getEntity());
                throw new Exception(response.getStatusLine().getReasonPhrase());
            }

            RecaptchaAssessmentResponse assessment = JsonSerialization.readValue(
                    response.getEntity().getContent(), RecaptchaAssessmentResponse.class);
            LOGGER.tracef("Got assessment response: %s", assessment);

            String tokenAction = assessment.getTokenProperties().getAction();
            String expectedAction = assessment.getEvent().getExpectedAction();
            if (!tokenAction.equals(expectedAction)) {
                // This may indicates that an attacker is attempting to falsify actions
                LOGGER.warnf("The action name of the reCAPTCHA token '%s' does not match the expected action '%s'!",
                        tokenAction, expectedAction);
                return false;
            }

            boolean valid = assessment.getTokenProperties().isValid();
            double score = assessment.getRiskAnalysis().getScore();
            LOGGER.debugf("ReCAPTCHA assessment: valid=%s, score=%f", valid, score);

            return valid && score >= parseDoubleFromConfig(config, SCORE_THRESHOLD);

        } catch (Exception e) {
            ServicesLogger.LOGGER.recaptchaFailed(e);
        }

        return false;
    }

    private HttpPost buildAssessmentRequest(String captcha, Map<String, String> config)
            throws UnsupportedEncodingException, IOException {

        String url = String.format("https://recaptchaenterprise.googleapis.com/v1/projects/%s/assessments?key=%s",
                config.get(PROJECT_ID), config.get(API_KEY));

        HttpPost request = new HttpPost(url);
        RecaptchaAssessmentRequest body = new RecaptchaAssessmentRequest(
                captcha, config.get(SITE_KEY), config.get(ACTION));
        request.setEntity(new StringEntity(JsonSerialization.writeValueAsString(body)));
        request.setHeader("Content-type", "application/json; charset=utf-8");

        LOGGER.tracef("Built assessment request: %s", body);
        return request;
    }

    private Double parseDoubleFromConfig(Map<String, String> config, String key) {
        String value = config.getOrDefault(key, "");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warnf("Could not parse config %s as double: '%s'", key, value);
        }
        return null;
    }

    @Override
    public void success(FormContext context) {
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }
}
