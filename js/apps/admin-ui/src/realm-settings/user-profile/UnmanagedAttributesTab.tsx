import { useTranslation } from "react-i18next";
import { KeycloakSpinner } from "../../components/keycloak-spinner/KeycloakSpinner";
import { useUserProfile } from "./UserProfileContext";
import {
  FormGroup,
  Grid,
  GridItem,
  PageSection,
  Switch,
} from "@patternfly/react-core";
import { HelpItem } from "ui-shared";
import { Policy } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import { FormAccess } from "../../components/form/FormAccess";
import { useEffect, useState } from "react";

export const UnmanagedAttributesTab = () => {
  const { config, save } = useUserProfile();
  const { t } = useTranslation();
  const [enableUnmanaged, setEnableUnmanaged] = useState(false);

  useEffect(() => {
    setEnableUnmanaged(config?.unmanagedAttributesPolicy != undefined);
  });

  if (!config) {
    return <KeycloakSpinner />;
  }

  function setPolicy(policy?: Policy) {
    save({
      ...config,
      unmanagedAttributesPolicy:
        config?.unmanagedAttributesPolicy === policy ? Policy.enabled : policy,
    });
  }

  return (
    <PageSection variant="light">
      <FormAccess isHorizontal role="manage-realm">
        <FormGroup
          label={t("enableUnmanaged")}
          fieldId="kc-up-unmanaged-attribute-policy-enableUnmanaged"
          labelIcon={
            <HelpItem
              helpText={t("enableUnmanagedHelpText")}
              fieldLabelId="enableUnmanaged"
            />
          }
          hasNoPaddingTop
        >
          <Switch
            id="kc-up-unmanaged-attributes-enableUnmanaged"
            data-testid="up-unmanaged-attributes-enableUnmanaged"
            value={enableUnmanaged ? "on" : "off"}
            label={t("on")}
            labelOff={t("off")}
            isChecked={enableUnmanaged}
            onChange={(value) => {
              setPolicy(value ? Policy.enabled : undefined);
            }}
            aria-label={t("enableUnmanaged")}
          />
        </FormGroup>
        {enableUnmanaged && (
          <FormGroup
            label={t("allowUnmanagedAdmin")}
            fieldId="kc-up-unmanaged-attribute-policy-allowUnmanagedAdmin"
            labelIcon={
              <HelpItem
                helpText={t("allowUnmanagedAdminHelpText")}
                fieldLabelId="allowUnmanagedAdmin"
              />
            }
            hasNoPaddingTop
          >
            <Grid>
              <GridItem>
                <FormGroup
                  label={t("read")}
                  fieldId="kc-up-unmanaged-attribute-policy-admin-view"
                  hasNoPaddingTop
                >
                  <Switch
                    id="kc-up-unmanaged-attributes-admin-view"
                    data-testid="up-unmanaged-attributes-admin-view"
                    value={
                      config.unmanagedAttributesPolicy === Policy.adminView
                        ? "on"
                        : "off"
                    }
                    label={t("on")}
                    labelOff={t("off")}
                    isChecked={
                      config.unmanagedAttributesPolicy === Policy.adminView
                    }
                    onChange={() => {
                      setPolicy(Policy.adminView);
                    }}
                    aria-label={t("adminView")}
                  />
                </FormGroup>
              </GridItem>
              <GridItem>
                <FormGroup
                  label={t("write")}
                  fieldId="kc-up-unmanaged-attribute-policy-adminEdit"
                  hasNoPaddingTop
                >
                  <Switch
                    id="kc-up-unmanaged-attributes-adminEdit"
                    data-testid="up-unmanaged-attributes-adminEdit"
                    value={
                      config.unmanagedAttributesPolicy == Policy.adminEdit
                        ? "on"
                        : "off"
                    }
                    label={t("on")}
                    labelOff={t("off")}
                    isChecked={
                      config.unmanagedAttributesPolicy == Policy.adminEdit
                    }
                    onChange={() => {
                      setPolicy(Policy.adminEdit);
                    }}
                    aria-label={t("adminEdit")}
                  />
                </FormGroup>
              </GridItem>
            </Grid>
          </FormGroup>
        )}
      </FormAccess>
    </PageSection>
  );
};
