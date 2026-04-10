/**
 * TypeScript types for the Study domain, mirroring the Java StudyResponse record.
 * Shared across API calls, components, and hooks to ensure consistency.
 */

/** Mirrors Java StudyStatus enum — all four lifecycle states. */
export type StudyStatus = "DRAFT" | "OPEN" | "CLOSED" | "ARCHIVED";

/** Mirrors Java StudyResponse record — outbound payload from the API. */
export interface Study {
  id: number;
  title: string;
  status: StudyStatus;
  maxEnrollment: number;
  currentEnrollment: number;
  eligibilityCriteria: string | null;
  lastRecruitedAt: string | null; // ISO-8601 timestamp string from Jackson
}

/** Payload for POST /api/studies and PUT /api/studies/{id}. */
export interface StudyRequest {
  title: string;
  maxEnrollment: number;
  eligibilityCriteria?: string;
}

/** Spring Data pagination wrapper returned by list endpoints. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page (zero-based)
  size: number;
}
