import type UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import { PageSection, PageSectionVariants } from "@patternfly/react-core";
import { UseFormReturn, useFormContext } from "react-hook-form";

import {
  AttributeForm,
  AttributesForm,
} from "../components/key-value-form/AttributeForm";
import { UserFormFields, toUserFormFields } from "./form-state";
import useIsFeatureEnabled, { Feature } from "../utils/useIsFeatureEnabled";
import {
  Policy,
  UserProfileConfig,
} from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";

type UserAttributesProps = {
  user: UserRepresentation;
  save: (user: UserFormFields) => void;
  upConfig?: UserProfileConfig;
};

export const UserAttributes = ({
  user,
  save,
  upConfig,
}: UserAttributesProps) => {
  const form = useFormContext<UserFormFields>();
  const isFeatureEnabled = useIsFeatureEnabled();
  const userProfileEnabled = isFeatureEnabled(Feature.DeclarativeUserProfile);

  return (
    <PageSection variant={PageSectionVariants.light}>
      <AttributesForm
        form={form as UseFormReturn<AttributeForm>}
        save={save}
        fineGrainedAccess={user.access?.manage}
        reset={() =>
          form.reset({
            ...form.getValues(),
            attributes: toUserFormFields(user, userProfileEnabled).attributes,
          })
        }
        name={userProfileEnabled ? "unmanagedAttributes" : "attributes"}
        readOnly={Policy.adminView == upConfig?.unmanagedAttributesPolicy}
      />
    </PageSection>
  );
};
