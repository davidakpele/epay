'use client';

import React, { useState, useEffect, useCallback } from 'react';
import {
  TrendingUp, Lock, CheckCircle, AlertCircle, Clock,
  RefreshCw, DollarSign, BarChart2, Wallet, ShieldCheck,
  Calendar, Percent, ArrowUpRight, Info
} from 'lucide-react';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import LoadingScreen from '@/components/loader/Loadingscreen';
import SupportChatBot from '@/components/SupportChatBot';
import { getUserId, getUserWalletId, getActiveWallet, getFiat, investmentService } from '../../api';
import { Investment, InvestmentPlan } from '../../api/services/investmentService';
import './InvestPage.css';

const DURATION_META: Record<string, { label: string; days: number; icon: React.ReactNode }> = {
  WEEKLY:    { label: '1 Week',    days: 7,   icon: <Clock size={16} /> },
  MONTHLY:   { label: '1 Month',   days: 30,  icon: <Calendar size={16} /> },
  QUARTERLY: { label: '3 Months',  days: 90,  icon: <BarChart2 size={16} /> },
  YEARLY:    { label: '12 Months', days: 365, icon: <TrendingUp size={16} /> },
};

const CURRENCIES = ['NGN','USD','GBP','EUR','AUD','CAD','CHF','JPY','CNY','INR'];

const getCurrencySymbol = (code: string) =>
  ({ NGN:'₦', USD:'$', GBP:'£', EUR:'€', AUD:'A$', CAD:'C$', CHF:'Fr', JPY:'¥', CNY:'¥', INR:'₹' }[code] ?? code);

const fmt = (n: number) =>
  n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const fmtDate = (d: string) =>
  d ? new Date(d).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' }) : '—';

const statusClass = (s: string) => s.toLowerCase().replace('_', '-');

export default function InvestPage() {
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [pageLoading, setPageLoading] = useState(true);
  const [isChatOpen, setIsChatOpen]   = useState(false);

  const [plans, setPlans]                   = useState<InvestmentPlan[]>([]);
  const [selectedDuration, setSelected]     = useState('MONTHLY');
  const [principal, setPrincipal]           = useState('');
  const [currency, setCurrency]             = useState('NGN');
  const [preview, setPreview]               = useState<any>(null);
  const [calculating, setCalculating]       = useState(false);
  const [submitting, setSubmitting]         = useState(false);
  const [submitError, setSubmitError]       = useState('');
  const [successData, setSuccessData]       = useState<Investment | null>(null);
  const [investments, setInvestments]       = useState<Investment[]>([]);
  const [loadingList, setLoadingList]       = useState(false);

  const userId   = getUserId();
  const walletId = getUserWalletId();

  // ── Load ──────────────────────────────────────────────────────

  const loadPlans = useCallback(async () => {
    try {
      const res = await investmentService.getPlans();
      if (res?.data?.length) setPlans(res.data);
      else throw new Error();
    } catch {
      setPlans([
        { duration:'WEEKLY',    label:'1 Week',    annualRate:8,  durationDays:7   },
        { duration:'MONTHLY',   label:'1 Month',   annualRate:12, durationDays:30  },
        { duration:'QUARTERLY', label:'3 Months',  annualRate:18, durationDays:90  },
        { duration:'YEARLY',    label:'12 Months', annualRate:24, durationDays:365 },
      ]);
    }
  }, []);

  const loadInvestments = useCallback(async () => {
    if (!userId) return;
    setLoadingList(true);
    try {
      const res = await investmentService.getByUserId(userId);
      setInvestments(res?.data ?? []);
    } catch { /* ignore */ }
    finally { setLoadingList(false); }
  }, [userId]);

  useEffect(() => {
    Promise.all([loadPlans(), loadInvestments()])
      .finally(() => setTimeout(() => setPageLoading(false), 400));
  }, [loadPlans, loadInvestments]);

  // Set currency from active wallet on mount
  useEffect(() => {
    const active = getActiveWallet() || getFiat();
    if (active) setCurrency(active);
  }, []);

  // ── Live preview ──────────────────────────────────────────────

  useEffect(() => {
    const amount = parseFloat(principal);
    if (!amount || amount <= 0) { setPreview(null); return; }

    const t = setTimeout(async () => {
      setCalculating(true);
      try {
        const res = await investmentService.calculate({ principal: amount, duration: selectedDuration, currencyCode: currency });
        setPreview(res?.data ?? null);
      } catch {
        const plan = plans.find(p => p.duration === selectedDuration);
        if (plan) {
          const profit = Math.round(amount * (plan.annualRate / 100) * (plan.durationDays / 365) * 10000) / 10000;
          const mat = new Date();
          mat.setDate(mat.getDate() + plan.durationDays);
          setPreview({ principal: amount, duration: selectedDuration, durationDays: plan.durationDays, annualRate: plan.annualRate, expectedProfit: profit, totalPayout: amount + profit, currencyCode: currency, maturityDate: mat.toISOString().split('T')[0] });
        }
      }
      setCalculating(false);
    }, 500);
    return () => clearTimeout(t);
  }, [principal, selectedDuration, currency, plans]);

  // ── Submit ────────────────────────────────────────────────────

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitError('');
    const amount = parseFloat(principal);
    if (!amount || amount <= 0) { setSubmitError('Enter a valid amount.'); return; }
    if (!userId || !walletId)   { setSubmitError('Wallet not found. Please log in again.'); return; }
    setSubmitting(true);
    try {
      const res = await investmentService.create({
        userId: Number(userId), walletId: Number(walletId),
        currencyCode: currency, principal: amount, duration: selectedDuration,
      });
      setSuccessData(res?.data ?? null);
      setPrincipal('');
      setPreview(null);
      await loadInvestments();
    } catch (err: any) {
      setSubmitError(err?.message ?? 'Investment failed. Please try again.');
    }
    setSubmitting(false);
  };

  // ── Stats ─────────────────────────────────────────────────────

  const totalInvested = investments.filter(i => i.status === 'ACTIVE').reduce((s, i) => s + i.principal, 0);
  const totalProfit   = investments.filter(i => i.status === 'PAID_OUT').reduce((s, i) => s + i.expectedProfit, 0);
  const activeCount   = investments.filter(i => i.status === 'ACTIVE').length;
  const paidCount     = investments.filter(i => i.status === 'PAID_OUT').length;

  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light');
  const sym = getCurrencySymbol(currency);

  if (pageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />
      <main className="main-content">
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <div className="invest-page">

            {/* ── Hero banner ─────────────────────────────────── */}
            <div className="invest-hero">
              <div className="invest-hero__left">
                <h1>Grow Your Money</h1>
                <p>Lock funds for a fixed term and earn guaranteed returns. Choose a plan below and start investing in seconds.</p>
              </div>
              <div className="invest-hero__badge">
                <ShieldCheck size={16} />
                Capital-protected returns
              </div>
            </div>

            {/* ── Stats row ───────────────────────────────────── */}
            <div className="invest-stats">
              <div className="invest-stat-card">
                <div className="invest-stat-card__icon purple"><Wallet size={18} /></div>
                <span className="invest-stat-card__label">Locked In</span>
                <span className="invest-stat-card__value">{sym}{fmt(totalInvested)}</span>
              </div>
              <div className="invest-stat-card">
                <div className="invest-stat-card__icon green"><DollarSign size={18} /></div>
                <span className="invest-stat-card__label">Total Earned</span>
                <span className="invest-stat-card__value">{sym}{fmt(totalProfit)}</span>
              </div>
              <div className="invest-stat-card">
                <div className="invest-stat-card__icon amber"><TrendingUp size={18} /></div>
                <span className="invest-stat-card__label">Active Plans</span>
                <span className="invest-stat-card__value">{activeCount}</span>
              </div>
              <div className="invest-stat-card">
                <div className="invest-stat-card__icon blue"><CheckCircle size={18} /></div>
                <span className="invest-stat-card__label">Matured Plans</span>
                <span className="invest-stat-card__value">{paidCount}</span>
              </div>
            </div>

            {/* ── Main two-column layout ───────────────────────── */}
            <div className="invest-layout">

              {/* Left: investments history */}
              <div>
                <div className="invest-history__header">
                  <span className="invest-history__title">Your Investments</span>
                  <button className="invest-refresh-btn" onClick={loadInvestments}>
                    <RefreshCw size={14} /> Refresh
                  </button>
                </div>

                {loadingList ? (
                  <div style={{ textAlign:'center', padding: 40, color:'var(--text-secondary)' }}>
                    <RefreshCw size={24} style={{ animation:'spin 1s linear infinite', opacity:0.4 }} />
                  </div>
                ) : investments.length === 0 ? (
                  <div className="invest-empty">
                    <TrendingUp size={40} />
                    <p>No investments yet.<br />Create your first one using the panel on the right.</p>
                  </div>
                ) : (
                  investments.map(inv => {
                    const meta = DURATION_META[inv.duration];
                    const sc   = statusClass(inv.status);
                    return (
                      <div key={inv.id} className="invest-item">
                        <div className={`invest-item__icon ${sc}`}>
                          {inv.status === 'ACTIVE'   && <Lock size={18} />}
                          {inv.status === 'PAID_OUT'  && <CheckCircle size={18} />}
                          {inv.status === 'FAILED'    && <AlertCircle size={18} />}
                          {inv.status === 'MATURED'   && <Clock size={18} />}
                        </div>
                        <div className="invest-item__body">
                          <div className="invest-item__name">
                            {meta?.label ?? inv.duration} Investment
                            <span className={`invest-status-badge ${sc}`}>
                              {inv.status.replace('_', ' ')}
                            </span>
                          </div>
                          <div className="invest-item__meta">
                            <Calendar size={11} style={{ display:'inline', marginRight:3 }} />
                            {fmtDate(inv.startDate)} → {fmtDate(inv.maturityDate)}
                            {inv.paidOutAt && ` · Paid ${fmtDate(inv.paidOutAt)}`}
                          </div>
                        </div>
                        <div className="invest-item__right">
                          <div className="invest-item__principal">{inv.currencyCode} {fmt(inv.principal)}</div>
                          <div className="invest-item__profit">
                            <ArrowUpRight size={11} style={{ display:'inline' }} />
                            {inv.currencyCode} {fmt(inv.expectedProfit)} profit
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              {/* Right: new investment form */}
              <div>
                {/* Plan picker */}
                <p className="invest-plans-section__title">Select a Plan</p>
                <div className="invest-plans">
                  {plans.map(plan => {
                    const meta = DURATION_META[plan.duration];
                    return (
                      <div
                        key={plan.duration}
                        className={`invest-plan-card ${selectedDuration === plan.duration ? 'selected' : ''}`}
                        onClick={() => setSelected(plan.duration)}
                      >
                        <div className="invest-plan-card__top">
                          <div className="invest-plan-card__icon">{meta?.icon}</div>
                          <span className="invest-plan-card__label">{plan.label}</span>
                        </div>
                        <div className="invest-plan-card__rate">{plan.annualRate}%</div>
                        <div className="invest-plan-card__sub">Annual return</div>
                      </div>
                    );
                  })}
                </div>

                {/* Form */}
                <div className="invest-form-panel">
                  <div className="invest-form-panel__title">
                    <DollarSign size={18} /> New Investment
                  </div>

                  <form onSubmit={handleSubmit}>
                    <div className="invest-form__row">
                      <label className="invest-form__label">Currency</label>
                      <select className="invest-form__input" value={currency} onChange={e => setCurrency(e.target.value)}>
                        {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
                      </select>
                    </div>

                    <div className="invest-form__row">
                      <label className="invest-form__label">Amount to invest ({currency})</label>
                      <input
                        type="number" min="0.01" step="0.01"
                        className="invest-form__input"
                        placeholder="e.g. 50,000"
                        value={principal}
                        onChange={e => setPrincipal(e.target.value)}
                        required
                      />
                    </div>

                    {/* Live preview */}
                    {calculating && (
                      <div style={{ fontSize:'0.8rem', color:'var(--text-secondary)', marginBottom:12 }}>
                        Calculating returns…
                      </div>
                    )}
                    {preview && !calculating && (
                      <div className="invest-preview">
                        <div>
                          <div className="invest-preview__item-label">You Invest</div>
                          <div className="invest-preview__item-value">{sym}{fmt(preview.principal)}</div>
                        </div>
                        <div>
                          <div className="invest-preview__item-label">Duration</div>
                          <div className="invest-preview__item-value">{DURATION_META[preview.duration]?.label}</div>
                        </div>
                        <div>
                          <div className="invest-preview__item-label">
                            <Percent size={10} style={{ display:'inline', marginRight:2 }} />Rate
                          </div>
                          <div className="invest-preview__item-value">{preview.annualRate}% p.a.</div>
                        </div>
                        <div>
                          <div className="invest-preview__item-label">Profit</div>
                          <div className="invest-preview__item-value profit">+{sym}{fmt(preview.expectedProfit)}</div>
                        </div>
                        <div style={{ gridColumn:'1/-1' }}>
                          <div className="invest-preview__item-label">Total Payout on {fmtDate(preview.maturityDate)}</div>
                          <div className="invest-preview__item-value total">{sym}{fmt(preview.totalPayout)}</div>
                        </div>
                      </div>
                    )}

                    {/* Lock notice */}
                    <div className="invest-lock-notice">
                      <Info size={16} style={{ flexShrink:0, marginTop:1 }} />
                      Funds are locked until maturity. Early withdrawal is not permitted.
                    </div>

                    {submitError && (
                      <div className="invest-error">
                        <AlertCircle size={15} />
                        {submitError}
                      </div>
                    )}

                    <button type="submit" className="invest-btn" disabled={submitting || !principal}>
                      {submitting
                        ? <><div className="invest-spinner" /> Processing…</>
                        : <><Lock size={16} /> Lock &amp; Invest</>
                      }
                    </button>
                  </form>
                </div>
              </div>

            </div>{/* end layout */}
          </div>
          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav />

      {/* Success modal */}
      {successData && (
        <div className="invest-success-overlay" onClick={() => setSuccessData(null)}>
          <div className="invest-success-card" onClick={e => e.stopPropagation()}>
            <div className="invest-success-card__icon-wrap">
              <CheckCircle size={32} />
            </div>
            <h3 className="invest-success-card__title">Investment Created!</h3>
            <p className="invest-success-card__text">
              <strong>{successData.currencyCode} {fmt(successData.principal)}</strong> has been locked.
              Your investment matures on <strong>{fmtDate(successData.maturityDate)}</strong>.
              Expected payout: <strong>{successData.currencyCode} {fmt(successData.totalPayout)}</strong>.
            </p>
            <button className="invest-btn" onClick={() => setSuccessData(null)}>
              <CheckCircle size={16} /> Done
            </button>
          </div>
        </div>
      )}

      <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
      {!isChatOpen && (
        <button className="chat-fab" onClick={() => setIsChatOpen(true)}>
          <TrendingUp size={20} />
        </button>
      )}
    </div>
  );
}
