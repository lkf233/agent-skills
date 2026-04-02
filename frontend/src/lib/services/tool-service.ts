import { API_ENDPOINTS } from "@/lib/api-config";
import { requestData } from "@/lib/http-client";

type JsonObject = Record<string, unknown>;

type ToolApiObject = {
  id: string;
  name: string;
  description: string;
  toolType: string;
  status: string;
  configJson: JsonObject;
  authConfigJson: JsonObject;
  updatedAt: string;
};

export type ToolResponseObject = {
  id: string;
  name: string;
  description: string;
  toolType: string;
  status: string;
  configJson: JsonObject;
  authConfigJson: JsonObject;
  updatedAt: string;
};

export type ListToolsParams = {
  keyword?: string;
  toolType?: string;
  status?: string;
};

export type CreateToolParams = {
  name: string;
  description?: string;
  status?: string;
  endpoint: string;
  bearerToken?: string;
  apiKeyHeaderName?: string;
  apiKey?: string;
  customHeaders?: JsonObject;
};

export type ToolTestResponseObject = {
  connected: boolean;
  message: string;
  rawResponseJson: unknown;
};

export type ToolRemoteToolsResponseObject = {
  validMcpService: boolean;
  message: string;
  toolsJson: unknown[];
  rawResponseJson: unknown;
};

function mapTool(item: ToolApiObject): ToolResponseObject {
  return {
    id: item.id,
    name: item.name,
    description: item.description,
    toolType: item.toolType,
    status: item.status,
    configJson: item.configJson ?? {},
    authConfigJson: item.authConfigJson ?? {},
    updatedAt: item.updatedAt,
  };
}

export async function listTools(params?: ListToolsParams) {
  const list = await requestData<ToolApiObject[]>({
    method: "GET",
    url: API_ENDPOINTS.tools.root,
    params,
  });
  return list.map(mapTool);
}

export async function createTool(params: CreateToolParams) {
  const authConfigJson: JsonObject = {};
  if (params.bearerToken?.trim()) {
    authConfigJson.bearerToken = params.bearerToken.trim();
  }
  if (params.apiKeyHeaderName?.trim() && params.apiKey?.trim()) {
    authConfigJson.apiKeyHeaderName = params.apiKeyHeaderName.trim();
    authConfigJson.apiKey = params.apiKey.trim();
  }
  if (params.customHeaders && Object.keys(params.customHeaders).length > 0) {
    authConfigJson.customHeaders = params.customHeaders;
  }
  const data = await requestData<ToolApiObject>({
    method: "POST",
    url: API_ENDPOINTS.tools.root,
    data: {
      name: params.name.trim(),
      description: params.description?.trim() ?? "",
      toolType: "REMOTE_MCP",
      status: params.status ?? "ACTIVE",
      configJson: {
        endpoint: params.endpoint.trim(),
      },
      authConfigJson,
    },
  });
  return mapTool(data);
}

export async function deleteTool(toolId: string) {
  await requestData<void>({
    method: "DELETE",
    url: API_ENDPOINTS.tools.detail(toolId),
  });
}

export async function testTool(toolId: string) {
  return requestData<ToolTestResponseObject>({
    method: "POST",
    url: API_ENDPOINTS.tools.test(toolId),
  });
}

export async function listRemoteTools(toolId: string) {
  const data = await requestData<ToolRemoteToolsResponseObject>({
    method: "GET",
    url: API_ENDPOINTS.tools.remoteTools(toolId),
  });
  return {
    ...data,
    toolsJson: Array.isArray(data.toolsJson) ? data.toolsJson : [],
  };
}
