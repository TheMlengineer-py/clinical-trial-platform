import { useState } from "react";
import { useRecruitPatient } from "../../hooks/usePatients";
import { useStudies } from "../../hooks/useStudies";
import type { Patient } from "../../types/patient";
import ErrorBanner from "../shared/ErrorBanner";

interface Props {
  patient: Patient;
  onClose: () => void;
}

/**
 * Modal for recruiting a patient into an OPEN study.
 *
 * Only OPEN studies with available capacity are shown in the dropdown.
 * Eligibility feedback (pass/fail) is returned from the backend — the
 * frontend trusts the server result rather than re-implementing the rules.
 */
export default function RecruitModal({ patient, onClose }: Props) {
  const [studyId, setStudyId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Fetch OPEN studies to populate the dropdown
  const { data: studies } = useStudies("OPEN");
  const recruit = useRecruitPatient();

  // Filter to only studies with remaining capacity
  const available =
    studies?.content.filter((s) => s.currentEnrollment < s.maxEnrollment) ?? [];

  const handleRecruit = () => {
    if (!studyId) {
      setError("Please select a study");
      return;
    }
    setError(null);
    recruit.mutate(
      { patientId: patient.id, studyId },
      {
        onSuccess: onClose,
        onError: (e) => setError((e as Error).message),
      },
    );
  };

  return (
    <div style={overlay}>
      <div style={modal}>
        <div style={header}>
          <span style={{ fontWeight: 500 }}>Recruit patient into study</span>
          <button onClick={onClose} style={closeBtn}>
            ✕
          </button>
        </div>

        <div style={body}>
          {/* Patient summary card */}
          <div style={patientCard}>
            <div style={{ fontWeight: 500 }}>{patient.name}</div>
            <div style={{ color: "#5F5E5A", fontSize: 11, marginTop: 2 }}>
              Age {patient.age} · {patient.condition}
            </div>
          </div>

          {error && <ErrorBanner message={error} />}

          {/* Study selector */}
          <div style={{ marginBottom: 12 }}>
            <label style={labelStyle}>Select open study</label>
            <select
              value={studyId ?? ""}
              onChange={(e) => setStudyId(Number(e.target.value) || null)}
              style={{ ...inputStyle, width: "100%" }}
            >
              <option value="">-- Choose a study --</option>
              {available.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.title} ({s.maxEnrollment - s.currentEnrollment} slots left)
                </option>
              ))}
            </select>
            <div style={{ fontSize: 10, color: "#888", marginTop: 3 }}>
              Only OPEN studies with available capacity are shown
            </div>
          </div>

          {/* Eligibility note — server enforces the actual check */}
          <div style={eligibilityNote}>
            ℹ Eligibility will be verified server-side on recruitment
          </div>
        </div>

        <div style={footer}>
          <button onClick={onClose} style={cancelBtn}>
            Cancel
          </button>
          <button
            onClick={handleRecruit}
            disabled={!studyId || recruit.isPending}
            style={recruitBtn}
          >
            {recruit.isPending ? "Recruiting..." : "Confirm recruitment"}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Styles ─────────────────────────────────────────────────────────────────────
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
  width: 340,
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
const patientCard: React.CSSProperties = {
  background: "#f9f9f7",
  borderRadius: 8,
  padding: 10,
  marginBottom: 14,
  fontSize: 12,
};
const labelStyle: React.CSSProperties = {
  display: "block",
  fontSize: 11,
  fontWeight: 500,
  color: "#5F5E5A",
  marginBottom: 4,
};
const inputStyle: React.CSSProperties = {
  fontSize: 12,
  padding: "6px 9px",
  border: "1px solid #d3d1c7",
  borderRadius: 6,
};
const eligibilityNote: React.CSSProperties = {
  fontSize: 11,
  color: "#5F5E5A",
  background: "#f9f9f7",
  padding: "7px 10px",
  borderRadius: 6,
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
const recruitBtn: React.CSSProperties = {
  fontSize: 12,
  padding: "5px 12px",
  border: "none",
  borderRadius: 6,
  background: "#185FA5",
  color: "#fff",
  cursor: "pointer",
};
