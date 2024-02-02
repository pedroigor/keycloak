import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import {
  ActionGroup,
  Button,
  FormGroup,
  NumberInput,
  Select,
  SelectOption,
  SelectVariant,
} from "@patternfly/react-core";
import { useEffect, useState } from "react";
import { Controller, FormProvider, useForm, useWatch } from "react-hook-form";
import { useTranslation } from "react-i18next";

import { FormAccess } from "../../components/form/FormAccess";
import { HelpItem } from "ui-shared";
import { convertToFormValues } from "../../util";
import { Time } from "./Time";

type BruteForceDetectionProps = {
  realm: RealmRepresentation;
  save: (realm: RealmRepresentation) => void;
};

export const BruteForceDetection = ({
  realm,
  save,
}: BruteForceDetectionProps) => {
  const { t } = useTranslation();
  const form = useForm();
  const {
    setValue,
    handleSubmit,
    control,
    formState: { isDirty },
  } = form;

  const enable = useWatch({
    control,
    name: "bruteForceProtected",
  });

  const [isBruteForceModeOpen, setIsBruteForceModeOpen] = useState(false);

  enum BruteForceMode {
    Disabled = "Disabled",
    PermanentLockout = "PermanentLockout",
    TemporaryLockout = "TemporaryLockout",
    PermanentAfterTemporaryLockout = "PermanentAfterTemporaryLockout",
  }

  const [bruteForceMode, setBruteForceMode] = useState(String);

  const bruteForceModes = [
    BruteForceMode.Disabled,
    BruteForceMode.PermanentLockout,
    BruteForceMode.TemporaryLockout,
    BruteForceMode.PermanentAfterTemporaryLockout,
  ];

  const setupForm = () => convertToFormValues(realm, setValue);
  useEffect(setupForm, []);

  return (
    <FormProvider {...form}>
      <FormAccess
        role="manage-realm"
        isHorizontal
        onSubmit={handleSubmit(save)}
      >
        <FormGroup
          label={t("bruteForceMode")}
          fieldId="kc-brute-force-mode"
          labelIcon={
            <HelpItem
              helpText={t("bruteForceModeHelpText")}
              fieldLabelId="bruteForceMode"
            />
          }
        >
          <Select
            toggleId="kc-brute-force-mode"
            onToggle={() => setIsBruteForceModeOpen(!isBruteForceModeOpen)}
            onSelect={(_, value) => {
              form.setValue(
                "bruteForceProtected",
                value !== BruteForceMode.Disabled,
              );
              form.setValue(
                "permanentLockout",
                value === BruteForceMode.PermanentLockout ||
                  value === BruteForceMode.PermanentAfterTemporaryLockout,
              );
              if (value === BruteForceMode.PermanentAfterTemporaryLockout) {
                let maxTemporaryLockouts = form.getValues(
                  "maxTemporaryLockouts",
                );

                if (maxTemporaryLockouts <= 0) {
                  maxTemporaryLockouts = 1;
                }

                form.setValue("maxTemporaryLockouts", maxTemporaryLockouts);
              }
              if (
                value === BruteForceMode.TemporaryLockout ||
                value === BruteForceMode.PermanentLockout
              ) {
                form.setValue("maxTemporaryLockouts", 0);
              }
              setBruteForceMode(value as BruteForceMode);
              setIsBruteForceModeOpen(false);
            }}
            selections={bruteForceMode}
            variant={SelectVariant.single}
            isOpen={isBruteForceModeOpen}
            aria-label={t("selectUnmanagedAttributePolicy")}
          >
            {bruteForceModes.map((mode) => (
              <SelectOption key={mode} value={mode}>
                {t(`bruteForceMode.${mode}`)}
              </SelectOption>
            ))}
          </Select>
        </FormGroup>
        {enable && (
          <>
            <FormGroup
              label={t("failureFactor")}
              labelIcon={
                <HelpItem
                  helpText={t("failureFactorHelp")}
                  fieldLabelId="failureFactor"
                />
              }
              fieldId="failureFactor"
            >
              <Controller
                name="failureFactor"
                defaultValue={0}
                control={control}
                rules={{ required: true }}
                render={({ field }) => (
                  <NumberInput
                    type="text"
                    id="failureFactor"
                    value={field.value}
                    onPlus={() => field.onChange(field.value + 1)}
                    onMinus={() => field.onChange(field.value - 1)}
                    onChange={(event) =>
                      field.onChange(
                        Number((event.target as HTMLInputElement).value),
                      )
                    }
                  />
                )}
              />
            </FormGroup>

            {bruteForceMode ==
              BruteForceMode.PermanentAfterTemporaryLockout && (
              <FormGroup
                label={t("maxTemporaryLockouts")}
                labelIcon={
                  <HelpItem
                    helpText={t("maxTemporaryLockoutsHelp")}
                    fieldLabelId="maxTemporaryLockouts"
                  />
                }
                fieldId="maxTemporaryLockouts"
                hasNoPaddingTop
              >
                <Controller
                  name="maxTemporaryLockouts"
                  defaultValue={0}
                  control={control}
                  render={({ field }) => (
                    <NumberInput
                      type="text"
                      id="maxTemporaryLockouts"
                      value={field.value}
                      onPlus={() => field.onChange(field.value + 1)}
                      onMinus={() => field.onChange(field.value - 1)}
                      onChange={(event) =>
                        field.onChange(
                          Number((event.target as HTMLInputElement).value),
                        )
                      }
                      aria-label={t("maxTemporaryLockouts")}
                    />
                  )}
                />
              </FormGroup>
            )}

            {(bruteForceMode === BruteForceMode.TemporaryLockout ||
              bruteForceMode ===
                BruteForceMode.PermanentAfterTemporaryLockout) && (
              <>
                <Time name="waitIncrementSeconds" />
                <Time name="maxFailureWaitSeconds" />
                <Time name="maxDeltaTimeSeconds" />
              </>
            )}

            <FormGroup
              label={t("quickLoginCheckMilliSeconds")}
              labelIcon={
                <HelpItem
                  helpText={t("quickLoginCheckMilliSecondsHelp")}
                  fieldLabelId="quickLoginCheckMilliSeconds"
                />
              }
              fieldId="quickLoginCheckMilliSeconds"
            >
              <Controller
                name="quickLoginCheckMilliSeconds"
                defaultValue={0}
                control={control}
                render={({ field }) => (
                  <NumberInput
                    type="text"
                    id="quickLoginCheckMilliSeconds"
                    value={field.value}
                    onPlus={() => field.onChange(field.value + 1)}
                    onMinus={() => field.onChange(field.value - 1)}
                    onChange={(event) =>
                      field.onChange(
                        Number((event.target as HTMLInputElement).value),
                      )
                    }
                  />
                )}
              />
            </FormGroup>

            <Time name="minimumQuickLoginWaitSeconds" />
          </>
        )}

        <ActionGroup>
          <Button
            variant="primary"
            type="submit"
            data-testid="brute-force-tab-save"
            isDisabled={!isDirty}
          >
            {t("save")}
          </Button>
          <Button variant="link" onClick={setupForm}>
            {t("revert")}
          </Button>
        </ActionGroup>
      </FormAccess>
    </FormProvider>
  );
};
