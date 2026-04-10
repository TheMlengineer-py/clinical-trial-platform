interface Props {
  page: number; // current zero-based page
  totalPages: number;
  onPageChange: (page: number) => void;
}

/**
 * Simple prev/next pagination control.
 * Page numbers are displayed as 1-based to match user expectations.
 */
export default function Pagination({ page, totalPages, onPageChange }: Props) {
  if (totalPages <= 1) return null;

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        gap: 8,
        padding: "10px 16px",
        borderTop: "1px solid #e5e5e0",
        fontSize: 12,
        color: "#5F5E5A",
      }}
    >
      <button
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
        style={btnStyle}
      >
        ←
      </button>
      <span>
        Page {page + 1} of {totalPages}
      </span>
      <button
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
        style={btnStyle}
      >
        →
      </button>
    </div>
  );
}

const btnStyle: React.CSSProperties = {
  padding: "3px 9px",
  border: "1px solid #d3d1c7",
  borderRadius: 5,
  background: "white",
  cursor: "pointer",
  fontSize: 12,
};
