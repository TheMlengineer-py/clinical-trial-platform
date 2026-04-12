import { Routes, Route, NavLink, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/shared/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import StudyTable from "./components/studies/StudyTable";
import PatientTable from "./components/patients/PatientTable";
import PatientDetail from "./components/patients/PatientDetail";
import type { Role } from "./types/auth";

export default function App() {
  const { user, isLoading } = useAuth();

  return (
    <div
      style={{
        fontFamily: "system-ui, sans-serif",
        minHeight: "100vh",
        background: "#f9f9f7",
      }}
    >
      {!isLoading && user && (
        <NavBar role={user.role} username={user.username} />
      )}
      <main style={{ padding: "24px" }}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <StudyTable />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patients"
            element={
              <ProtectedRoute>
                <PatientTable />
              </ProtectedRoute>
            }
          />
          <Route
            path="/patients/:id"
            element={
              <ProtectedRoute>
                <PatientDetail />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}

function NavBar({ role, username }: { role: Role; username: string }) {
  const { signOut } = useAuth();
  return (
    <nav style={nav}>
      <span style={brandStyle}>
        Barts <span style={{ color: "#185FA5" }}>ClinicalTrials</span>
      </span>
      <NavLink to="/" style={navLinkStyle} end>
        Studies
      </NavLink>
      <NavLink to="/patients" style={navLinkStyle}>
        Patients
      </NavLink>
      <div
        style={{
          marginLeft: "auto",
          display: "flex",
          alignItems: "center",
          gap: 12,
        }}
      >
        <RoleBadge role={role} />
        <span style={{ fontSize: 12, color: "#5F5E5A" }}>{username}</span>
        <button onClick={signOut} style={signOutBtn}>
          Sign out
        </button>
      </div>
    </nav>
  );
}

function RoleBadge({ role }: { role: Role }) {
  const isAdmin = role === "ADMIN";
  return (
    <span
      style={{
        fontSize: 10,
        fontWeight: 600,
        padding: "2px 8px",
        borderRadius: 4,
        background: isAdmin ? "#E6F1FB" : "#EAF3DE",
        color: isAdmin ? "#185FA5" : "#3B6D11",
        letterSpacing: "0.04em",
      }}
    >
      {role}
    </span>
  );
}

const nav: React.CSSProperties = {
  background: "#fff",
  borderBottom: "1px solid #e5e5e0",
  padding: "0 24px",
  display: "flex",
  alignItems: "center",
  gap: 24,
  height: 52,
};
const brandStyle: React.CSSProperties = {
  fontWeight: 600,
  fontSize: 15,
  color: "#1a1a18",
};
const navLinkStyle = ({
  isActive,
}: {
  isActive: boolean;
}): React.CSSProperties => ({
  fontSize: 13,
  fontWeight: isActive ? 500 : 400,
  color: isActive ? "#185FA5" : "#5F5E5A",
  textDecoration: "none",
  borderBottom: isActive ? "2px solid #185FA5" : "2px solid transparent",
  paddingBottom: 2,
});
const signOutBtn: React.CSSProperties = {
  fontSize: 12,
  padding: "4px 10px",
  border: "1px solid #d3d1c7",
  borderRadius: 6,
  background: "#fff",
  cursor: "pointer",
  color: "#5F5E5A",
};
