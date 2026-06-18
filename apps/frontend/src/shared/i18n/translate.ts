import type { Messages } from "@/shared/i18n/types";

export function translate(
  messages: Messages,
  key: string,
  params?: Record<string, string>
): string {
  const parts = key.split(".");
  let current: unknown = messages;

  for (const part of parts) {
    if (typeof current !== "object" || current === null || !(part in current)) {
      return key;
    }
    current = (current as Record<string, unknown>)[part];
  }

  if (typeof current !== "string") {
    return key;
  }

  if (!params) {
    return current;
  }

  return Object.entries(params).reduce(
    (text, [paramKey, paramValue]) => text.replaceAll(`{{${paramKey}}}`, paramValue),
    current
  );
}
