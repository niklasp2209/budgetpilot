export type DateRangePreset = "30d" | "90d" | "365d" | "all" | "custom";

export function defaultCustomFromDate(): string {
  const date = new Date();
  date.setDate(date.getDate() - 30);
  return date.toISOString().slice(0, 10);
}

export function defaultCustomToDate(): string {
  return new Date().toISOString().slice(0, 10);
}

export function resolveDateRangeIso(
  preset: DateRangePreset,
  customFrom: string,
  customTo: string
): { from: string; to: string } {
  const to = new Date();
  const from = new Date();

  if (preset === "30d") {
    from.setDate(from.getDate() - 30);
  } else if (preset === "90d") {
    from.setDate(from.getDate() - 90);
  } else if (preset === "365d") {
    from.setDate(from.getDate() - 365);
  } else if (preset === "all") {
    from.setFullYear(from.getFullYear() - 10);
  } else {
    from.setTime(new Date(`${customFrom}T00:00:00`).getTime());
    to.setTime(new Date(`${customTo}T23:59:59`).getTime());
  }

  if (preset !== "custom") {
    from.setHours(0, 0, 0, 0);
    to.setHours(23, 59, 59, 999);
  }

  return { from: from.toISOString(), to: to.toISOString() };
}

export function toLocalDateTimeInput(iso: string): string {
  const date = new Date(iso);
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatDateRangeLabel(preset: DateRangePreset): string {
  if (preset === "30d") {
    return "Last 30 days";
  }
  if (preset === "90d") {
    return "Last 90 days";
  }
  if (preset === "365d") {
    return "Last 12 months";
  }
  if (preset === "all") {
    return "All time";
  }
  return "Custom range";
}
