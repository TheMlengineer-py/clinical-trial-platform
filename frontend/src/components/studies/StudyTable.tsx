import { useState } from "react";
import { useStudies, useDeleteStudy } from "../../hooks/useStudies";
import type { StudyStatus } from "../../types/study";
import StudyStatusBadge from "./StudyStatusBadge";
import StudyLifecycleControls from "./StudyLifecycleControls";
import StudyForm from "./StudyForm";
import LoadingSpinner from "../shared/LoadingSpinner";
import ErrorBanner from "../shared/ErrorBanner";
import Pagination from "../shared/Pagination";

/**
 * Main study management table.
 * Features: status filter, add/edit/delete, lifecycle controls, pagination.
 */
export default function StudyTable() {
  const [status, setStatus] = useState<StudyStatus | undefined>();
  const [page, setPage] = useState(0);
  const [showForm, setForm] = useState(false);
  const [editing, setEditing] = useState<number | undefined>();

  const { data, isLoading, isError, error } = useStudies(status, page);
  const deleteStudy = useDeleteStudy();

  if (isLoading) return <LoadingSpinner />;
  if (isError) return <ErrorBanner message={(error as Error).message} />;

  return (
    <div style={panel}>
      {/* Header row */}
      <div style={panelHeader}>
        <span style={{ fontWeight: 500, fontSize: 13 }}>Studies</span>
        <div style={{ display: "flex", gap: 8 }}>
          <select
            value={status ?? ""}
            onChange={(e) =>
              setStatus((e.target.value as StudyStatus) || undefined)
            }
            style={selectStyle}
          >
            <option value="">All statuses</option>
            {(["DRAFT", "OPEN", "CLOSED", "ARCHIVED"] as StudyStatus[]).map(
              (s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ),
            )}
          </select>
          <button onClick={() => setForm(true)} style={primaryBtn}>
            + New study
          </button>
        </div>
      </div>

      {/* Table */}
      <table
        style={{ width: "100%", borderCollapse: "collapse", fontSize: 12 }}
      >
        <thead>
          <tr style={{ background: "#f9f9f7" }}>
            {["Title", "Status", "Enrollment", "Actions"].map((h) => (
              <th key={h} style={th}>
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data?.content.map((study) => (
            <tr key={study.id} style={{ borderBottom: "1px solid #f0f0ec" }}>
              <td style={td}>
                <div style={{ fontWeight: 500 }}>{study.title}</div>
                {study.eligibilityCriteria && (
                  <div style={{ fontSize: 10, color: "#888" }}>
                    {study.eligibilityCriteria}
                  </div>
                )}
              </td>
              <td style={td}>
                <StudyStatusBadge status={study.status} />
              </td>
              <td style={td}>
                {/* Progress bar */}
                <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                  <div
                    style={{
                      width: 80,
                      height: 4,
                      background: "#e5e5e0",
                      borderRadius: 2,
                      overflow: "hidden",
                    }}
                  >
                    <div
                      style={{
                        height: "100%",
                        width: `${Math.min(100, (study.currentEnrollment / study.maxEnrollment) * 100)}%`,
                        background:
                          study.status === "OPEN" ? "#3B6D11" : "#888",
                        borderRadius: 2,
                      }}
                    />
                  </div>
                  <span style={{ color: "#888" }}>
                    {study.currentEnrollment}/{study.maxEnrollment}
                  </span>
                </div>
              </td>
              <td style={td}>
                <div style={{ display: "flex", gap: 4, flexWrap: "wrap" }}>
                  <StudyLifecycleControls study={study} />
                  <button
                    onClick={() => setEditing(study.id)}
                    style={actionBtn}
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => {
                      if (window.confirm(`Delete "${study.title}"?`))
                        deleteStudy.mutate(study.id);
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

      {/* Create / Edit modal */}
      {showForm && <StudyForm onClose={() => setForm(false)} />}
      {editing && (
        <StudyForm
          existing={data?.content.find((s) => s.id === editing)}
          onClose={() => setEditing(undefined)}
        />
      )}
    </div>
  );
}

// ── Styles ─────────────────────────────────────────────────────────────────────
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
const selectStyle: React.CSSProperties = {
  fontSize: 12,
  padding: "4px 8px",
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
