"use client";

import { createContext, useContext, useMemo, useState } from "react";

type RagChatState = {
  streaming: boolean;
  setStreaming: (value: boolean) => void;
};

const RagChatContext = createContext<RagChatState | null>(null);

export function RagChatProvider({ children }: { children: React.ReactNode }) {
  const [streaming, setStreaming] = useState(false);

  const value = useMemo(
    () => ({
      streaming,
      setStreaming,
    }),
    [streaming]
  );

  return <RagChatContext.Provider value={value}>{children}</RagChatContext.Provider>;
}

export function useRagChatContext() {
  const context = useContext(RagChatContext);
  if (!context) {
    throw new Error("useRagChatContext must be used within RagChatProvider");
  }
  return context;
}
