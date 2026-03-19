"use client";

import { useEffect, useRef } from "react";
import styles from "./Chart.module.css";

const DONUT_DATA = [
  { label: "New Users",      pct: "22%", color: "#4895d4" },
  { label: "Active Users",   pct: "65%", color: "#52b788" },
  { label: "Inactive Users", pct: "12%", color: "#f8c630" },
];

export default function UserStatsChart() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const chartRef  = useRef<any>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    const init = () => {
      if (chartRef.current) chartRef.current.destroy();
      chartRef.current = new (window as any).Chart(canvasRef.current!.getContext("2d"), {
        type: "doughnut",
        data: {
          labels: DONUT_DATA.map((d) => d.label),
          datasets: [{
            data: [22, 65, 13],
            backgroundColor: DONUT_DATA.map((d) => d.color),
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
              callbacks: { label: (c: any) => ` ${c.label}: ${c.parsed}%` },
            },
          },
        },
      });
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
  }, []);

  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">User Statistics</span>
      </div>
      <div className="card-body">
        <div className={styles.donutWrap}>
          <canvas ref={canvasRef} />
        </div>
        <div className={styles.donutLegend}>
          {DONUT_DATA.map((d) => (
            <div key={d.label} className={styles.donutRow}>
              <div className={styles.donutLeft}>
                <span className={styles.donutDot} style={{ background: d.color }} />
                {d.label}
              </div>
              <span className={styles.donutPct}>{d.pct}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}