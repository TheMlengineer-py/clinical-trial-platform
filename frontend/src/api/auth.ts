import client from "./client";
import type { AuthUser } from "../types/auth";

export interface LoginCredentials {
  username: string;
  password: string;
}

export const login = async (
  credentials: LoginCredentials,
): Promise<AuthUser> => {
  const { data } = await client.post<AuthUser>("/auth/login", credentials);
  return data;
};

export const getMe = async (): Promise<AuthUser | null> => {
  try {
    const { data } = await client.get<AuthUser>("/auth/me");
    return data;
  } catch {
    return null;
  }
};

export const logout = async (): Promise<void> => {
  await client.post("/auth/logout");
};
