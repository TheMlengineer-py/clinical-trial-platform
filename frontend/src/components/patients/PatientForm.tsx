import { useState } from "react";
import { useCreatePatient, useUpdatePatient } from "../../hooks/usePatients";
import type { Patient, PatientRequest } from "../../types/patient";
import ErrorBanner from "../shared/ErrorBanner";

interface Props {
  existing?: Patient;
  onClose: () => void;
}

/** Create / edit form for patient records. Mirrors StudyForm pattern. */
export default function PatientForm({ existing, onClose }: Props) {
  const [name, setName] = useState(existing?.name ?? "");
  const [age, setAge] = useState(existing?.age ?? 0);
  const [condition, setCond] = useState(existing?.condition ?? "");
  const [error, setError] = useState<string | null>(null);

  const create = useCreatePatient();
  const update = useUpdatePatient();
  const isPending = create.isPending || update.isPending;

  const validate = (): boolean => {
    if (!name.trim()) {
      setError("Name is required");
      return false;
    }
    if (age < 0 || age > 130) {
      setError("Enter a valid age (0-130)");
      return false;
    }
    if (!condition.trim()) {
      setError("Condition is required");
      return false;
    }
    return true;
  };

  const handleSubmit = () => {
    if (!validate()) return;
    setError(null);
    const req: PatientRequest = { name, age, condition };

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
            {existing ? "Edit patient" : "Add patient"}
          </span>
          <button onClick={onClose} style={closeBtn}>
            ✕
          </button>
        </div>
        <div style={{ padding: 16 }}>
          {error && <ErrorBanner message={error} />}
          {[
            {
              label: "Full name",
              value: name,
              set: setName,
              type: "text",
              ph: "Patient name",
            },
            {
              label: "Age",
              value: age,
              set: (v: string) => setAge(Number(v)),
              type: "number",
              ph: "0",
            },
            {
              label: "Condition",
              value: condition,
              set: setCond,
              type: "text",
              ph: "e.g. NSCLC",
            },
          ].map(({ label, value, set, type, ph }) => (
            <div key={label} style={{ marginBottom: 12 }}>
              <label style={labelStyle}>{label}</label>
              <input
                type={type}
                value={value}
                onChange={(e) => set(e.target.value)}
                placeholder={ph}
                style={inputStyle}
              />
            </div>
          ))}
        </div>
        <div style={footer}>
          <button onClick={onClose} style={cancelBtn}>
            Cancel
          </button>
          <button onClick={handleSubmit} disabled={isPending} style={submitBtn}>
            {isPending ? "Saving..." : existing ? "Save" : "Add patient"}
          </button>
        </div>
      </div>
    </div>
  );
}

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
const footer: React.CSSProperties = {
  padding: "12px 16px",
  borderTop: "1px solid #e5e5e0",
  display: "flex",
  justifyContent: "flex-end",
  gap: 8,
};
const labelStyle: React.CSSProperties = {
  display: "block",
  fontSize: 11,
  fontWeight: 500,
  color: "#5F5E5A",
  marginBottom: 4,
};
const inputStyle: React.CSSProperties = {
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
