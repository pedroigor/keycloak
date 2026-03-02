import { fetchWithError } from "@keycloak/keycloak-admin-client";
import {
  Action,
  KeycloakDataTable,
  ListEmptyState,
  useAlerts,
} from "@keycloak/keycloak-ui-shared";
import {
  AlertVariant,
  Button,
  Label,
  PageSection,
} from "@patternfly/react-core";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useAdminClient } from "../../admin-client";
import { useRealm } from "../../context/realm-context/RealmContext";
import { getAuthorizationHeaders } from "../../utils/getAuthorizationHeaders";
import { joinPath } from "../../utils/joinPath";
import { ResourceTypeEditor } from "./ResourceTypeEditor";

type SchemaExtension = {
  schema: string;
  required: boolean;
};

export type ScimResourceType = {
  id: string;
  name: string;
  description: string;
  endpoint: string;
  schema: string;
  schemaExtensions: SchemaExtension[];
  schemas: string[];
  meta?: {
    resourceType: string;
    location: string;
  };
};

export const ResourceTypesTab = () => {
  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { realm: realmName } = useRealm();
  const { addAlert, addError } = useAlerts();

  const [key, setKey] = useState(0);
  const refresh = () => setKey(key + 1);

  const [selectedResourceType, setSelectedResourceType] =
    useState<ScimResourceType>();
  const [isEditorOpen, setIsEditorOpen] = useState(false);

  const scimBaseUrl = () =>
    joinPath(adminClient.baseUrl, "realms", realmName, "scim/v2");

  const getHeaders = async () => ({
    Accept: "application/scim+json",
    "Content-Type": "application/scim+json",
    ...getAuthorizationHeaders(await adminClient.getAccessToken()),
  });

  const loader = async () => {
    try {
      const response = {
        Resources: [
          {
            id: "User",
            name: "User",
            description: "User Account",
            endpoint: "/Users",
            schema: "urn:ietf:params:scim:schemas:core:2.0:User",
            schemaExtensions: [
              {
                schema:
                  "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User",
                required: false,
              },
            ],
            schemas: ["urn:ietf:params:scim:schemas:core:2.0:ResourceType"],
          },
          {
            id: "Group",
            name: "Group",
            description: "Group",
            endpoint: "/Groups",
            schema: "urn:ietf:params:scim:schemas:core:2.0:Group",
            schemaExtensions: [],
            schemas: ["urn:ietf:params:scim:schemas:core:2.0:ResourceType"],
          },
        ],
      };
      const resources = response.Resources;
      return resources as ScimResourceType[];
    } catch {
      return [];
    }
  };

  const onEdit = (resourceType: ScimResourceType) => {
    setSelectedResourceType(resourceType);
    setIsEditorOpen(true);
  };

  const onSave = async (resourceType: ScimResourceType) => {
    try {
      await fetchWithError(
        joinPath(scimBaseUrl(), "ResourceTypes", resourceType.id),
        {
          method: "PUT",
          headers: await getHeaders(),
          body: JSON.stringify(resourceType),
        },
      );
      addAlert(t("scimResourceTypeUpdatedSuccess"), AlertVariant.success);
      setIsEditorOpen(false);
      refresh();
    } catch (error) {
      addError("scimResourceTypeSaveError", error);
    }
  };

  return (
    <PageSection variant="light" padding={{ default: "noPadding" }}>
      {isEditorOpen && selectedResourceType && (
        <ResourceTypeEditor
          resourceType={selectedResourceType}
          onSave={onSave}
          onClose={() => setIsEditorOpen(false)}
        />
      )}
      {!isEditorOpen && (
        <KeycloakDataTable
          key={key}
          isPaginated={false}
          isSearching
          searchPlaceholderKey="scimResourceTypeSearchPlaceholder"
          columns={[
            {
              name: "name",
              displayKey: "name",
              cellRenderer: (row: ScimResourceType) => (
                <Button variant="link" onClick={() => onEdit(row)}>
                  {row.name}
                </Button>
              ),
            },
            {
              name: "endpoint",
              displayKey: "scimResourceTypeEndpoint",
            },
            {
              name: "schema",
              displayKey: "scimResourceTypeSchema",
            },
            {
              name: "schemaExtensions",
              displayKey: "scimResourceTypeSchemaExtensions",
              cellRenderer: (row: ScimResourceType) => (
                <>
                  {(row.schemaExtensions || []).map((ext) => (
                    <Label key={ext.schema} className="pf-v5-u-mr-xs">
                      {ext.schema}
                    </Label>
                  ))}
                  {(!row.schemaExtensions ||
                    row.schemaExtensions.length === 0) && (
                    <span>{t("none")}</span>
                  )}
                </>
              ),
            },
          ]}
          actions={[
            {
              title: t("edit"),
              onRowClick: (resourceType) => {
                onEdit(resourceType);
              },
            } as Action<ScimResourceType>,
          ]}
          loader={loader}
          ariaLabelKey="scimResourceTypes"
          emptyState={
            <ListEmptyState
              message={t("scimResourceTypesEmpty")}
              instructions={t("scimResourceTypesEmptyInstructions")}
            />
          }
        />
      )}
    </PageSection>
  );
};
