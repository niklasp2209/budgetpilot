"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

const NAV_ITEMS = [
  { href: "/dashboard", key: "nav.dashboard" },
  { href: "/accounting", key: "nav.accounting" },
  { href: "/budgets", key: "nav.budgets" },
  { href: "/members", key: "nav.members" }
] as const;

export function AppNav() {
  const pathname = usePathname();
  const { t } = useTranslation();

  return (
    <nav className="app-nav">
      {NAV_ITEMS.map((item) => {
        const isActive = pathname === item.href;
        return (
          <Link
            key={item.href}
            href={item.href}
            className={isActive ? "app-nav-link active" : "app-nav-link"}
          >
            {t(item.key)}
          </Link>
        );
      })}
    </nav>
  );
}
