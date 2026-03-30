import { API_ENDPOINTS } from "@/lib/api-config";
import { request } from "@/lib/http-client";

export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginRes {
  code: number;
  message: string;
  data: {
    accessToken: string;
  };
}

export interface RegisterParams {
  username: string;
  password: string;
}

export interface RegisterRes {
  code: number;
  message: string;
  data: Record<string, unknown>;
}

export async function login(params: LoginParams): Promise<LoginRes> {
  const response = await request.post<LoginRes, LoginParams>(API_ENDPOINTS.auth.login, params);
  if (response.code !== 0) {
    throw new Error(response.message || "登录失败");
  }
  return response;
}

export async function register(params: RegisterParams): Promise<RegisterRes> {
  const response = await request.post<RegisterRes, RegisterParams>(API_ENDPOINTS.auth.register, params);
  if (response.code !== 0) {
    throw new Error(response.message || "注册失败");
  }
  return response;
}
