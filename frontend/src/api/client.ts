import axios from "axios";

/**
 * Global Axios instance used by all API modules.
 *
 * baseURL: Read from VITE_API_BASE_URL environment variable.
 *  - Local dev:  http://localhost:8080/api (or via Vite proxy)
 *  - Production: https://your-app.onrender.com/api (set in Netlify dashboard)
 *
 * The response interceptor normalises all API errors into a single
 * Error shape with a human-readable message — components only need
 * to handle one error type.
 */
const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  headers: { "Content-Type": "application/json" },
  timeout: 10_000, // 10s — reasonable for Render's cold-start latency
});

/**
 * Response interceptor: extracts the structured error message from the
 * GlobalExceptionHandler envelope and forwards it as a plain Error.
 *
 * Backend envelope shape:
 * { timestamp, status, error, message }
 *
 * Components receive: new Error("Study 3 is not OPEN for recruitment")
 */
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const message: string =
      error.response?.data?.message ?? // structured backend error
      error.message ?? // axios/network error
      "An unexpected error occurred";
    return Promise.reject(new Error(message));
  },
);

export default client;
