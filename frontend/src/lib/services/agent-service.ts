import { API_ENDPOINTS } from "@/lib/api-config";
import { requestData } from "@/lib/http-client";

type AgentApiObject = {
  id: string;
  name: string;
  description?: string;
  systemPrompt: string;
  avatarUrl?: string;
  status: string;
  toolIds?: string[];
  knowledgeBaseIds?: string[];
  updatedAt: string;
};

export type AgentResponseObject = {
  id: string;
  name: string;
  description: string;
  systemPrompt: string;
  avatarUrl: string;
  status: string;
  toolIds: string[];
  knowledgeBaseIds: string[];
  updatedAt: string;
};

export type CreateAgentParams = {
  name: string;
  description?: string;
  systemPrompt: string;
  avatarUrl?: string;
  toolIds: string[];
  knowledgeBaseIds: string[];
};

export type UpdateAgentParams = {
  name?: string;
  description?: string;
  systemPrompt?: string;
  avatarUrl?: string;
};

function mapAgent(item: AgentApiObject): AgentResponseObject {
  return {
    id: item.id,
    name: item.name,
    description: item.description ?? "",
    systemPrompt: item.systemPrompt ?? "",
    avatarUrl: item.avatarUrl ?? "",
    status: item.status,
    toolIds: item.toolIds ?? [],
    knowledgeBaseIds: item.knowledgeBaseIds ?? [],
    updatedAt: item.updatedAt,
  };
}

export async function listAgents() {
  const list = await requestData<AgentApiObject[]>({
    method: "GET",
    url: API_ENDPOINTS.agents.root,
  });
  return list.map(mapAgent);
}

export async function createAgent(params: CreateAgentParams) {
  const data = await requestData<AgentApiObject>({
    method: "POST",
    url: API_ENDPOINTS.agents.root,
    data: {
      name: params.name.trim(),
      description: params.description?.trim() ?? "",
      systemPrompt: params.systemPrompt.trim(),
      avatarUrl: params.avatarUrl?.trim() ?? "",
      toolIds: params.toolIds,
      knowledgeBaseIds: params.knowledgeBaseIds,
    },
  });
  return mapAgent(data);
}

export async function updateAgent(agentId: string, params: UpdateAgentParams) {
  await requestData<void>({
    method: "PATCH",
    url: API_ENDPOINTS.agents.detail(agentId),
    data: {
      name: params.name?.trim(),
      description: params.description?.trim(),
      systemPrompt: params.systemPrompt?.trim(),
      avatarUrl: params.avatarUrl?.trim() ?? "",
    },
  });
}

export async function deleteAgent(agentId: string) {
  await requestData<void>({
    method: "DELETE",
    url: API_ENDPOINTS.agents.detail(agentId),
  });
}
