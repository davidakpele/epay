"use client";

import { useEffect, useRef } from "react";
import styles from "./Chart.module.css";
import { CHART_MONTHS, CHART_REVENUE, CHART_TRANSACTIONS } from "@/app/lib/data";

export default function AnalyticsChart() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const chartRef  = useRef<any>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    const init = () => {
      if (chartRef.current) chartRef.current.destroy();
      chartRef.current = new (window as any).Chart(canvasRef.current!.getContext("2d"), {
        type: "bar",
        data: {
          labels: CHART_MONTHS,
          datasets: [
            {
              label: "Transactions",
              data: CHART_TRANSACTIONS,
              backgroundColor: "rgba(22,103,1,0.85)",
              borderRadius: 4,
              borderSkipped: false,
              order: 2,
            },
            {
              label: "Revenue",
              data: CHART_REVENUE,
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
              callbacks: { label: (c: any) => ` ${c.dataset.label}: ${c.parsed.y.toLocaleString()}` },
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
        <span className="card-title">Analytics</span>
        <div className={styles.legend}>
          <span className={styles.legItem}><span className={styles.legDotDark} />Transactions</span>
          <span className={styles.legItem}><span className={styles.legDotLight} />Revenue</span>
        </div>
      </div>
      <div className="card-body">
        <div className={styles.chartWrap}>
          <canvas ref={canvasRef} />
        </div>
      </div>
    </div>
  );
}