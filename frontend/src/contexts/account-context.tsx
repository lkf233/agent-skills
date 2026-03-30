"use client";

import { createContext, useContext, useMemo, useState } from "react";

type AccountState = {
  token: string | null;
  username: string | null;
  setToken: (token: string | null) => void;
  setUsername: (username: string | null) => void;
  clear: () => void;
};

const AccountContext = createContext<AccountState | null>(null);

export function AccountProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);

  const value = useMemo<AccountState>(
    () => ({
      token,
      username,
      setToken,
      setUsername,
      clear: () => {
        setToken(null);
        setUsername(null);
      },
    }),
    [token, username]
  );

  return <AccountContext.Provider value={value}>{children}</AccountContext.Provider>;
}

export function useAccountContext() {
  const context = useContext(AccountContext);
  if (!context) {
    throw new Error("useAccountContext must be used within AccountProvider");
  }
  return context;
}
