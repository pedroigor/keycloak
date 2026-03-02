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
  TextContent,
  TextInput,
  Title,
} from "@patternfly/react-core";
import { MinusCircleIcon, PlusCircleIcon } from "@patternfly/react-icons";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { FormAccess } from "../../components/form/FormAccess";
import type { ScimSchema, ScimSchemaAttribute } from "./SchemasTab";

type SchemaEditorProps = {
  schema?: ScimSchema;
  mode: "create" | "edit";
  onSave: (schema: ScimSchema) => void;
  onClose: () => void;
};

const ATTRIBUTE_TYPES = [
  "string",
  "boolean",
  "decimal",
  "integer",
  "dateTime",
  "binary",
  "reference",
  "complex",
];

const MUTABILITY_VALUES = ["readOnly", "readWrite", "immutable", "writeOnly"];

const RETURNED_VALUES = ["always", "never", "default", "request"];

const UNIQUENESS_VALUES = ["none", "server", "global"];

const CORE_SCHEMA_OPTIONS = [
  {
    value: "urn:ietf:params:scim:schemas:core:2.0:User",
    label: "User",
  },
  {
    value: "urn:ietf:params:scim:schemas:core:2.0:Group",
    label: "Group",
  },
];

type UserProfileAttribute = {
  name: string;
  displayName?: string;
  multivalued?: boolean;
};

const emptyAttribute = (): ScimSchemaAttribute => ({
  name: "",
  type: "string",
  multiValued: false,
  description: "",
  required: false,
  caseExact: false,
  mutability: "readWrite",
  returned: "default",
  uniqueness: "none",
});

export const SchemaEditor = ({
  schema,
  mode,
  onSave,
  onClose,
}: SchemaEditorProps) => {
  const { t } = useTranslation();

  const [name, setName] = useState(schema?.name || "");
  const [id, setId] = useState(schema?.id || "");
  const [description, setDescription] = useState(schema?.description || "");
  const [attributes, setAttributes] = useState<ScimSchemaAttribute[]>(
    schema?.attributes || [],
  );
  const [coreSchema, setCoreSchema] = useState(CORE_SCHEMA_OPTIONS[0].value);
  const [userProfileAttributes, setUserProfileAttributes] = useState<
    UserProfileAttribute[]
  >([]);

  const isUserCoreSchema =
    coreSchema === "urn:ietf:params:scim:schemas:core:2.0:User";

  useFetch(
    async () => {
      if (!isUserCoreSchema) return [];
      try {
        const data = {
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
        };
        return (data.attributes || []).map((attr: any) => ({
          name: attr.name,
          displayName: attr.displayName || attr.name,
          multivalued: attr.multivalued || false,
        }));
      } catch {
        return [];
      }
    },
    (attrs) => setUserProfileAttributes(attrs),
    [coreSchema],
  );

  const addAttribute = () => {
    setAttributes([...attributes, emptyAttribute()]);
  };

  const addFromUserProfile = (upAttr: UserProfileAttribute) => {
    const newAttr: ScimSchemaAttribute = {
      name: upAttr.name,
      type: "string",
      multiValued: upAttr.multivalued || false,
      description: upAttr.displayName || upAttr.name,
      required: false,
      caseExact: false,
      mutability: "readWrite",
      returned: "default",
      uniqueness: "none",
    };
    setAttributes([...attributes, newAttr]);
  };

  const removeAttribute = (index: number) => {
    setAttributes(attributes.filter((_, i) => i !== index));
  };

  const updateAttribute = (
    index: number,
    field: keyof ScimSchemaAttribute,
    value: any,
  ) => {
    const updated = [...attributes];
    (updated[index] as any)[field] = value;
    setAttributes(updated);
  };

  const handleSave = () => {
    const schemaToSave: ScimSchema = {
      id: mode === "edit" ? schema!.id : id,
      name,
      description,
      attributes,
      schemas: ["urn:ietf:params:scim:schemas:core:2.0:Schema"],
    };
    onSave(schemaToSave);
  };

  return (
    <PageSection variant="light">
      <TextContent>
        <Title headingLevel="h2">
          {mode === "create" ? t("scimSchemaCreate") : t("scimSchemaEdit")}
        </Title>
      </TextContent>
      <FormAccess role="manage-realm" isHorizontal>
        {mode === "create" && (
          <FormGroup
            label={t("scimSchemaCoreSchema")}
            fieldId="coreSchema"
            isRequired
          >
            <FormSelect
              id="coreSchema"
              value={coreSchema}
              onChange={(_event, value) => setCoreSchema(value)}
            >
              {CORE_SCHEMA_OPTIONS.map((option) => (
                <FormSelectOption
                  key={option.value}
                  value={option.value}
                  label={option.label}
                />
              ))}
            </FormSelect>
          </FormGroup>
        )}
        <FormGroup label={t("scimSchemaId")} fieldId="schemaId" isRequired>
          <TextInput
            id="schemaId"
            value={id}
            onChange={(_event, value) => setId(value)}
            isDisabled={mode === "edit"}
            placeholder="urn:ietf:params:scim:schemas:extension:..."
          />
        </FormGroup>
        <FormGroup label={t("name")} fieldId="schemaName" isRequired>
          <TextInput
            id="schemaName"
            value={name}
            onChange={(_event, value) => setName(value)}
          />
        </FormGroup>
        <FormGroup label={t("description")} fieldId="schemaDescription">
          <TextInput
            id="schemaDescription"
            value={description}
            onChange={(_event, value) => setDescription(value)}
          />
        </FormGroup>

        <Divider />

        <TextContent className="pf-v5-u-mt-md pf-v5-u-mb-md">
          <Title headingLevel="h3">{t("scimSchemaAttributes")}</Title>
        </TextContent>

        {isUserCoreSchema &&
          mode === "create" &&
          userProfileAttributes.length > 0 && (
            <FormGroup
              label={t("scimSchemaAddFromUserProfile")}
              fieldId="addFromUserProfile"
            >
              <FormSelect
                id="addFromUserProfile"
                value=""
                onChange={(_event, value) => {
                  if (value) {
                    const upAttr = userProfileAttributes.find(
                      (a) => a.name === value,
                    );
                    if (upAttr) addFromUserProfile(upAttr);
                  }
                }}
              >
                <FormSelectOption
                  value=""
                  label={t("scimSchemaSelectUserProfileAttribute")}
                />
                {userProfileAttributes
                  .filter(
                    (upAttr) => !attributes.some((a) => a.name === upAttr.name),
                  )
                  .map((upAttr) => (
                    <FormSelectOption
                      key={upAttr.name}
                      value={upAttr.name}
                      label={upAttr.displayName || upAttr.name}
                    />
                  ))}
              </FormSelect>
            </FormGroup>
          )}

        {attributes.map((attr, index) => (
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
                {attr.name || t("scimSchemaNewAttribute")}
              </Title>
              <Button
                variant="plain"
                onClick={() => removeAttribute(index)}
                aria-label={t("remove")}
              >
                <MinusCircleIcon />
              </Button>
            </div>
            <FormGroup
              label={t("name")}
              fieldId={`attr-name-${index}`}
              isRequired
            >
              <TextInput
                id={`attr-name-${index}`}
                value={attr.name}
                onChange={(_event, value) =>
                  updateAttribute(index, "name", value)
                }
              />
            </FormGroup>
            <FormGroup label={t("type")} fieldId={`attr-type-${index}`}>
              <FormSelect
                id={`attr-type-${index}`}
                value={attr.type}
                onChange={(_event, value) =>
                  updateAttribute(index, "type", value)
                }
              >
                {ATTRIBUTE_TYPES.map((type) => (
                  <FormSelectOption key={type} value={type} label={type} />
                ))}
              </FormSelect>
            </FormGroup>
            <FormGroup label={t("description")} fieldId={`attr-desc-${index}`}>
              <TextInput
                id={`attr-desc-${index}`}
                value={attr.description || ""}
                onChange={(_event, value) =>
                  updateAttribute(index, "description", value)
                }
              />
            </FormGroup>
            <FormGroup
              label={t("scimSchemaAttrMultiValued")}
              fieldId={`attr-multi-${index}`}
            >
              <FormSelect
                id={`attr-multi-${index}`}
                value={String(attr.multiValued)}
                onChange={(_event, value) =>
                  updateAttribute(index, "multiValued", value === "true")
                }
              >
                <FormSelectOption value="false" label="false" />
                <FormSelectOption value="true" label="true" />
              </FormSelect>
            </FormGroup>
            <FormGroup
              label={t("scimSchemaAttrRequired")}
              fieldId={`attr-required-${index}`}
            >
              <FormSelect
                id={`attr-required-${index}`}
                value={String(attr.required)}
                onChange={(_event, value) =>
                  updateAttribute(index, "required", value === "true")
                }
              >
                <FormSelectOption value="false" label="false" />
                <FormSelectOption value="true" label="true" />
              </FormSelect>
            </FormGroup>
            <FormGroup
              label={t("scimSchemaAttrCaseExact")}
              fieldId={`attr-case-${index}`}
            >
              <FormSelect
                id={`attr-case-${index}`}
                value={String(attr.caseExact)}
                onChange={(_event, value) =>
                  updateAttribute(index, "caseExact", value === "true")
                }
              >
                <FormSelectOption value="false" label="false" />
                <FormSelectOption value="true" label="true" />
              </FormSelect>
            </FormGroup>
            <FormGroup
              label={t("scimSchemaAttrMutability")}
              fieldId={`attr-mutability-${index}`}
            >
              <FormSelect
                id={`attr-mutability-${index}`}
                value={attr.mutability}
                onChange={(_event, value) =>
                  updateAttribute(index, "mutability", value)
                }
              >
                {MUTABILITY_VALUES.map((v) => (
                  <FormSelectOption key={v} value={v} label={v} />
                ))}
              </FormSelect>
            </FormGroup>
            <FormGroup
              label={t("scimSchemaAttrReturned")}
              fieldId={`attr-returned-${index}`}
            >
              <FormSelect
                id={`attr-returned-${index}`}
                value={attr.returned}
                onChange={(_event, value) =>
                  updateAttribute(index, "returned", value)
                }
              >
                {RETURNED_VALUES.map((v) => (
                  <FormSelectOption key={v} value={v} label={v} />
                ))}
              </FormSelect>
            </FormGroup>
            <FormGroup
              label={t("scimSchemaAttrUniqueness")}
              fieldId={`attr-uniqueness-${index}`}
            >
              <FormSelect
                id={`attr-uniqueness-${index}`}
                value={attr.uniqueness}
                onChange={(_event, value) =>
                  updateAttribute(index, "uniqueness", value)
                }
              >
                {UNIQUENESS_VALUES.map((v) => (
                  <FormSelectOption key={v} value={v} label={v} />
                ))}
              </FormSelect>
            </FormGroup>
          </div>
        ))}

        <Button
          variant="link"
          icon={<PlusCircleIcon />}
          onClick={addAttribute}
          data-testid="add-scim-schema-attribute"
        >
          {t("scimSchemaAddAttribute")}
        </Button>

        <ActionGroup className="pf-v5-u-mt-lg">
          <Button
            data-testid="save-scim-schema"
            variant="primary"
            onClick={handleSave}
            isDisabled={!name || !id}
          >
            {t("save")}
          </Button>
          <Button
            data-testid="cancel-scim-schema"
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
