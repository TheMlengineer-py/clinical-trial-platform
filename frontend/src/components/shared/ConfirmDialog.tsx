/**
 * Simple confirmation dialog for destructive actions (delete, unenrol).
 * Uses native browser confirm() — sufficient for a demo; replace with
 * a modal component if the UI needs more polish.
 */
export function useConfirm() {
  return (message: string): boolean => window.confirm(message);
}
