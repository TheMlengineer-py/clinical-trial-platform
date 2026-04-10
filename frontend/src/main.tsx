import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import App from "./App";

/**
 * Application bootstrap.
 *
 * QueryClient config:
 * - staleTime: 30s — data is considered fresh for 30 seconds before background refetch.
 *   Prevents hammering the Render free tier on every component mount.
 * - retry: 1 — retry failed queries once before showing an error.
 *   Helps with Render cold-start latency without masking real errors.
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
    },
  },
});

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>,
);
