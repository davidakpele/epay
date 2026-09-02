"use client";

import React, { useState, useEffect, useRef, useCallback } from "react";
import {
  TrendingUp,
  DollarSign,
  BarChart2,
  Calendar,
  Shield,
  ChevronDown,
  CheckCircle,
  Loader2,
  Link as LinkIcon,
} from "lucide-react";
import Link from "next/link";
import Sidebar from "@/components/Sidebar";
import Header from "@/components/Header";
import Footer from "@/components/Footer";
import MobileNav from "@/components/MobileNav";
import DepositModal from "@/components/DepositModal";
import LoadingScreen from "@/components/loader/Loadingscreen";
import SupportChatBot from "@/components/SupportChatBot";
import { Toast } from "@/app/types/auth";
import {
  getFiat,
  getToken,
  getUserId,
  getUserWalletId,
  getWallet,
  setWalletContainer,
  walletService,
} from "@/app/api";
import { investmentService } from "@/app/api";
import "./Investments.css";

/* ─── Types ───────────────────────────────────────────────────────────────── */
type Duration = "WEEKLY" | "MONTHLY" | "QUARTERLY" | "YEARLY";

interface ApiPlan {
  duration: Duration;
  durationDays: number;
  annualRate: number;
}

interface CalcResult {
  principal: number;
  duration: Duration;
  durationDays: number;
  annualRate: number;
  expectedProfit: number;
  totalPayout: number;
  currencyCode: string;
  maturityDate: string; // YYYY-MM-DD
}

interface RecentInvestment {
  id: number;
  referenceId: string;
  duration: Duration;
  principal: number;
  returnRate: number;
  expectedProfit: number;
  totalPayout: number;
  startDate: string;
  maturityDate: string;
  status: string;
  currencyCode: string;
}

/* ─── Duration display helpers ────────────────────────────────────────────── */
const DURATION_META: Record<Duration, { label: string; popular?: boolean }> = {
  WEEKLY: { label: "Weekly Plan" },
  MONTHLY: { label: "Monthly Plan" },
  QUARTERLY: { label: "Quarterly Plan", popular: true },
  YEARLY: { label: "Yearly Plan" },
};

const fmtAmt = (n: number, sym = "₦") =>
  `${sym} ${(n ?? 0).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

const fmtDate = (iso: string) => {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
};

const statusClass = (s: string) => {
  switch (s?.toUpperCase()) {
    case "ACTIVE":
      return "inv-status-badge--active";
    case "MATURED":
      return "inv-status-badge--matured";
    case "PAID_OUT":
      return "inv-status-badge--matured";
    default:
      return "inv-status-badge--pending";
  }
};

/* ─── Component ───────────────────────────────────────────────────────────── */
export default function InvestmentsPage() {
  /* scaffold */
  const [theme, setTheme] = useState<"light" | "dark">("light");
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);

  /* plans from API */
  const [plans, setPlans] = useState<ApiPlan[]>([]);
  const [plansLoading, setPlansLoading] = useState(true);
  const [selectedDuration, setSelectedDuration] =
    useState<Duration>("QUARTERLY");
  const [isPlanDropdownOpen, setIsPlanDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  /* calculator */
  const [calcAmount, setCalcAmount] = useState("500,000");
  const [rawAmount, setRawAmount] = useState("500000");
  const [calcResult, setCalcResult] = useState<CalcResult | null>(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const calcDebounceRef = useRef<NodeJS.Timeout | null>(null);

  /* invest */
  const [isInvesting, setIsInvesting] = useState(false);

  /* recent investments */
  const [recentInvestments, setRecentInvestments] = useState<
    RecentInvestment[]
  >([]);
  const [recentLoading, setRecentLoading] = useState(true);

  /* portfolio stats */
  const [totalInvested, setTotalInvested] = useState(0);
  const [totalReturns, setTotalReturns] = useState(0);
  const [totalPaid, setTotalPaid] = useState(0);
  const [activeCount, setActiveCount] = useState(0);

  /* wallet */
  const [currencySymbol, setCurrencySymbol] = useState("₦");
  const [currencyCode, setCurrencyCode] = useState("NGN");

  /* ── toast helper ── */
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

  /* ── close dropdown on outside click ── */
  useEffect(() => {
    const h = (e: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node)
      )
        setIsPlanDropdownOpen(false);
    };
    document.addEventListener("mousedown", h);
    return () => document.removeEventListener("mousedown", h);
  }, []);

  /* ── load plans ── */
  useEffect(() => {
    investmentService
      .getPlans()
      .then((res: any) => {
        if (res?.status === "success" && Array.isArray(res.data)) {
          setPlans(res.data);
        }
      })
      .catch(() => showToast("Failed to load investment plans", "warning"))
      .finally(() => setPlansLoading(false));
  }, []);

  /* ── load recent + stats ── */
  useEffect(() => {
    const userId = getUserId();
    if (!userId) {
      setRecentLoading(false);
      return;
    }

    investmentService
      .getByUserId(userId)
      .then((res: any) => {
        if (res?.status === "success" && Array.isArray(res.data)) {
          const list: RecentInvestment[] = res.data;
          setRecentInvestments(list.slice(0, 5)); // show latest 5

          const active = list.filter((i) => i.status === "ACTIVE");
          const paidOut = list.filter(
            (i) => i.status === "PAID_OUT" || i.status === "MATURED",
          );

          setTotalInvested(list.reduce((s, i) => s + (i.principal ?? 0), 0));
          setTotalReturns(
            list.reduce((s, i) => s + (i.expectedProfit ?? 0), 0),
          );
          setTotalPaid(
            paidOut.reduce((s, i) => s + (i.expectedProfit ?? 0), 0),
          );
          setActiveCount(active.length);
        }
      })
      .catch(() => {
        /* silently ignore — user may have no investments yet */
      })
      .finally(() => setRecentLoading(false));
  }, []);

  /* ── wallet / currency ── */
  useEffect(() => {
    document.title = "Investments – ePay Online Business Banking";
    try {
      const fiat = getFiat();
      const w = fiat ? getWallet(fiat) : null;
      if (w?.symbol) setCurrencySymbol(w.symbol);
      if (fiat) setCurrencyCode(fiat);
    } catch (_) {}
    const t = setTimeout(() => setIsPageLoading(false), 800);
    return () => clearTimeout(t);
  }, []);

  /* ── debounced calculate ── */
  const triggerCalculate = useCallback(
    (amount: string, duration: Duration) => {
      const principal = parseFloat(amount.replace(/,/g, ""));
      if (!principal || principal <= 0) {
        setCalcResult(null);
        return;
      }

      if (calcDebounceRef.current) clearTimeout(calcDebounceRef.current);
      calcDebounceRef.current = setTimeout(async () => {
        setIsCalculating(true);
        try {
          const res: any = await investmentService.calculate({
            principal,
            duration,
            currencyCode,
          });
          if (res?.status === "success" && res.data) {
            setCalcResult(res.data);
          } else {
            setCalcResult(null);
            if (res?.message) showToast(res.message, "warning");
          }
        } catch (err: any) {
          setCalcResult(null);
        } finally {
          setIsCalculating(false);
        }
      }, 500);
    },
    [currencyCode, showToast],
  );

  /* recalculate whenever amount or duration changes */
  useEffect(() => {
    triggerCalculate(rawAmount, selectedDuration);
  }, [rawAmount, selectedDuration, triggerCalculate]);

  /* ── amount input ── */
  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value.replace(/,/g, "");
    if (raw === "" || /^\d*\.?\d*$/.test(raw)) {
      setRawAmount(raw);
      const num = parseFloat(raw);
      setCalcAmount(
        raw === "" ? "" : isNaN(num) ? "" : num.toLocaleString("en-US"),
      );
    }
  };

  const handleQuickAmount = (val: number) => {
    setRawAmount(String(val));
    setCalcAmount(val.toLocaleString("en-US"));
  };

  /* ── invest now ── */
  const handleInvestNow = async () => {
    const principal = parseFloat(rawAmount.replace(/,/g, ""));
    const userId = getUserId();
    const walletId = getUserWalletId();
    const token = getToken();

    if (!principal || principal <= 0) {
      showToast("Please enter a valid amount", "warning");
      return;
    }
    if (!userId || !walletId) {
      showToast("Session expired. Please log in again.", "warning");
      return;
    }

    setIsInvesting(true);
    try {
      const res: any = await investmentService.create({
        userId: Number(userId),
        walletId: Number(walletId),
        principal,
        duration: selectedDuration,
        currencyCode,
      });

      if (res?.status === "success") {
        showToast("Investment created successfully!", "success");
        // refresh wallet balance
        if (token && userId) {
          const walletRes: any = await walletService.getByUserId(userId, token);
          if (walletRes?.wallet_balances) {
            setWalletContainer(
              walletRes.wallet_balances,
              walletRes.hasTransferPin,
              walletRes.walletId,
            );
          }
        }
        // refresh recent investments
        const updated: any = await investmentService.getByUserId(userId);
        if (updated?.status === "success" && Array.isArray(updated.data)) {
          const list: RecentInvestment[] = updated.data;
          setRecentInvestments(list.slice(0, 5));
          setActiveCount(list.filter((i) => i.status === "ACTIVE").length);
          setTotalInvested(list.reduce((s, i) => s + (i.principal ?? 0), 0));
          setTotalReturns(
            list.reduce((s, i) => s + (i.expectedProfit ?? 0), 0),
          );
        }
        // reset form
        setRawAmount("");
        setCalcAmount("");
        setCalcResult(null);
      } else {
        showToast(
          res?.message || "Investment failed. Please try again.",
          "warning",
        );
      }
    } catch (err: any) {
      showToast(
        err?.message || "Something went wrong. Please try again.",
        "warning",
      );
    } finally {
      setIsInvesting(false);
    }
  };

  /* ── selected plan meta ── */
  const selectedPlan = plans.find((p) => p.duration === selectedDuration);

  /* ── display values (prefer API result, fall back to local estimate) ── */
  const displayInterest = calcResult?.expectedProfit ?? 0;
  const displayPayout = calcResult?.totalPayout ?? 0;
  const displayRate = calcResult?.annualRate ?? selectedPlan?.annualRate ?? 0;
  const displayMaturity = calcResult?.maturityDate
    ? fmtDate(calcResult.maturityDate)
    : "—";
  const displayReturnPct = calcResult
    ? ((calcResult.expectedProfit / calcResult.principal) * 100).toFixed(2)
    : "0.00";

  if (isPageLoading) return <LoadingScreen />;

  const overallReturnPct =
    totalInvested > 0
      ? ((totalReturns / totalInvested) * 100).toFixed(2)
      : "0.00";

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

          <div className="inv-page">
            {/* ── Page header ── */}
            <div className="inv-page-header">
              <h1 className="inv-page-title">Investments</h1>
              <p className="inv-page-subtitle">
                Grow your money securely and earn attractive returns
              </p>
            </div>

            {/* ── Stat cards ── */}
            <div className="inv-stats-row">
              <div className="inv-stat-card">
                <div className="inv-stat-body">
                  <p className="inv-stat-label">Total Invested</p>
                  <p className="inv-stat-value">
                    {fmtAmt(totalInvested, currencySymbol)}
                  </p>
                  <p className="inv-stat-sub">
                    Across {activeCount} active investment
                    {activeCount !== 1 ? "s" : ""}
                  </p>
                </div>
                <div className="inv-stat-icon inv-stat-icon--blue">
                  <DollarSign size={24} />
                </div>
              </div>

              <div className="inv-stat-card">
                <div className="inv-stat-body">
                  <p className="inv-stat-label">Total Returns (Accrued)</p>
                  <p className="inv-stat-value">
                    {fmtAmt(totalReturns, currencySymbol)}
                  </p>
                  <p className="inv-stat-sub inv-stat-sub--green">
                    +{overallReturnPct}% overall return
                  </p>
                </div>
                <div className="inv-stat-icon inv-stat-icon--green">
                  <TrendingUp size={24} />
                </div>
              </div>

              <div className="inv-stat-card">
                <div className="inv-stat-body">
                  <p className="inv-stat-label">Total Earnings Paid</p>
                  <p className="inv-stat-value">
                    {fmtAmt(totalPaid, currencySymbol)}
                  </p>
                </div>
                <div className="inv-stat-icon inv-stat-icon--orange">
                  <BarChart2 size={24} />
                </div>
              </div>

              <div className="inv-stat-card">
                <div className="inv-stat-body">
                  <p className="inv-stat-label">Active Investments</p>
                  <p className="inv-stat-value inv-stat-value--count">
                    {activeCount}
                  </p>
                  <p className="inv-stat-sub">View all your investments</p>
                </div>
                <div className="inv-stat-icon inv-stat-icon--purple">
                  <Shield size={24} />
                </div>
              </div>
            </div>

            {/* ── Two-column layout ── */}
            <div className="inv-main-grid">
              {/* Left col */}
              <div className="inv-left-col">
                {/* Calculator card */}
                <div className="inv-calculator-card">
                  <div className="inv-calc-header">
                    <h2 className="inv-calc-title">Investment Calculator</h2>
                    <p className="inv-calc-subtitle">
                      Enter an amount and select a plan to see your potential
                      returns
                    </p>
                  </div>

                  <div className="inv-calc-body">
                    <div className="inv-calc-row">
                      {/* Amount field */}
                      <div className="inv-calc-field">
                        <label className="inv-label">Enter Amount</label>
                        <div className="inv-amount-input-wrap">
                          <span className="inv-currency-prefix">
                            {currencySymbol}
                          </span>
                          <input
                            type="text"
                            className="inv-amount-input"
                            value={calcAmount}
                            onChange={handleAmountChange}
                            placeholder="0"
                          />
                          {isCalculating && (
                            <Loader2 size={16} className="inv-calc-spinner" />
                          )}
                        </div>
                        <div className="inv-quick-amounts">
                          {[10000, 50000, 100000, 500000].map((v) => (
                            <button
                              key={v}
                              className="inv-quick-btn"
                              onClick={() => handleQuickAmount(v)}
                            >
                              +{v >= 1000 ? `${v / 1000}K` : v}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Plan selector */}
                      <div className="inv-calc-field">
                        <label className="inv-label">
                          Select Investment Plan
                        </label>
                        <div className="inv-plan-select-wrap" ref={dropdownRef}>
                          <button
                            className="inv-plan-select-btn"
                            onClick={() => setIsPlanDropdownOpen((p) => !p)}
                            disabled={plansLoading}
                          >
                            <span>
                              {plansLoading
                                ? "Loading plans…"
                                : selectedPlan
                                  ? `${DURATION_META[selectedDuration].label} (${selectedPlan.annualRate}% p.a.)`
                                  : "Select a plan"}
                            </span>
                            <ChevronDown
                              size={16}
                              className={
                                isPlanDropdownOpen ? "inv-chevron-open" : ""
                              }
                            />
                          </button>
                          {isPlanDropdownOpen && !plansLoading && (
                            <div className="inv-plan-dropdown">
                              {plans.map((p) => (
                                <button
                                  key={p.duration}
                                  className={`inv-plan-option ${selectedDuration === p.duration ? "inv-plan-option--active" : ""}`}
                                  onClick={() => {
                                    setSelectedDuration(p.duration);
                                    setIsPlanDropdownOpen(false);
                                  }}
                                >
                                  {DURATION_META[p.duration].label} (
                                  {p.annualRate}% p.a.)
                                  {DURATION_META[p.duration].popular && (
                                    <span className="inv-popular-badge">
                                      Popular
                                    </span>
                                  )}
                                </button>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Period buttons */}
                    <div className="inv-calc-row">
                      <div className="inv-calc-field">
                        <label className="inv-label">Investment Period</label>
                        <div className="inv-period-btns">
                          {plans.map((p) => (
                            <button
                              key={p.duration}
                              className={`inv-period-btn ${selectedDuration === p.duration ? "inv-period-btn--active" : ""}`}
                              onClick={() => setSelectedDuration(p.duration)}
                            >
                              {p.durationDays < 30
                                ? `${p.durationDays}d`
                                : p.durationDays < 120
                                  ? `${Math.round(p.durationDays / 30)}M`
                                  : p.durationDays < 200
                                    ? `${Math.round(p.durationDays / 30)}M`
                                    : "1Y"}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Rate display */}
                      <div className="inv-calc-field">
                        <label className="inv-label">
                          Interest Rate (p.a.)
                        </label>
                        <div className="inv-rate-display">{displayRate}%</div>
                      </div>
                    </div>

                    {/* Result row */}
                    <div className="inv-result-row">
                      <div className="inv-result-item">
                        <p className="inv-result-label">Interest Earned</p>
                        <p className="inv-result-value">
                          {isCalculating
                            ? "—"
                            : fmtAmt(displayInterest, currencySymbol)}
                        </p>
                      </div>
                      <div className="inv-result-item">
                        <p className="inv-result-label">Maturity Amount</p>
                        <p className="inv-result-value">
                          {isCalculating
                            ? "—"
                            : fmtAmt(displayPayout, currencySymbol)}
                        </p>
                      </div>
                      <div className="inv-result-item">
                        <p className="inv-result-label">Total Return</p>
                        <p className="inv-result-value">
                          {isCalculating ? "—" : `${displayReturnPct}%`}
                        </p>
                      </div>
                      <div className="inv-result-item">
                        <p className="inv-result-label">Est. Maturity Date</p>
                        <p className="inv-result-value inv-result-value--date">
                          {isCalculating ? "—" : displayMaturity}
                        </p>
                      </div>
                    </div>

                    {/* Invest Now */}
                    <button
                      className="inv-cta-btn"
                      onClick={handleInvestNow}
                      disabled={isInvesting || isCalculating || !calcResult}
                    >
                      {isInvesting ? (
                        <>
                          <Loader2 size={18} className="inv-btn-spinner" />{" "}
                          Processing…
                        </>
                      ) : (
                        "Invest Now"
                      )}
                    </button>
                    <p className="inv-secure-note">
                      <Shield size={13} />
                      Your investment is 100% secure and insured
                    </p>
                  </div>
                </div>

                {/* Available Plans */}
                <div className="inv-plans-section">
                  <h2 className="inv-section-title">
                    Available Investment Plans
                  </h2>
                  <p className="inv-section-subtitle">
                    Choose a plan that fits your financial goals
                  </p>

                  <div className="inv-plans-grid">
                    {plansLoading
                      ? Array.from({ length: 4 }).map((_, i) => (
                          <div
                            key={i}
                            className="inv-plan-card inv-plan-card--skeleton"
                          />
                        ))
                      : plans.map((plan) => (
                          <div
                            key={plan.duration}
                            className={`inv-plan-card ${selectedDuration === plan.duration ? "inv-plan-card--active" : ""}`}
                            onClick={() => setSelectedDuration(plan.duration)}
                          >
                            <div className="inv-plan-card-header">
                              <div
                                className={`inv-plan-radio ${selectedDuration === plan.duration ? "inv-plan-radio--on" : ""}`}
                              />
                              <span className="inv-plan-name">
                                {DURATION_META[plan.duration].label}
                              </span>
                              {DURATION_META[plan.duration].popular && (
                                <span className="inv-popular-badge">
                                  Popular
                                </span>
                              )}
                            </div>
                            <p className="inv-plan-rate">
                              {plan.annualRate}% <span>p.a.</span>
                            </p>
                            <p className="inv-plan-min-label">Duration</p>
                            <p className="inv-plan-min-value">
                              {plan.durationDays} days
                            </p>
                            <div className="inv-plan-duration">
                              <Calendar size={14} />
                              <span>
                                {plan.duration.charAt(0) +
                                  plan.duration.slice(1).toLowerCase()}
                              </span>
                            </div>
                          </div>
                        ))}
                  </div>
                </div>
              </div>

              {/* Right col */}
              <div className="inv-right-col">
                {/* Why Invest */}
                <div className="inv-why-card">
                  <h3 className="inv-why-title">Why Invest with ePay?</h3>
                  <div className="inv-why-list">
                    {[
                      {
                        icon: <CheckCircle size={18} />,
                        cls: "green",
                        title: "Secure & Trusted",
                        desc: "Your funds are protected with bank-level security",
                      },
                      {
                        icon: <TrendingUp size={18} />,
                        cls: "blue",
                        title: "High Returns",
                        desc: "Competitive interest rates on all investment plans",
                      },
                      {
                        icon: <BarChart2 size={18} />,
                        cls: "orange",
                        title: "Flexible Plans",
                        desc: "Choose a plan that suits your financial goals",
                      },
                      {
                        icon: <DollarSign size={18} />,
                        cls: "purple",
                        title: "Easy Withdrawals",
                        desc: "Withdraw your earnings anytime, hassle-free",
                      },
                    ].map((item) => (
                      <div key={item.title} className="inv-why-item">
                        <div
                          className={`inv-why-icon inv-why-icon--${item.cls}`}
                        >
                          {item.icon}
                        </div>
                        <div>
                          <p className="inv-why-item-title">{item.title}</p>
                          <p className="inv-why-item-desc">{item.desc}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Tips */}
                <div className="inv-tips-card">
                  <h3 className="inv-tips-title">Investment Tips</h3>
                  <div className="inv-quote">
                    <span className="inv-quote-mark">&ldquo;</span>
                    <p className="inv-quote-text">
                      The best time to invest was yesterday.
                      <br />
                      The second best time is today.
                    </p>
                    <p className="inv-quote-author">– Warren Buffett</p>
                  </div>
                </div>

                {/* Recent Investments */}
                <div className="inv-recent-card">
                  <div className="inv-recent-header">
                    <h3 className="inv-recent-title">Recent Investments</h3>
                    <Link
                      href="/investments/portfolio"
                      className="inv-view-all-btn"
                    >
                      View All
                    </Link>
                  </div>

                  {recentLoading ? (
                    <div className="inv-recent-loading">
                      <Loader2 size={22} className="inv-spin" />
                    </div>
                  ) : recentInvestments.length === 0 ? (
                    <p className="inv-recent-empty">
                      No investments yet. Start investing above!
                    </p>
                  ) : (
                    <div className="inv-recent-list">
                      {recentInvestments.map((inv) => (
                        <div key={inv.id} className="inv-recent-item">
                          <div className="inv-recent-icon">
                            <TrendingUp size={18} />
                          </div>
                          <div className="inv-recent-info">
                            <p className="inv-recent-plan">
                              {DURATION_META[inv.duration]?.label ??
                                inv.duration}{" "}
                              ({inv.returnRate}% p.a.)
                            </p>
                            <p className="inv-recent-meta">
                              Invested {fmtDate(inv.startDate)}
                            </p>
                            <p className="inv-recent-meta">
                              Matures {fmtDate(inv.maturityDate)}
                            </p>
                          </div>
                          <div className="inv-recent-right">
                            <p className="inv-recent-amount">
                              {fmtAmt(inv.principal, currencySymbol)}
                            </p>
                            <span
                              className={`inv-status-badge ${statusClass(inv.status)}`}
                            >
                              {inv.status.charAt(0) +
                                inv.status.slice(1).toLowerCase()}
                            </span>
                            <p className="inv-recent-rate">
                              +{inv.returnRate}% p.a.
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}

                  <Link
                    href="/investments/portfolio"
                    className="inv-view-all-full-btn"
                  >
                    View All Investments
                  </Link>
                </div>
              </div>
            </div>
          </div>

          <Footer theme={theme} />
        </div>
      </main>

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
