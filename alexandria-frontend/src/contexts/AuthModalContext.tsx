"use client";

import { createContext, useContext, useState, ReactNode } from "react";

interface AuthModalContextType {
  isOpen: boolean;
  openLoginModal: () => void;
  closeLoginModal: () => void;
}

const AuthModalContext = createContext<AuthModalContextType | null>(null);

export function AuthModalProvider({ children }: { children: ReactNode }) {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <AuthModalContext.Provider
      value={{
        isOpen,
        openLoginModal: () => setIsOpen(true),
        closeLoginModal: () => setIsOpen(false),
      }}
    >
      {children}
    </AuthModalContext.Provider>
  );
}

export function useAuthModal() {
  const ctx = useContext(AuthModalContext);
  if (!ctx) throw new Error("useAuthModal must be used within AuthModalProvider");
  return ctx;
}
