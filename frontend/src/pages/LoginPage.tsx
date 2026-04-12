import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import ErrorBanner from "../components/shared/ErrorBanner";

export default function LoginPage() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      setError("Username and password are required");
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await signIn({ username: username.trim(), password });
      navigate("/", { replace: true });
    } catch (err) {
      setError((err as Error).message ?? "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={page}>
      <div style={card}>
        <div style={brand}>
          Barts <span style={{ color: "#185FA5" }}>ClinicalTrials</span>
        </div>
        <div style={subtitle}>Sign in to continue</div>

        {error && <ErrorBanner message={error} />}

        <form onSubmit={handleSubmit} noValidate>
          <div style={fieldWrap}>
            <label style={labelStyle} htmlFor="username">
              Username
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              style={inputStyle}
              disabled={loading}
            />
          </div>
          <div style={fieldWrap}>
            <label style={labelStyle} htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              style={inputStyle}
              disabled={loading}
            />
          </div>
          <button type="submit" disabled={loading} style={submitBtn}>
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}

const page: React.CSSProperties = {
  minHeight: "100vh",
  background: "#f9f9f7",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  fontFamily: "system-ui, sans-serif",
};
const card: React.CSSProperties = {
  background: "#fff",
  border: "1px solid #e5e5e0",
  borderRadius: 14,
  padding: "32px 36px",
  width: 320,
  boxShadow: "0 2px 12px rgba(0,0,0,0.06)",
};
const brand: React.CSSProperties = {
  fontWeight: 700,
  fontSize: 18,
  color: "#1a1a18",
  marginBottom: 4,
  textAlign: "center",
};
const subtitle: React.CSSProperties = {
  fontSize: 13,
  color: "#5F5E5A",
  textAlign: "center",
  marginBottom: 20,
};
const fieldWrap: React.CSSProperties = { marginBottom: 14 };
const labelStyle: React.CSSProperties = {
  display: "block",
  fontSize: 11,
  fontWeight: 500,
  color: "#5F5E5A",
  marginBottom: 4,
};
const inputStyle: React.CSSProperties = {
  width: "100%",
  fontSize: 13,
  padding: "8px 10px",
  border: "1px solid #d3d1c7",
  borderRadius: 7,
  boxSizing: "border-box",
};
const submitBtn: React.CSSProperties = {
  width: "100%",
  padding: "9px 0",
  background: "#185FA5",
  color: "#fff",
  border: "none",
  borderRadius: 7,
  fontSize: 13,
  fontWeight: 500,
  cursor: "pointer",
  marginTop: 4,
};
