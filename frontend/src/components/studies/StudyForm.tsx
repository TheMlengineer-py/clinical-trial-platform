import { useState } from "react";
import { useCreateStudy, useUpdateStudy } from "../../hooks/useStudies";
import type { Study, StudyRequest } from "../../types/study";
import ErrorBanner from "../shared/ErrorBanner";

interface Props {
  existing?: Study; // pass to edit, omit to create
  onClose: () => void;
}

/**
 * Controlled form for creating and editing studies.
 * Handles its own local state and delegates to the appropriate React Query mutation.
 * Inline form validation mirrors the backend Bean Validation rules.
 */
export default function StudyForm({ existing, onClose }: Props) {
  const [title, setTitle] = useState(existing?.title ?? "");
  const [maxEnrollment, setMax] = useState(existing?.maxEnrollment ?? 10);
  const [criteria, setCriteria] = useState(existing?.eligibilityCriteria ?? "");
  const [error, setError] = useState<string | null>(null);

  const create = useCreateStudy();
  const update = useUpdateStudy();
  const isPending = create.isPending || update.isPending;

  const validate = (): boolean => {
    if (!title.trim()) {
      setError("Title is required");
      return false;
    }
    if (maxEnrollment < 1) {
      setError("Enrollment must be at least 1");
      return false;
    }
    return true;
  };

  const handleSubmit = () => {
    if (!validate()) return;
    setError(null);
    const req: StudyRequest = {
      title,
      maxEnrollment,
      eligibilityCriteria: criteria || undefined,
    };

    if (existing) {
      update.mutate(
        { id: existing.id, req },
        { onSuccess: onClose, onError: (e) => setError((e as Error).message) },
      );
    } else {
      create.mutate(req, {
        onSuccess: onClose,
        onError: (e) => setError((e as Error).message),
      });
    }
  };

  return (
    <div style={overlay}>
      <div style={modal}>
        <div style={header}>
          <span style={{ fontWeight: 500 }}>
            {existing ? "Edit study" : "New study"}
          </span>
          <button onClick={onClose} style={closeBtn}>
            ✕
          </button>
        </div>
        <div style={body}>
          {error && <ErrorBanner message={error} />}
          <Field label="Title">
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Study title"
              style={input}
            />
          </Field>
          <Field label="Max enrollment">
            <input
              type="number"
              value={maxEnrollment}
              min={1}
              onChange={(e) => setMax(Number(e.target.value))}
              style={input}
            />
          </Field>
          <Field
            label="Eligibility criteria (optional)"
            hint='Example: "age>18,condition=NSCLC"'
          >
            <input
              value={criteria}
              onChange={(e) => setCriteria(e.target.value)}
              placeholder="age>18,condition=..."
              style={input}
            />
          </Field>
        </div>
        <div style={footer}>
          <button onClick={onClose} style={cancelBtn}>
            Cancel
          </button>
          <button onClick={handleSubmit} disabled={isPending} style={submitBtn}>
            {isPending
              ? "Saving..."
              : existing
                ? "Save changes"
                : "Create study"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Sub-component ─────────────────────────────────────────────────────────────
function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <div style={{ marginBottom: 12 }}>
      <label
        style={{
          display: "block",
          fontSize: 11,
          fontWeight: 500,
          color: "#5F5E5A",
          marginBottom: 4,
        }}
      >
        {label}
      </label>
      {children}
      {hint && (
        <div style={{ fontSize: 10, color: "#888", marginTop: 3 }}>{hint}</div>
      )}
    </div>
  );
}

// ── Styles ────────────────────────────────────────────────────────────────────
const overlay: React.CSSProperties = {
  position: "fixed",
  inset: 0,
  background: "rgba(0,0,0,0.3)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  zIndex: 100,
};
const modal: React.CSSProperties = {
  background: "#fff",
  borderRadius: 12,
  border: "1px solid #e5e5e0",
  width: 360,
  overflow: "hidden",
};
const header: React.CSSProperties = {
  padding: "14px 16px",
  borderBottom: "1px solid #e5e5e0",
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  fontSize: 13,
};
const body: React.CSSProperties = { padding: 16 };
const footer: React.CSSProperties = {
  padding: "12px 16px",
  borderTop: "1px solid #e5e5e0",
  display: "flex",
  justifyContent: "flex-end",
  gap: 8,
};
const input: React.CSSProperties = {
  width: "100%",
  fontSize: 12,
  padding: "6px 9px",
  border: "1px solid #d3d1c7",
  borderRadius: 6,
  boxSizing: "border-box",
};
const closeBtn: React.CSSProperties = {
  background: "none",
  border: "none",
  cursor: "pointer",
  fontSize: 14,
};
const cancelBtn: React.CSSProperties = {
  fontSize: 12,
  padding: "5px 12px",
  border: "1px solid #d3d1c7",
  borderRadius: 6,
  background: "#fff",
  cursor: "pointer",
};
const submitBtn: React.CSSProperties = {
  fontSize: 12,
  padding: "5px 12px",
  border: "none",
  borderRadius: 6,
  background: "#185FA5",
  color: "#fff",
  cursor: "pointer",
};
