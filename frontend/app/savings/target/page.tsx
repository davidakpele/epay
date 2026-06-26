'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { PiggyBank, Plus, ArrowDownCircle, CheckCircle, RefreshCw, X } from 'lucide-react';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import LoadingScreen from '@/components/loader/Loadingscreen';
import SupportChatBot from '@/components/SupportChatBot';
import { getUserId, getUserWalletId, targetSavingsService } from '../../api';
import { TargetSavings } from '../../api/services/targetSavingsService';
import './TargetSavings.css';

const GOAL_ICONS = ['🏠', '🚗', '✈️', '📱', '💍', '🎓', '🏋️', '🛍️', '💻', '🌴', '👶', '💰'];

type ModalMode = 'topup' | 'withdraw' | null;

export default function TargetSavingsPage() {
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [isPageLoading, setIsPageLoading]   = useState(true);
  const [isChatOpen, setIsChatOpen]         = useState(false);

  // Goals list
  const [goals, setGoals]           = useState<TargetSavings[]>([]);
  const [loadingGoals, setLoadingGoals] = useState(false);

  // New goal form
  const [showNewForm, setShowNewForm]   = useState(false);
  const [goalName, setGoalName]         = useState('');
  const [description, setDescription]   = useState('');
  const [targetAmount, setTargetAmount] = useState('');
  const [targetDate, setTargetDate]     = useState('');
  const [currency, setCurrency]         = useState('NGN');
  const [selectedIcon, setSelectedIcon] = useState('🏠');
  const [creating, setCreating]         = useState(false);
  const [createError, setCreateError]   = useState('');

  // Top-up / withdraw modal
  const [modalGoal, setModalGoal]   = useState<TargetSavings | null>(null);
  const [modalMode, setModalMode]   = useState<ModalMode>(null);
  const [topupAmount, setTopupAmount] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError]     = useState('');

  const userId   = getUserId();
  const walletId = getUserWalletId();

  // ── Load goals ────────────────────────────────────────────────────────────

  const loadGoals = useCallback(async () => {
    if (!userId) return;
    try {
      setLoadingGoals(true);
      const res = await targetSavingsService.getByUserId(userId);
      setGoals(res?.data ?? []);
    } catch {
      // ignore
    } finally {
      setLoadingGoals(false);
    }
  }, [userId]);

  useEffect(() => {
    loadGoals().finally(() => setTimeout(() => setIsPageLoading(false), 500));
  }, [loadGoals]);

  // ── Create goal ───────────────────────────────────────────────────────────

  const handleCreateGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError('');

    const amount = parseFloat(targetAmount);
    if (!amount || amount <= 0) { setCreateError('Target amount must be greater than 0.'); return; }
    if (!goalName.trim())       { setCreateError('Goal name is required.'); return; }
    if (!userId || !walletId)   { setCreateError('Please log in and ensure your wallet is set up.'); return; }

    try {
      setCreating(true);
      await targetSavingsService.create({
        userId:      Number(userId),
        walletId:    Number(walletId),
        currencyCode: currency,
        goalName:    goalName.trim(),
        description: description.trim() || undefined,
        targetAmount: amount,
        targetDate:  targetDate || undefined,
        goalIcon:    selectedIcon,
      });
      // Reset form
      setGoalName('');
      setDescription('');
      setTargetAmount('');
      setTargetDate('');
      setSelectedIcon('🏠');
      setShowNewForm(false);
      await loadGoals();
    } catch (err: any) {
      setCreateError(err?.message ?? 'Failed to create savings goal. Please try again.');
    } finally {
      setCreating(false);
    }
  };

  // ── Top-Up ────────────────────────────────────────────────────────────────

  const handleTopUp = async () => {
    setActionError('');
    const amount = parseFloat(topupAmount);
    if (!amount || amount <= 0) { setActionError('Enter a valid amount.'); return; }
    if (!modalGoal || !userId || !walletId) return;

    try {
      setActionLoading(true);
      await targetSavingsService.topUp(modalGoal.id, {
        userId:   Number(userId),
        walletId: Number(walletId),
        amount,
      });
      setModalGoal(null);
      setModalMode(null);
      setTopupAmount('');
      await loadGoals();
    } catch (err: any) {
      setActionError(err?.message ?? 'Top-up failed. Please try again.');
    } finally {
      setActionLoading(false);
    }
  };

  // ── Withdraw ──────────────────────────────────────────────────────────────

  const handleWithdraw = async () => {
    setActionError('');
    if (!modalGoal || !userId || !walletId) return;

    try {
      setActionLoading(true);
      await targetSavingsService.withdraw(modalGoal.id, {
        userId:   Number(userId),
        walletId: Number(walletId),
      });
      setModalGoal(null);
      setModalMode(null);
      await loadGoals();
    } catch (err: any) {
      setActionError(err?.message ?? 'Withdrawal failed. Please try again.');
    } finally {
      setActionLoading(false);
    }
  };

  // ── Helpers ───────────────────────────────────────────────────────────────

  const formatAmount = (n: number) =>
    n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

  const progressPercent = (saved: number, target: number) =>
    Math.min(100, target > 0 ? Math.round((saved / target) * 100) : 0);

  const toggleTheme = () => setTheme(t => t === 'light' ? 'dark' : 'light');

  const openTopUp   = (g: TargetSavings) => { setModalGoal(g); setModalMode('topup');    setActionError(''); setTopupAmount(''); };
  const openWithdraw = (g: TargetSavings) => { setModalGoal(g); setModalMode('withdraw'); setActionError(''); };
  const closeModal  = () => { setModalGoal(null); setModalMode(null); };

  const canTopUp   = (g: TargetSavings) => g.status === 'ACTIVE';
  const canWithdraw = (g: TargetSavings) => g.savedAmount > 0 && (g.status === 'ACTIVE' || g.status === 'COMPLETED');

  if (isPageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />
      <main className="main-content">
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <div className="savings-page">
            <h1 className="savings-page__title">
              <PiggyBank size={24} style={{ display: 'inline', marginRight: 8, verticalAlign: 'middle', color: 'var(--bg-main)' }} />
              Target Savings
            </h1>
            <p className="savings-page__subtitle">
              Save towards specific goals — a car, a house, travel or anything else.
              Deposit whenever you like and withdraw when you're ready.
            </p>

            {/* ── New Goal Form ─────────────────────────────────────────── */}
            {!showNewForm ? (
              <button
                className="savings-submit-btn"
                style={{ maxWidth: 260, marginBottom: 32 }}
                onClick={() => setShowNewForm(true)}
              >
                <Plus size={16} /> Create New Goal
              </button>
            ) : (
              <div className="savings-new-goal">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                  <h2 className="savings-new-goal__title">New Savings Goal</h2>
                  <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280' }}
                    onClick={() => setShowNewForm(false)}>
                    <X size={18} />
                  </button>
                </div>

                <form onSubmit={handleCreateGoal}>
                  {/* Icon picker */}
                  <div className="savings-form__row">
                    <label className="savings-form__label">Goal Icon</label>
                    <div className="icon-picker">
                      {GOAL_ICONS.map(icon => (
                        <div
                          key={icon}
                          className={`icon-picker__item ${selectedIcon === icon ? 'selected' : ''}`}
                          onClick={() => setSelectedIcon(icon)}
                        >
                          {icon}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="savings-form__row-inline">
                    <div className="savings-form__row">
                      <label className="savings-form__label">Goal Name *</label>
                      <input
                        className="savings-form__input"
                        placeholder="e.g. Dream Car"
                        value={goalName}
                        onChange={e => setGoalName(e.target.value)}
                        required
                      />
                    </div>
                    <div className="savings-form__row">
                      <label className="savings-form__label">Currency</label>
                      <select
                        className="savings-form__input"
                        value={currency}
                        onChange={e => setCurrency(e.target.value)}
                      >
                        {['NGN', 'USD', 'GBP', 'EUR', 'AUD', 'CAD', 'CHF', 'JPY', 'CNY', 'INR'].map(c => (
                          <option key={c} value={c}>{c}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div className="savings-form__row-inline">
                    <div className="savings-form__row">
                      <label className="savings-form__label">Target Amount ({currency}) *</label>
                      <input
                        type="number"
                        className="savings-form__input"
                        placeholder="e.g. 5000000"
                        min="0.01"
                        step="0.01"
                        value={targetAmount}
                        onChange={e => setTargetAmount(e.target.value)}
                        required
                      />
                    </div>
                    <div className="savings-form__row">
                      <label className="savings-form__label">Target Date (Optional)</label>
                      <input
                        type="date"
                        className="savings-form__input"
                        value={targetDate}
                        min={new Date().toISOString().split('T')[0]}
                        onChange={e => setTargetDate(e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="savings-form__row">
                    <label className="savings-form__label">Description (Optional)</label>
                    <input
                      className="savings-form__input"
                      placeholder="A short note about this goal"
                      value={description}
                      onChange={e => setDescription(e.target.value)}
                    />
                  </div>

                  {createError && (
                    <div style={{ color: '#dc2626', background: '#fee2e2', padding: '10px 14px', borderRadius: 8, fontSize: '0.82rem', marginBottom: 12 }}>
                      {createError}
                    </div>
                  )}

                  <div style={{ display: 'flex', gap: 12 }}>
                    <button
                      type="button"
                      className="savings-submit-btn"
                      style={{ background: '#f3f4f6', color: '#374151', flex: 0.4 }}
                      onClick={() => setShowNewForm(false)}
                    >
                      Cancel
                    </button>
                    <button type="submit" className="savings-submit-btn" disabled={creating} style={{ flex: 1 }}>
                      {creating
                        ? <><div className="savings-spinner" /> Creating…</>
                        : <><Plus size={16} /> Create Goal</>
                      }
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* ── Goals List ─────────────────────────────────────────────── */}
            <div className="savings-section-header">
              <h2 className="savings-section-title">Your Savings Goals</h2>
              <button
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--bg-main)', display: 'flex', alignItems: 'center', gap: 4 }}
                onClick={loadGoals}
              >
                <RefreshCw size={15} />
                <span style={{ fontSize: '0.8rem' }}>Refresh</span>
              </button>
            </div>

            {loadingGoals ? (
              <div style={{ textAlign: 'center', padding: 32, color: 'var(--text-secondary)' }}>Loading…</div>
            ) : goals.length === 0 ? (
              <div className="savings-empty">
                <div className="savings-empty__icon">🐷</div>
                <p>No savings goals yet. Create your first one above!</p>
              </div>
            ) : (
              <div className="savings-goals-grid">
                {goals.map(goal => {
                  const pct = progressPercent(goal.savedAmount, goal.targetAmount);
                  const isComplete = pct >= 100;
                  const statusKey = goal.status.toLowerCase() as any;

                  return (
                    <div key={goal.id} className="savings-goal-card">
                      <div className="savings-goal-card__header">
                        <div className="savings-goal-card__icon">{goal.goalIcon ?? '💰'}</div>
                        <div className="savings-goal-card__info">
                          <div className="savings-goal-card__name">{goal.goalName}</div>
                          <div className={`savings-goal-card__status ${statusKey}`}>
                            {isComplete && <CheckCircle size={11} />}
                            {goal.status === 'COMPLETED' ? 'Goal Reached!' : goal.status}
                          </div>
                        </div>
                      </div>

                      {/* Progress Bar */}
                      <div className="savings-progress">
                        <div className="savings-progress__info">
                          <span>{goal.currencyCode} {formatAmount(goal.savedAmount)} saved</span>
                          <span>{pct}%</span>
                        </div>
                        <div className="savings-progress__bar-track">
                          <div
                            className={`savings-progress__bar-fill ${isComplete ? 'complete' : ''}`}
                            style={{ width: `${pct}%` }}
                          />
                        </div>
                      </div>

                      {/* Amounts */}
                      <div className="savings-goal-card__amounts">
                        <div className="savings-goal-card__amount-item">
                          <span className="savings-goal-card__amount-label">Saved</span>
                          <span className="savings-goal-card__amount-value saved">
                            {goal.currencyCode} {formatAmount(goal.savedAmount)}
                          </span>
                        </div>
                        <div className="savings-goal-card__amount-item" style={{ textAlign: 'right' }}>
                          <span className="savings-goal-card__amount-label">Target</span>
                          <span className="savings-goal-card__amount-value">
                            {goal.currencyCode} {formatAmount(goal.targetAmount)}
                          </span>
                        </div>
                      </div>

                      {/* Remaining */}
                      {goal.status === 'ACTIVE' && goal.savedAmount < goal.targetAmount && (
                        <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginBottom: 14 }}>
                          Still need: {goal.currencyCode} {formatAmount(goal.targetAmount - goal.savedAmount)}
                          {goal.targetDate && ` · Target date: ${new Date(goal.targetDate).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}`}
                        </div>
                      )}

                      {/* Actions */}
                      {(goal.status === 'ACTIVE' || goal.status === 'COMPLETED') && (
                        <div className="savings-goal-card__actions">
                          {canTopUp(goal) && (
                            <button className="savings-action-btn savings-action-btn--topup" onClick={() => openTopUp(goal)}>
                              <Plus size={13} /> Add Funds
                            </button>
                          )}
                          {canWithdraw(goal) && (
                            <button className="savings-action-btn savings-action-btn--withdraw" onClick={() => openWithdraw(goal)}>
                              <ArrowDownCircle size={13} /> Withdraw
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

      {/* ── Top-Up / Withdraw Modal ───────────────────────────────────────── */}
      {modalGoal && modalMode && (
        <div className="savings-modal-overlay" onClick={closeModal}>
          <div className="savings-modal" onClick={e => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
              <h3 className="savings-modal__title">
                {modalMode === 'topup' ? '💳 Add Funds' : '💸 Withdraw Savings'}
              </h3>
              <button style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280' }} onClick={closeModal}>
                <X size={18} />
              </button>
            </div>

            <p className="savings-modal__sub">
              {modalMode === 'topup'
                ? `Deposit into "${modalGoal.goalName}" — deducted from your main wallet.`
                : `Withdraw all ${modalGoal.currencyCode} ${formatAmount(modalGoal.savedAmount)} back to your main wallet.`
              }
            </p>

            {modalMode === 'topup' && (
              <div className="savings-form__row">
                <label className="savings-form__label">Amount ({modalGoal.currencyCode})</label>
                <input
                  type="number"
                  className="savings-form__input"
                  placeholder="Amount to add"
                  min="0.01"
                  step="0.01"
                  value={topupAmount}
                  onChange={e => setTopupAmount(e.target.value)}
                  autoFocus
                />
              </div>
            )}

            {modalMode === 'withdraw' && (
              <div style={{
                background: '#fff7ed',
                border: '1px solid #fed7aa',
                borderRadius: 10,
                padding: '12px 14px',
                fontSize: '0.82rem',
                color: '#92400e',
                marginBottom: 4
              }}>
                ⚠️ Withdrawing will move all your saved funds back to your main wallet.
                Your goal will be marked as withdrawn.
              </div>
            )}

            {actionError && (
              <div style={{ color: '#dc2626', background: '#fee2e2', padding: '8px 12px', borderRadius: 8, fontSize: '0.82rem', margin: '12px 0' }}>
                {actionError}
              </div>
            )}

            <div className="savings-modal__actions">
              <button
                className="savings-action-btn savings-action-btn--cancel"
                style={{ flex: 0.5 }}
                onClick={closeModal}
                disabled={actionLoading}
              >
                Cancel
              </button>

              {modalMode === 'topup' ? (
                <button
                  className="savings-action-btn savings-action-btn--topup"
                  style={{ flex: 1 }}
                  onClick={handleTopUp}
                  disabled={actionLoading || !topupAmount}
                >
                  {actionLoading
                    ? <><div className="savings-spinner" /> Processing…</>
                    : <><Plus size={13} /> Confirm Top-Up</>
                  }
                </button>
              ) : (
                <button
                  className="savings-action-btn savings-action-btn--withdraw"
                  style={{ flex: 1 }}
                  onClick={handleWithdraw}
                  disabled={actionLoading}
                >
                  {actionLoading
                    ? <><div className="savings-spinner" /> Processing…</>
                    : <><ArrowDownCircle size={13} /> Confirm Withdraw</>
                  }
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
      {!isChatOpen && (
        <button className="chat-fab" onClick={() => setIsChatOpen(true)}>💬</button>
      )}
    </div>
  );
}
