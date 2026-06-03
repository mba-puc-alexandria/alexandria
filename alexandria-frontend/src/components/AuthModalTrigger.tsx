"use client";

import { useEffect } from "react";
import { useSearchParams, useRouter, usePathname } from "next/navigation";
import { useAuthModal } from "@/contexts/AuthModalContext";

export default function AuthModalTrigger() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const { openLoginModal } = useAuthModal();

  useEffect(() => {
    if (searchParams.get("auth") === "1") {
      openLoginModal();
      router.replace(pathname);
    }
  }, [searchParams, openLoginModal, router, pathname]);

  return null;
}
