"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import styles from "./Chart.module.css";

interface DashboardSummary {
  totalUsers: number;
  totalHistory: number;
  totalVirtualCards: number;
  platformTotalBalance: string;
  totalActiveAlerts: number;
}

interface TransactionPoint {
  label: string;
  count: number;
  amount: number;
}

interface AnalyticsResponse {
  period: string;
  transactions: TransactionPoint[];
}

interface DonutSlice {
  label: string;
  value: number;
  pct: string;
  color: string;
}

function getToken(): string | null {
  return (
    localStorage.getItem("accessToken") ??
    sessionStorage.getItem("accessToken")
  );
}

/**
 * Derives user segments from available API data:
 *
 * - Active Users  = users who appear in transaction history this period
 *   Capped at totalUsers. We use totalHistory as a proxy signal:
 *   if totalHistory > 0 we assume at least 1 user is active.
 *   Active = min(totalHistory, totalUsers)
 * - New Users     = totalActiveAlerts used as a new-signup proxy,
 *   or a fixed 10% floor if no signal exists
 * - Inactive Users = remainder
 *
 * NOTE: Replace this logic once the backend exposes explicit
 * user-status fields (e.g. activeUsers, newUsers).
 */
function buildDonutSlices(summary: DashboardSummary, analytics: AnalyticsResponse): DonutSlice[] {
  const total = summary.totalUsers || 1; // avoid div/0

  const activeRaw  = Math.min(summary.totalHistory, total);
  const active     = Math.max(activeRaw, Math.round(total * 0.1)); // floor 10%
  const newUsers   = Math.min(summary.totalActiveAlerts || Math.round(total * 0.1), total - active);
  const inactive   = Math.max(total - active - newUsers, 0);

  const pct = (n: number) => `${Math.round((n / total) * 100)}%`;

  return [
    { label: "Active Users",   value: active,   pct: pct(active),   color: "#52b788" },
    { label: "New Users",      value: newUsers,  pct: pct(newUsers),  color: "#4895d4" },
    { label: "Inactive Users", value: inactive,  pct: pct(inactive),  color: "#f8c630" },
  ];
}

export default function UserStatsChart() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const chartRef  = useRef<any>(null);

  const [slices,  setSlices]  = useState<DonutSlice[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) throw new Error("Not authenticated.");

      const headers = { Authorization: `Bearer ${token}` };

      const [summaryRes, analyticsRes] = await Promise.all([
        fetch("http://localhost:8109/admin/dashboard/summary",           { headers }),
        fetch("http://localhost:8109/admin/dashboard/analytics?period=MONTHLY", { headers }),
      ]);

      if (!summaryRes.ok)   throw new Error(`Summary error: ${summaryRes.status}`);
      if (!analyticsRes.ok) throw new Error(`Analytics error: ${analyticsRes.status}`);

      const summary:   DashboardSummary  = await summaryRes.json();
      const analytics: AnalyticsResponse = await analyticsRes.json();

      setSlices(buildDonutSlices(summary, analytics));
    } catch (err: any) {
      setError(err.message ?? "Failed to load user stats.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  // ── Draw / redraw chart whenever slices change ────────────────────────
  useEffect(() => {
    if (!canvasRef.current || loading || !slices) return;

    const init = () => {
      if (chartRef.current) chartRef.current.destroy();
      chartRef.current = new (window as any).Chart(
        canvasRef.current!.getContext("2d"),
        {
          type: "doughnut",
          data: {
            labels: slices.map((d) => d.label),
            datasets: [{
              data:            slices.map((d) => d.value),
              backgroundColor: slices.map((d) => d.color),
              borderWidth: 0,
              hoverOffset: 6,
            }],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "62%",
            plugins: {
              legend: { display: false },
              tooltip: {
                backgroundColor: "#1a2e1e",
                titleFont: { family: "Sora, sans-serif", size: 11 },
                bodyFont:  { family: "DM Mono, monospace", size: 11 },
                padding: 10,
                cornerRadius: 8,
                callbacks: {
                  label: (c: any) => {
                    const s = slices[c.dataIndex];
                    return ` ${s.label}: ${s.value} (${s.pct})`;
                  },
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
  }, [slices, loading]);

  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">User Statistics</span>
      </div>
      <div className="card-body">

        {/* Loading */}
        {loading && (
          <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: 180 }}>
            <span style={{ fontSize: "0.8rem", color: "#166701", fontFamily: "Sora, sans-serif", fontWeight: 600 }}>
              Loading…
            </span>
          </div>
        )}

        {/* Error */}
        {error && !loading && (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", height: 180, gap: 10 }}>
            <span style={{ fontSize: "0.8rem", color: "#c0392b", fontFamily: "Sora, sans-serif" }}>{error}</span>
            <button
              onClick={fetchData}
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

        {/* Chart + legend */}
        {!loading && !error && slices && (
          <>
            <div className={styles.donutWrap}>
              <canvas ref={canvasRef} />
            </div>
            <div className={styles.donutLegend}>
              {slices.map((d) => (
                <div key={d.label} className={styles.donutRow}>
                  <div className={styles.donutLeft}>
                    <span className={styles.donutDot} style={{ background: d.color }} />
                    {d.label}
                  </div>
                  <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end" }}>
                    <span className={styles.donutPct}>{d.pct}</span>
                    <span style={{ fontSize: "0.7rem", color: "#9eada5", fontFamily: "DM Mono, monospace" }}>
                      {d.value.toLocaleString()} users
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

      </div>
    </div>
  );
}