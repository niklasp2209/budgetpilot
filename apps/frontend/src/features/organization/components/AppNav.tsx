"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const NAV_ITEMS = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/accounting", label: "Accounting" },
  { href: "/budgets", label: "Budgets" },
  { href: "/members", label: "Members" }
] as const;

export function AppNav() {
  const pathname = usePathname();

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
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}
