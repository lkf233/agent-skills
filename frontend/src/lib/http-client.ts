import axios from "axios";
import { nanoid } from "nanoid";
import { toast } from "sonner";
import { API_CONFIG } from "@/lib/api-config";
import { ApiResponseObject } from "@/lib/types/api/common";

const client = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
});

client.interceptors.request.use((config) => {
  const token = typeof window === "undefined" ? null : localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers["x-request-id"] = nanoid();
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && typeof window !== "undefined") {
      localStorage.removeItem("accessToken");
      window.location.href = "/login";
    }
    if (error.response?.status >= 500) {
      toast.error("系统繁忙，请稍后重试");
    }
    return Promise.reject(error);
  }
);

export const request = {
  get<T>(url: string, config?: Parameters<typeof client.get>[1]) {
    return client.get<T>(url, config).then((response) => response.data);
  },
  post<T, D = unknown>(url: string, data?: D, config?: Parameters<typeof client.post>[2]) {
    return client.post<T>(url, data, config).then((response) => response.data);
  },
  put<T, D = unknown>(url: string, data?: D, config?: Parameters<typeof client.put>[2]) {
    return client.put<T>(url, data, config).then((response) => response.data);
  },
  patch<T, D = unknown>(url: string, data?: D, config?: Parameters<typeof client.patch>[2]) {
    return client.patch<T>(url, data, config).then((response) => response.data);
  },
  delete<T>(url: string, config?: Parameters<typeof client.delete>[1]) {
    return client.delete<T>(url, config).then((response) => response.data);
  },
};

export async function requestData<T>(config: Parameters<typeof client.request>[0]) {
  const response = await client.request<ApiResponseObject<T>>(config);
  if (response.data.code !== 0) {
    throw new Error(response.data.message || "请求失败");
  }
  return response.data.data;
}
