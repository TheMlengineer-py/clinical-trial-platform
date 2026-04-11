import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as patientsApi from "../api/patients";
import type { PatientRequest, RecruitmentRequest } from "../types/patient";

/**
 * React Query hooks for patient data.
 *
 * Polling: refetchInterval keeps patient enrollment status
 * up to date for all users without requiring a manual page refresh.
 */

export const patientKeys = {
  all: (condition?: string) => ["patients", { condition }] as const,
  detail: (id: number) => ["patients", id] as const,
};

/** Paginated patients list with optional condition filter.
 *  Polls every 30 seconds so recruitment status stays current. */
export const usePatients = (condition?: string, page = 0) =>
  useQuery({
    queryKey: patientKeys.all(condition),
    queryFn: () => patientsApi.getPatients(condition, page),
    refetchInterval: 30_000,
  });

/** Single patient detail. */
export const usePatient = (id: number) =>
  useQuery({
    queryKey: patientKeys.detail(id),
    queryFn: () => patientsApi.getPatientById(id),
    enabled: !!id,
    refetchInterval: 30_000,
  });

/** Create patient. */
export const useCreatePatient = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: PatientRequest) => patientsApi.createPatient(req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patients"] }),
  });
};

/** Update patient fields. */
export const useUpdatePatient = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: number; req: PatientRequest }) =>
      patientsApi.updatePatient(id, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patients"] }),
  });
};

/** Delete patient. */
export const useDeletePatient = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => patientsApi.deletePatient(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["patients"] }),
  });
};

/**
 * Recruit patient into study.
 * Invalidates both patients and studies caches on success
 * because the study's currentEnrollment and lastRecruitedAt change too.
 */
export const useRecruitPatient = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: RecruitmentRequest) => patientsApi.recruitPatient(req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["patients"] });
      qc.invalidateQueries({ queryKey: ["studies"] });
    },
  });
};
