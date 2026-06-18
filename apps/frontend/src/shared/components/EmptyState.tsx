import Link from "next/link";
import type { ReactNode } from "react";

type EmptyStateProps = Readonly<{
  title: string;
  description: string;
  href?: string;
  linkLabel?: string;
  children?: ReactNode;
}>;

export function EmptyState({ title, description, href, linkLabel, children }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      <p className="muted">{description}</p>
      {href && linkLabel ? (
        <Link href={href} className="empty-state-link">
          {linkLabel}
        </Link>
      ) : null}
      {children}
    </div>
  );
}
