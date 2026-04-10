/**
 * TypeScript types for the Patient domain, mirroring Java PatientResponse record.
 */

/** Mirrors Java PatientResponse record. */
export interface Patient {
  id: number;
  name: string;
  age: number;
  condition: string;
  enrolledStudyId: number | null; // null = not enrolled
  recruitedAt: string | null; // ISO-8601 or null
}

/** Payload for POST /api/patients and PUT /api/patients/{id}. */
export interface PatientRequest {
  name: string;
  age: number;
  condition: string;
}

/** Payload for POST /api/recruitment. */
export interface RecruitmentRequest {
  patientId: number;
  studyId: number;
}

/** Mirrors Spring Page<PatientResponse> response. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
