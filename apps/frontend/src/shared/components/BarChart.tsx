"use client";

import { formatCents } from "@/shared/lib/format";

export type BarChartItem = {
  label: string;
  value: number;
  tone?: "positive" | "negative" | "neutral";
};

type BarChartProps = Readonly<{
  items: BarChartItem[];
  formatValue?: (value: number) => string;
}>;

export function BarChart({ items, formatValue = formatCents }: BarChartProps) {
  if (items.length === 0) {
    return null;
  }

  const maxValue = Math.max(...items.map((item) => item.value), 1);

  return (
    <div className="bar-chart">
      {items.map((item) => (
        <div key={item.label} className="bar-chart-row">
          <span className="bar-chart-label">{item.label}</span>
          <div className="bar-chart-track">
            <div
              className={`bar-chart-fill bar-${item.tone ?? "neutral"}`}
              style={{ width: `${(item.value / maxValue) * 100}%` }}
            />
          </div>
          <span className="bar-chart-value">{formatValue(item.value)}</span>
        </div>
      ))}
    </div>
  );
}
