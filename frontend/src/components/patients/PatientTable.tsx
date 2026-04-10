import { useState } from "react";
import { usePatients, useDeletePatient } from "../../hooks/usePatients";
import type { Patient } from "../../types/patient";
import PatientForm from "./PatientForm";
import RecruitModal from "./RecruitModal";
import LoadingSpinner from "../shared/LoadingSpinner";
import ErrorBanner from "../shared/ErrorBanner";
import Pagination from "../shared/Pagination";

/**
 * Main patient management table.
 * Sorted by most recently recruited (handled by the backend).
 * Features: condition filter, add/edit/delete, recruit modal.
 */
export default function PatientTable() {
  const [condition, setCond] = useState("");
  const [page, setPage] = useState(0);
  const [showForm, setForm] = useState(false);
  const [editing, setEditing] = useState<Patient | undefined>();
  const [recruiting, setRecruit] = useState<Patient | undefined>();

  const { data, isLoading, isError, error } = usePatients(
    condition || undefined,
    page,
  );
  const deletePatient = useDeletePatient();

  if (isLoading) return <LoadingSpinner />;
  if (isError) return <ErrorBanner message={(error as Error).message} />;

  return (
    <div style={panel}>
      <div style={panelHeader}>
        <span style={{ fontWeight: 500, fontSize: 13 }}>Patients</span>
        <div style={{ display: "flex", gap: 8 }}>
          <input
            placeholder="Filter by condition..."
            value={condition}
            onChange={(e) => {
              setCond(e.target.value);
              setPage(0);
            }}
            style={searchInput}
          />
          <button onClick={() => setForm(true)} style={primaryBtn}>
            + Add patient
          </button>
        </div>
      </div>

      <table
        style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}
      >
        <thead>
          <tr style={{ background: "#f9f9f7" }}>
            {[
              "Patient",
              "Condition",
              "Enrolled in",
              "Recruited",
              "Actions",
            ].map((h) => (
              <th key={h} style={th}>
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data?.content.map((patient) => (
            <tr key={patient.id} style={{ borderBottom: "1px solid #f0f0ec" }}>
              <td style={td}>
                <div style={{ fontWeight: 500 }}>{patient.name}</div>
                <div style={{ fontSize: 10, color: "#888" }}>
                  Age {patient.age}
                </div>
              </td>
              <td style={td}>{patient.condition}</td>
              <td style={td}>
                {patient.enrolledStudyId ? (
                  <span style={enrolledBadge}>
                    Study #{patient.enrolledStudyId}
                  </span>
                ) : (
                  <span style={noneBadge}>None</span>
                )}
              </td>
              <td style={{ ...td, color: "#888", fontSize: 11 }}>
                {patient.recruitedAt
                  ? new Date(patient.recruitedAt).toLocaleDateString()
                  : "—"}
              </td>
              <td style={td}>
                <div style={{ display: "flex", gap: 4 }}>
                  {!patient.enrolledStudyId && (
                    <button
                      onClick={() => setRecruit(patient)}
                      style={recruitBtn}
                    >
                      Recruit
                    </button>
                  )}
                  <button onClick={() => setEditing(patient)} style={actionBtn}>
                    Edit
                  </button>
                  <button
                    onClick={() => {
                      if (window.confirm(`Delete ${patient.name}?`))
                        deletePatient.mutate(patient.id);
                    }}
                    style={{ ...actionBtn, color: "#A32D2D" }}
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 1}
        onPageChange={setPage}
      />

      {showForm && <PatientForm onClose={() => setForm(false)} />}
      {editing && (
        <PatientForm existing={editing} onClose={() => setEditing(undefined)} />
      )}
      {recruiting && (
        <RecruitModal
          patient={recruiting}
          onClose={() => setRecruit(undefined)}
        />
      )}
    </div>
  );
}

const panel: React.CSSProperties = {
  background: "#fff",
  border: "1px solid #e5e5e0",
  borderRadius: 12,
  overflow: "hidden",
};
const panelHeader: React.CSSProperties = {
  padding: "12px 16px",
  borderBottom: "1px solid #e5e5e0",
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
};
const th: React.CSSProperties = {
  padding: "8px 12px",
  textAlign: "left",
  fontSize: 11,
  fontWeight: 500,
  color: "#5F5E5A",
  borderBottom: "1px solid #e5e5e0",
};
const td: React.CSSProperties = {
  padding: "8px 12px",
  verticalAlign: "middle",
};
const searchInput: React.CSSProperties = {
  fontSize: 12,
  padding: "4px 9px",
  border: "1px solid #d3d1c7",
  borderRadius: 6,
};
const primaryBtn: React.CSSProperties = {
  fontSize: 12,
  padding: "4px 10px",
  background: "#185FA5",
  color: "#fff",
  border: "none",
  borderRadius: 6,
  cursor: "pointer",
};
const actionBtn: React.CSSProperties = {
  fontSize: 11,
  padding: "3px 8px",
  border: "1px solid #d3d1c7",
  borderRadius: 5,
  background: "#fff",
  cursor: "pointer",
};
const recruitBtn: React.CSSProperties = {
  fontSize: 11,
  padding: "3px 8px",
  border: "1px solid #B5D4F4",
  borderRadius: 5,
  background: "#E6F1FB",
  color: "#185FA5",
  cursor: "pointer",
};
const enrolledBadge: React.CSSProperties = {
  fontSize: 10,
  padding: "2px 7px",
  background: "#E6F1FB",
  color: "#185FA5",
  borderRadius: 4,
  fontWeight: 500,
};
const noneBadge: React.CSSProperties = {
  fontSize: 10,
  padding: "2px 7px",
  background: "#F1EFE8",
  color: "#5F5E5A",
  borderRadius: 4,
};
