"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { getStoredTokens } from "@/shared/lib/storage";

export default function HomePage() {
  const router = useRouter();

  useEffect(() => {
    router.replace(getStoredTokens() ? "/dashboard" : "/login");
  }, [router]);

  return (
    <div className="page-center">
      <p className="muted">Redirecting...</p>
    </div>
  );
}
