"use client";

import type { ReactNode } from "react";
import { LanguageSwitcher } from "@/features/i18n/components/LanguageSwitcher";

type PageShellProps = Readonly<{
  children: ReactNode;
}>;

export function PageShell({ children }: PageShellProps) {
  return (
    <div className="page-center page-center-with-lang">
      <div className="page-center-inner">
        <div className="page-shell-top">
          <LanguageSwitcher />
        </div>
        {children}
      </div>
    </div>
  );
}
