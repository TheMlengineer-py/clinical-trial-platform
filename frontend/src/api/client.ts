import axios from "axios";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  headers: { "Content-Type": "application/json" },
  timeout: 10_000,
  withCredentials: true,
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const message: string =
      error.response?.data?.message ??
      error.message ??
      "An unexpected error occurred";
    return Promise.reject(new Error(message));
  },
);

export default client;
