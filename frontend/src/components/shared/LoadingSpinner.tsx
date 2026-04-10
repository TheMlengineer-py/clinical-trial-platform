/**
 * Full-width centred loading indicator.
 * Displayed while React Query is fetching data.
 */
export default function LoadingSpinner() {
  return (
    <div style={{ textAlign: "center", padding: "48px", color: "#888" }}>
      Loading...
    </div>
  );
}
