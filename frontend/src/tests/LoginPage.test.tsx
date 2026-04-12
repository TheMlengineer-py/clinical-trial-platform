import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import LoginPage from "../pages/LoginPage";
import * as AuthContext from "../context/AuthContext";

const mockSignIn = vi.fn();

vi.mock("../context/AuthContext", async () => {
  const actual = await vi.importActual<typeof AuthContext>(
    "../context/AuthContext",
  );
  return {
    ...actual,
    useAuth: () => ({
      user: null,
      isLoading: false,
      signIn: mockSignIn,
      signOut: vi.fn(),
    }),
  };
});

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return { ...actual, useNavigate: () => mockNavigate };
});

const renderLogin = () =>
  render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );

describe("LoginPage", () => {
  beforeEach(() => {
    mockSignIn.mockReset();
    mockNavigate.mockReset();
  });

  it("renders the brand name", () => {
    renderLogin();
    expect(screen.getByText("ClinicalTrials")).toBeInTheDocument();
  });

  it("renders username and password inputs", () => {
    renderLogin();
    expect(screen.getByLabelText("Username")).toBeInTheDocument();
    expect(screen.getByLabelText("Password")).toBeInTheDocument();
  });

  it("shows validation error when fields are empty", async () => {
    renderLogin();
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));
    expect(
      await screen.findByText("Username and password are required"),
    ).toBeInTheDocument();
  });

  it("calls signIn with entered credentials", async () => {
    mockSignIn.mockResolvedValue(undefined);
    renderLogin();
    fireEvent.change(screen.getByLabelText("Username"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "password" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));
    await waitFor(() =>
      expect(mockSignIn).toHaveBeenCalledWith({
        username: "admin",
        password: "password",
      }),
    );
  });

  it("shows error banner when signIn rejects", async () => {
    mockSignIn.mockRejectedValue(new Error("Invalid credentials"));
    renderLogin();
    fireEvent.change(screen.getByLabelText("Username"), {
      target: { value: "bad" },
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "wrong" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Sign in" }));
    expect(await screen.findByText("Invalid credentials")).toBeInTheDocument();
  });
});
