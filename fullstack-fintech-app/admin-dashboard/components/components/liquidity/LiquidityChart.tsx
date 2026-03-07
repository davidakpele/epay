"use client";

import { useEffect, useRef, useState } from "react";
import styles from "./LiquidityChart.module.css";

type Period = "Today" | "Weekly" | "Monthly";

const DATA: Record<Period, { labels: string[]; deposits: number[]; withdrawals: number[] }> = {
  Today: {
    labels:      ["8am", "10am", "12pm", "2pm", "4pm", "6pm", "8pm"],
    deposits:    [8200000, 8500000, 9100000, 8800000, 9300000, 9000000, 9500000],
    withdrawals: [3200000, 3400000, 3300000, 3600000, 3500000, 3700000, 3600000],
  },
  Weekly: {
    labels:      ["Apr 18","Apr 19","Apr 20","Apr 21","Apr 22","Apr 23","Apr 24"],
    deposits:    [8000000, 7900000, 9400000, 8600000, 8200000, 8900000, 9800000],
    withdrawals: [3300000, 3500000, 3400000, 3700000, 3600000, 3500000, 3800000],
  },
  Monthly: {
    labels:      ["Week 1","Week 2","Week 3","Week 4"],
    deposits:    [7500000, 8800000, 9200000, 9600000],
    withdrawals: [3100000, 3500000, 3700000, 3900000],
  },
};

export default function LiquidityChart() {
  const canvasRef  = useRef<HTMLCanvasElement>(null);
  const chartRef   = useRef<unknown>(null);
  const [period, setPeriod] = useState<Period>("Weekly");
  const [ready, setReady]   = useState(false);

  useEffect(() => {
    const existing = document.querySelector('script[data-chartjs]');
    if (existing) { setReady(true); return; }
    const s = document.createElement("script");
    s.src = "https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js";
    s.dataset.chartjs = "true";
    s.onload = () => setReady(true);
    document.head.appendChild(s);
  }, []);

  useEffect(() => {
    if (!ready || !canvasRef.current) return;
    const Chart = (window as unknown as { Chart: new (...a: unknown[]) => unknown }).Chart;
    if (!Chart) return;

    if (chartRef.current) (chartRef.current as { destroy: () => void }).destroy();

    const d = DATA[period];

    chartRef.current = new Chart(canvasRef.current, {
      type: "line",
      data: {
        labels: d.labels,
        datasets: [
          {
            label:           "Deposits",
            data:            d.deposits,
            borderColor:     "#4db825",
            backgroundColor: "rgba(77,184,37,0.10)",
            borderWidth:     2.5,
            pointBackgroundColor: "#fff",
            pointBorderColor:    "#4db825",
            pointBorderWidth:    2,
            pointRadius:         5,
            pointHoverRadius:    7,
            fill:            true,
            tension:         0.35,
          },
          {
            label:           "Withdrawals",
            data:            d.withdrawals,
            borderColor:     "#e05555",
            backgroundColor: "rgba(224,85,85,0.05)",
            borderWidth:     2,
            pointBackgroundColor: "#fff",
            pointBorderColor:    "#e05555",
            pointBorderWidth:    2,
            pointRadius:         4,
            pointHoverRadius:    6,
            fill:            false,
            tension:         0.35,
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
            backgroundColor: "#fff",
            titleColor:      "#2a3a2a",
            bodyColor:       "#5a6a5a",
            borderColor:     "#e4ebe6",
            borderWidth:     1,
            padding:         10,
            callbacks: {
              label: (ctx: { dataset: { label: string }; parsed: { y: number } }) =>
                ` ${ctx.dataset.label}: ₦${ctx.parsed.y.toLocaleString()}`,
            },
          },
        },
        scales: {
          x: {
            grid:  { color: "rgba(0,0,0,0.04)" },
            ticks: { font: { family: "'Sora', sans-serif", size: 11 }, color: "#8aaa8a" },
          },
          y: {
            grid:  { color: "rgba(0,0,0,0.05)" },
            ticks: {
              font: { family: "'Sora', sans-serif", size: 11 },
              color: "#8aaa8a",
              callback: (v: unknown) => `₦ ${Number(v).toLocaleString()}`,
            },
          },
        },
      },
    });
  }, [ready, period]);

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <h3 className={styles.title}>Liquidity Overview</h3>
        <div className={styles.periodBtns}>
          {(["Today","Weekly","Monthly"] as Period[]).map(p => (
            <button
              key={p}
              className={`${styles.periodBtn} ${period === p ? styles.periodBtnActive : ""}`}
              onClick={() => setPeriod(p)}
            >
              {p}
            </button>
          ))}
        </div>
      </div>

      <div className={styles.chartWrap}>
        <canvas ref={canvasRef} />
      </div>

      {/* Legend */}
      <div className={styles.legend}>
        <span className={styles.legendItem}>
          <span className={styles.legendDot} style={{ background: "#4db825" }} />
          Deposits
        </span>
        <span className={styles.legendItem}>
          <span className={styles.legendDot} style={{ background: "#e05555" }} />
          Withdrawals
        </span>
      </div>
    </div>
  );
}