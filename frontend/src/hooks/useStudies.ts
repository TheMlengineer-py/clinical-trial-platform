import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import * as studiesApi from "../api/studies";
import type { StudyStatus, StudyRequest } from "../types/study";

/**
 * React Query hooks for study data.
 *
 * Why React Query over Redux?
 * Studies and patients are server state — the source of truth is the backend.
 * React Query handles caching, background refetching, loading/error states,
 * and cache invalidation automatically. Redux would require significant
 * boilerplate for the same behaviour with no real benefit here.
 *
 * Polling: refetchInterval keeps enrollment counts and study statuses
 * up to date for all users without requiring a manual page refresh.
 */

/** Query key factory — consistent keys prevent stale cache issues. */
export const studyKeys = {
  all: (status?: StudyStatus) => ["studies", { status }] as const,
  detail: (id: number) => ["studies", id] as const,
};

/** Paginated studies list with optional status filter.
 *  Polls every 30 seconds so enrollment counts stay current. */
export const useStudies = (status?: StudyStatus, page = 0) =>
  useQuery({
    queryKey: studyKeys.all(status),
    queryFn: () => studiesApi.getStudies(status, page),
    refetchInterval: 30_000,
  });

/** Single study by ID. */
export const useStudy = (id: number) =>
  useQuery({
    queryKey: studyKeys.detail(id),
    queryFn: () => studiesApi.getStudyById(id),
    enabled: !!id,
    refetchInterval: 30_000,
  });

/** Create study — invalidates the studies list on success. */
export const useCreateStudy = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: StudyRequest) => studiesApi.createStudy(req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["studies"] }),
  });
};

/** Update study fields. */
export const useUpdateStudy = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: number; req: StudyRequest }) =>
      studiesApi.updateStudy(id, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["studies"] }),
  });
};

/** Advance study lifecycle. */
export const useTransitionStudy = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: StudyStatus }) =>
      studiesApi.transitionStudyStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["studies"] }),
  });
};

/** Delete study. */
export const useDeleteStudy = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => studiesApi.deleteStudy(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["studies"] }),
  });
};
