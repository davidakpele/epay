"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import styles from "./Chart.module.css";

type Period = "DAILY" | "WEEKLY" | "MONTHLY";

interface TransactionPoint {
  label: string;
  count: number;
  amount: number;
}

interface AnalyticsResponse {
  period: Period;
  transactions: TransactionPoint[];
  generatedAt: string;
}

function getToken(): string | null {
  return (
    localStorage.getItem("accessToken") ??
    sessionStorage.getItem("accessToken")
  );
}

const PERIODS: Period[] = ["DAILY", "WEEKLY", "MONTHLY"];

export default function AnalyticsChart() {
  const canvasRef  = useRef<HTMLCanvasElement>(null);
  const chartRef   = useRef<any>(null);
  const [period,   setPeriod]  = useState<Period>("MONTHLY");
  const [loading,  setLoading] = useState(true);
  const [error,    setError]   = useState<string | null>(null);
  const [data,     setData]    = useState<AnalyticsResponse | null>(null);

  // ── Fetch analytics ───────────────────────────────────────────────────
  const fetchAnalytics = useCallback(async (p: Period) => {
    setLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) throw new Error("Not authenticated.");

      const res = await fetch(
        `http://localhost:8109/admin/dashboard/analytics?period=${p}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (!res.ok) throw new Error(`Server error: ${res.status}`);
      const json: AnalyticsResponse = await res.json();
      setData(json);
    } catch (err: any) {
      setError(err.message ?? "Failed to load analytics.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAnalytics(period); }, [period, fetchAnalytics]);

  // ── Build / update chart whenever data changes ────────────────────────
  useEffect(() => {
    if (!canvasRef.current || loading || !data) return;

    const labels       = data.transactions.map((t) => t.label);
    const transactions = data.transactions.map((t) => t.count);
    const revenue      = data.transactions.map((t) => t.amount);

    const init = () => {
      if (chartRef.current) chartRef.current.destroy();
      chartRef.current = new (window as any).Chart(
        canvasRef.current!.getContext("2d"),
        {
          type: "bar",
          data: {
            labels,
            datasets: [
              {
                label: "Transactions",
                data: transactions,
                backgroundColor: "rgba(22,103,1,0.85)",
                borderRadius: 4,
                borderSkipped: false,
                order: 2,
              },
              {
                label: "Revenue (₦)",
                data: revenue,
                type: "line",
                borderColor: "#74c69d",
                backgroundColor: "rgba(116,198,157,0.1)",
                borderWidth: 2.5,
                pointBackgroundColor: "#74c69d",
                pointRadius: 3,
                pointHoverRadius: 5,
                tension: 0.4,
                fill: true,
                order: 1,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: "index", intersect: false },
            plugins: {
              legend: { display: false },
              tooltip: {
                backgroundColor: "#1a2e1e",
                titleFont: { family: "Sora, sans-serif", size: 11, weight: "600" },
                bodyFont:  { family: "DM Mono, monospace", size: 10 },
                padding: 10,
                cornerRadius: 9,
                callbacks: {
                  label: (c: any) =>
                    c.dataset.label === "Revenue (₦)"
                      ? ` Revenue: ₦${c.parsed.y.toLocaleString("en-NG", { minimumFractionDigits: 2 })}`
                      : ` Transactions: ${c.parsed.y.toLocaleString()}`,
                },
              },
            },
            scales: {
              x: {
                grid: { display: false },
                ticks: { font: { family: "Sora, sans-serif", size: 9 }, color: "#9eada5" },
              },
              y: {
                grid: { color: "rgba(0,0,0,0.04)" },
                ticks: {
                  font: { family: "DM Mono, monospace", size: 9 },
                  color: "#9eada5",
                  callback: (v: number) => v >= 1000 ? (v / 1000).toFixed(0) + "k" : v,
                },
              },
            },
          },
        }
      );
    };

    if ((window as any).Chart) {
      init();
    } else {
      const s = document.createElement("script");
      s.src = "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js";
      s.onload = init;
      document.head.appendChild(s);
    }

    return () => { chartRef.current?.destroy(); };
  }, [data, loading]);

  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">Analytics</span>

        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          {/* Period toggle */}
          <div style={{
            display: "flex",
            background: "rgba(0,0,0,0.06)",
            borderRadius: 8,
            padding: 3,
            gap: 2,
          }}>
            {PERIODS.map((p) => (
              <button
                key={p}
                onClick={() => setPeriod(p)}
                style={{
                  padding: "4px 12px",
                  borderRadius: 6,
                  border: "none",
                  cursor: "pointer",
                  fontSize: "0.72rem",
                  fontWeight: 600,
                  fontFamily: "Sora, sans-serif",
                  transition: "all 0.15s",
                  background: period === p ? "#166701" : "transparent",
                  color:      period === p ? "#fff"    : "#9eada5",
                }}
              >
                {p.charAt(0) + p.slice(1).toLowerCase()}
              </button>
            ))}
          </div>

          {/* Legend */}
          <div className={styles.legend}>
            <span className={styles.legItem}><span className={styles.legDotDark} />Transactions</span>
            <span className={styles.legItem}><span className={styles.legDotLight} />Revenue</span>
          </div>
        </div>
      </div>

      <div className="card-body">
        <div className={styles.chartWrap} style={{ position: "relative" }}>

          {/* Loading overlay */}
          {loading && (
            <div style={{
              position: "absolute", inset: 0,
              display: "flex", alignItems: "center", justifyContent: "center",
              background: "rgba(255,255,255,0.6)",
              borderRadius: 8,
              zIndex: 10,
            }}>
              <span style={{ fontSize: "0.8rem", color: "#166701", fontFamily: "Sora, sans-serif", fontWeight: 600 }}>
                Loading…
              </span>
            </div>
          )}

          {/* Error state */}
          {error && !loading && (
            <div style={{
              position: "absolute", inset: 0,
              display: "flex", alignItems: "center", justifyContent: "center",
              flexDirection: "column", gap: 8,
              background: "rgba(255,255,255,0.85)",
              borderRadius: 8,
              zIndex: 10,
            }}>
              <span style={{ fontSize: "0.8rem", color: "#c0392b", fontFamily: "Sora, sans-serif" }}>{error}</span>
              <button
                onClick={() => fetchAnalytics(period)}
                style={{
                  padding: "5px 14px", background: "#166701", color: "#fff",
                  border: "none", borderRadius: 6, cursor: "pointer",
                  fontSize: "0.75rem", fontFamily: "Sora, sans-serif", fontWeight: 600,
                }}
              >
                Retry
              </button>
            </div>
          )}

          {/* Empty state */}
          {!loading && !error && data?.transactions.length === 0 && (
            <div style={{
              position: "absolute", inset: 0,
              display: "flex", alignItems: "center", justifyContent: "center",
              background: "rgba(255,255,255,0.85)", borderRadius: 8, zIndex: 10,
            }}>
              <span style={{ fontSize: "0.8rem", color: "#9eada5", fontFamily: "Sora, sans-serif" }}>
                No data for this period.
              </span>
            </div>
          )}

          <canvas ref={canvasRef} />
        </div>
      </div>
    </div>
  );
}