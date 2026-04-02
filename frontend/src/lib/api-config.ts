export const API_CONFIG = {
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api",
  timeout: 15000,
};

export const API_ENDPOINTS = {
  auth: {
    login: "/auth/login",
    register: "/auth/register",
  },
  tools: {
    root: "/tools",
    detail: (toolId: string) => `/tools/${toolId}`,
    test: (toolId: string) => `/tools/${toolId}/test`,
    remoteTools: (toolId: string) => `/tools/${toolId}/remote-tools`,
  },
  agents: {
    root: "/agents",
    detail: (agentId: string) => `/agents/${agentId}`,
  },
  conversations: {
    root: "/conversations",
    detail: (conversationId: string) => `/conversations/${conversationId}`,
    messages: (conversationId: string) => `/conversations/${conversationId}/messages`,
    summaries: (conversationId: string) => `/conversations/${conversationId}/summaries`,
    stream: (conversationId: string) => `/conversations/${conversationId}/stream`,
  },
  knowledgeBases: {
    root: "/knowledge-bases",
    detail: (kbId: string) => `/knowledge-bases/${kbId}`,
    files: (kbId: string) => `/knowledge-bases/${kbId}/files`,
    fileContent: (kbId: string, fileId: string) => `/knowledge-bases/${kbId}/files/${fileId}/content`,
  },
};
