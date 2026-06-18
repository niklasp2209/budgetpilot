"use client";

import {
  defaultCustomFromDate,
  defaultCustomToDate,
  type DateRangePreset
} from "@/shared/lib/dateRange";

type DateRangeFilterProps = Readonly<{
  preset: DateRangePreset;
  customFrom: string;
  customTo: string;
  onPresetChange: (preset: DateRangePreset) => void;
  onCustomFromChange: (value: string) => void;
  onCustomToChange: (value: string) => void;
}>;

const PRESETS: { value: DateRangePreset; label: string }[] = [
  { value: "30d", label: "30 days" },
  { value: "90d", label: "90 days" },
  { value: "365d", label: "12 months" },
  { value: "all", label: "All time" },
  { value: "custom", label: "Custom" }
];

export function DateRangeFilter({
  preset,
  customFrom,
  customTo,
  onPresetChange,
  onCustomFromChange,
  onCustomToChange
}: DateRangeFilterProps) {
  return (
    <div className="date-range-filter">
      <select value={preset} onChange={(event) => onPresetChange(event.target.value as DateRangePreset)}>
        {PRESETS.map((entry) => (
          <option key={entry.value} value={entry.value}>
            {entry.label}
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
