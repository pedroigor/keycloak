import { lazy } from "react";
import type { Path } from "react-router-dom";
import { generateEncodedPath } from "../../utils/generateEncodedPath";
import type { AppRouteObject } from "../../routes";

export type ScimSubTab =
  | "service-provider-config"
  | "schemas"
  | "resource-types";

export type ScimParams = {
  realm: string;
  tab: ScimSubTab;
};

const RealmSettingsSection = lazy(() => import("../RealmSettingsSection"));

export const ScimRoute: AppRouteObject = {
  path: "/:realm/realm-settings/scim/:tab",
  element: <RealmSettingsSection />,
  breadcrumb: (t) => t("scim"),
  handle: {
    access: "view-realm",
  },
};

export const toScim = (params: ScimParams): Partial<Path> => ({
  pathname: generateEncodedPath(ScimRoute.path, params),
});
