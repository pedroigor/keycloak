import Resource from "./resource.js";
import type { ServerInfoRepresentation } from "../defs/serverInfoRepesentation.js";
import type KeycloakAdminClient from "../index.js";

export class ServerInfo extends Resource<{ realm?: string }> {
  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/serverinfo",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl
    });
  }

  public find = this.makeRequest<{ realm: string }, ServerInfoRepresentation>({
    method: "GET",
    path: "/",
  });
}
