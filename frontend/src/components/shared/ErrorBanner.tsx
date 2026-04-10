/**
 * Red error banner displayed when an API call fails.
 * Receives the error message from the Axios interceptor via React Query.
 */
export default function ErrorBanner({ message }: { message: string }) {
  return (
    <div
      style={{
        background: "#FCEBEB",
        border: "1px solid #F7C1C1",
        borderRadius: 8,
        padding: "10px 16px",
        color: "#A32D2D",
        fontSize: 13,
        marginBottom: 12,
      }}
    >
      {message}
    </div>
  );
}
