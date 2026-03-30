"use client";

import { createContext, useContext, useMemo, useState } from "react";

type WorkspaceState = {
  activeWorkspaceId: string | null;
  setActiveWorkspaceId: (workspaceId: string | null) => void;
};

const WorkspaceContext = createContext<WorkspaceState | null>(null);

export function WorkspaceProvider({ children }: { children: React.ReactNode }) {
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null);

  const value = useMemo(
    () => ({
      activeWorkspaceId,
      setActiveWorkspaceId,
    }),
    [activeWorkspaceId]
  );

  return <WorkspaceContext.Provider value={value}>{children}</WorkspaceContext.Provider>;
}

export function useWorkspaceContext() {
  const context = useContext(WorkspaceContext);
  if (!context) {
    throw new Error("useWorkspaceContext must be used within WorkspaceProvider");
  }
  return context;
}
