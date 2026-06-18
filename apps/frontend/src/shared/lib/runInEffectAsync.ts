export function runInEffectAsync(
  action: (isCancelled: () => boolean) => Promise<void>
): () => void {
  let cancelled = false;

  queueMicrotask(() => {
    void action(() => cancelled);
  });

  return () => {
    cancelled = true;
  };
}
