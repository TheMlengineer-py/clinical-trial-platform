export type Role = "ADMIN" | "RESEARCHER";

export interface AuthUser {
  username: string;
  role: Role;
}
