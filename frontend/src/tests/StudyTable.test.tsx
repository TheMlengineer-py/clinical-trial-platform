import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter } from "react-router-dom";
import { vi } from "vitest";
import StudyTable from "../components/studies/StudyTable";

// Mock the hook so we don't need a real API in tests
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

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={new QueryClient()}>
    <MemoryRouter>{children}</MemoryRouter>
  </QueryClientProvider>
);

describe("StudyTable", () => {
  it("renders study title in the table", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("BRCA Trial")).toBeInTheDocument();
  });

  it("renders the OPEN status badge", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("OPEN")).toBeInTheDocument();
  });

  it("renders the New study button", () => {
    render(<StudyTable />, { wrapper });
    expect(screen.getByText("+ New study")).toBeInTheDocument();
  });
});
