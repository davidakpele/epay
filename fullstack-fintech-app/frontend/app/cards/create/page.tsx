'use client';

import React, { useState, useEffect } from 'react';
import DepositModal from '@/components/DepositModal';
import Footer from '@/components/Footer';
import Header from '@/components/Header';
import MobileNav from '@/components/MobileNav';
import Sidebar from '@/components/Sidebar';
import LoadingScreen from '@/components/loader/Loadingscreen';
import Link from 'next/link';
import { ChevronRight, Plus, CreditCard, Eye, EyeOff, Info, ArrowUpRight, ArrowDownLeft, ChevronDown } from 'lucide-react';
import './VirtualCards.css';
import { Toast } from '@/app/types/auth';

// ─── Types ────────────────────────────────────────────────────────────────────

type CardStatus = 'Active' | 'Pending' | 'On Review';
type CardNetwork = 'Visa' | 'Mastercard';
type WalletType = 'USD' | 'EUR' | 'GBP';

interface VirtualCard {
  id: string;
  network: CardNetwork;
  currency: WalletType;
  label: string;
  last4: string;
  expiry: string;
  holder: string;
  status: CardStatus;
  balance: number;
  flag: string;
}

interface CardRequest {
  id: string;
  currency: WalletType;
  network: CardNetwork;
  amount: number;
  status: CardStatus;
  requestedOn: string;
  approvedOn?: string;
  flag: string;
}

interface Wallet {
  code: WalletType;
  label: string;
  balance: number;
  symbol: string;
  flag: string;
}

// ─── Flag image helper ────────────────────────────────────────────────────────

const Flag = ({ src, alt }: { src: string; alt: string }) => (
  <img src={src} alt={alt} className="flag-img" />
);

// ─── Data ─────────────────────────────────────────────────────────────────────

const wallets: Wallet[] = [
  { code: 'USD', label: 'USD Wallet', balance: 3280.50, symbol: '$', flag: '../../assets/images/america-flag.png' },
  { code: 'EUR', label: 'EUR Wallet', balance: 2450.00, symbol: '€', flag: '../../assets/images/euro.png' },
];

const initialCards: VirtualCard[] = [
  { id: '1', network: 'Visa',       currency: 'USD', label: 'USD Virtual Card', last4: '4521', expiry: '08/26', holder: 'JOHN A. DOE', status: 'Active',    balance: 1200, flag: '../../assets/images/america-flag.png' },
  { id: '2', network: 'Mastercard', currency: 'EUR', label: 'EUR Virtual Card', last4: '7890', expiry: '09/26', holder: 'EMILY R.',    status: 'Pending',   balance: 300,  flag: '../../assets/images/euro.png' },
  { id: '3', network: 'Visa',       currency: 'USD', label: 'USD Virtual Card', last4: '9632', expiry: '09/26', holder: 'MICHAEL T.', status: 'Active',    balance: 850,  flag: '../../assets/images/america-flag.png' },
  { id: '4', network: 'Visa',       currency: 'GBP', label: 'GBP Virtual Card', last4: '2210', expiry: '11/26', holder: 'SARAH K.',   status: 'On Review', balance: 0,    flag: '../../assets/images/uk-flag.png' },
];

const requestHistory: CardRequest[] = [
  { id: 'r1', currency: 'USD', network: 'Visa',       amount: 500,  status: 'Pending',   requestedOn: 'Apr 20, 2024',                             flag: '../../assets/images/america-flag.png' },
  { id: 'r2', currency: 'EUR', network: 'Mastercard', amount: 300,  status: 'On Review', requestedOn: 'Apr 18, 2024',                             flag: '../../assets/images/euro.png' },
  { id: 'r3', currency: 'USD', network: 'Visa',       amount: 1200, status: 'Active',    requestedOn: 'Apr 15, 2024', approvedOn: 'Apr 15, 2024', flag: '../../assets/images/america-flag.png' },
];

const statusCounts = { Pending: 2, 'On Review': 1, Active: 3 };

const cardCurrencyOptions = [
  { code: 'USD', symbol: '$', label: 'USD ($)', flag: '../../assets/images/america-flag.png' },
  { code: 'EUR', symbol: '€', label: 'EUR (€)', flag: '../../assets/images/euro.png' },
  { code: 'GBP', symbol: '£', label: 'GBP (£)', flag: '../../assets/images/uk-flag.png' },
];

// ─── Component ────────────────────────────────────────────────────────────────

const VirtualCardsPage = () => {
  const [isDepositOpen, setIsDepositOpen]           = useState(false);
  const [theme, setTheme]                           = useState<'light' | 'dark'>('light');
  const [isPageLoading, setIsPageLoading]           = useState(true);
  const [toasts, setToasts]                         = useState<Toast[]>([]);

  const [cards, setCards]                           = useState<VirtualCard[]>(initialCards);
  const [activeTab, setActiveTab]                   = useState<'All Cards' | 'Active' | 'Pending' | 'On Review'>('All Cards');
  const [hiddenCards, setHiddenCards]               = useState<Set<string>>(new Set());
  const [selectedCardDetail, setSelectedCardDetail] = useState<VirtualCard | null>(null);

  const [showBalances, setShowBalances]             = useState(true);
  const [selectedWallet, setSelectedWallet]         = useState<Wallet>(wallets[0]);

  const [formWallet, setFormWallet]                 = useState<Wallet>(wallets[0]);
  const [isFormWalletOpen, setIsFormWalletOpen]     = useState(false);
  const [cardNetwork, setCardNetwork]               = useState<CardNetwork>('Visa');
  const [cardCurrency, setCardCurrency]             = useState(cardCurrencyOptions[0]);
  const [isCurrencyDropOpen, setIsCurrencyDropOpen] = useState(false);
  const [fundAmount, setFundAmount]                 = useState('');
  const [isSubmitting, setIsSubmitting]             = useState(false);

  const [fundModal, setFundModal]                   = useState<{ open: boolean; card: VirtualCard | null }>({ open: false, card: null });
  const [withdrawModal, setWithdrawModal]           = useState<{ open: boolean; card: VirtualCard | null }>({ open: false, card: null });
  const [modalAmount, setModalAmount]               = useState('');

  // ─── Helpers ──────────────────────────────────────────────────────────────────

  const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
    setToasts((prev) => {
      if (prev.length >= 5) return prev;
      const id = Date.now();
      const newToast: Toast = { id, message: msg, type, exiting: false };
      setTimeout(() => {
        setToasts((cur) => cur.map((t) => (t.id === id ? { ...t, exiting: true } : t)));
        setTimeout(() => setToasts((cur) => cur.filter((t) => t.id !== id)), 300);
      }, 4000);
      return [...prev, newToast];
    });
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.classList.toggle('dark', newTheme === 'dark');
  };

  const toggleCardVisibility = (id: string) => {
    setHiddenCards((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const getStatusColor = (status: CardStatus) => {
    switch (status) {
      case 'Active':    return 'status-active';
      case 'Pending':   return 'status-pending';
      case 'On Review': return 'status-review';
    }
  };

  const getCardGradient = (card: VirtualCard) => {
    if (card.status === 'Pending' || card.status === 'On Review') return 'card-gradient-dark';
    if (card.currency === 'EUR') return 'card-gradient-blue';
    if (card.currency === 'GBP') return 'card-gradient-purple';
    return 'card-gradient-green';
  };

  const getCurrencySymbol = (code: WalletType) =>
    wallets.find((w) => w.code === code)?.symbol || '$';

  const filteredCards = activeTab === 'All Cards'
    ? cards
    : cards.filter((c) => c.status === activeTab);

  // ─── Handlers ─────────────────────────────────────────────────────────────────

  const handleRequestCard = async () => {
    if (!fundAmount && Number(fundAmount) < 0) {
      showToast('Please enter a valid amount', 'warning');
      return;
    }
    setIsSubmitting(true);
    await new Promise((r) => setTimeout(r, 2000));
    const newCard: VirtualCard = {
      id: String(Date.now()),
      network: cardNetwork,
      currency: formWallet.code,
      label: `${formWallet.code} Virtual Card`,
      last4: String(Math.floor(1000 + Math.random() * 9000)),
      expiry: '12/27',
      holder: 'YOU',
      status: 'Pending',
      balance: Number(fundAmount) || 0,
      flag: formWallet.flag,
    };
    setCards((prev) => [newCard, ...prev]);
    setFundAmount('');
    setIsSubmitting(false);
    showToast('Card request submitted successfully!', 'success');
  };

  const handleFundCard = async () => {
    const amt = Number(modalAmount);
    if (!amt || amt <= 0) { showToast('Enter a valid amount', 'warning'); return; }
    setIsSubmitting(true);
    await new Promise((r) => setTimeout(r, 1500));
    setCards((prev) => prev.map((c) => c.id === fundModal.card?.id ? { ...c, balance: c.balance + amt } : c));
    setFundModal({ open: false, card: null });
    setModalAmount('');
    setIsSubmitting(false);
    showToast(`Card funded with ${getCurrencySymbol(fundModal.card!.currency)}${amt.toLocaleString()}!`, 'success');
  };

  const handleWithdraw = async () => {
    const amt = Number(modalAmount);
    if (!amt || amt <= 0) { showToast('Enter a valid amount', 'warning'); return; }
    if (amt > (withdrawModal.card?.balance || 0)) { showToast('Insufficient card balance', 'warning'); return; }
    setIsSubmitting(true);
    await new Promise((r) => setTimeout(r, 1500));
    setCards((prev) => prev.map((c) => c.id === withdrawModal.card?.id ? { ...c, balance: c.balance - amt } : c));
    setWithdrawModal({ open: false, card: null });
    setModalAmount('');
    setIsSubmitting(false);
    showToast(`${getCurrencySymbol(withdrawModal.card!.currency)}${amt.toLocaleString()} withdrawn successfully!`, 'success');
  };

  useEffect(() => {
    const t = setTimeout(() => setIsPageLoading(false), 2000);
    return () => clearTimeout(t);
  }, []);

  if (isPageLoading) return <LoadingScreen />;

  // ─── Render ──────────────────────────────────────────────────────────────────

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />

      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />

        <div className="scrollable-content">
          <div className="toastrs">
          {toasts.map((toast) => (
            <div
              key={toast.id}
              className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}>
              <div className="toast-icon">
                <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`} aria-hidden="true"></i>
              </div>
              <div className="toast-message">{toast.message}</div>
            </div>
          ))}
        </div>

          <div className="main-container">

            {/* ── Breadcrumb ── */}
            <div className="airtime-breadcrumb">
              <Link href="/dashboard" className="breadcrumb-link">Dashboard</Link>
              <ChevronRight size={13} className="breadcrumb-sep" />
              <span className="breadcrumb-current">Virtual Cards</span>
            </div>

            {/* ── Topbar ── */}
            <div className="vc-topbar">
              <div>
                <h1 className="vc-title"><span className="title-green">Virtual</span> Cards</h1>
                <p className="vc-subtitle">Request and manage your virtual cards (USD, EUR, GBP)</p>
              </div>
              <button
                className="vc-request-btn"
                onClick={() => document.getElementById('request-section')?.scrollIntoView({ behavior: 'smooth' })}
              >
                <Plus size={16} /> Request Virtual Card
              </button>
            </div>

            {/* ── Top Row: Wallet Balances + Card Request Status ── */}
            <div className="vc-top-row">

              {/* Wallet Balances */}
              <div className="vc-wallets-card">
                <div className="vc-wallets-header">
                  <span className="vc-wallets-title">Available Wallet Balance</span>
                  <button className="vc-eye-btn" onClick={() => setShowBalances(!showBalances)}>
                    {showBalances ? <Eye size={15} /> : <EyeOff size={15} />}
                  </button>
                  <Info size={14} className="vc-info-icon" />
                </div>
                <div className="vc-wallets-row">
                  {wallets.map((w, i) => (
                    <React.Fragment key={w.code}>
                      <div
                        className={`vc-wallet-item ${selectedWallet.code === w.code ? 'vc-wallet-selected' : ''}`}
                        onClick={() => setSelectedWallet(w)}
                      >
                        <Flag src={w.flag} alt={w.code} />
                        <div className="vc-wallet-item-inner">
                          <span className="vc-wallet-label">{w.label}</span>
                          <div className="vc-wallet-dots">
                            <span /><span /><span />
                          </div>
                        </div>
                      </div>
                      {i < wallets.length - 1 && <div className="vc-wallet-divider" />}
                    </React.Fragment>
                  ))}
                  <div className="vc-wallet-balance">
                    <span className="vc-balance-currency">{selectedWallet.symbol}</span>
                    <span className="vc-balance-amount">
                      {showBalances
                        ? `${selectedWallet.symbol}${selectedWallet.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
                        : `${selectedWallet.symbol} ••••••`}
                    </span>
                  </div>
                </div>
              </div>

              {/* Card Request Status */}
              <div className="vc-status-card">
                <h3 className="vc-status-title">Card Request Status</h3>
                <div className="vc-status-row">
                  <div className="vc-status-item status-pending-bg">
                    <span className="vc-status-count">{statusCounts.Pending}</span>
                    <div className="flex-status-tag">
                      <span className="vc-status-dot dot-pending" />
                      <span className="vc-status-label">Pending</span>
                    </div>
                  </div>
                  <div className="vc-status-item status-review-bg">
                    <span className="vc-status-count">{statusCounts['On Review']}</span>
                    <div className="flex-status-tag">
                      <span className="vc-status-dot dot-review" />
                      <span className="vc-status-label">On Review</span>
                    </div>
                  </div>
                  <div className="vc-status-item status-active-bg">
                    <span className="vc-status-count">{statusCounts.Active}</span>
                    <div className="flex-status-tag">
                      <span className="vc-status-dot dot-active" />
                      <span className="vc-status-label">Approved</span>
                    </div>
                  </div>
                </div>
              </div>

            </div>

            {/* ── Middle Row: Request New Card + Request History ── */}
            <div className="vc-mid-row">

              {/* Request New Card */}
              <div className="vc-request-card" id="request-section">
                <h3 className="vc-request-title"><span className="title-green">Request</span> New Card</h3>
                <div className="vc-request-form">

                  {/* Select Wallet */}
                  <div className="vc-form-col">
                    <label className="vc-form-label">Select Wallet</label>
                    <div className="vc-dropdown" onClick={() => setIsFormWalletOpen(!isFormWalletOpen)}>
                      <Flag src={formWallet.flag} alt={formWallet.code} />
                      <span>{formWallet.label}</span>
                      <ChevronDown size={14} className="vc-dropdown-chevron" />
                    </div>
                    {isFormWalletOpen && (
                      <div className="vc-dropdown-menu">
                        {wallets.map((w) => (
                          <div
                            key={w.code}
                            className="vc-dropdown-item"
                            onClick={() => {
                              setFormWallet(w);
                              setIsFormWalletOpen(false);
                              setCardCurrency(cardCurrencyOptions.find(c => c.code === w.code) || cardCurrencyOptions[0]);
                            }}
                          >
                            <Flag src={w.flag} alt={w.code} />
                            <span>{w.label}</span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Card Details */}
                  <div className="vc-form-col vc-form-details">
                    <label className="vc-form-label">Card Details</label>
                    <div className="vc-details-row">

                      {/* Card Type */}
                      <div className="vc-detail-group">
                        <span className="vc-detail-sub">Card Type:</span>
                        <div className="vc-network-btns">
                          {(['Visa', 'Mastercard'] as CardNetwork[]).map((n) => (
                            <button
                              key={n}
                              className={`vc-network-btn ${cardNetwork === n ? 'vc-network-selected' : ''}`}
                              onClick={() => setCardNetwork(n)}
                            >
                              {n === 'Visa'
                                ? <span className="vc-visa-logo">VISA</span>
                                : <span className="vc-mc-logo"><span className="mc-left" /><span className="mc-right" /></span>
                              }
                              {n}
                            </button>
                          ))}
                        </div>
                      </div>

                      {/* Card Currency */}
                      <div className="vc-detail-group">
                        <span className="vc-detail-sub">Card Currency:</span>
                        <div className="vc-dropdown vc-currency-drop" onClick={() => setIsCurrencyDropOpen(!isCurrencyDropOpen)}>
                          <Flag src={cardCurrency.flag} alt={cardCurrency.code} />
                          <span>{cardCurrency.label}</span>
                          <ChevronDown size={13} className="vc-dropdown-chevron" />
                        </div>
                        {isCurrencyDropOpen && (
                          <div className="vc-dropdown-menu">
                            {cardCurrencyOptions.map((c) => (
                              <div
                                key={c.code}
                                className="vc-dropdown-item"
                                onClick={() => { setCardCurrency(c); setIsCurrencyDropOpen(false); }}
                              >
                                <Flag src={c.flag} alt={c.code} />
                                <span>{c.label}</span>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Amount */}
                    <div className="vc-amount-row">
                      <div className="vc-amount-field">
                        <label className="vc-detail-sub">
                          Amount <span className="vc-optional">(Optional):</span>
                        </label>
                        <div className="vc-amount-input-wrap">
                          <span className="vc-amount-symbol">{cardCurrency.symbol}</span>
                          <input
                            type="number"
                            className="vc-amount-input"
                            placeholder="Enter amount"
                            value={fundAmount}
                            onChange={(e) => setFundAmount(e.target.value)}
                          />
                        </div>
                      </div>
                      <button className="vc-submit-btn" onClick={handleRequestCard} disabled={isSubmitting}>
                        {isSubmitting ? <span className="btn-spinner-sm" /> : null}
                        Request Card
                      </button>
                    </div>
                  </div>

                </div>
              </div>

              {/* Request History */}
              <div className="vc-history-card">
                <h3 className="vc-status-title">Request History</h3>
                <div className="vc-history-list">
                  {requestHistory.map((r) => (
                    <div key={r.id} className="vc-history-item">
                      {/* Top line: flag + colored dot + name + status pill */}
                      <div className="vc-history-top">
                        <div className="vc-history-top-left">
                          <Flag src={r.flag} alt={r.currency} />
                          <span
                            className="vc-history-dot"
                            style={{
                              background:
                                r.status === 'Active' ? '#22c55e' :
                                r.status === 'Pending' ? '#f59e0b' : '#3b82f6'
                            }}
                          />
                          <p className="vc-history-name">{r.currency} Virtual Card</p>
                        </div>
                        <span className={`vc-history-status ${getStatusColor(r.status)}`}>
                          {r.status === 'Active' ? 'Approved' : r.status}
                          <span
                            className="vc-status-dot-sm"
                            style={{
                              background:
                                r.status === 'Active' ? '#22c55e' :
                                r.status === 'Pending' ? '#f59e0b' : '#3b82f6'
                            }}
                          />
                        </span>
                      </div>
                      {/* Bottom line: network logo + amount + separator + date */}
                      <div className="vc-history-meta">
                        <span className="vc-history-network">
                          {r.network === 'Visa'
                            ? <span className="vc-visa-sm">VISA</span>
                            : <span className="vc-mc-sm"><span /><span /></span>
                          }
                        </span>
                        <span className="vc-history-amount">
                          {getCurrencySymbol(r.currency)}{r.amount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </span>
                        <span>•</span>
                        {r.approvedOn
                          ? <><span className="vc-history-meta-approved">Approved on:</span><span>{r.approvedOn}</span></>
                          : <><span>Requested on:</span><span>{r.requestedOn}</span></>
                        }
                      </div>
                    </div>
                  ))}
                </div>
                <button className="vc-history-view-all">View All</button>
              </div>

            </div>

            {/* ── My Virtual Cards ── */}
            <div className="vc-cards-section">
              <div className="vc-cards-header">
                <h3 className="vc-cards-title">My <span className="title-green">Virtual Cards</span></h3>
                <div className="vc-tabs">
                  {(['All Cards', 'Active', 'Pending', 'On Review'] as const).map((tab) => (
                    <button
                      key={tab}
                      className={`vc-tab ${activeTab === tab ? 'vc-tab-active' : ''}`}
                      onClick={() => setActiveTab(tab)}
                    >
                      {tab}
                    </button>
                  ))}
                </div>
                <button className="vc-view-all">View All <ChevronRight size={14} /></button>
              </div>

              <div className="vc-cards-grid">
                {filteredCards.map((card) => {
                  const hidden = hiddenCards.has(card.id);
                  return (
                    <div key={card.id} className="vc-card-wrapper">
                      {/* Card face */}
                      <div className={`vc-card ${getCardGradient(card)}`}>
                        {/* Row 1: Network + Label + Status badge */}
                        <div className="vc-card-row1">
                          <div className="vc-card-row1-left">
                            <span className={`vc-card-network ${card.network === 'Visa' ? 'vc-card-visa' : 'vc-card-mc'}`}>
                              {card.network === 'Visa' ? 'VISA' : (
                                <span className="vc-mc-card"><span /><span /></span>
                              )}
                            </span>
                            <span className="vc-card-label">{card.label}</span>
                          </div>
                          <span className={`vc-card-badge ${getStatusColor(card.status)}`}>
                            {card.status === 'Active' ? '▼' : '●'} {card.status}
                          </span>
                        </div>

                        {/* Row 2: Card number + Flag */}
                        <div className="vc-card-row2">
                          <div className="vc-card-number">
                            **** **** **** {hidden ? '••••' : card.last4}
                          </div>
                          <div className="vc-card-flag-wrap">
                            <img src={card.flag} alt={card.currency} className="vc-card-flag-img" />
                            <button className="vc-eye-card-btn" onClick={() => toggleCardVisibility(card.id)}>
                              {hidden ? <Eye size={13} /> : <EyeOff size={13} />}
                            </button>
                          </div>
                        </div>

                        {/* Row 3: Holder name + Expiry */}
                        <div className="vc-card-row3">
                          <p className="vc-card-holder">{card.holder}</p>
                          <div className="vc-card-expiry-block">
                            <span className="vc-card-expiry-label">Expiry</span>
                            <strong className="vc-card-expiry-date">{card.expiry}</strong>
                          </div>
                        </div>

                        {/* Review overlay */}
                        {card.status === 'On Review' && (
                          <div className="vc-card-overlay">
                            <span>🔍 On Review by Admin</span>
                          </div>
                        )}
                      </div>

                      {/* Card actions */}
                      <div className="vc-card-actions">
                        {card.status === 'Active' ? (
                          <>
                            <button
                              className="vc-action-btn vc-action-outline"
                              onClick={() => setSelectedCardDetail(card)}
                            >
                              View Details
                            </button>
                            <button
                              className="vc-action-btn vc-action-fund"
                              onClick={() => { setFundModal({ open: true, card }); setModalAmount(''); }}
                            >
                              <ArrowDownLeft size={14} /> Fund Card
                            </button>
                          </>
                        ) : card.status === 'Pending' ? (
                          <div className="vc-on-review-badge">
                            <span className="vc-on-review-icon">🔍</span>
                            <span>On Review by Admin</span>
                          </div>
                        ) : card.status === 'On Review' ? (
                          <div className="vc-on-review-badge">
                            <span className="vc-on-review-icon">🔍</span>
                            <span>On Review by Admin</span>
                          </div>
                        ) : (
                          <button
                            className="vc-action-btn vc-action-outline"
                            onClick={() => setSelectedCardDetail(card)}
                          >
                            View Details
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

          </div>

          {/* ── Fund Card Modal ── */}
          {fundModal.open && fundModal.card && (
            <div className="modal-overlay" onClick={() => setFundModal({ open: false, card: null })}>
              <div className="vc-modal" onClick={(e) => e.stopPropagation()}>
                <h3 className="vc-modal-title"><ArrowDownLeft size={18} /> Fund Card</h3>
                <p className="vc-modal-sub">
                  Add funds to your {fundModal.card.currency} Virtual Card ending in {fundModal.card.last4}
                </p>
                <div className="vc-modal-field">
                  <label>Amount ({getCurrencySymbol(fundModal.card.currency)})</label>
                  <div className="vc-amount-input-wrap">
                    <span className="vc-amount-symbol">{getCurrencySymbol(fundModal.card.currency)}</span>
                    <input
                      type="number"
                      className="vc-amount-input"
                      placeholder="0.00"
                      value={modalAmount}
                      onChange={(e) => setModalAmount(e.target.value)}
                    />
                  </div>
                </div>
                <div className="vc-modal-actions">
                  <button className="vc-action-btn vc-action-outline" onClick={() => setFundModal({ open: false, card: null })}>
                    Cancel
                  </button>
                  <button className="vc-action-btn vc-action-fund" onClick={handleFundCard} disabled={isSubmitting}>
                    {isSubmitting ? <span className="btn-spinner-sm" /> : <ArrowDownLeft size={14} />} Fund Card
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* ── Withdraw Modal ── */}
          {withdrawModal.open && withdrawModal.card && (
            <div className="modal-overlay" onClick={() => setWithdrawModal({ open: false, card: null })}>
              <div className="vc-modal" onClick={(e) => e.stopPropagation()}>
                <h3 className="vc-modal-title"><ArrowUpRight size={18} /> Withdraw Funds</h3>
                <p className="vc-modal-sub">
                  Withdraw from your {withdrawModal.card.currency} Card •••• {withdrawModal.card.last4}
                  <br />
                  <span className="vc-modal-balance">
                    Available: {getCurrencySymbol(withdrawModal.card.currency)}{withdrawModal.card.balance.toLocaleString()}
                  </span>
                </p>
                <div className="vc-modal-field">
                  <label>Amount ({getCurrencySymbol(withdrawModal.card.currency)})</label>
                  <div className="vc-amount-input-wrap">
                    <span className="vc-amount-symbol">{getCurrencySymbol(withdrawModal.card.currency)}</span>
                    <input
                      type="number"
                      className="vc-amount-input"
                      placeholder="0.00"
                      value={modalAmount}
                      onChange={(e) => setModalAmount(e.target.value)}
                    />
                  </div>
                </div>
                <div className="vc-modal-actions">
                  <button className="vc-action-btn vc-action-outline" onClick={() => setWithdrawModal({ open: false, card: null })}>
                    Cancel
                  </button>
                  <button className="vc-action-btn vc-action-submit" onClick={handleWithdraw} disabled={isSubmitting}>
                    {isSubmitting ? <span className="btn-spinner-sm" /> : <ArrowUpRight size={14} />} Withdraw
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* ── Card Detail Modal ── */}
          {selectedCardDetail && (
            <div className="modal-overlay" onClick={() => setSelectedCardDetail(null)}>
              <div className="vc-modal vc-detail-modal" onClick={(e) => e.stopPropagation()}>
                <h3 className="vc-modal-title"><CreditCard size={18} /> Card Details</h3>
                <div className={`vc-detail-card-preview ${getCardGradient(selectedCardDetail)}`}>
                  <div className="vc-card-top">
                    <span className={`vc-card-network ${selectedCardDetail.network === 'Visa' ? 'vc-card-visa' : 'vc-card-mc'}`}>
                      {selectedCardDetail.network === 'Visa' ? 'VISA' : <span className="vc-mc-card"><span /><span /></span>}
                    </span>
                    <span className={`vc-card-badge ${getStatusColor(selectedCardDetail.status)}`}>
                      {selectedCardDetail.status}
                    </span>
                  </div>
                  <div className="vc-card-number">**** **** **** {selectedCardDetail.last4}</div>
                  <div className="vc-card-bottom">
                    <p className="vc-card-holder">{selectedCardDetail.holder}</p>
                    <p className="vc-card-expiry-label">Expiry <strong>{selectedCardDetail.expiry}</strong></p>
                  </div>
                </div>
                <div className="vc-detail-rows">
                  <div className="vc-detail-row"><span>Card Number</span><span>**** **** **** {selectedCardDetail.last4}</span></div>
                  <div className="vc-detail-row">
                    <span>Currency</span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Flag src={selectedCardDetail.flag} alt={selectedCardDetail.currency} />
                      {selectedCardDetail.currency}
                    </span>
                  </div>
                  <div className="vc-detail-row"><span>Network</span><span>{selectedCardDetail.network}</span></div>
                  <div className="vc-detail-row"><span>Balance</span><span>{getCurrencySymbol(selectedCardDetail.currency)}{selectedCardDetail.balance.toLocaleString()}</span></div>
                  <div className="vc-detail-row"><span>Expiry</span><span>{selectedCardDetail.expiry}</span></div>
                  <div className="vc-detail-row">
                    <span>Status</span>
                    <span className={`vc-history-status ${getStatusColor(selectedCardDetail.status)}`}>
                      {selectedCardDetail.status}
                    </span>
                  </div>
                </div>
                <button
                  className="vc-action-btn vc-action-submit"
                  style={{ width: '100%' }}
                  onClick={() => setSelectedCardDetail(null)}
                >
                  Close
                </button>
              </div>
            </div>
          )}

          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
      <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
    </div>
  );
};

export default VirtualCardsPage;