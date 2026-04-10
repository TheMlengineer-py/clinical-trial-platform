import type { StudyStatus } from "../../types/study";

/** Colour map — each status gets a distinct background matching the mockup. */
const colours: Record<StudyStatus, { bg: string; color: string }> = {
  OPEN: { bg: "#EAF3DE", color: "#3B6D11" },
  DRAFT: { bg: "#F1EFE8", color: "#5F5E5A" },
  CLOSED: { bg: "#FAEEDA", color: "#854F0B" },
  ARCHIVED: { bg: "#FCEBEB", color: "#A32D2D" },
};

/**
 * Pill badge displaying a study's lifecycle status with a colour
 * that makes the current state immediately scannable in the table.
 */
export default function StudyStatusBadge({ status }: { status: StudyStatus }) {
  const { bg, color } = colours[status];
  return (
    <span
      style={{
        background: bg,
        color,
        padding: "2px 8px",
        borderRadius: 4,
        fontSize: 11,
        fontWeight: 500,
      }}
    >
      {status}
    </span>
  );
}
