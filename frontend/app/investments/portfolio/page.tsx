"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import {
  TrendingUp,
  Plus,
  Filter,
  MoreHorizontal,
  Calendar,
  ArrowRight,
  DollarSign,
  BarChart2,
  Shield,
  Headphones,
  ChevronDown,
  Calculator,
  Loader2,
  RefreshCw,
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import Sidebar from "@/components/Sidebar";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import MobileNav from "@/components/MobileNav";
import DepositModal from "@/components/DepositModal";
import LoadingScreen from "@/components/loader/Loadingscreen";
import SupportChatBot from "@/components/SupportChatBot";
import { Toast } from "@/app/types/auth";
import { getFiat, getToken, getUserId, getWallet } from "@/app/api/utils";
import { investmentService } from "@/app/api";
import "./MyInvestments.css";

/* ─── Types ───────────────────────────────────────────────────────────────── */
type Duration = "WEEKLY" | "MONTHLY" | "QUARTERLY" | "YEARLY";
type InvStatus = "ACTIVE" | "MATURED" | "PAID_OUT" | "FAILED";
type TabFilter = "All" | "Active" | "Matured" | "Withdrawn";

interface ApiInvestment {
  id: number;
  userId: number;
  walletId: number;
  currencyCode: string;
  principal: number;
  returnRate: number;
  expectedProfit: number;
  totalPayout: number;
  duration: Duration;
  durationDays: number;
  startDate: string;
  maturityDate: string;
  status: InvStatus;
  paidOutAt: string | null;
  referenceId: string;
  createdOn: string;
  updatedOn: string;
}

interface ChartPoint {
  label: string;
  value: number;
}

/* ─── Helpers ─────────────────────────────────────────────────────────────── */
const DURATION_LABELS: Record<Duration, string> = {
  WEEKLY: "Weekly Plan",
  MONTHLY: "Monthly Plan",
  QUARTERLY: "Quarterly Plan",
  YEARLY: "Yearly Plan",
};

const fmt = (n: number, sym = "₦") =>
  `${sym} ${(n ?? 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

const fmtDate = (iso: string | null | undefined) => {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
};

const progressPercent = (inv: ApiInvestment): number => {
  const start = new Date(inv.startDate).getTime();
  const end = new Date(inv.maturityDate).getTime();
  const now = Date.now();
  if (end <= start) return 100;
  const pct = ((now - start) / (end - start)) * 100;
  return Math.min(Math.max(Math.round(pct), 0), 100);
};

const tabStatusMatch = (inv: ApiInvestment, tab: TabFilter): boolean => {
  if (tab === "All") return true;
  if (tab === "Active") return inv.status === "ACTIVE";
  if (tab === "Matured")
    return inv.status === "MATURED" || inv.status === "PAID_OUT";
  if (tab === "Withdrawn") return inv.status === "PAID_OUT";
  return true;
};

const statusBadgeClass = (s: InvStatus) => {
  switch (s) {
    case "ACTIVE":
      return "mi-status-badge--active";
    case "MATURED":
      return "mi-status-badge--matured";
    case "PAID_OUT":
      return "mi-status-badge--matured";
    default:
      return "mi-status-badge--pending";
  }
};
const statusLabel = (s: InvStatus) =>
  s === "PAID_OUT" ? "Paid Out" : s.charAt(0) + s.slice(1).toLowerCase();

/* ─── SVG area chart ──────────────────────────────────────────────────────── */
const AreaChart = ({ points }: { points: ChartPoint[] }) => {
  if (points.length < 2) return null;
  const W = 560,
    H = 160,
    PL = 50,
    PB = 28,
    PT = 12,
    PR = 16;
  const cW = W - PL - PR,
    cH = H - PB - PT;
  const maxVal = Math.max(...points.map((p) => p.value), 1);
  const ticks = [0, maxVal * 0.25, maxVal * 0.5, maxVal * 0.75, maxVal].map(
    (v) => Math.round(v),
  );
  const toX = (i: number) => PL + (i / (points.length - 1)) * cW;
  const toY = (v: number) => PT + cH - (v / maxVal) * cH;
  const line = points
    .map((p, i) => `${i === 0 ? "M" : "L"}${toX(i)},${toY(p.value)}`)
    .join(" ");
  const area = `${line} L${toX(points.length - 1)},${PT + cH} L${toX(0)},${PT + cH} Z`;
  return (
    <svg
      viewBox={`0 0 ${W} ${H}`}
      preserveAspectRatio="none"
      style={{ width: "100%", height: 160 }}
    >
      <defs>
        <linearGradient id="cg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#7c3aed" stopOpacity="0.35" />
          <stop offset="100%" stopColor="#7c3aed" stopOpacity="0.02" />
        </linearGradient>
      </defs>
      {ticks.map((t) => {
        const y = toY(t);
        return (
          <g key={t}>
            <line
              x1={PL}
              y1={y}
              x2={W - PR}
              y2={y}
              stroke="#e2e8f0"
              strokeWidth="1"
            />
            <text
              x={PL - 6}
              y={y + 4}
              textAnchor="end"
              fontSize="9"
              fill="#94a3b8"
            >
              {t >= 1000 ? `${(t / 1000).toFixed(0)}K` : t}
            </text>
          </g>
        );
      })}
      <path d={area} fill="url(#cg)" />
      <path
        d={line}
        fill="none"
        stroke="#7c3aed"
        strokeWidth="2.5"
        strokeLinejoin="round"
        strokeLinecap="round"
      />
      {points.map((p, i) => (
        <circle
          key={i}
          cx={toX(i)}
          cy={toY(p.value)}
          r="4"
          fill="#fff"
          stroke="#7c3aed"
          strokeWidth="2"
        />
      ))}
      {points.map((p, i) => (
        <text
          key={i}
          x={toX(i)}
          y={H - 6}
          textAnchor="middle"
          fontSize="9"
          fill="#94a3b8"
        >
          {p.label}
        </text>
      ))}
    </svg>
  );
};

/* ─── SVG donut ───────────────────────────────────────────────────────────── */
const DonutChart = ({
  activeAmt,
  maturedAmt,
  symbol,
}: {
  activeAmt: number;
  maturedAmt: number;
  symbol: string;
}) => {
  const total = activeAmt + maturedAmt || 1;
  const R = 70,
    CX = 90,
    CY = 90;
  const circ = 2 * Math.PI * R;
  const aDash = (activeAmt / total) * circ;
  const mDash = (maturedAmt / total) * circ;
  return (
    <svg viewBox="0 0 180 180" style={{ width: 160, height: 160 }}>
      <circle
        cx={CX}
        cy={CY}
        r={R}
        fill="none"
        stroke="#e2e8f0"
        strokeWidth="22"
      />
      <circle
        cx={CX}
        cy={CY}
        r={R}
        fill="none"
        stroke="#7c3aed"
        strokeWidth="22"
        strokeDasharray={`${aDash} ${circ}`}
        strokeDashoffset={0}
        transform={`rotate(-90 ${CX} ${CY})`}
        strokeLinecap="butt"
      />
      {maturedAmt > 0 && (
        <circle
          cx={CX}
          cy={CY}
          r={R}
          fill="none"
          stroke="#22c55e"
          strokeWidth="22"
          strokeDasharray={`${mDash} ${circ}`}
          strokeDashoffset={-aDash}
          transform={`rotate(-90 ${CX} ${CY})`}
          strokeLinecap="butt"
        />
      )}
      <text
        x={CX}
        y={CY - 8}
        textAnchor="middle"
        fontSize="10"
        fontWeight="700"
        fill="#1e293b"
      >
        {symbol}{" "}
        {(activeAmt + maturedAmt).toLocaleString("en-US", {
          maximumFractionDigits: 0,
        })}
      </text>
      <text x={CX} y={CY + 8} textAnchor="middle" fontSize="9" fill="#64748b">
        Total Invested
      </text>
    </svg>
  );
};

/* ─── Page ────────────────────────────────────────────────────────────────── */
export default function MyInvestmentsPage() {
  const router = useRouter();

  /* scaffold */
  const [theme, setTheme] = useState<"light" | "dark">("light");
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);

  /* data */
  const [investments, setInvestments] = useState<ApiInvestment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  /* ui */
  const [activeTab, setActiveTab] = useState<TabFilter>("All");
  const [chartRange, setChartRange] = useState("All Time");
  const [showChartDd, setShowChartDd] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState<ApiInvestment | null>(
    null,
  );
  const chartDdRef = useRef<HTMLDivElement>(null);

  /* wallet */
  const [currencySymbol, setCurrencySymbol] = useState("₦");

  /* ── toast ── */
  const showToast = useCallback(
    (msg: string, type: "warning" | "success" = "warning") => {
      setToasts((prev) => {
        if (prev.length >= 5) return prev;
        const id = Date.now();
        const t: Toast = { id, message: msg, type, exiting: false };
        setTimeout(() => {
          setToasts((c) =>
            c.map((x) => (x.id === id ? { ...x, exiting: true } : x)),
          );
          setTimeout(() => setToasts((c) => c.filter((x) => x.id !== id)), 300);
        }, 3500);
        return [...prev, t];
      });
    },
    [],
  );

  const toggleTheme = () => {
    const next = theme === "light" ? "dark" : "light";
    setTheme(next);
    localStorage.setItem("theme", next);
    document.documentElement.classList.toggle("dark", next === "dark");
    document.body.classList.toggle("dark-theme", next === "dark");
  };

  /* close chart dropdown on outside click */
  useEffect(() => {
    const h = (e: MouseEvent) => {
      if (chartDdRef.current && !chartDdRef.current.contains(e.target as Node))
        setShowChartDd(false);
    };
    document.addEventListener("mousedown", h);
    return () => document.removeEventListener("mousedown", h);
  }, []);

  /* ── fetch investments ── */
  const loadInvestments = useCallback(async () => {
    const userId = getUserId();
    if (!userId) {
      setLoadError("Session expired. Please log in again.");
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setLoadError("");
    try {
      const res: any = await investmentService.getByUserId(userId);
      if (res?.status === "success" && Array.isArray(res.data)) {
        // sort newest first
        const sorted = [...res.data].sort(
          (a, b) =>
            new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime(),
        );
        setInvestments(sorted);
      } else {
        setLoadError(res?.message || "Unable to retrieve investments.");
      }
    } catch {
      setLoadError("Unable to retrieve investments. Please try again.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  /* ── init ── */
  useEffect(() => {
    document.title = "My Investments – ePay Online Business Banking";
    try {
      const fiat = getFiat();
      const w = fiat ? getWallet(fiat) : null;
      if (w?.symbol) setCurrencySymbol(w.symbol);
    } catch (_) {}

    loadInvestments().finally(() => setIsPageLoading(false));
  }, [loadInvestments]);

  /* ── derived stats ── */
  const totalInvested = investments.reduce((s, i) => s + (i.principal ?? 0), 0);
  const totalReturns = investments.reduce(
    (s, i) => s + (i.expectedProfit ?? 0),
    0,
  );
  const totalPaid = investments
    .filter((i) => i.status === "PAID_OUT")
    .reduce((s, i) => s + (i.expectedProfit ?? 0), 0);
  const activeCount = investments.filter((i) => i.status === "ACTIVE").length;
  const maturedAmt = investments
    .filter((i) => i.status === "MATURED" || i.status === "PAID_OUT")
    .reduce((s, i) => s + (i.principal ?? 0), 0);

  const overallReturnPct =
    totalInvested > 0
      ? ((totalReturns / totalInvested) * 100).toFixed(2)
      : "0.00";

  /* ── tab counts ── */
  const tabCounts = {
    All: investments.length,
    Active: investments.filter((i) => i.status === "ACTIVE").length,
    Matured: investments.filter(
      (i) => i.status === "MATURED" || i.status === "PAID_OUT",
    ).length,
    Withdrawn: investments.filter((i) => i.status === "PAID_OUT").length,
  };

  const filtered = investments.filter((i) => tabStatusMatch(i, activeTab));

  /* ── chart data — cumulative profit over time ── */
  const chartPoints: ChartPoint[] = (() => {
    if (investments.length === 0) return [];
    const sorted = [...investments].sort(
      (a, b) =>
        new Date(a.startDate).getTime() - new Date(b.startDate).getTime(),
    );
    let cumulative = 0;
    return sorted.map((inv) => {
      cumulative += inv.expectedProfit ?? 0;
      return {
        label: new Date(inv.startDate).toLocaleDateString("en-US", {
          month: "short",
          year: "2-digit",
        }),
        value: cumulative,
      };
    });
  })();

  /* ── upcoming maturities ── */
  const upcomingMaturities = investments
    .filter((i) => i.status === "ACTIVE")
    .sort(
      (a, b) =>
        new Date(a.maturityDate).getTime() - new Date(b.maturityDate).getTime(),
    )
    .slice(0, 3);

  if (isPageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === "dark" ? "dark" : ""}`}>
      <Sidebar />

      <main className={`main-content ${isDepositOpen ? "dashboard-blur" : ""}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />

        <div className="scrollable-content">
          {/* Toasts */}
          <div className="toastrs">
            {toasts.map((toast) => (
              <div
                key={toast.id}
                className={`toastr toastr--${toast.type} ${toast.exiting ? "toast-exit" : ""}`}
              >
                <div className="toast-icon">
                  <i
                    className={`fa ${toast.type === "success" ? "fa-check-circle" : "fa-exclamation-circle"}`}
                    aria-hidden="true"
                  />
                </div>
                <div className="toast-message">{toast.message}</div>
              </div>
            ))}
          </div>

          <div className="mi-page">
            {/* ── Header ── */}
            <div className="mi-page-header">
              <div>
                <h1 className="mi-page-title">My Investments</h1>
                <p className="mi-page-subtitle">
                  Manage and monitor all your investment portfolios.
                </p>
              </div>
              <div style={{ display: "flex", gap: 10 }}>
                <button
                  className="mi-new-btn mi-refresh-btn"
                  onClick={loadInvestments}
                  disabled={isLoading}
                  title="Refresh"
                >
                  <RefreshCw size={16} className={isLoading ? "mi-spin" : ""} />
                </button>
                <Link href="/investments" className="mi-new-btn">
                  <Plus size={16} />
                  New Investment
                </Link>
              </div>
            </div>

            {/* ── Stat cards ── */}
            <div className="mi-stats-row">
              <div className="mi-stat-card">
                <div className="mi-stat-icon mi-stat-icon--blue">
                  <DollarSign size={22} />
                </div>
                <div className="mi-stat-body">
                  <p className="mi-stat-label">Total Invested</p>
                  <p className="mi-stat-value">
                    {fmt(totalInvested, currencySymbol)}
                  </p>
                  <p className="mi-stat-sub">
                    Across {activeCount} active investment
                    {activeCount !== 1 ? "s" : ""}
                  </p>
                </div>
              </div>
              <div className="mi-stat-card">
                <div className="mi-stat-icon mi-stat-icon--green">
                  <TrendingUp size={22} />
                </div>
                <div className="mi-stat-body">
                  <p className="mi-stat-label">Total Returns (Accrued)</p>
                  <p className="mi-stat-value">
                    {fmt(totalReturns, currencySymbol)}
                  </p>
                  <p className="mi-stat-sub mi-stat-sub--green">
                    +{overallReturnPct}% all time return
                  </p>
                </div>
              </div>
              <div className="mi-stat-card">
                <div className="mi-stat-icon mi-stat-icon--orange">
                  <BarChart2 size={22} />
                </div>
                <div className="mi-stat-body">
                  <p className="mi-stat-label">Total Earnings Paid</p>
                  <p className="mi-stat-value">
                    {fmt(totalPaid, currencySymbol)}
                  </p>
                  <p className="mi-stat-sub">Withdrawn to wallet</p>
                </div>
              </div>
              <div className="mi-stat-card">
                <div className="mi-stat-icon mi-stat-icon--purple">
                  <Shield size={22} />
                </div>
                <div className="mi-stat-body">
                  <p className="mi-stat-label">Active Investments</p>
                  <p className="mi-stat-value mi-stat-value--count">
                    {activeCount}
                  </p>
                  <p className="mi-stat-sub">View active plans</p>
                </div>
              </div>
            </div>

            {/* ── Two-column layout ── */}
            <div className="mi-main-grid">
              {/* Left col */}
              <div className="mi-left-col">
                {/* Portfolio card */}
                <div className="mi-portfolio-card">
                  <div className="mi-portfolio-header">
                    <h2 className="mi-portfolio-title">
                      Your Investment Portfolio
                    </h2>
                    <div className="mi-portfolio-actions">
                      <button className="mi-filter-btn">
                        <Filter size={15} />
                        Filter
                      </button>
                      <button className="mi-more-btn">
                        <MoreHorizontal size={18} />
                      </button>
                    </div>
                  </div>

                  {/* Tabs */}
                  <div className="mi-tabs">
                    {(
                      ["All", "Active", "Matured", "Withdrawn"] as TabFilter[]
                    ).map((tab) => (
                      <button
                        key={tab}
                        className={`mi-tab ${activeTab === tab ? "mi-tab--active" : ""}`}
                        onClick={() => setActiveTab(tab)}
                      >
                        {tab} ({tabCounts[tab]})
                      </button>
                    ))}
                  </div>

                  {/* List */}
                  <div className="mi-investment-list">
                    {isLoading ? (
                      <div className="mi-list-loading">
                        <Loader2 size={28} className="mi-spin" />
                        <p>Loading investments…</p>
                      </div>
                    ) : loadError ? (
                      <div className="mi-list-error">
                        <p>{loadError}</p>
                        <button
                          className="mi-retry-btn"
                          onClick={loadInvestments}
                        >
                          Retry
                        </button>
                      </div>
                    ) : filtered.length === 0 ? (
                      <div className="mi-empty-state">
                        <TrendingUp size={36} className="mi-empty-icon" />
                        <p className="mi-empty-text">
                          {activeTab === "All"
                            ? "No investments yet."
                            : `No ${activeTab.toLowerCase()} investments.`}
                        </p>
                        {activeTab === "All" && (
                          <Link href="/investments" className="mi-empty-cta">
                            Start Investing
                          </Link>
                        )}
                      </div>
                    ) : (
                      filtered.map((inv) => {
                        const pct = progressPercent(inv);
                        return (
                          <div key={inv.id} className="mi-investment-row">
                            <div className="mi-inv-top">
                              <div className="mi-inv-icon">
                                <TrendingUp size={20} />
                              </div>
                              <div className="mi-inv-info">
                                <div className="mi-inv-title-row">
                                  <span className="mi-inv-plan">
                                    {DURATION_LABELS[inv.duration] ??
                                      inv.duration}{" "}
                                    ({inv.returnRate}% p.a.)
                                  </span>
                                  <span
                                    className={`mi-status-badge ${statusBadgeClass(inv.status)}`}
                                  >
                                    {statusLabel(inv.status)}
                                  </span>
                                </div>
                                <p className="mi-inv-meta">
                                  Invested on {fmtDate(inv.startDate)}
                                  &nbsp;&nbsp;•&nbsp;&nbsp;Matures on{" "}
                                  {fmtDate(inv.maturityDate)}
                                </p>
                                <p className="mi-inv-id">
                                  Ref: {inv.referenceId}
                                </p>
                              </div>
                              <div className="mi-inv-progress-col">
                                <p className="mi-progress-label">
                                  Progress to Maturity
                                </p>
                                <p className="mi-progress-pct">{pct}%</p>
                                <div className="mi-progress-bar-bg">
                                  <div
                                    className="mi-progress-bar-fill"
                                    style={{ width: `${pct}%` }}
                                  />
                                </div>
                              </div>
                              <button
                                className="mi-more-btn mi-more-btn--row"
                                onClick={() => setShowDetailModal(inv)}
                              >
                                <MoreHorizontal size={16} />
                              </button>
                            </div>
                            <div className="mi-inv-bottom">
                              <div className="mi-inv-stats">
                                <div className="mi-inv-stat-item">
                                  <p className="mi-inv-stat-label">
                                    Invested Amount
                                  </p>
                                  <p className="mi-inv-stat-value">
                                    {fmt(inv.principal, currencySymbol)}
                                  </p>
                                </div>
                                <div className="mi-inv-stat-item">
                                  <p className="mi-inv-stat-label">
                                    Interest Rate (p.a.)
                                  </p>
                                  <p className="mi-inv-stat-value">
                                    {inv.returnRate}%
                                  </p>
                                </div>
                                <div className="mi-inv-stat-item">
                                  <p className="mi-inv-stat-label">
                                    Interest Earned
                                  </p>
                                  <p className="mi-inv-stat-value mi-inv-stat-value--green">
                                    {fmt(inv.expectedProfit, currencySymbol)}
                                  </p>
                                </div>
                                <div className="mi-inv-stat-item">
                                  <p className="mi-inv-stat-label">
                                    Maturity Amount
                                  </p>
                                  <p className="mi-inv-stat-value">
                                    {fmt(inv.totalPayout, currencySymbol)}
                                  </p>
                                </div>
                              </div>
                              <button
                                className="mi-view-details-btn"
                                onClick={() => setShowDetailModal(inv)}
                              >
                                View Details
                              </button>
                            </div>
                          </div>
                        );
                      })
                    )}
                  </div>
                </div>

                {/* Performance chart */}
                <div className="mi-chart-card">
                  <div className="mi-chart-header">
                    <div>
                      <h2 className="mi-chart-title">Investment Performance</h2>
                      <p className="mi-chart-total-label">Total Returns</p>
                      <p className="mi-chart-total-value">
                        {fmt(totalReturns, currencySymbol)}
                      </p>
                      <p className="mi-chart-total-sub mi-stat-sub--green">
                        +{overallReturnPct}% vs total invested
                      </p>
                    </div>
                    <div className="mi-chart-range-wrap" ref={chartDdRef}>
                      <button
                        className="mi-chart-range-btn"
                        onClick={() => setShowChartDd((p) => !p)}
                      >
                        {chartRange}
                        <ChevronDown
                          size={14}
                          className={showChartDd ? "mi-chevron-open" : ""}
                        />
                      </button>
                      {showChartDd && (
                        <div className="mi-chart-dropdown">
                          {["All Time", "6 Months", "3 Months", "1 Month"].map(
                            (r) => (
                              <button
                                key={r}
                                className={`mi-chart-dropdown-item ${chartRange === r ? "mi-chart-dropdown-item--active" : ""}`}
                                onClick={() => {
                                  setChartRange(r);
                                  setShowChartDd(false);
                                }}
                              >
                                {r}
                              </button>
                            ),
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="mi-chart-area">
                    {chartPoints.length >= 2 ? (
                      <AreaChart points={chartPoints} />
                    ) : (
                      <p className="mi-chart-empty">
                        Not enough data to display chart.
                      </p>
                    )}
                  </div>
                </div>
              </div>

              {/* Right col */}
              <div className="mi-right-col">
                {/* Portfolio overview */}
                <div className="mi-overview-card">
                  <h3 className="mi-overview-title">Portfolio Overview</h3>
                  <div className="mi-donut-wrap">
                    <DonutChart
                      activeAmt={investments
                        .filter((i) => i.status === "ACTIVE")
                        .reduce((s, i) => s + i.principal, 0)}
                      maturedAmt={maturedAmt}
                      symbol={currencySymbol}
                    />
                  </div>
                  <div className="mi-legend">
                    <div className="mi-legend-item">
                      <span className="mi-legend-dot mi-legend-dot--purple" />
                      <span className="mi-legend-label">
                        Active Investments
                      </span>
                      <span className="mi-legend-value">
                        {fmt(
                          investments
                            .filter((i) => i.status === "ACTIVE")
                            .reduce((s, i) => s + i.principal, 0),
                          currencySymbol,
                        )}
                      </span>
                    </div>
                    <div className="mi-legend-item">
                      <span className="mi-legend-dot mi-legend-dot--green" />
                      <span className="mi-legend-label">
                        Matured Investments
                      </span>
                      <span className="mi-legend-value">
                        {fmt(maturedAmt, currencySymbol)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Upcoming maturities */}
                <div className="mi-maturities-card">
                  <div className="mi-maturities-header">
                    <h3 className="mi-maturities-title">Upcoming Maturities</h3>
                    <Calendar size={18} className="mi-maturities-icon" />
                  </div>
                  {upcomingMaturities.length === 0 ? (
                    <p
                      style={{
                        fontSize: "0.8rem",
                        color: "var(--text-muted)",
                        padding: "8px 0",
                      }}
                    >
                      No upcoming maturities.
                    </p>
                  ) : (
                    <div className="mi-maturities-list">
                      {upcomingMaturities.map((inv) => (
                        <div key={inv.id} className="mi-maturity-item">
                          <div className="mi-maturity-left">
                            <p className="mi-maturity-plan">
                              {DURATION_LABELS[inv.duration]} ({inv.returnRate}%
                              p.a.)
                            </p>
                            <p className="mi-maturity-date">
                              Matures on {fmtDate(inv.maturityDate)}
                            </p>
                          </div>
                          <div className="mi-maturity-right">
                            <p className="mi-maturity-label">Amount</p>
                            <p className="mi-maturity-value">
                              {fmt(inv.totalPayout, currencySymbol)}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                  <button className="mi-view-calendar-btn">
                    View Calendar <ArrowRight size={14} />
                  </button>
                </div>

                {/* Quick actions */}
                <div className="mi-quick-card">
                  <h3 className="mi-quick-title">Quick Actions</h3>
                  <div className="mi-quick-grid">
                    <Link href="/investments" className="mi-quick-action">
                      <div className="mi-quick-icon mi-quick-icon--purple">
                        <Plus size={20} />
                      </div>
                      <span>New Investment</span>
                    </Link>
                    <button
                      className="mi-quick-action"
                      onClick={() =>
                        showToast(
                          "Withdraw earnings feature coming soon!",
                          "success",
                        )
                      }
                    >
                      <div className="mi-quick-icon mi-quick-icon--green">
                        <DollarSign size={20} />
                      </div>
                      <span>Withdraw Earnings</span>
                    </button>
                    <Link href="/investments" className="mi-quick-action">
                      <div className="mi-quick-icon mi-quick-icon--blue">
                        <Calculator size={20} />
                      </div>
                      <span>Investment Calculator</span>
                    </Link>
                    <Link href="/support" className="mi-quick-action">
                      <div className="mi-quick-icon mi-quick-icon--orange">
                        <Headphones size={20} />
                      </div>
                      <span>Help Center</span>
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <Footer theme={theme} />
        </div>
      </main>

      {/* Detail modal */}
      {showDetailModal && (
        <>
          <div
            className="mi-modal-overlay"
            onClick={() => setShowDetailModal(null)}
          />
          <div className={`mi-detail-modal ${theme}`}>
            <div className="mi-detail-modal-header">
              <h3>
                {DURATION_LABELS[showDetailModal.duration]} (
                {showDetailModal.returnRate}% p.a.)
              </h3>
              <button
                className="mi-modal-close"
                onClick={() => setShowDetailModal(null)}
              >
                ✕
              </button>
            </div>
            <div className="mi-detail-modal-body">
              <div className="mi-detail-grid">
                <div>
                  <p className="mi-detail-label">Reference ID</p>
                  <p className="mi-detail-val" style={{ fontSize: "0.75rem" }}>
                    {showDetailModal.referenceId}
                  </p>
                </div>
                <div>
                  <p className="mi-detail-label">Status</p>
                  <span
                    className={`mi-status-badge ${statusBadgeClass(showDetailModal.status)}`}
                  >
                    {statusLabel(showDetailModal.status)}
                  </span>
                </div>
                <div>
                  <p className="mi-detail-label">Invested On</p>
                  <p className="mi-detail-val">
                    {fmtDate(showDetailModal.startDate)}
                  </p>
                </div>
                <div>
                  <p className="mi-detail-label">Matures On</p>
                  <p className="mi-detail-val">
                    {fmtDate(showDetailModal.maturityDate)}
                  </p>
                </div>
                <div>
                  <p className="mi-detail-label">Principal</p>
                  <p className="mi-detail-val">
                    {fmt(showDetailModal.principal, currencySymbol)}
                  </p>
                </div>
                <div>
                  <p className="mi-detail-label">Interest Rate (p.a.)</p>
                  <p className="mi-detail-val">{showDetailModal.returnRate}%</p>
                </div>
                <div>
                  <p className="mi-detail-label">Expected Profit</p>
                  <p className="mi-detail-val mi-inv-stat-value--green">
                    {fmt(showDetailModal.expectedProfit, currencySymbol)}
                  </p>
                </div>
                <div>
                  <p className="mi-detail-label">Total Payout</p>
                  <p className="mi-detail-val">
                    {fmt(showDetailModal.totalPayout, currencySymbol)}
                  </p>
                </div>
              </div>
              <div className="mi-detail-progress">
                <div className="mi-detail-progress-row">
                  <span>Progress to Maturity</span>
                  <span>{progressPercent(showDetailModal)}%</span>
                </div>
                <div className="mi-progress-bar-bg">
                  <div
                    className="mi-progress-bar-fill"
                    style={{ width: `${progressPercent(showDetailModal)}%` }}
                  />
                </div>
              </div>
            </div>
            <div className="mi-detail-modal-footer">
              <button
                className="mi-modal-secondary"
                onClick={() => setShowDetailModal(null)}
              >
                Close
              </button>
              <button
                className="mi-modal-primary"
                onClick={() => {
                  setShowDetailModal(null);
                  showToast("Withdraw feature coming soon!", "success");
                }}
              >
                Withdraw Earnings
              </button>
            </div>
          </div>
        </>
      )}

      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
      <DepositModal
        isOpen={isDepositOpen}
        onClose={() => setIsDepositOpen(false)}
        theme={theme}
      />

      {!isChatOpen && (
        <button
          className="chat-fab"
          onClick={() => setIsChatOpen(true)}
          aria-label="Open support chat"
        >
          <i className="fa-solid fa-comment-dots" />
        </button>
      )}
      <SupportChatBot
        isOpen={isChatOpen}
        onClose={() => setIsChatOpen(false)}
      />
    </div>
  );
}
