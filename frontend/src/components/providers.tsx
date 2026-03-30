"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";
import { Toaster } from "sonner";
import { AccountProvider } from "@/contexts/account-context";
import { RagChatProvider } from "@/contexts/rag-chat-context";
import { WorkspaceProvider } from "@/contexts/workspace-context";

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            retry: 1,
            refetchOnWindowFocus: false,
          },
          mutations: {
            retry: 0,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      <AccountProvider>
        <WorkspaceProvider>
          <RagChatProvider>
            {children}
            <Toaster richColors position="top-right" />
          </RagChatProvider>
        </WorkspaceProvider>
      </AccountProvider>
    </QueryClientProvider>
  );
}
