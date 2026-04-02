import { API_ENDPOINTS } from "@/lib/api-config";
import { requestData } from "@/lib/http-client";

type KnowledgeBaseApiObject = {
  id: string;
  name: string;
  description?: string;
  embeddingProvider?: string;
  embeddingModel?: string;
  status?: string;
};

export type KnowledgeBaseResponseObject = {
  kbId: string;
  name: string;
  description?: string;
  embeddingProvider?: string;
  embeddingModel?: string;
  status?: string;
};

export type CreateKnowledgeBaseParams = {
  name: string;
  description?: string;
  embeddingProvider: string;
  embeddingModel: string;
};

export type UpdateKnowledgeBaseParams = {
  name: string;
  description?: string;
  status?: string;
};

type KnowledgeBaseFileApiObject = {
  id: string;
  fileName: string;
  mimeType: string;
  sizeBytes: number;
  parseStatus: string;
  errorMessage: string;
  createdAt: string;
  recallCount: number;
};

export type KnowledgeBaseFileResponseObject = {
  fileId: string;
  fileName: string;
  mimeType: string;
  sizeBytes: number;
  parseStatus: string;
  errorMessage: string;
  createdAt: string;
  recallCount: number;
};

export type ListKnowledgeBaseFilesParams = {
  parseStatus?: string;
  fileName?: string;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  page?: number;
  pageSize?: number;
};

type KnowledgeBaseFilePageApiObject = {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  records: KnowledgeBaseFileApiObject[];
};

export type KnowledgeBaseFilePageResponseObject = {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  records: KnowledgeBaseFileResponseObject[];
};

type KnowledgeBaseFileContentApiObject = {
  fileId: string;
  fileName: string;
  mimeType: string;
  content: string;
};

export type KnowledgeBaseFileContentResponseObject = {
  fileId: string;
  fileName: string;
  mimeType: string;
  content: string;
};

function mapKnowledgeBase(item: KnowledgeBaseApiObject): KnowledgeBaseResponseObject {
  return {
    kbId: item.id,
    name: item.name,
    description: item.description,
    embeddingProvider: item.embeddingProvider,
    embeddingModel: item.embeddingModel,
    status: item.status,
  };
}

function mapKnowledgeBaseFile(item: KnowledgeBaseFileApiObject): KnowledgeBaseFileResponseObject {
  return {
    fileId: item.id,
    fileName: item.fileName,
    mimeType: item.mimeType,
    sizeBytes: item.sizeBytes,
    parseStatus: item.parseStatus,
    errorMessage: item.errorMessage,
    createdAt: item.createdAt,
    recallCount: item.recallCount,
  };
}

export async function listKnowledgeBases() {
  const list = await requestData<KnowledgeBaseApiObject[]>({
    method: "GET",
    url: API_ENDPOINTS.knowledgeBases.root,
  });
  return list.map(mapKnowledgeBase);
}

export async function createKnowledgeBase(params: CreateKnowledgeBaseParams) {
  const data = await requestData<KnowledgeBaseApiObject>({
    method: "POST",
    url: API_ENDPOINTS.knowledgeBases.root,
    data: params,
  });
  return mapKnowledgeBase(data);
}

export async function updateKnowledgeBase(kbId: string, params: UpdateKnowledgeBaseParams) {
  await requestData<void>({
    method: "PATCH",
    url: API_ENDPOINTS.knowledgeBases.detail(kbId),
    data: params,
  });
}

export async function deleteKnowledgeBase(kbId: string) {
  await requestData<void>({
    method: "DELETE",
    url: API_ENDPOINTS.knowledgeBases.detail(kbId),
  });
}

export async function listKnowledgeBaseFiles(kbId: string, params?: ListKnowledgeBaseFilesParams) {
  const pageData = await requestData<KnowledgeBaseFilePageApiObject>({
    method: "GET",
    url: API_ENDPOINTS.knowledgeBases.files(kbId),
    params,
  });
  return {
    page: pageData.page,
    pageSize: pageData.pageSize,
    total: pageData.total,
    totalPages: pageData.totalPages,
    records: pageData.records.map(mapKnowledgeBaseFile),
  };
}

export async function uploadKnowledgeBaseFile(kbId: string, file: File) {
  const formData = new FormData();
  formData.append("file", file);
  const data = await requestData<KnowledgeBaseFileApiObject>({
    method: "POST",
    url: API_ENDPOINTS.knowledgeBases.files(kbId),
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return mapKnowledgeBaseFile(data);
}

export async function getKnowledgeBaseFileContent(kbId: string, fileId: string): Promise<KnowledgeBaseFileContentResponseObject> {
  return requestData<KnowledgeBaseFileContentApiObject>({
    method: "GET",
    url: API_ENDPOINTS.knowledgeBases.fileContent(kbId, fileId),
  });
}
