import client from "./client";
import type {
  Patient,
  PatientRequest,
  RecruitmentRequest,
  Page,
} from "../types/patient";

/**
 * API module for all patient and recruitment HTTP calls.
 */

/** GET /api/patients — paginated, optionally filtered by condition. */
export const getPatients = async (
  condition?: string,
  page = 0,
  size = 10,
): Promise<Page<Patient>> => {
  const params: Record<string, unknown> = { page, size };
  if (condition) params.condition = condition;
  const { data } = await client.get<Page<Patient>>("/patients", { params });
  return data;
};

/** GET /api/patients/:id — fetch a single patient. */
export const getPatientById = async (id: number): Promise<Patient> => {
  const { data } = await client.get<Patient>(`/patients/${id}`);
  return data;
};

/** POST /api/patients — create a new patient. */
export const createPatient = async (req: PatientRequest): Promise<Patient> => {
  const { data } = await client.post<Patient>("/patients", req);
  return data;
};

/** PUT /api/patients/:id — update patient fields (not enrolment). */
export const updatePatient = async (
  id: number,
  req: PatientRequest,
): Promise<Patient> => {
  const { data } = await client.put<Patient>(`/patients/${id}`, req);
  return data;
};

/** DELETE /api/patients/:id — delete a patient record. */
export const deletePatient = async (id: number): Promise<void> => {
  await client.delete(`/patients/${id}`);
};

/**
 * POST /api/recruitment — recruit a patient into a study.
 * This is the core operation — all business guards run server-side.
 */
export const recruitPatient = async (
  req: RecruitmentRequest,
): Promise<Patient> => {
  const { data } = await client.post<Patient>("/recruitment", req);
  return data;
};
