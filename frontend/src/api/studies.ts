import client from "./client";
import type { Study, StudyRequest, StudyStatus, Page } from "../types/study";

/**
 * API module for all study-related HTTP calls.
 * Each function maps 1-to-1 with a backend endpoint.
 * React Query hooks in useStudies.ts call these functions.
 */

/** GET /api/studies — paginated, optionally filtered by status. */
export const getStudies = async (
  status?: StudyStatus,
  page = 0,
  size = 10,
): Promise<Page<Study>> => {
  const params: Record<string, unknown> = {
    page,
    size,
    sort: "lastRecruitedAt,desc",
  };
  if (status) params.status = status;
  const { data } = await client.get<Page<Study>>("/studies", { params });
  return data;
};

/** GET /api/studies/:id — fetch a single study. */
export const getStudyById = async (id: number): Promise<Study> => {
  const { data } = await client.get<Study>(`/studies/${id}`);
  return data;
};

/** POST /api/studies — create a new study (starts in DRAFT). */
export const createStudy = async (req: StudyRequest): Promise<Study> => {
  const { data } = await client.post<Study>("/studies", req);
  return data;
};

/** PUT /api/studies/:id — update mutable study fields. */
export const updateStudy = async (
  id: number,
  req: StudyRequest,
): Promise<Study> => {
  const { data } = await client.put<Study>(`/studies/${id}`, req);
  return data;
};

/**
 * PATCH /api/studies/:id/status — advance the study lifecycle.
 * Throws if the transition is invalid (409 from backend).
 */
export const transitionStudyStatus = async (
  id: number,
  status: StudyStatus,
): Promise<Study> => {
  const { data } = await client.patch<Study>(`/studies/${id}/status`, null, {
    params: { status },
  });
  return data;
};

/** DELETE /api/studies/:id — delete a non-OPEN study. */
export const deleteStudy = async (id: number): Promise<void> => {
  await client.delete(`/studies/${id}`);
};
