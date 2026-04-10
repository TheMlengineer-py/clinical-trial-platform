import { Routes, Route, NavLink } from "react-router-dom";
import StudyTable from "./components/studies/StudyTable";
import PatientTable from "./components/patients/PatientTable";
import PatientDetail from "./components/patients/PatientDetail";

/**
 * Root component — defines the top-level navigation and route structure.
 *
 * Routes:
 *  /          → Studies table (default view)
 *  /patients  → Patients table sorted by most recent recruitment
 *  /patients/:id → Patient detail view with enrolment info
 */
export default function App() {
  return (
    <div
      style={{
        fontFamily: "system-ui, sans-serif",
        minHeight: "100vh",
        background: "#f9f9f7",
      }}
    >
      {/* Top navigation bar */}
      <nav
        style={{
          background: "#fff",
          borderBottom: "1px solid #e5e5e0",
          padding: "0 24px",
          display: "flex",
          alignItems: "center",
          gap: 24,
          height: 52,
        }}
      >
        <span style={{ fontWeight: 600, fontSize: 15, color: "#1a1a18" }}>
          Barts <span style={{ color: "#185FA5" }}>ClinicalTrials</span>
        </span>
        <NavLink to="/" style={navStyle} end>
          Studies
        </NavLink>
        <NavLink to="/patients" style={navStyle}>
          Patients
        </NavLink>
      </nav>

      {/* Page content */}
      <main style={{ padding: "24px" }}>
        <Routes>
          <Route path="/" element={<StudyTable />} />
          <Route path="/patients" element={<PatientTable />} />
          <Route path="/patients/:id" element={<PatientDetail />} />
        </Routes>
      </main>
    </div>
  );
}

/** Inline nav link style — active link gets an underline accent. */
const navStyle = ({ isActive }: { isActive: boolean }) => ({
  fontSize: 13,
  fontWeight: isActive ? 500 : 400,
  color: isActive ? "#185FA5" : "#5F5E5A",
  textDecoration: "none",
  borderBottom: isActive ? "2px solid #185FA5" : "2px solid transparent",
  paddingBottom: 2,
});
