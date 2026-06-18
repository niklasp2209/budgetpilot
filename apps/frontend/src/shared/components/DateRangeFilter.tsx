"use client";

import {
  defaultCustomFromDate,
  defaultCustomToDate,
  type DateRangePreset
} from "@/shared/lib/dateRange";
import { useTranslation } from "@/features/i18n/context/I18nProvider";

type DateRangeFilterProps = Readonly<{
  preset: DateRangePreset;
  customFrom: string;
  customTo: string;
  onPresetChange: (preset: DateRangePreset) => void;
  onCustomFromChange: (value: string) => void;
  onCustomToChange: (value: string) => void;
}>;

const PRESET_KEYS: { value: DateRangePreset; key: string }[] = [
  { value: "30d", key: "dateRange.days30" },
  { value: "90d", key: "dateRange.days90" },
  { value: "365d", key: "dateRange.months12" },
  { value: "all", key: "dateRange.allTime" },
  { value: "custom", key: "dateRange.custom" }
];

export function DateRangeFilter({
  preset,
  customFrom,
  customTo,
  onPresetChange,
  onCustomFromChange,
  onCustomToChange
}: DateRangeFilterProps) {
  const { t } = useTranslation();

  return (
    <div className="date-range-filter">
      <select value={preset} onChange={(event) => onPresetChange(event.target.value as DateRangePreset)}>
        {PRESET_KEYS.map((entry) => (
          <option key={entry.value} value={entry.value}>
            {t(entry.key)}
          </option>
        ))}
      </select>
      {preset === "custom" ? (
        <>
          <input
            type="date"
            value={customFrom || defaultCustomFromDate()}
            onChange={(event) => onCustomFromChange(event.target.value)}
          />
          <input
            type="date"
            value={customTo || defaultCustomToDate()}
            onChange={(event) => onCustomToChange(event.target.value)}
          />
        </>
      ) : null}
    </div>
  );
}
