import type { ServerInfoRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/serverInfoRepesentation";
import { PropsWithChildren, useState } from "react";
import { createNamedContext, useRequiredContext } from "ui-shared";

import { adminClient } from "../../admin-client";
import { sortProviders } from "../../util";
import { useFetch } from "../../utils/useFetch";
import {useRealm} from "../realm-context/RealmContext";

export const ServerInfoContext = createNamedContext<
  ServerInfoRepresentation | undefined
>("ServerInfoContext", undefined);

export const useServerInfo = () => useRequiredContext(ServerInfoContext);

export const useLoginProviders = () =>
  sortProviders(useServerInfo().providers!["login-protocol"].providers);

export const ServerInfoProvider = ({ children }: PropsWithChildren) => {
  const [serverInfo, setServerInfo] = useState<ServerInfoRepresentation>({});
  const { realm: realmName } = useRealm();

  useFetch(() => adminClient.serverInfo.find({ realm: realmName }), setServerInfo, [realmName]);

  return (
    <ServerInfoContext.Provider value={serverInfo}>
      {children}
    </ServerInfoContext.Provider>
  );
};
