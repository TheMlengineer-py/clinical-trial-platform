import { render, screen } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi } from "vitest";
import RecruitModal from "../components/patients/RecruitModal";
import type { Patient } from "../types/patient";

vi.mock("../hooks/usePatients", () => ({
  useRecruitPatient: () => ({ mutate: vi.fn(), isPending: false }),
}));

vi.mock("../hooks/useStudies", () => ({
  useStudies: () => ({
    data: {
      content: [
        {
          id: 2,
          title: "Lung Trial",
          status: "OPEN",
          maxEnrollment: 20,
          currentEnrollment: 8,
        },
      ],
    },
  }),
}));

const patient: Patient = {
  id: 1,
  name: "Alice",
  age: 30,
  condition: "NSCLC",
  enrolledStudyId: null,
  recruitedAt: null,
};

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={new QueryClient()}>
    {children}
  </QueryClientProvider>
);

describe("RecruitModal", () => {
  it("shows patient name in the summary card", () => {
    render(<RecruitModal patient={patient} onClose={vi.fn()} />, { wrapper });
    expect(screen.getByText("Alice")).toBeInTheDocument();
  });

  it("lists the OPEN study in the dropdown", () => {
    render(<RecruitModal patient={patient} onClose={vi.fn()} />, { wrapper });
    expect(screen.getByText(/Lung Trial/)).toBeInTheDocument();
  });

  it("renders the confirm recruitment button", () => {
    render(<RecruitModal patient={patient} onClose={vi.fn()} />, { wrapper });
    expect(screen.getByText("Confirm recruitment")).toBeInTheDocument();
  });
});
