import { render, screen, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { vi } from "vitest";
import PatientForm from "../components/patients/PatientForm";

const mockCreate = vi.fn();
vi.mock("../hooks/usePatients", () => ({
  useCreatePatient: () => ({ mutate: mockCreate, isPending: false }),
  useUpdatePatient: () => ({ mutate: vi.fn(), isPending: false }),
}));

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <QueryClientProvider client={new QueryClient()}>
    {children}
  </QueryClientProvider>
);

describe("PatientForm", () => {
  it("renders all input fields", () => {
    render(<PatientForm onClose={vi.fn()} />, { wrapper });
    expect(screen.getByPlaceholderText("Patient name")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("e.g. NSCLC")).toBeInTheDocument();
  });

  it("shows validation error when name is empty", () => {
    render(<PatientForm onClose={vi.fn()} />, { wrapper });
    fireEvent.click(screen.getByRole("button", { name: "Add patient" }));
    expect(screen.getByText("Name is required")).toBeInTheDocument();
  });

  it("calls create mutation with correct data", () => {
    render(<PatientForm onClose={vi.fn()} />, { wrapper });
    fireEvent.change(screen.getByPlaceholderText("Patient name"), {
      target: { value: "Bob" },
    });
    fireEvent.change(screen.getByPlaceholderText("0"), {
      target: { value: "45" },
    });
    fireEvent.change(screen.getByPlaceholderText("e.g. NSCLC"), {
      target: { value: "NSCLC" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Add patient" }));
    expect(mockCreate).toHaveBeenCalledWith(
      { name: "Bob", age: 45, condition: "NSCLC" },
      expect.any(Object),
    );
  });
});
