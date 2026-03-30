export const API_CONFIG = {
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://8.146.230.205:8080/api",
  timeout: 15000,
};

export const API_ENDPOINTS = {
  auth: {
    login: "/auth/login",
    register: "/auth/register",
  },
  conversations: {
    root: "/conversations",
  },
  knowledgeBases: {
    root: "/knowledge-bases",
    detail: (kbId: string) => `/knowledge-bases/${kbId}`,
    files: (kbId: string) => `/knowledge-bases/${kbId}/files`,
  },
};
