import { useTransitionStudy } from "../../hooks/useStudies";
import type { Study, StudyStatus } from "../../types/study";

/** Maps current status to the next allowed state and button label. */
const NEXT_STATE: Partial<
  Record<StudyStatus, { to: StudyStatus; label: string }>
> = {
  DRAFT: { to: "OPEN", label: "Open for recruitment" },
  OPEN: { to: "CLOSED", label: "Close study" },
  CLOSED: { to: "ARCHIVED", label: "Archive" },
};

/**
 * Renders the single contextual action button for advancing a study's lifecycle.
 * Shows nothing for ARCHIVED studies (terminal state).
 *
 * Example: An OPEN study shows "Close study". A CLOSED study shows "Archive".
 */
export default function StudyLifecycleControls({ study }: { study: Study }) {
  const transition = useTransitionStudy();
  const next = NEXT_STATE[study.status];

  if (!next) return null; // ARCHIVED — no further transitions

  const handleClick = () => {
    if (window.confirm(`Transition "${study.title}" to ${next.to}?`)) {
      transition.mutate({ id: study.id, status: next.to });
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={transition.isPending}
      style={{
        fontSize: 11,
        padding: "3px 8px",
        border: "1px solid #B5D4F4",
        borderRadius: 5,
        background: "#E6F1FB",
        color: "#185FA5",
        cursor: "pointer",
      }}
    >
      {transition.isPending ? "..." : next.label}
    </button>
  );
}
