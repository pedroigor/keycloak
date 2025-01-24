import { useTranslation } from "react-i18next";
import { FormGroup, Radio } from "@patternfly/react-core";
import { HelpItem, useFetch } from "@keycloak/keycloak-ui-shared";
import { useFormContext } from "react-hook-form";
import {useEffect, useState} from "react";
import { UserSelect } from "../../../components/users/UserSelect";
import { useAdminClient } from "../../../admin-client";
import { useParams } from "react-router-dom";
import { PermissionConfigurationDetailsParams } from "../../routes/PermissionConfigurationDetails";

export const Users = () => {
  const { t } = useTranslation();
  const form = useFormContext();
  const [isSpecificUsers, setIsSpecificUsers] = useState(form.getValues("resources").length > 0);

  console.log("resources", form.getValues("resources"));

  return (
    <>
      <FormGroup
        label={t("applyPermissionTo")}
        labelIcon={
          <HelpItem
            helpText={t("applyPermissionToHelpText")}
            fieldLabelId="apply-permission-to"
          />
        }
        fieldId="applyPermissionTo"
        hasNoPaddingTop
        isRequired
      >
        <Radio
          id="allUsers"
          data-testid="allUsers"
          isChecked={!isSpecificUsers}
          name="applyPermissionTo"
          label={t("allUsers")}
          onChange={() => {
            setIsSpecificUsers(false);
            form.setValue("resources", []);
          }}
          className="pf-v5-u-mb-md"
        />
        <Radio
          id="specificUsers"
          data-testid="specificUsers"
          isChecked={isSpecificUsers}
          name="applyPermissionTo"
          label={t("specificUsers")}
          onChange={() => {
            setIsSpecificUsers(true);
            form.setValue("resources", []);
          }}
          className="pf-v5-u-mb-md"
        />
      </FormGroup>
      {isSpecificUsers && (
        <UserSelect
          name="resources"
          helpText={t("permissionUsersHelpText")}
          defaultValue={[]}
          variant="typeaheadMulti"
        />
      )}
    </>
  );
};
