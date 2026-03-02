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
  ButtonVariant,
  Label,
  PageSection,
} from "@patternfly/react-core";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useAdminClient } from "../../admin-client";
import { useConfirmDialog } from "../../components/confirm-dialog/ConfirmDialog";
import { useRealm } from "../../context/realm-context/RealmContext";
import { getAuthorizationHeaders } from "../../utils/getAuthorizationHeaders";
import { joinPath } from "../../utils/joinPath";
import { SchemaEditor } from "./SchemaEditor";

export type ScimSchemaAttribute = {
  name: string;
  type: string;
  multiValued: boolean;
  description: string;
  required: boolean;
  canonicalValues?: string[];
  caseExact: boolean;
  mutability: string;
  returned: string;
  uniqueness: string;
  subAttributes?: ScimSchemaAttribute[];
  referenceTypes?: string[];
};

export type ScimSchema = {
  id: string;
  name: string;
  description: string;
  attributes: ScimSchemaAttribute[];
  schemas: string[];
  meta?: {
    resourceType: string;
    location: string;
  };
};

export const SchemasTab = () => {
  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { realm: realmName } = useRealm();
  const { addAlert, addError } = useAlerts();

  const [key, setKey] = useState(0);
  const refresh = () => setKey(key + 1);

  const [selectedSchema, setSelectedSchema] = useState<ScimSchema>();
  const [isEditorOpen, setIsEditorOpen] = useState(false);
  const [editorMode, setEditorMode] = useState<"create" | "edit">("create");

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
            id: "urn:ietf:params:scim:schemas:core:2.0:User",
            name: "User",
            description: "Core User Schema",
            attributes: [
              {
                name: "userName",
                type: "string",
                multiValued: false,
                description: "Unique identifier for the User",
                required: true,
                caseExact: false,
                mutability: "readWrite",
                returned: "always",
                uniqueness: "server",
              },
              {
                name: "name",
                type: "complex",
                multiValued: false,
                description: "The components of the user's real name",
                required: false,
                caseExact: false,
                mutability: "readWrite",
                returned: "default",
                uniqueness: "none",
                subAttributes: [
                  {
                    name: "formatted",
                    type: "string",
                    multiValued: false,
                    description:
                      "The full name, including all name parts, formatted for display",
                    required: false,
                    caseExact: false,
                    mutability: "readWrite",
                    returned: "default",
                    uniqueness: "none",
                  },
                  {
                    name: "familyName",
                    type: "string",
                    multiValued: false,
                    description: "The family name of the user",
                    required: false,
                    caseExact: false,
                    mutability: "readWrite",
                    returned: "default",
                    uniqueness: "none",
                  },
                  {
                    name: "givenName",
                    type: "string",
                    multiValued: false,
                    description: "The given name of the user",
                    required: false,
                    caseExact: false,
                    mutability: "readWrite",
                    returned: "default",
                    uniqueness: "none",
                  },
                ],
              },
            ],
            schemas: ["urn:ietf:params:scim:schemas:core:2.0:Schema"],
          },
        ],
      };
      const resources = response.Resources;
      return resources as ScimSchema[];
    } catch {
      return [];
    }
  };

  const [toggleDeleteDialog, DeleteConfirm] = useConfirmDialog({
    titleKey: "scimSchemaDeleteConfirmTitle",
    messageKey: t("scimSchemaDeleteConfirmMessage", {
      name: selectedSchema?.name || "",
    }),
    continueButtonLabel: "delete",
    continueButtonVariant: ButtonVariant.danger,
    onConfirm: async () => {
      try {
        await fetchWithError(
          joinPath(scimBaseUrl(), "Schemas", selectedSchema!.id),
          {
            method: "DELETE",
            headers: await getHeaders(),
          },
        );
        addAlert(t("scimSchemaDeletedSuccess"), AlertVariant.success);
        refresh();
      } catch (error) {
        addError("scimSchemaDeleteError", error);
      }
    },
  });

  const onCreateOrEdit = (schema?: ScimSchema) => {
    if (schema) {
      setEditorMode("edit");
      setSelectedSchema(schema);
    } else {
      setEditorMode("create");
      setSelectedSchema(undefined);
    }
    setIsEditorOpen(true);
  };

  const onSave = async (schema: ScimSchema) => {
    try {
      if (editorMode === "create") {
        await fetchWithError(joinPath(scimBaseUrl(), "Schemas"), {
          method: "POST",
          headers: await getHeaders(),
          body: JSON.stringify(schema),
        });
        addAlert(t("scimSchemaCreatedSuccess"), AlertVariant.success);
      } else {
        await fetchWithError(joinPath(scimBaseUrl(), "Schemas", schema.id), {
          method: "PUT",
          headers: await getHeaders(),
          body: JSON.stringify(schema),
        });
        addAlert(t("scimSchemaUpdatedSuccess"), AlertVariant.success);
      }
      setIsEditorOpen(false);
      refresh();
    } catch (error) {
      addError("scimSchemaSaveError", error);
    }
  };

  return (
    <PageSection variant="light" padding={{ default: "noPadding" }}>
      <DeleteConfirm />
      {isEditorOpen && (
        <SchemaEditor
          schema={selectedSchema}
          mode={editorMode}
          onSave={onSave}
          onClose={() => setIsEditorOpen(false)}
        />
      )}
      {!isEditorOpen && (
        <KeycloakDataTable
          key={key}
          isPaginated={false}
          toolbarItem={
            <Button
              data-testid="create-scim-schema"
              onClick={() => onCreateOrEdit()}
            >
              {t("scimSchemaCreate")}
            </Button>
          }
          isSearching
          searchPlaceholderKey="scimSchemaSearchPlaceholder"
          columns={[
            {
              name: "name",
              displayKey: "name",
              cellRenderer: (row: ScimSchema) => (
                <Button variant="link" onClick={() => onCreateOrEdit(row)}>
                  {row.name}
                </Button>
              ),
            },
            {
              name: "id",
              displayKey: "id",
            },
            {
              name: "description",
              displayKey: "description",
            },
            {
              name: "attributes",
              displayKey: "scimSchemaAttributes",
              cellRenderer: (row: ScimSchema) => (
                <Label>{row.attributes?.length ?? 0}</Label>
              ),
            },
          ]}
          actions={[
            {
              title: t("edit"),
              onRowClick: (schema) => {
                onCreateOrEdit(schema);
              },
            } as Action<ScimSchema>,
            {
              title: t("delete"),
              onRowClick: (schema) => {
                setSelectedSchema(schema);
                toggleDeleteDialog();
              },
            } as Action<ScimSchema>,
          ]}
          loader={loader}
          ariaLabelKey="scimSchemas"
          emptyState={
            <ListEmptyState
              message={t("scimSchemasEmpty")}
              instructions={t("scimSchemasEmptyInstructions")}
              primaryActionText={t("scimSchemaCreate")}
              onPrimaryAction={() => onCreateOrEdit()}
            />
          }
        />
      )}
    </PageSection>
  );
};
