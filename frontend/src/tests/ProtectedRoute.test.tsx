import { render, screen } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";
import { vi } from "vitest";
import ProtectedRoute from "../components/shared/ProtectedRoute";
import * as AuthContext from "../context/AuthContext";

vi.mock("../context/AuthContext", async () => {
  const actual = await vi.importActual<typeof AuthContext>(
    "../context/AuthContext",
  );
  return { ...actual, useAuth: vi.fn() };
});

const mockUseAuth = AuthContext.useAuth as ReturnType<typeof vi.fn>;

const renderProtected = () =>
  render(
    <MemoryRouter initialEntries={["/protected"]}>
      <Routes>
        <Route path="/login" element={<div>Login Page</div>} />
        <Route
          path="/protected"
          element={
            <ProtectedRoute>
              <div>Protected Content</div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>,
  );

describe("ProtectedRoute", () => {
  it("shows loading spinner while auth is loading", () => {
    mockUseAuth.mockReturnValue({ user: null, isLoading: true });
    renderProtected();
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("redirects to /login when user is null", () => {
    mockUseAuth.mockReturnValue({ user: null, isLoading: false });
    renderProtected();
    expect(screen.getByText("Login Page")).toBeInTheDocument();
  });

  it("renders children when user is authenticated", () => {
    mockUseAuth.mockReturnValue({
      user: { username: "admin", role: "ADMIN" },
      isLoading: false,
    });
    renderProtected();
    expect(screen.getByText("Protected Content")).toBeInTheDocument();
  });
});
