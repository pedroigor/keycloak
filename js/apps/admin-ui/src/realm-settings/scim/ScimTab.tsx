import { Tab, TabTitleText } from "@patternfly/react-core";
import { useTranslation } from "react-i18next";

import {
  RoutableTabs,
  useRoutableTab,
} from "../../components/routable-tabs/RoutableTabs";
import { useRealm } from "../../context/realm-context/RealmContext";
import { toScim } from "../routes/Scim";
import { ServiceProviderConfigTab } from "./ServiceProviderConfigTab";
import { SchemasTab } from "./SchemasTab";
import { ResourceTypesTab } from "./ResourceTypesTab";

export const ScimTab = () => {
  const { t } = useTranslation();
  const { realm: realmName } = useRealm();

  const serviceProviderConfigTab = useRoutableTab(
    toScim({ realm: realmName, tab: "service-provider-config" }),
  );
  const schemasTab = useRoutableTab(
    toScim({ realm: realmName, tab: "schemas" }),
  );
  const resourceTypesTab = useRoutableTab(
    toScim({ realm: realmName, tab: "resource-types" }),
  );

  return (
    <RoutableTabs
      mountOnEnter
      unmountOnExit
      defaultLocation={toScim({
        realm: realmName,
        tab: "service-provider-config",
      })}
    >
      <Tab
        id="serviceProviderConfig"
        data-testid="rs-scim-service-provider-config-tab"
        aria-label="service-provider-config-subtab"
        title={<TabTitleText>{t("scimServiceProviderConfig")}</TabTitleText>}
        {...serviceProviderConfigTab}
      >
        <ServiceProviderConfigTab />
      </Tab>
      <Tab
        id="schemas"
        data-testid="rs-scim-schemas-tab"
        aria-label="schemas-subtab"
        title={<TabTitleText>{t("scimSchemas")}</TabTitleText>}
        {...schemasTab}
      >
        <SchemasTab />
      </Tab>
      <Tab
        id="resourceTypes"
        data-testid="rs-scim-resource-types-tab"
        aria-label="resource-types-subtab"
        title={<TabTitleText>{t("scimResourceTypes")}</TabTitleText>}
        {...resourceTypesTab}
      >
        <ResourceTypesTab />
      </Tab>
    </RoutableTabs>
  );
};
