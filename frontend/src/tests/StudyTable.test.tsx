import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import StudyTable from "../components/studies/StudyTable";
import * as AuthContext from "../context/AuthContext";

vi.mock("../hooks/useStudies", () => ({
  useStudies: () => ({
    data: {
      content: [
        {
          id: 1,
          title: "BRCA Trial",
          status: "OPEN",
          maxEnrollment: 25,
          currentEnrollment: 10,
          eligibilityCriteria: "age>18",
          lastRecruitedAt: null,
        },
      ],
      totalPages: 1,
    },
    isLoading: false,
    isError: false,
  }),
  useDeleteStudy: () => ({ mutate: vi.fn() }),
  useTransitionStudy: () => ({ mutate: vi.fn(), isPending: false }),
}));

vi.mock("../context/AuthContext", async () => {
  const actual = await vi.importActual<typeof AuthContext>(
    "../context/AuthContext",
  );
  return { ...actual, useAuth: vi.fn() };
});

const mockUseAuth = AuthContext.useAuth as ReturnType<typeof vi.fn>;

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={new QueryClient()}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
);

describe("StudyTable — admin view", () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      user: { username: "admin", role: "ADMIN" },
      isLoading: false,
    });
  });

  it("renders study title", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("BRCA Trial")).toBeInTheDocument();
  });

  it("renders OPEN status badge", () => {
    render(<StudyTable />, { wrapper });
    expect(
      screen.getAllByText("OPEN").some((el) => el.tagName === "SPAN"),
    ).toBe(true);
  });

  it("renders New study button for admin", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("+ New study")).toBeInTheDocument();
  });

  it("renders Edit and Delete buttons for admin", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("Edit")).toBeInTheDocument();
    expect(screen.getByText("Delete")).toBeInTheDocument();
  });
});

describe("StudyTable — researcher view", () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      user: { username: "researcher", role: "RESEARCHER" },
      isLoading: false,
    });
  });

  it("does NOT render New study button", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.queryByText("+ New study")).not.toBeInTheDocument();
  });

  it("does NOT render Edit button", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.queryByText("Edit")).not.toBeInTheDocument();
  });

  it("shows View only label", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("View only")).toBeInTheDocument();
  });
});
