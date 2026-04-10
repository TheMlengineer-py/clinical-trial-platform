import { useParams, useNavigate } from "react-router-dom";
import { usePatient } from "../../hooks/usePatients";
import LoadingSpinner from "../shared/LoadingSpinner";
import ErrorBanner from "../shared/ErrorBanner";

/**
 * Detail view for a single patient — accessed via /patients/:id.
 * Shows all fields including current enrolment status and recruitment timestamp.
 */
export default function PatientDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: patient, isLoading, isError, error } = usePatient(Number(id));

  if (isLoading) return <LoadingSpinner />;
  if (isError) return <ErrorBanner message={(error as Error).message} />;
  if (!patient) return null;

  return (
    <div style={{ maxWidth: 480 }}>
      <button onClick={() => navigate("/patients")} style={backBtn}>
        ← Back to patients
      </button>

      <div style={card}>
        {/* Identity block */}
        <div style={avatarRow}>
          <div style={avatar}>{patient.name.charAt(0)}</div>
          <div>
            <div style={{ fontWeight: 500, fontSize: 16 }}>{patient.name}</div>
            <div style={{ color: "#5F5E5A", fontSize: 13 }}>
              Age {patient.age}
            </div>
          </div>
        </div>

        <hr
          style={{
            border: "none",
            borderTop: "1px solid #e5e5e0",
            margin: "14px 0",
          }}
        />

        {/* Detail rows */}
        {[
          ["Condition", patient.condition],
          [
            "Enrolled in",
            patient.enrolledStudyId
              ? `Study #${patient.enrolledStudyId}`
              : "Not enrolled",
          ],
          [
            "Recruited at",
            patient.recruitedAt
              ? new Date(patient.recruitedAt).toLocaleString()
              : "Never",
          ],
        ].map(([label, value]) => (
          <div key={label} style={detailRow}>
            <span style={detailLabel}>{label}</span>
            <span style={detailValue}>{value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

const card: React.CSSProperties = {
  background: "#fff",
  border: "1px solid #e5e5e0",
  borderRadius: 12,
  padding: "20px 24px",
};
const avatarRow: React.CSSProperties = {
  display: "flex",
  alignItems: "center",
  gap: 14,
};
const avatar: React.CSSProperties = {
  width: 44,
  height: 44,
  borderRadius: "50%",
  background: "#E6F1FB",
  color: "#185FA5",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontWeight: 500,
  fontSize: 18,
};
const detailRow: React.CSSProperties = {
  display: "flex",
  justifyContent: "space-between",
  padding: "6px 0",
  fontSize: 13,
};
const detailLabel: React.CSSProperties = { color: "#5F5E5A" };
const detailValue: React.CSSProperties = { fontWeight: 500 };
const backBtn: React.CSSProperties = {
  background: "none",
  border: "none",
  color: "#185FA5",
  cursor: "pointer",
  fontSize: 13,
  padding: 0,
  marginBottom: 16,
};
