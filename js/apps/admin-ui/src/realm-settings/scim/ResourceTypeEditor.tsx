import { useFetch } from "@keycloak/keycloak-ui-shared";
import {
  ActionGroup,
  Button,
  ButtonVariant,
  Divider,
  FormGroup,
  FormSelect,
  FormSelectOption,
  PageSection,
  Switch,
  TextContent,
  TextInput,
  Title,
} from "@patternfly/react-core";
import { MinusCircleIcon, PlusCircleIcon } from "@patternfly/react-icons";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { FormAccess } from "../../components/form/FormAccess";
import type { ScimResourceType } from "./ResourceTypesTab";
import type { ScimSchema } from "./SchemasTab";

type ResourceTypeEditorProps = {
  resourceType: ScimResourceType;
  onSave: (resourceType: ScimResourceType) => void;
  onClose: () => void;
};

type SchemaExtension = {
  schema: string;
  required: boolean;
};

export const ResourceTypeEditor = ({
  resourceType,
  onSave,
  onClose,
}: ResourceTypeEditorProps) => {
  const { t } = useTranslation();

  const [name, setName] = useState(resourceType.name || "");
  const [description, setDescription] = useState(
    resourceType.description || "",
  );
  const [endpoint, setEndpoint] = useState(resourceType.endpoint || "");
  const [schemaExtensions, setSchemaExtensions] = useState<SchemaExtension[]>(
    resourceType.schemaExtensions || [],
  );
  const [availableSchemas, setAvailableSchemas] = useState<ScimSchema[]>([]);

  useFetch(
    async () => {
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
        return (response.Resources || []) as ScimSchema[];
      } catch {
        return [];
      }
    },
    (schemas) => setAvailableSchemas(schemas),
    [],
  );

  const addSchemaExtension = () => {
    setSchemaExtensions([...schemaExtensions, { schema: "", required: false }]);
  };

  const removeSchemaExtension = (index: number) => {
    setSchemaExtensions(schemaExtensions.filter((_, i) => i !== index));
  };

  const updateSchemaExtension = (
    index: number,
    field: keyof SchemaExtension,
    value: any,
  ) => {
    const updated = [...schemaExtensions];
    (updated[index] as any)[field] = value;
    setSchemaExtensions(updated);
  };

  const handleSave = () => {
    const updated: ScimResourceType = {
      ...resourceType,
      name,
      description,
      endpoint,
      schemaExtensions,
    };
    onSave(updated);
  };

  return (
    <PageSection variant="light">
      <TextContent>
        <Title headingLevel="h2">{t("scimResourceTypeEdit")}</Title>
      </TextContent>
      <FormAccess role="manage-realm" isHorizontal>
        <FormGroup label={t("name")} fieldId="resourceTypeName" isRequired>
          <TextInput
            id="resourceTypeName"
            value={name}
            onChange={(_event, value) => setName(value)}
          />
        </FormGroup>
        <FormGroup label={t("description")} fieldId="resourceTypeDescription">
          <TextInput
            id="resourceTypeDescription"
            value={description}
            onChange={(_event, value) => setDescription(value)}
          />
        </FormGroup>
        <FormGroup
          label={t("scimResourceTypeEndpoint")}
          fieldId="resourceTypeEndpoint"
        >
          <TextInput
            id="resourceTypeEndpoint"
            value={endpoint}
            onChange={(_event, value) => setEndpoint(value)}
          />
        </FormGroup>
        <FormGroup
          label={t("scimResourceTypeCoreSchema")}
          fieldId="resourceTypeCoreSchema"
        >
          <TextInput
            id="resourceTypeCoreSchema"
            value={resourceType.schema}
            isDisabled
          />
        </FormGroup>

        <Divider />

        <TextContent className="pf-v5-u-mt-md pf-v5-u-mb-md">
          <Title headingLevel="h3">
            {t("scimResourceTypeSchemaExtensions")}
          </Title>
        </TextContent>

        {schemaExtensions.map((ext, index) => (
          <div
            key={index}
            className="pf-v5-u-mb-md"
            style={{
              border: "1px solid var(--pf-v5-global--BorderColor--100)",
              padding: "1rem",
              borderRadius: "3px",
            }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "0.5rem",
              }}
            >
              <Title headingLevel="h4">
                {ext.schema || t("scimResourceTypeNewExtension")}
              </Title>
              <Button
                variant="plain"
                onClick={() => removeSchemaExtension(index)}
                aria-label={t("remove")}
              >
                <MinusCircleIcon />
              </Button>
            </div>
            <FormGroup
              label={t("scimResourceTypeExtensionSchema")}
              fieldId={`ext-schema-${index}`}
              isRequired
            >
              {availableSchemas.length > 0 ? (
                <FormSelect
                  id={`ext-schema-${index}`}
                  value={ext.schema}
                  onChange={(_event, value) =>
                    updateSchemaExtension(index, "schema", value)
                  }
                >
                  <FormSelectOption value="" label={t("scimSelectSchema")} />
                  {availableSchemas
                    .filter(
                      (s) =>
                        s.id !== resourceType.schema &&
                        !schemaExtensions.some(
                          (e, i) => i !== index && e.schema === s.id,
                        ),
                    )
                    .map((s) => (
                      <FormSelectOption
                        key={s.id}
                        value={s.id}
                        label={s.name || s.id}
                      />
                    ))}
                </FormSelect>
              ) : (
                <TextInput
                  id={`ext-schema-${index}`}
                  value={ext.schema}
                  onChange={(_event, value) =>
                    updateSchemaExtension(index, "schema", value)
                  }
                />
              )}
            </FormGroup>
            <FormGroup
              label={t("required")}
              fieldId={`ext-required-${index}`}
              hasNoPaddingTop
            >
              <Switch
                id={`ext-required-${index}`}
                label={t("yes")}
                labelOff={t("no")}
                isChecked={ext.required}
                onChange={(_event, checked) =>
                  updateSchemaExtension(index, "required", checked)
                }
              />
            </FormGroup>
          </div>
        ))}

        <Button
          variant="link"
          icon={<PlusCircleIcon />}
          onClick={addSchemaExtension}
          data-testid="add-schema-extension"
        >
          {t("scimResourceTypeAddExtension")}
        </Button>

        <ActionGroup className="pf-v5-u-mt-lg">
          <Button
            data-testid="save-scim-resource-type"
            variant="primary"
            onClick={handleSave}
            isDisabled={!name}
          >
            {t("save")}
          </Button>
          <Button
            data-testid="cancel-scim-resource-type"
            variant={ButtonVariant.link}
            onClick={onClose}
          >
            {t("cancel")}
          </Button>
        </ActionGroup>
      </FormAccess>
    </PageSection>
  );
};
