"use client";

import { useEffect, useRef, useState } from "react";
import { SUPPORTED_LOCALES } from "@/shared/i18n/config";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

export function LanguageSwitcher() {
  const { locale, setLocale, t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const current = SUPPORTED_LOCALES.find((entry) => entry.id === locale) ?? SUPPORTED_LOCALES[0];

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    function handlePointerDown(event: MouseEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsOpen(false);
      }
    }

    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen]);

  return (
    <div className="language-dropdown" ref={containerRef}>
      <button
        type="button"
        className="language-dropdown-trigger"
        onClick={() => setIsOpen((open) => !open)}
        aria-label={t("i18n.language")}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
      >
        <span className="language-dropdown-value">
          <span aria-hidden="true">{current.flag}</span>
          <span>{current.label}</span>
        </span>
        <span className="language-dropdown-chevron" aria-hidden="true">
          ▾
        </span>
      </button>
      {isOpen ? (
        <ul className="language-dropdown-menu" role="listbox" aria-label={t("i18n.language")}>
          {SUPPORTED_LOCALES.map((entry) => (
            <li key={entry.id} role="presentation">
              <button
                type="button"
                role="option"
                aria-selected={locale === entry.id}
                className={
                  locale === entry.id
                    ? "language-dropdown-option active"
                    : "language-dropdown-option"
                }
                onClick={() => {
                  setLocale(entry.id);
                  setIsOpen(false);
                }}
              >
                <span aria-hidden="true">{entry.flag}</span>
                <span>{entry.label}</span>
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}
