"use client";

import { useMemo } from "react";

export function useUserTools() {
  const tools = useMemo(
    () => [
      { id: "kb-search", name: "知识库检索", status: "enabled" },
      { id: "sql-query", name: "SQL 查询", status: "disabled" },
    ],
    []
  );

  return {
    tools,
  };
}
