export type ApiResponseObject<T> = {
  code: number;
  message: string;
  data: T;
  requestId?: string;
};

export type PageResponseObject<T> = {
  total: number;
  pageNo: number;
  pageSize: number;
  records: T[];
};
