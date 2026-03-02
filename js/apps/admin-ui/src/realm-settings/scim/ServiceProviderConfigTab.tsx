import { KeycloakSpinner, useFetch } from "@keycloak/keycloak-ui-shared";
import {
  FormGroup,
  PageSection,
  Switch,
  TextInput,
} from "@patternfly/react-core";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { FormAccess } from "../../components/form/FormAccess";

type Supported = {
  supported: boolean;
};

type BulkSupport = Supported & {
  maxOperations: number;
  maxPayloadSize: number;
};

type FilterSupport = Supported & {
  maxResults: number;
};

type AuthenticationScheme = {
  type: string;
  name: string;
  description: string;
  specUri: string;
  documentationUri: string;
};

type ServiceProviderConfig = {
  documentationUri?: string;
  patch: Supported;
  bulk: BulkSupport;
  filter: FilterSupport;
  changePassword: Supported;
  sort: Supported;
  etag: Supported;
  authenticationSchemes: AuthenticationScheme[];
};

export const ServiceProviderConfigTab = () => {
  const { t } = useTranslation();
  const [config, setConfig] = useState<ServiceProviderConfig>();

  useFetch(
    async () => {
      return {
        documentationUri:
          "https://www.keycloak.org/docs/latest/securing_apps/#_service_provider_config",
        patch: { supported: true },
        bulk: {
          supported: false,
          maxOperations: -1,
          maxPayloadSize: -1,
        },
        filter: {
          supported: true,
          maxResults: 200,
        },
        changePassword: { supported: false },
        sort: { supported: false },
        etag: { supported: false },
        authenticationSchemes: [
          {
            type: "oauthbearertoken",
            name: "OAuth Bearer Token",
            description:
              "Authentication scheme using the OAuth Bearer Token standard",
            specUri: "https://tools.ietf.org/html/rfc6750",
            documentationUri:
              "https://www.keycloak.org/docs/latest/securing_apps/#_service_provider_config",
          },
        ],
      };
    },
    (data) => setConfig(data),
    [],
  );

  if (!config) {
    return <KeycloakSpinner />;
  }

  return (
    <PageSection variant="light">
      <FormAccess role="manage-realm" isHorizontal>
        <FormGroup label={t("scimDocumentationUri")} fieldId="documentationUri">
          <TextInput
            id="documentationUri"
            value={config.documentationUri || ""}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimPatchSupported")}
          fieldId="patchSupported"
          hasNoPaddingTop
        >
          <Switch
            id="patchSupported"
            label={t("enabled")}
            labelOff={t("disabled")}
            isChecked={config.patch?.supported || false}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimBulkSupported")}
          fieldId="bulkSupported"
          hasNoPaddingTop
        >
          <Switch
            id="bulkSupported"
            label={t("enabled")}
            labelOff={t("disabled")}
            isChecked={config.bulk?.supported || false}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimBulkMaxOperations")}
          fieldId="bulkMaxOperations"
        >
          <TextInput
            id="bulkMaxOperations"
            value={config.bulk?.maxOperations ?? 0}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimBulkMaxPayloadSize")}
          fieldId="bulkMaxPayloadSize"
        >
          <TextInput
            id="bulkMaxPayloadSize"
            value={config.bulk?.maxPayloadSize ?? 0}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimFilterSupported")}
          fieldId="filterSupported"
          hasNoPaddingTop
        >
          <Switch
            id="filterSupported"
            label={t("enabled")}
            labelOff={t("disabled")}
            isChecked={config.filter?.supported || false}
            isDisabled
          />
        </FormGroup>
        <FormGroup label={t("scimFilterMaxResults")} fieldId="filterMaxResults">
          <TextInput
            id="filterMaxResults"
            value={config.filter?.maxResults ?? 0}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimChangePasswordSupported")}
          fieldId="changePasswordSupported"
          hasNoPaddingTop
        >
          <Switch
            id="changePasswordSupported"
            label={t("enabled")}
            labelOff={t("disabled")}
            isChecked={config.changePassword?.supported || false}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimSortSupported")}
          fieldId="sortSupported"
          hasNoPaddingTop
        >
          <Switch
            id="sortSupported"
            label={t("enabled")}
            labelOff={t("disabled")}
            isChecked={config.sort?.supported || false}
            isDisabled
          />
        </FormGroup>
        <FormGroup
          label={t("scimEtagSupported")}
          fieldId="etagSupported"
          hasNoPaddingTop
        >
          <Switch
            id="etagSupported"
            label={t("enabled")}
            labelOff={t("disabled")}
            isChecked={config.etag?.supported || false}
            isDisabled
          />
        </FormGroup>
      </FormAccess>
    </PageSection>
  );
};
