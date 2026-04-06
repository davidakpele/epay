'use client';

import { ChevronRight, Plus, CreditCard, Eye, EyeOff, Info, ArrowUpRight, ArrowDownLeft, ChevronDown, Search, CreditCardIcon, ClipboardListIcon } from 'lucide-react';
import React, { useState, useEffect, useCallback, useRef } from 'react';
import DepositModal from '@/components/DepositModal';
import Footer from '@/components/Footer';
import Header from '@/components/Header';
import MobileNav from '@/components/MobileNav';
import Sidebar from '@/components/Sidebar';
import LoadingScreen from '@/components/loader/Loadingscreen';
import Link from 'next/link';
import './VirtualCards.css';
import { Toast } from '@/app/types/auth';
import { getFiat, getToken, getUserFullName, getUserId, setActiveWallet, setFiat, setWalletContainer } from '@/app/api/utils';
import { virtualCardService, walletService } from '@/app/api';
import { Currency } from '@/app/types/api';
import { useRouter } from 'next/navigation';
import SupportChatBot from '@/components/SupportChatBot';

type CardStatus = 'Active' | 'Pending' | 'On Review';
type CardNetwork = 'Visa' | 'Master';
type WalletType = 'USD' | 'EUR' | 'GBP' | string;

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

const FLAG_MAP: Record<string, string> = {
  USD: '../../assets/images/america-flag.png',
  EUR: '../../assets/images/euro.png',
  GBP: '../../assets/images/uk-flag.png',
  NGN: '../../assets/images/nigeria-flag.png',
};

const cardFees: Record<CardNetwork, string> = {
  Visa: '$5',
  Master: '$25',
};

const Flag = ({ src, alt }: { src: string; alt: string }) => (
  <img src={src} alt={alt} className="flag-img" />
);

/** Map raw API card data → VirtualCard */
const mapApiCard = (raw: any, userFullName: string): VirtualCard => {
  const currency = raw.currency || raw.currency_code || 'USD';
  const network: CardNetwork =
    String(raw.card_type || raw.cardType || raw.network || 'Visa').includes('MASTER') ||
    String(raw.card_type || raw.cardType || raw.network || '').toLowerCase() === 'master'
      ? 'Master'
      : 'Visa';

  const rawStatus = raw.status || 'Pending';
  const status: CardStatus =
    rawStatus === 'ACTIVE' || rawStatus === 'active' || rawStatus === 'Active'
      ? 'Active'
      : rawStatus === 'ON_REVIEW' || rawStatus === 'on_review' || rawStatus === 'On Review'
      ? 'On Review'
      : 'Pending';

  return {
    id: String(raw.id || raw.card_id || raw.cardId || Date.now()),
    network,
    currency,
    label: `${currency} Virtual Card`,
    last4: String(raw.last4 || raw.last_four || raw.lastFour || '****'),
    expiry: raw.expiry || raw.expiry_date || raw.expiryDate || '**/**',
    holder: raw.account_holder_name || raw.accountHolderName || userFullName.toUpperCase(),
    status,
    balance: parseFloat(raw.balance || raw.current_balance || 0),
    flag: FLAG_MAP[currency] || FLAG_MAP['USD'],
  };
};

/** Map raw API request history → CardRequest */
const mapApiRequest = (raw: any): CardRequest => {
  const currency = raw.currency || raw.currency_code || 'USD';
  const network: CardNetwork =
    String(raw.card_type || raw.cardType || raw.network || '').toLowerCase() === 'master'
      ? 'Master'
      : 'Visa';

  const rawStatus = raw.status || 'Pending';
  const status: CardStatus =
    rawStatus === 'ACTIVE' || rawStatus === 'active' || rawStatus === 'Active'
      ? 'Active'
      : rawStatus === 'ON_REVIEW' || rawStatus === 'on_review' || rawStatus === 'On Review'
      ? 'On Review'
      : 'Pending';

  const fmt = (d: string) => {
    if (!d) return '';
    try { return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }); }
    catch { return d; }
  };

  return {
    id: String(raw.id || raw.request_id || raw.requestId || Date.now()),
    currency,
    network,
    amount: parseFloat(raw.initial_balance || raw.initialBalance || raw.amount || 0),
    status,
    requestedOn: fmt(raw.created_at || raw.createdAt || raw.requestedOn || ''),
    approvedOn: status === 'Active' ? fmt(raw.updated_at || raw.updatedAt || raw.approvedOn || '') : undefined,
    flag: FLAG_MAP[currency] || FLAG_MAP['USD'],
  };
};

const VirtualCardsPage = () => {
  const [fundModal, setFundModal]                   = useState<{ open: boolean; card: VirtualCard | null }>({ open: false, card: null });
  const [withdrawModal, setWithdrawModal]           = useState<{ open: boolean; card: VirtualCard | null }>({ open: false, card: null });
  const [activeTab, setActiveTab]                   = useState<'All Cards' | 'Active' | 'Pending' | 'On Review'>('All Cards');
  const [theme, setTheme]                           = useState<'light' | 'dark'>('light');
  const [cards, setCards]                           = useState<VirtualCard[]>([]);
  const [hiddenCards, setHiddenCards]               = useState<Set<string>>(new Set());
  const [selectedCardDetail, setSelectedCardDetail] = useState<VirtualCard | null>(null);
  const [cardNetwork, setCardNetwork]               = useState<CardNetwork>('Visa');
  const [wallets, setWallets]                       = useState<Wallet[]>([]);
  const [selectedWallet, setSelectedWallet]         = useState<Wallet | null>(null);
  const [toasts, setToasts]                         = useState<Toast[]>([]);
  const [isDepositOpen, setIsDepositOpen]           = useState(false);
  const [isPageLoading, setIsPageLoading]           = useState(true);
  const [isCardsLoading, setIsCardsLoading]         = useState(true);
  const [showBalances, setShowBalances]             = useState(true);
  const [isModalOpen, setIsModalOpen]               = useState(false);
  const [fundAmount, setFundAmount]                 = useState('');
  const [isSubmitting, setIsSubmitting]             = useState(false);
  const [currencies, setCurrencies]                 = useState<Currency[]>([]);
  const [selectedCurrency, setSelectedCurrency]     = useState<Currency | null>(null);
  const [error, setError]                           = useState('');
  const [wallet, setWallet]                         = useState<any>(null);
  const [isDropdownOpen, setIsDropdownOpen]         = useState(false);
  const scrollTimer                                  = useRef<NodeJS.Timeout | null>(null);
  const [searchTerm, setSearchTerm]                 = useState('');
  const [isScrolling, setIsScrolling]               = useState(false);
  const [requestHistory, setRequestHistory]         = useState<CardRequest[]>([]);
  const [statusCounts, setStatusCounts]             = useState({ Pending: 0, 'On Review': 0, Active: 0 });
  const [modalAmount, setModalAmount]               = useState('');
  const [isChatOpen, setIsChatOpen] = useState(false);
  const userFullName = getUserFullName() || 'User';
  const router = useRouter();

  // ── Toast helper ──────────────────────────────────────────────────────────
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
    wallets.find((w) => w.code === code)?.symbol ||
    currencies.find((c) => c.code === code)?.symbol ||
    '$';

  const filteredCards = activeTab === 'All Cards'
    ? cards
    : cards.filter((c) => c.status === activeTab);

  const filteredCurrencies = currencies.filter((c) =>
    c.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.code.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // ── Recompute status counts whenever cards change ─────────────────────────
  const recomputeCounts = (cardList: VirtualCard[]) => {
    const counts = { Pending: 0, 'On Review': 0, Active: 0 };
    cardList.forEach((c) => { counts[c.status] = (counts[c.status] || 0) + 1; });
    setStatusCounts(counts);
  };

  // ── Load wallet balances ───────────────────────────────────────────────────
  const loadWalletBalance = useCallback(async () => {
    try {
      setError('');
      const token = getToken();
      const userId = getUserId();
      if (!token || !userId) { setError('Please login to view wallet'); return; }

      const response = await walletService.getByUserId(userId, token);
      if (!response || response.status === 401 || response.status === 500) {
        setError('Failed to fetch wallet data');
        router.push('/auth/logout');
        return;
      }
      setWallet(response);

      if (response?.wallet_balances) {
        const apiCurrencies: Currency[] = response.wallet_balances.map((b: any) => ({
          name: b.currency_code,
          code: b.currency_code,
          symbol: b.symbol,
        }));
        setCurrencies(apiCurrencies);

        const saved = getFiat();
        if (saved) {
          const found = apiCurrencies.find((c) => c.code === saved);
          if (found) setSelectedCurrency(found);
        } else if (apiCurrencies.length > 0) {
          const def = apiCurrencies.find((c) => c.code === 'NGN') || apiCurrencies[0];
          setSelectedCurrency(def);
          setActiveWallet(def.code);
          setFiat(def.code);
        }

        setWalletContainer(response.wallet_balances, response.hasTransferPin, response.walletId);
      }
    } catch (e) {
      console.error('Wallet load error:', e);
    }
  }, []);

  // ── Load virtual cards + request history from API ─────────────────────────
  const loadCards = useCallback(async () => {
    try {
      setIsCardsLoading(true);
      const userId = getUserId();
      const token  = getToken();
      if (!userId || !token) return;

      // Fetch user's virtual cards
      const cardsResponse = await virtualCardService.fetchUserVirtualCardsByUserId(userId);

      let rawCards: any[] = [];
      if (Array.isArray(cardsResponse)) {
        rawCards = cardsResponse;
      } else if (cardsResponse?.data && Array.isArray(cardsResponse.data)) {
        rawCards = cardsResponse.data;
      } else if (cardsResponse?.cards && Array.isArray(cardsResponse.cards)) {
        rawCards = cardsResponse.cards;
      }

      const mapped = rawCards.map((r) => mapApiCard(r, userFullName));
      setCards(mapped);
      recomputeCounts(mapped);

      // Build request history from the same card list (each card = a request)
      const history = rawCards.map((r) => mapApiRequest(r));
      setRequestHistory(history);
    } catch (e) {
      console.error('Cards load error:', e);
    } finally {
      setIsCardsLoading(false);
    }
  }, [userFullName]);

  // ── Effects ───────────────────────────────────────────────────────────────
  useEffect(() => {
    const init = async () => {
      await loadWalletBalance();
      await loadCards();
      setTimeout(() => setIsPageLoading(false), 2000);
    };
    init();
  }, []);

  useEffect(() => {
    if (wallet?.wallet_balances) {
      const filtered = wallet.wallet_balances
        .filter((w: any) => w.currency_code === 'USD' || w.currency_code === 'NGN')
        .map((w: any) => ({
          code: w.currency_code,
          label: `${w.currency_code} Wallet`,
          balance: parseFloat(w.balance),
          symbol: w.symbol,
          flag: FLAG_MAP[w.currency_code] || FLAG_MAP['USD'],
        }));
      setWallets(filtered);
      if (filtered.length > 0) setSelectedWallet(filtered[0]);
    }
  }, [wallet]);

  const handleScroll = () => {
    setIsScrolling(true);
    if (scrollTimer.current) clearTimeout(scrollTimer.current);
    scrollTimer.current = setTimeout(() => setIsScrolling(false), 1000);
  };


  const handleRequestCard = async () => {
  if (!selectedCurrency) {
    showToast('Please select a wallet currency', 'warning');
    return;
  }

  if (!cardNetwork) {
    showToast('Please select a card type (Visa or Mastercard)', 'warning');
    return;
  }

  setIsSubmitting(true);

  const currencyCode = selectedCurrency.code as WalletType;
  const requestedOn = new Date().toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });

  const optimisticCard: VirtualCard = {
    id: String(Date.now()),
    network: cardNetwork,
    currency: currencyCode,
    label: `${currencyCode} Virtual Card`,
    last4: String(Math.floor(1000 + Math.random() * 9000)),
    expiry: '12/27',
    holder: userFullName.toUpperCase(),
    status: 'Pending',
    balance: Number(fundAmount) || 0,
    flag: FLAG_MAP[currencyCode] || FLAG_MAP['USD'],
  };

  const optimisticRequest: CardRequest = {
    id: `r${Date.now()}`,
    currency: currencyCode,
    network: cardNetwork,
    amount: Number(fundAmount) || 0,
    status: 'Pending',
    requestedOn,
    flag: FLAG_MAP[currencyCode] || FLAG_MAP['USD'],
  };

try {
  const userId = getUserId();
  const cardRequest = {
    userId,
    accountHolderName: userFullName,
    cardType: cardNetwork.toUpperCase(),
    currency: currencyCode,
    initialBalance: Number(fundAmount) || 0,
    allowInternational: true,
    allowOnline: true,
    allowAtm: false,
    allowContactless: true,
    merchantName: 'ePay',
    merchantId: Math.random().toString(36).substring(2, 12).toUpperCase(),
    merchantCountry: currencyCode,
    merchantCity: 'Unknown',
    limitPeriod: 'MONTHLY',
    spendingLimit: 10000.00,
  };

  await virtualCardService.createCard(cardRequest);

  setFundAmount('');
  showToast('Card request submitted successfully!', 'success');
  await loadCards();

} catch (e) {
  console.error('Card creation error:', e);
  showToast('Failed to submit card request. Please try again.', 'warning');
} finally {
  setIsSubmitting(false);
}
};

  // ── Fund card ─────────────────────────────────────────────────────────────
  const handleFundCard = async () => {
    const amt = Number(modalAmount);
    if (!amt || amt <= 0) { showToast('Enter a valid amount', 'warning'); return; }
    setIsSubmitting(true);
    // try {
    //   if (fundModal.card) {
    //     await virtualCardService.fundCard?.({
    //       cardId: fundModal.card.id,
    //       amount: amt,
    //     });
    //   }
    // } catch (e) {
    //   console.error('Fund card error:', e);
    // }
    setCards((prev) =>
      prev.map((c) => c.id === fundModal.card?.id ? { ...c, balance: c.balance + amt } : c)
    );
    setFundModal({ open: false, card: null });
    setModalAmount('');
    setIsSubmitting(false);
    showToast(`Card funded with ${getCurrencySymbol(fundModal.card!.currency)}${amt.toLocaleString()}!`, 'success');
  };

  // ── Withdraw ──────────────────────────────────────────────────────────────
  const handleWithdraw = async () => {
    const amt = Number(modalAmount);
    if (!amt || amt <= 0) { showToast('Enter a valid amount', 'warning'); return; }
    if (amt > (withdrawModal.card?.balance || 0)) { showToast('Insufficient card balance', 'warning'); return; }
    setIsSubmitting(true);
    // try {
    //   if (withdrawModal.card) {
    //     await virtualCardService.withdrawFromCard?.({
    //       cardId: withdrawModal.card.id,
    //       amount: amt,
    //     });
    //   }
    // } catch (e) {
    //   console.error('Withdraw error:', e);
    // }
    setCards((prev) =>
      prev.map((c) => c.id === withdrawModal.card?.id ? { ...c, balance: c.balance - amt } : c)
    );
    setWithdrawModal({ open: false, card: null });
    setModalAmount('');
    setIsSubmitting(false);
    showToast(`${getCurrencySymbol(withdrawModal.card!.currency)}${amt.toLocaleString()} withdrawn successfully!`, 'success');
  };

  if (isPageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />

      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />

        <div className="scrollable-content">
          {/* ── Toasts ── */}
          <div className="toastrs">
            {toasts.map((toast) => (
              <div key={toast.id} className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}>
                <div className="toast-icon">
                  <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`} aria-hidden="true" />
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
                        className={`vc-wallet-item ${selectedWallet?.code === w.code ? 'vc-wallet-selected' : ''}`}
                        onClick={() => setSelectedWallet(w)}
                      >
                        <Flag src={w.flag} alt={w.code} />
                        <div className="vc-wallet-item-inner">
                          <span className="vc-wallet-label">{w.label}</span>
                          <div className="vc-wallet-dots"><span /><span /><span /></div>
                        </div>
                      </div>
                      {i < wallets.length - 1 && <div className="vc-wallet-divider" />}
                    </React.Fragment>
                  ))}
                  <div className="vc-wallet-balance">
                    <span className="vc-balance-amount">
                      {showBalances ? (
                        `${selectedWallet?.symbol} ${selectedWallet?.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
                      ) : (
                        <>
                          <span className="hash-symbol">{selectedWallet?.symbol}</span>
                          <span className="hash-amount-action"> *****</span>
                        </>
                      )}
                    </span>
                  </div>
                </div>
              </div>

              {/* Card Request Status — fully dynamic */}
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
                <h3 className="vc-request-title">
                  <span className="title-green">Request</span> New Card
                </h3>

                <div className="vc-request-form">

                  {/* Select Wallet */}
                  <div>
                    <label className="vc-form-label">Select Wallet</label>
                    <div className="btn-vc-dropdown" onClick={() => { setIsModalOpen(true); setIsDropdownOpen(!isDropdownOpen); }}>
                      <span className="card-currency-option">
                        {selectedCurrency?.code || 'NGN'}
                        <ChevronDown size={14} className="vc-dropdown-chevron" />
                      </span>
                    </div>
                  </div>

                  {/* Card Holder + Issuance Fee */}
                  <div className="vc-request-meta">
                    <div className="vc-meta-chip">
                      <div className="vc-meta-chip-header">
                        <span className="vc-meta-label-icon">
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                          </svg>
                        </span>
                        <span className="vc-meta-chip-label">Card Holder</span>
                      </div>
                      <div className="vc-meta-chip-body">
                        <div className="vc-meta-avatar">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z"/>
                          </svg>
                        </div>
                        <span className="vc-meta-chip-value">{userFullName}</span>
                      </div>
                    </div>

                    <div className="vc-meta-chip">
                      <div className="vc-meta-chip-header">
                        <span className="vc-meta-label-icon">
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="18" y1="20" x2="18" y2="10"/>
                            <line x1="12" y1="20" x2="12" y2="4"/>
                            <line x1="6" y1="20" x2="6" y2="14"/>
                          </svg>
                        </span>
                        <span className="vc-meta-chip-label">Issuance Fee</span>
                      </div>
                      <div className="vc-meta-chip-body">
                        <span className="vc-meta-chip-value fee-green">{cardFees[cardNetwork]}</span>
                      </div>
                    </div>
                  </div>

                  {/* Card Type */}
                  <div className="vc-card-type-row">
                    <span className="vc-detail-sub">Card Type</span>
                    <div className="vc-network-btns">
                      {(['Visa', 'Master'] as CardNetwork[]).map((n) => (
                        <button
                          key={n}
                          className={`vc-network-btn ${cardNetwork === n ? 'vc-network-selected' : ''}`}
                          onClick={() => setCardNetwork(n)}
                        >
                          {n === 'Visa'
                            ? <span className="vc-visa-logo">VISA</span>
                            : <span className="vc-mc-logo"><span className="mc-left" /><span className="mc-right" /></span>
                          }
                          <span>{n}</span>
                          <span className="vc-network-fee">{cardFees[n]}</span>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Amount + Button */}
                  <div className="vc-amount-row">
                    <button className="vc-request-btn" onClick={handleRequestCard} disabled={isSubmitting}>
                      {isSubmitting ? <span className="btn-spinner-sm" /> : <Plus size={16} />}
                      Request Virtual Card
                    </button>
                  </div>

                </div>
              </div>

              {/* Request History — fully dynamic */}
              <div className="vc-history-card">
                <h3 className="vc-status-title">Request History</h3>
                <div className="vc-history-list">
                  {isCardsLoading ? (
                    <div className="vc-loading-state">
                      <span className="btn-spinner-sm" /> Loading history...
                    </div>
                  ) : requestHistory.length === 0 ? (
                    <div className="vc-empty-state">
                      <div className="vc-empty-icon">
                        <ClipboardListIcon size={24} color="#16a34a" />
                      </div>
                      <p className="vc-empty-title">No requests yet</p>
                      <p className="vc-empty-sub">Your card request history will appear here once you submit a request.</p>
                    </div>
                  ) : (
                    requestHistory.map((r) => (
                      <div key={r.id} className="vc-history-item">
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
                    ))
                  )}
                </div>
                <button className="vc-history-view-all">View All</button>
              </div>

            </div>

            {/* ── My Virtual Cards — fully dynamic ── */}
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

              {isCardsLoading ? (
                <div className="vc-cards-loading">
                  <span className="btn-spinner-sm" /> Loading cards...
                </div>
              ) : filteredCards.length === 0 ? (
                <div className="vc-cards-empty">
                <div className="vc-empty-icon"><CreditCardIcon size={24} color="#16a34a" /></div>
                <p className="vc-empty-title">
                  {activeTab === 'All Cards' ? 'No virtual cards yet' : `No ${activeTab} cards`}
                </p>
                <p className="vc-empty-sub">
                  {activeTab === 'All Cards'
                    ? 'Request your first virtual card using the form above.'
                    : `You have no ${activeTab.toLowerCase()} cards at the moment.`}
                </p>
              </div>
              ) : (
                <div className="vc-cards-grid">
                  {filteredCards.map((card) => {
                    const hidden   = hiddenCards.has(card.id);
                    const isLocked = card.status !== 'Active';

                    return (
                      <div key={card.id} className="vc-card-wrapper">
                        {/* Card face */}
                        <div className={`vc-card ${getCardGradient(card)} ${isLocked ? 'vc-card-locked' : ''}`}>
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

                          <div className="vc-card-row2">
                            <div className="vc-card-number">
                              {isLocked ? '**** **** **** ••••' : `**** **** **** ${hidden ? '••••' : card.last4}`}
                            </div>
                            <div className="vc-card-flag-wrap">
                              <img src={card.flag} alt={card.currency} className="vc-card-flag-img" />
                              {!isLocked && (
                                <button className="vc-eye-card-btn" onClick={() => toggleCardVisibility(card.id)}>
                                  {hidden ? <Eye size={13} /> : <EyeOff size={13} />}
                                </button>
                              )}
                            </div>
                          </div>

                          <div className="vc-card-row3">
                            <p className="vc-card-holder">{isLocked ? '••••• •••••••' : card.holder}</p>
                            <div className="vc-card-expiry-block">
                              <span className="vc-card-expiry-label">Expiry</span>
                              <strong className="vc-card-expiry-date">{isLocked ? '••/••' : card.expiry}</strong>
                            </div>
                          </div>

                          {isLocked && (
                            <div className="vc-card-overlay">
                              {card.status === 'Pending' ? (
                                <div className="vc-overlay-content">
                                  <span className="vc-overlay-icon">⏳</span>
                                  <span className="vc-overlay-text">Awaiting Admin Approval</span>
                                  <span className="vc-overlay-sub">Your card request is being reviewed</span>
                                </div>
                              ) : (
                                <div className="vc-overlay-content">
                                  <span className="vc-overlay-icon">🔍</span>
                                  <span className="vc-overlay-text">Under Review</span>
                                  <span className="vc-overlay-sub">Our team is reviewing your request</span>
                                </div>
                              )}
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
                          ) : (
                            <div className={`vc-on-review-badge ${card.status === 'Pending' ? 'badge-pending' : 'badge-review'}`}>
                              <span className="vc-on-review-icon">{card.status === 'Pending' ? '⏳' : '🔍'}</span>
                              <span>{card.status === 'Pending' ? 'Pending Admin Approval' : 'On Review by Admin'}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
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
                  <button className="vc-action-btn vc-action-outline" onClick={() => setFundModal({ open: false, card: null })}>Cancel</button>
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
                  <button className="vc-action-btn vc-action-outline" onClick={() => setWithdrawModal({ open: false, card: null })}>Cancel</button>
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

          {/* ── Currency Select Modal ── */}
          {isModalOpen && (
            <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
              <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header"><h3>Select Currency</h3></div>
                <div className="search-container">
                  <span className="search-icon-inside"><Search size={16} /></span>
                  <input
                    type="text"
                    placeholder="Search"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                </div>
                <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                  {filteredCurrencies.map((c) => (
                    <div
                      key={c.code}
                      className="country-item"
                      onClick={() => {
                        setSelectedCurrency(c);
                        setIsModalOpen(false);
                        setFiat(c.code);
                        setActiveWallet(c.code);
                        setSearchTerm('');
                        setIsDropdownOpen(false);
                      }}
                    >
                      <span>{c.name} ({c.code})</span>
                      <div className={`radio-outer ${selectedCurrency?.code === c.code ? 'checked' : ''}`}>
                        <div className="radio-inner" />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
      <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
        {!isChatOpen && (
          <button
            className="chat-fab"
            onClick={() => setIsChatOpen(true)}
            aria-label="Open support chat"
          >
            <i className="fa-solid fa-comment-dots"></i>
          </button>
        )}

        <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
    </div>
  );
};

export default VirtualCardsPage;