"use client";

import { formatCurrencyOption, ORGANIZATION_CURRENCY_CODES } from "@/shared/lib/currencies";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

type CurrencySelectProps = Readonly<{
  value: string;
  onChange: (currency: string) => void;
  inline?: boolean;
}>;

export function CurrencySelect({ value, onChange, inline = false }: CurrencySelectProps) {
  const { t } = useTranslation();

  const select = (
    <select value={value} onChange={(event) => onChange(event.target.value)} required>
      {ORGANIZATION_CURRENCY_CODES.map((code) => (
        <option key={code} value={code}>
          {formatCurrencyOption(code, t(`currencies.${code}`))}
        </option>
      ))}
    </select>
  );

  if (inline) {
    return select;
  }

  return (
    <label>
      {t("common.currency")}
      {select}
    </label>
  );
}
