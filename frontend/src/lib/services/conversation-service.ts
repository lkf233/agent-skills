import { nanoid } from "nanoid";
import { API_CONFIG, API_ENDPOINTS } from "@/lib/api-config";
import { requestData } from "@/lib/http-client";

type ConversationApiObject = {
  id: string;
  title: string;
  agentId?: string;
};

type ConversationMessageApiObject = {
  id: string;
  role: string;
  content: string;
  seqNo: number;
  createdAt: string;
};

type ConversationMessagePageApiObject = {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  records: ConversationMessageApiObject[];
};

type ConversationSummaryApiObject = {
  id: string;
  rangeStartSeq: number;
  rangeEndSeq: number;
  summaryText: string;
  version: number;
  createdAt: string;
};

export type ConversationResponseObject = {
  id: string;
  title: string;
  agentId?: string;
};

export type ConversationMessageResponseObject = {
  id: string;
  role: string;
  content: string;
  seqNo: number;
  createdAt: string;
};

export type ConversationMessagePageResponseObject = {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  records: ConversationMessageResponseObject[];
};

export type ConversationSummaryResponseObject = {
  id: string;
  rangeStartSeq: number;
  rangeEndSeq: number;
  summaryText: string;
  version: number;
  createdAt: string;
};

type StreamHandlers = {
  onToken?: (token: string) => void;
  onDone?: () => void;
  onError?: (message: string) => void;
};

function mapConversation(item: ConversationApiObject): ConversationResponseObject {
  return {
    id: item.id,
    title: item.title,
    agentId: item.agentId,
  };
}

function mapMessage(item: ConversationMessageApiObject): ConversationMessageResponseObject {
  return {
    id: item.id,
    role: item.role,
    content: item.content,
    seqNo: item.seqNo,
    createdAt: item.createdAt,
  };
}

function mapSummary(item: ConversationSummaryApiObject): ConversationSummaryResponseObject {
  return {
    id: item.id,
    rangeStartSeq: item.rangeStartSeq,
    rangeEndSeq: item.rangeEndSeq,
    summaryText: item.summaryText,
    version: item.version,
    createdAt: item.createdAt,
  };
}

function parseStreamPayload(raw: string): unknown {
  const value = raw.trim();
  if (!value) {
    return null;
  }
  try {
    return JSON.parse(value) as unknown;
  } catch {
    return value;
  }
}

function readToken(payload: unknown): string {
  if (typeof payload === "string") {
    return payload;
  }
  if (payload && typeof payload === "object") {
    const token = (payload as { token?: unknown }).token;
    if (typeof token === "string") {
      return token;
    }
    const content = (payload as { content?: unknown }).content;
    if (typeof content === "string") {
      return content;
    }
    const delta = (payload as { delta?: unknown }).delta;
    if (typeof delta === "string") {
      return delta;
    }
  }
  return "";
}

function readErrorMessage(payload: unknown): string {
  if (typeof payload === "string") {
    return payload;
  }
  if (payload && typeof payload === "object") {
    const message = (payload as { message?: unknown }).message;
    if (typeof message === "string") {
      return message;
    }
    const error = (payload as { error?: unknown }).error;
    if (typeof error === "string") {
      return error;
    }
  }
  return "会话流式输出失败";
}

export async function listConversations(agentId?: string) {
  const list = await requestData<ConversationApiObject[]>({
    method: "GET",
    url: API_ENDPOINTS.conversations.root,
    params: {
      agentId: agentId?.trim() || undefined,
    },
  });
  return list.map(mapConversation);
}

export async function createConversation(params: { title: string; agentId: string }) {
  const data = await requestData<ConversationApiObject>({
    method: "POST",
    url: API_ENDPOINTS.conversations.root,
    data: {
      title: params.title.trim(),
      agentId: params.agentId,
    },
  });
  return mapConversation(data);
}

export async function sendConversationMessage(conversationId: string, content: string) {
  const data = await requestData<ConversationMessageApiObject>({
    method: "POST",
    url: API_ENDPOINTS.conversations.messages(conversationId),
    data: { content: content.trim() },
  });
  return mapMessage(data);
}

export async function listConversationMessages(conversationId: string, page = 1, pageSize = 50) {
  const pageData = await requestData<ConversationMessagePageApiObject>({
    method: "GET",
    url: API_ENDPOINTS.conversations.messages(conversationId),
    params: { page, pageSize },
  });
  return {
    page: pageData.page,
    pageSize: pageData.pageSize,
    total: pageData.total,
    totalPages: pageData.totalPages,
    records: pageData.records.map(mapMessage),
  };
}

export async function listConversationSummaries(conversationId: string, limit = 10) {
  const list = await requestData<ConversationSummaryApiObject[]>({
    method: "GET",
    url: API_ENDPOINTS.conversations.summaries(conversationId),
    params: { limit },
  });
  return list.map(mapSummary);
}

export async function streamConversation(conversationId: string, content: string, handlers: StreamHandlers) {
  const token = typeof window === "undefined" ? null : localStorage.getItem("accessToken");
  const response = await fetch(`${API_CONFIG.baseURL}${API_ENDPOINTS.conversations.stream(conversationId)}`, {
    method: "POST",
    headers: {
      Accept: "text/event-stream",
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
      "x-request-id": nanoid(),
    },
    body: JSON.stringify({ content: content.trim() }),
  });
  if (!response.ok || !response.body) {
    const message = `流式请求失败(${response.status})`;
    handlers.onError?.(message);
    throw new Error(message);
  }
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const chunks = buffer.split("\n\n");
    buffer = chunks.pop() ?? "";
    for (const chunk of chunks) {
      const lines = chunk.split("\n");
      let eventName = "message";
      const dataLines: string[] = [];
      for (const line of lines) {
        if (line.startsWith("event:")) {
          eventName = line.slice(6).trim();
          continue;
        }
        if (line.startsWith("data:")) {
          dataLines.push(line.slice(5).trim());
        }
      }
      const payload = parseStreamPayload(dataLines.join("\n"));
      if (eventName === "token") {
        const tokenPart = readToken(payload);
        if (tokenPart) {
          handlers.onToken?.(tokenPart);
        }
      }
      if (eventName === "error") {
        const message = readErrorMessage(payload);
        handlers.onError?.(message);
      }
      if (eventName === "done") {
        handlers.onDone?.();
      }
    }
  }
}
