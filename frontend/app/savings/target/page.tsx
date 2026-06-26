'use client';

import React, { useState, useEffect, useCallback } from 'react';
import {
  PiggyBank, Plus, ArrowDownCircle, CheckCircle, RefreshCw,
  X, Car, Home, Plane, Smartphone, GraduationCap, ShoppingBag,
  Laptop, DollarSign, Target, TrendingUp, AlertCircle, Info,
  Wallet, Calendar, ChevronRight
} from 'lucide-react';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import LoadingScreen from '@/components/loader/Loadingscreen';
import SupportChatBot from '@/components/SupportChatBot';
import { getUserId, getUserWalletId, getActiveWallet, getFiat, targetSavingsService } from '../../api';
import { TargetSavings } from '../../api/services/targetSavingsService';
import './TargetSavings.css';

// Lucide icon options for goal icons
const GOAL_ICON_OPTIONS: { key: string; icon: React.ReactNode; label: string }[] = [
  { key: 'home',     icon: <Home size={20} />,         label: 'House'     },
  { key: 'car',      icon: <Car size={20} />,          label: 'Car'       },
  { key: 'travel',   icon: <Plane size={20} />,        label: 'Travel'    },
  { key: 'phone',    icon: <Smartphone size={20} />,   label: 'Phone'     },
  { key: 'education',icon: <GraduationCap size={20} />,label: 'Education' },
  { key: 'shopping', icon: <ShoppingBag size={20} />,  label: 'Shopping'  },
  { key: 'laptop',   icon: <Laptop size={20} />,       label: 'Laptop'    },
  { key: 'savings',  icon: <DollarSign size={20} />,   label: 'Savings'   },
  { key: 'target',   icon: <Target size={20} />,       label: 'Custom'    },
  { key: 'invest',   icon: <TrendingUp size={20} />,   label: 'Invest'    },
];

const getGoalIcon = (key?: string) => {
  const found = GOAL_ICON_OPTIONS.find(o => o.key === key);
  return found ? found.icon : <PiggyBank size={20} />;
};

const CURRENCIES = ['NGN','USD','GBP','EUR','AUD','CAD','CHF','JPY','CNY','INR'];
const getCurrencySymbol = (c: string) =>
  ({ NGN:'₦', USD:'$', GBP:'£', EUR:'€', AUD:'A$', CAD:'C$', CHF:'Fr', JPY:'¥', CNY:'¥', INR:'₹' }[c] ?? c);

const fmt = (n: number) =>
  n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const pct = (saved: number, target: number) =>
  Math.min(100, target > 0 ? Math.round((saved / target) * 100) : 0);

type ModalMode = 'create' | 'topup' | 'withdraw' | null;

export default function TargetSavingsPage() {
  const [theme, setTheme]             = useState<'light' | 'dark'>('light');
  const [pageLoading, setPageLoading] = useState(true);
  const [isChatOpen, setIsChatOpen]   = useState(false);

  const [goals, setGoals]             = useState<TargetSavings[]>([]);
  const [loadingGoals, setLoadingGoals] = useState(false);

  // Modal state
  const [modalMode, setModalMode]     = useState<ModalMode>(null);
  const [activeGoal, setActiveGoal]   = useState<TargetSavings | null>(null);

  // Create form
  const [goalName, setGoalName]       = useState('');
  const [description, setDescription] = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [targetDate, setTargetDate]   = useState('');
  const [currency, setCurrency]       = useState('NGN');
  const [selectedIconKey, setIconKey] = useState('savings');
  const [creating, setCreating]       = useState(false);
  const [createError, setCreateError] = useState('');

  // Top-up / withdraw
  const [topupAmount, setTopupAmount]   = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError]   = useState('');

  const userId   = getUserId();
  const walletId = getUserWalletId();

  const loadGoals = useCallback(async () => {
    if (!userId) return;
    setLoadingGoals(true);
    try {
      const res = await targetSavingsService.getByUserId(userId);
      setGoals(res?.data ?? []);
    } catch { /* ignore */ }
    finally { setLoadingGoals(false); }
  }, [userId]);

  useEffect(() => {
    loadGoals().finally(() => setTimeout(() => setPageLoading(false), 400));
  }, [loadGoals]);

  useEffect(() => {
    const active = getActiveWallet() || getFiat();
    if (active) setCurrency(active);
  }, []);

  // ── Stats ─────────────────────────────────────────────────────
  const totalSaved     = goals.reduce((s, g) => s + g.savedAmount, 0);
  const activeGoals    = goals.filter(g => g.status === 'ACTIVE').length;
  const completedGoals = goals.filter(g => g.status === 'COMPLETED').length;
  const avgProgress    = goals.length
    ? Math.round(goals.reduce((s, g) => s + pct(g.savedAmount, g.targetAmount), 0) / goals.length)
    : 0;

  // ── Handlers ──────────────────────────────────────────────────

  const openCreate   = () => { setModalMode('create'); setCreateError(''); resetCreateForm(); };
  const openTopUp    = (g: TargetSavings) => { setActiveGoal(g); setModalMode('topup');    setActionError(''); setTopupAmount(''); };
  const openWithdraw = (g: TargetSavings) => { setActiveGoal(g); setModalMode('withdraw'); setActionError(''); };
  const closeModal   = () => { setModalMode(null); setActiveGoal(null); };

  const resetCreateForm = () => {
    setGoalName(''); setDescription(''); setTargetAmount('');
    setTargetDate(''); setIconKey('savings'); setCreateError('');
  };

  const handleCreateGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError('');
    const amount = parseFloat(targetAmount);
    if (!amount || amount <= 0) { setCreateError('Target amount must be greater than 0.'); return; }
    if (!goalName.trim())        { setCreateError('Goal name is required.'); return; }
    if (!userId || !walletId)    { setCreateError('Wallet not found. Please log in again.'); return; }
    setCreating(true);
    try {
      await targetSavingsService.create({
        userId: Number(userId), walletId: Number(walletId),
        currencyCode: currency, goalName: goalName.trim(),
        description: description.trim() || undefined,
        targetAmount: amount, targetDate: targetDate || undefined,
        goalIcon: selectedIconKey,
      });
      closeModal();
      await loadGoals();
    } catch (err: any) {
      setCreateError(err?.message ?? 'Failed to create goal. Please try again.');
    }
    setCreating(false);
  };

  const handleTopUp = async () => {
    setActionError('');
    const amount = parseFloat(topupAmount);
    if (!amount || amount <= 0) { setActionError('Enter a valid amount.'); return; }
    if (!activeGoal || !userId || !walletId) return;
    setActionLoading(true);
    try {
      await targetSavingsService.topUp(activeGoal.id, {
        userId: Number(userId), walletId: Number(walletId), amount,
      });
      closeModal();
      await loadGoals();
    } catch (err: any) {
      setActionError(err?.message ?? 'Top-up failed. Please try again.');
    }
    setActionLoading(false);
  };

  const handleWithdraw = async () => {
    setActionError('');
    if (!activeGoal || !userId || !walletId) return;
    setActionLoading(true);
    try {
      await targetSavingsService.withdraw(activeGoal.id, {
        userId: Number(userId), walletId: Number(walletId),
      });
      closeModal();
      await loadGoals();
    } catch (err: any) {
      setActionError(err?.message ?? 'Withdrawal failed. Please try again.');
    }
    setActionLoading(false);
  };

  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light');

  if (pageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />
      <main className="main-content">
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <div className="savings-page">

            {/* ── Hero ─────────────────────────────────────────── */}
            <div className="savings-hero">
              <div className="savings-hero__left">
                <h1>Target Savings</h1>
                <p>Set a goal, save towards it step by step, and withdraw whenever you reach your target or are ready.</p>
              </div>
              <button className="savings-hero__action" onClick={openCreate}>
                <Plus size={16} /> New Goal
              </button>
            </div>

            {/* ── Stats ────────────────────────────────────────── */}
            <div className="savings-stats">
              <div className="savings-stat-card">
                <div className="savings-stat-card__icon green"><Wallet size={18} /></div>
                <span className="savings-stat-card__label">Total Saved</span>
                <span className="savings-stat-card__value">{fmt(totalSaved)}</span>
              </div>
              <div className="savings-stat-card">
                <div className="savings-stat-card__icon blue"><Target size={18} /></div>
                <span className="savings-stat-card__label">Active Goals</span>
                <span className="savings-stat-card__value">{activeGoals}</span>
              </div>
              <div className="savings-stat-card">
                <div className="savings-stat-card__icon purple"><CheckCircle size={18} /></div>
                <span className="savings-stat-card__label">Completed</span>
                <span className="savings-stat-card__value">{completedGoals}</span>
              </div>
              <div className="savings-stat-card">
                <div className="savings-stat-card__icon amber"><TrendingUp size={18} /></div>
                <span className="savings-stat-card__label">Avg Progress</span>
                <span className="savings-stat-card__value">{avgProgress}%</span>
              </div>
            </div>

            {/* ── Goals section ────────────────────────────────── */}
            <div className="savings-section-header">
              <span className="savings-section-title">Your Goals</span>
              <button className="savings-refresh-btn" onClick={loadGoals}>
                <RefreshCw size={14} /> Refresh
              </button>
            </div>

            {loadingGoals ? (
              <div style={{ textAlign:'center', padding: 48, color:'var(--text-secondary)' }}>
                <RefreshCw size={24} style={{ animation:'spin 1s linear infinite', opacity:0.4 }} />
              </div>
            ) : goals.length === 0 ? (
              <div className="savings-empty">
                <PiggyBank size={44} />
                <p>No savings goals yet.<br />Hit <strong>New Goal</strong> above to get started.</p>
              </div>
            ) : (
              <div className="savings-goals-grid">
                {goals.map(goal => {
                  const progress  = pct(goal.savedAmount, goal.targetAmount);
                  const complete  = progress >= 100;
                  const statusKey = goal.status.toLowerCase() as any;
                  const sym       = getCurrencySymbol(goal.currencyCode);

                  return (
                    <div key={goal.id} className="savings-goal-card">
                      {/* Header */}
                      <div className="savings-goal-card__header">
                        <div className="savings-goal-card__icon">
                          {getGoalIcon(goal.goalIcon)}
                        </div>
                        <div style={{ flex:1, minWidth:0 }}>
                          <div className="savings-goal-card__name">{goal.goalName}</div>
                          <span className={`savings-goal-card__status ${statusKey}`}>
                            {complete && <CheckCircle size={10} />}
                            {goal.status === 'COMPLETED' ? 'Goal Reached' : goal.status}
                          </span>
                        </div>
                      </div>

                      {/* Progress */}
                      <div className="savings-progress">
                        <div className="savings-progress__info">
                          <span>{sym}{fmt(goal.savedAmount)} saved</span>
                          <span>{progress}%</span>
                        </div>
                        <div className="savings-progress__track">
                          <div
                            className={`savings-progress__fill ${complete ? 'complete' : ''}`}
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                      </div>

                      {/* Amounts */}
                      <div className="savings-card__amounts">
                        <div className="savings-card__amount-item">
                          <span className="savings-card__amount-label">Saved</span>
                          <span className="savings-card__amount-value saved">{sym}{fmt(goal.savedAmount)}</span>
                        </div>
                        <div className="savings-card__amount-item" style={{ textAlign:'right' }}>
                          <span className="savings-card__amount-label">Target</span>
                          <span className="savings-card__amount-value">{sym}{fmt(goal.targetAmount)}</span>
                        </div>
                      </div>

                      {/* Remaining / deadline */}
                      {goal.status === 'ACTIVE' && goal.savedAmount < goal.targetAmount && (
                        <div className="savings-card__remaining">
                          <ChevronRight size={12} />
                          {sym}{fmt(goal.targetAmount - goal.savedAmount)} remaining
                          {goal.targetDate && (
                            <> &bull; <Calendar size={11} /> {new Date(goal.targetDate).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' })}</>
                          )}
                        </div>
                      )}

                      {/* Actions */}
                      {(goal.status === 'ACTIVE' || goal.status === 'COMPLETED') && (
                        <div className="savings-card__actions">
                          {goal.status === 'ACTIVE' && (
                            <button className="savings-action-btn savings-action-btn--add" onClick={() => openTopUp(goal)}>
                              <Plus size={14} /> Add Funds
                            </button>
                          )}
                          {goal.savedAmount > 0 && (
                            <button className="savings-action-btn savings-action-btn--withdraw" onClick={() => openWithdraw(goal)}>
                              <ArrowDownCircle size={14} /> Withdraw
                            </button>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav />

      {/* ─────────────────── Modals ─────────────────────────────── */}
      {modalMode && (
        <div className="savings-modal-overlay" onClick={closeModal}>
          <div
            className={modalMode === 'create' ? 'savings-modal' : 'savings-action-modal'}
            onClick={e => e.stopPropagation()}
          >

            {/* ── Create goal ─────────────────────────────────── */}
            {modalMode === 'create' && (
              <>
                <div className="savings-modal__header">
                  <span className="savings-modal__title">
                    <Target size={18} style={{ display:'inline', marginRight:6, verticalAlign:'middle' }} />
                    New Savings Goal
                  </span>
                  <button className="savings-modal__close" onClick={closeModal}><X size={18} /></button>
                </div>

                <form onSubmit={handleCreateGoal}>
                  {/* Icon picker */}
                  <div className="savings-form__row">
                    <label className="savings-form__label">Goal Icon</label>
                    <div className="savings-icon-picker">
                      {GOAL_ICON_OPTIONS.map(opt => (
                        <div
                          key={opt.key}
                          className={`savings-icon-option ${selectedIconKey === opt.key ? 'selected' : ''}`}
                          onClick={() => setIconKey(opt.key)}
                          title={opt.label}
                        >
                          {opt.icon}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="savings-form__row-2">
                    <div className="savings-form__row">
                      <label className="savings-form__label">Goal Name *</label>
                      <input className="savings-form__input" placeholder="e.g. Dream Car" value={goalName} onChange={e => setGoalName(e.target.value)} required />
                    </div>
                    <div className="savings-form__row">
                      <label className="savings-form__label">Currency</label>
                      <select className="savings-form__input" value={currency} onChange={e => setCurrency(e.target.value)}>
                        {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
                      </select>
                    </div>
                  </div>

                  <div className="savings-form__row-2">
                    <div className="savings-form__row">
                      <label className="savings-form__label">Target Amount ({currency}) *</label>
                      <input type="number" className="savings-form__input" placeholder="e.g. 5,000,000" min="0.01" step="0.01" value={targetAmount} onChange={e => setTargetAmount(e.target.value)} required />
                    </div>
                    <div className="savings-form__row">
                      <label className="savings-form__label">Target Date (optional)</label>
                      <input type="date" className="savings-form__input" value={targetDate} min={new Date().toISOString().split('T')[0]} onChange={e => setTargetDate(e.target.value)} />
                    </div>
                  </div>

                  <div className="savings-form__row">
                    <label className="savings-form__label">Note (optional)</label>
                    <input className="savings-form__input" placeholder="What is this goal for?" value={description} onChange={e => setDescription(e.target.value)} />
                  </div>

                  {createError && (
                    <div className="savings-error"><AlertCircle size={14} />{createError}</div>
                  )}

                  <div style={{ display:'flex', gap:10 }}>
                    <button type="button" className="savings-submit-btn cancel" style={{ flex: '0 0 auto', width: 'auto', padding:'13px 20px' }} onClick={closeModal}>
                      Cancel
                    </button>
                    <button type="submit" className="savings-submit-btn" disabled={creating}>
                      {creating ? <><div className="savings-spinner" /> Creating…</> : <><Plus size={16} /> Create Goal</>}
                    </button>
                  </div>
                </form>
              </>
            )}

            {/* ── Top-up ──────────────────────────────────────── */}
            {modalMode === 'topup' && activeGoal && (
              <>
                <div className="savings-modal__header">
                  <span className="savings-modal__title">
                    <Plus size={18} style={{ display:'inline', marginRight:6, verticalAlign:'middle' }} />
                    Add Funds
                  </span>
                  <button className="savings-modal__close" onClick={closeModal}><X size={18} /></button>
                </div>
                <p className="savings-action-modal__sub">
                  Deposit into <strong>{activeGoal.goalName}</strong> — deducted from your main {activeGoal.currencyCode} wallet.
                </p>
                <div className="savings-form__row">
                  <label className="savings-form__label">Amount ({activeGoal.currencyCode})</label>
                  <input type="number" className="savings-form__input" placeholder="Amount to add" min="0.01" step="0.01" value={topupAmount} onChange={e => setTopupAmount(e.target.value)} autoFocus />
                </div>
                {actionError && <div className="savings-error"><AlertCircle size={14} />{actionError}</div>}
                <div className="savings-action-modal__btns">
                  <button className="savings-submit-btn cancel" style={{ flex:'0 0 auto', width:'auto', padding:'12px 20px' }} onClick={closeModal} disabled={actionLoading}>Cancel</button>
                  <button className="savings-submit-btn" onClick={handleTopUp} disabled={actionLoading || !topupAmount}>
                    {actionLoading ? <><div className="savings-spinner" /> Processing…</> : <><Plus size={15} /> Confirm</>}
                  </button>
                </div>
              </>
            )}

            {/* ── Withdraw ────────────────────────────────────── */}
            {modalMode === 'withdraw' && activeGoal && (
              <>
                <div className="savings-modal__header">
                  <span className="savings-modal__title">
                    <ArrowDownCircle size={18} style={{ display:'inline', marginRight:6, verticalAlign:'middle' }} />
                    Withdraw Savings
                  </span>
                  <button className="savings-modal__close" onClick={closeModal}><X size={18} /></button>
                </div>
                <p className="savings-action-modal__sub">
                  Withdraw <strong>{activeGoal.currencyCode} {fmt(activeGoal.savedAmount)}</strong> from <strong>{activeGoal.goalName}</strong> back to your main wallet.
                </p>
                <div className="savings-warning-box">
                  <Info size={15} style={{ flexShrink:0, marginTop:1 }} />
                  This will move all saved funds back to your wallet and mark the goal as withdrawn.
                </div>
                {actionError && <div className="savings-error"><AlertCircle size={14} />{actionError}</div>}
                <div className="savings-action-modal__btns">
                  <button className="savings-submit-btn cancel" style={{ flex:'0 0 auto', width:'auto', padding:'12px 20px' }} onClick={closeModal} disabled={actionLoading}>Cancel</button>
                  <button className="savings-submit-btn" style={{ background:'#dc2626' }} onClick={handleWithdraw} disabled={actionLoading}>
                    {actionLoading ? <><div className="savings-spinner" /> Processing…</> : <><ArrowDownCircle size={15} /> Confirm Withdraw</>}
                  </button>
                </div>
              </>
            )}

          </div>
        </div>
      )}

      <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
      {!isChatOpen && (
        <button className="chat-fab" onClick={() => setIsChatOpen(true)}>
          <PiggyBank size={20} />
        </button>
      )}
    </div>
  );
}
