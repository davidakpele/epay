'use client';

import { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import DepositModal from '@/components/DepositModal';
import Footer from '@/components/Footer';
import Header from '@/components/Header';
import MobileNav from '@/components/MobileNav';
import Sidebar from '@/components/Sidebar';
import LoadingScreen from '@/components/loader/Loadingscreen';
import Link from 'next/link';
import Image from 'next/image';
import { ChevronDown, ChevronRight, ArrowLeftRight, TrendingUp, Clock, CheckCircle, AlertCircle } from 'lucide-react';
import './Swap.css';
import { Toast } from '@/app/types/auth';
import { getUserId, getWalletList, updateNotificationContainer, walletService } from '@/app/api';
import SupportChatBot from '@/components/SupportChatBot';

interface Currency {
  code: string;
  name: string;
  symbol: string;
  flag: string;
}

const currencies: Currency[] = [
  { code: 'NGN', name: 'Nigerian Naira',   symbol: '₦',   flag: '🇳🇬' },
  { code: 'USD', name: 'US Dollar',         symbol: '$',   flag: '🇺🇸' },
  { code: 'EUR', name: 'Euro',              symbol: '€',   flag: '🇪🇺' },
  { code: 'GBP', name: 'British Pound',     symbol: '£',   flag: '🇬🇧' },
  { code: 'JPY', name: 'Japanese Yen',      symbol: '¥',   flag: '🇯🇵' },
  { code: 'AUD', name: 'Australian Dollar', symbol: '$',   flag: '🇦🇺' },
  { code: 'CAD', name: 'Canadian Dollar',   symbol: '$',   flag: '🇨🇦' },
  { code: 'CHF', name: 'Swiss Franc',       symbol: 'CHF', flag: '🇨🇭' },
  { code: 'CNY', name: 'Chinese Yuan',      symbol: '¥',   flag: '🇨🇳' },
  { code: 'INR', name: 'Indian Rupee',      symbol: '₹',   flag: '🇮🇳' },
];

const mockExchangeRates: Record<string, Record<string, number>> = {
  USD: { EUR: 0.92, GBP: 0.80, JPY: 147.11, AUD: 1.52, CAD: 1.35, CHF: 0.88, CNY: 7.25, INR: 83.12, NGN: 1500.50 },
  EUR: { USD: 1.09, GBP: 0.87, JPY: 159.25, AUD: 1.65, CAD: 1.47, CHF: 0.96, CNY: 7.88, INR: 90.35, NGN: 1630.75 },
  GBP: { USD: 1.25, EUR: 1.15, JPY: 184.22, AUD: 1.90, CAD: 1.69, CHF: 1.10, CNY: 9.06, INR: 103.89, NGN: 1875.30 },
  JPY: { USD: 0.0068, EUR: 0.0063, GBP: 0.0054, AUD: 0.0103, CAD: 0.0092, CHF: 0.0060, CNY: 0.0493, INR: 0.565, NGN: 10.20 },
  AUD: { USD: 0.66, EUR: 0.61, GBP: 0.53, JPY: 97.10, CAD: 0.89, CHF: 0.58, CNY: 4.77, INR: 54.68, NGN: 987.45 },
  CAD: { USD: 0.74, EUR: 0.68, GBP: 0.59, JPY: 108.75, AUD: 1.12, CHF: 0.65, CNY: 5.37, INR: 61.55, NGN: 1111.11 },
  CHF: { USD: 1.14, EUR: 1.04, GBP: 0.91, JPY: 166.67, AUD: 1.72, CAD: 1.54, CNY: 8.24, INR: 94.50, NGN: 1705.88 },
  CNY: { USD: 0.14, EUR: 0.13, GBP: 0.11, JPY: 20.28, AUD: 0.21, CAD: 0.19, CHF: 0.12, INR: 11.46, NGN: 206.90 },
  INR: { USD: 0.012, EUR: 0.011, GBP: 0.0096, JPY: 1.77, AUD: 0.018, CAD: 0.016, CHF: 0.0106, CNY: 0.087, NGN: 18.05 },
  NGN: { USD: 0.00067, EUR: 0.00061, GBP: 0.00053, JPY: 0.098, AUD: 0.00101, CAD: 0.00090, CHF: 0.00059, CNY: 0.0048, INR: 0.055 },
};

const feePercentage = 0.015;

const SwapPage = () => {
  const [isDepositOpen, setIsDepositOpen]       = useState(false);
  const [theme, setTheme]                       = useState<'light' | 'dark'>('light');
  const [isPageLoading, setIsPageLoading]       = useState(true);
  const [toasts, setToasts]                     = useState<Toast[]>([]);
  const [isChatOpen, setIsChatOpen]             = useState(false);
  const [fromCurrency, setFromCurrency]         = useState<Currency>(currencies[0]);
  const [toCurrency, setToCurrency]             = useState<Currency>(currencies[1]);
  const [fromAmount, setFromAmount]             = useState('');
  const [toAmount, setToAmount]                 = useState('');
  const [feeAmount, setFeeAmount]               = useState('0.00');
  const [exchangeRate, setExchangeRate]         = useState<number>(0);
  const [isLoadingRate, setIsLoadingRate]       = useState(false);
  const [rateError, setRateError]               = useState<string | null>(null);
  const [isFromModalOpen, setIsFromModalOpen]   = useState(false);
  const [isToModalOpen, setIsToModalOpen]       = useState(false);
  const [fromSearch, setFromSearch]             = useState('');
  const [toSearch, setToSearch]                 = useState('');
  const [isProcessing, setIsProcessing]         = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showFailModal, setShowFailModal]       = useState(false);
  const [errorMessage, setErrorMessage]         = useState('');
  const [exchangeSnapshot, setExchangeSnapshot] = useState<any>(null);
  const [userWallets, setUserWallets]           = useState<any[]>([]);
  const [isScrolling, setIsScrolling]           = useState(false);

  // PIN modal state
  const [showPinModal, setShowPinModal]         = useState(false);
  const [pin, setPin]                           = useState(['', '', '', '']);
  const [isSubmittingPin, setIsSubmittingPin]   = useState(false);
  const pinInputRefs                            = useRef<(HTMLInputElement | null)[]>([]);

  const scrollTimer = useRef<NodeJS.Timeout | null>(null);

  // Snapshot refs so modal callbacks always have fresh values
  const fromAmountRef   = useRef(fromAmount);
  const toAmountRef     = useRef(toAmount);
  const feeAmountRef    = useRef(feeAmount);
  const exchangeRateRef = useRef(exchangeRate);
  const fromCurrencyRef = useRef(fromCurrency);
  const toCurrencyRef   = useRef(toCurrency);
  useEffect(() => { fromAmountRef.current   = fromAmount;   }, [fromAmount]);
  useEffect(() => { toAmountRef.current     = toAmount;     }, [toAmount]);
  useEffect(() => { feeAmountRef.current    = feeAmount;    }, [feeAmount]);
  useEffect(() => { exchangeRateRef.current = exchangeRate; }, [exchangeRate]);
  useEffect(() => { fromCurrencyRef.current = fromCurrency; }, [fromCurrency]);
  useEffect(() => { toCurrencyRef.current   = toCurrency;   }, [toCurrency]);

  const showToast = useCallback((msg: string, type: 'warning' | 'success' = 'warning') => {
    setToasts((prev) => {
      if (prev.length >= 5) return prev;
      const id = Date.now();
      const newToast: Toast = { id, message: msg, type, exiting: false };
      setTimeout(() => {
        setToasts((cur) => cur.map((t) => (t.id === id ? { ...t, exiting: true } : t)));
        setTimeout(() => setToasts((cur) => cur.filter((t) => t.id !== id)), 300);
      }, 5000);
      return [...prev, newToast];
    });
  }, []);

  // ── Exchange rate (mock) ──────────────────────────────────────────────────
  const getExchangeRateWithMargin = (from: string, to: string) => {
    const raw = mockExchangeRates[from]?.[to];
    return raw ? raw * (1 - 0.005) : null;
  };
  const getRawRate = (from: string, to: string) => mockExchangeRates[from]?.[to] || null;

  const fetchExchangeRate = async (from: string, to: string) => {
    if (from === to) return;
    setIsLoadingRate(true);
    setRateError(null);
    try {
      await new Promise((r) => setTimeout(r, 600));
      const rate = getExchangeRateWithMargin(from, to);
      if (rate) {
        setExchangeRate(rate);
      } else {
        setExchangeRate(0);
        setRateError(`Exchange rate unavailable for ${from} → ${to}`);
      }
    } catch {
      setExchangeRate(0);
      setRateError('Failed to fetch exchange rate. Please try again.');
    } finally {
      setIsLoadingRate(false);
    }
  };

  const formatNumberWithCommas = (value: string): string => {
    const clean = value.replace(/,/g, '');
    if (!clean) return '';
    const parts = clean.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  };

  const calculateSwapAmounts = (raw: string) => {
    if (!raw || !exchangeRate) { setToAmount(''); setFeeAmount('0.00'); return; }
    const num = parseFloat(raw.replace(/,/g, ''));
    if (isNaN(num) || num <= 0) { setToAmount(''); setFeeAmount('0.00'); return; }
    const base  = num * exchangeRate;
    const fee   = base * feePercentage;
    const final = base - fee;
    setToAmount(formatNumberWithCommas(final.toFixed(4)));
    setFeeAmount(fee.toFixed(4));
  };

  const handleFromAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    if (val === '' || /^\d*\.?\d*$/.test(val.replace(/,/g, ''))) {
      setFromAmount(formatNumberWithCommas(val));
    }
  };

  useEffect(() => { calculateSwapAmounts(fromAmount); }, [fromAmount, exchangeRate]);

  // ── Wallets ───────────────────────────────────────────────────────────────
  const fetchUserWallets = () => {
    try {
      const list = getWalletList();
      setUserWallets(list || []);
    } catch {
      setUserWallets([]);
    }
  };

  const getWalletBalance = (code: string) => {
    const w = userWallets.find((x: any) => x.currency_code === code);
    if (!w) return 0;
    return parseFloat(String(w.balance).replace(/,/g, '')) || 0;
  };

  // ── Validation ────────────────────────────────────────────────────────────
  const validateSwap = (): string | null => {
    const userId = getUserId();
    if (!userId) return 'User session not found. Please log in again.';
    if (!fromAmount || fromAmount.trim() === '') return 'Please enter an amount.';
    const numericAmount = parseFloat(fromAmount.replace(/,/g, ''));
    if (isNaN(numericAmount) || numericAmount <= 0) return 'Amount must be greater than zero.';
    if (fromCurrency.code === toCurrency.code) return 'Please select two different currencies.';
    if (!exchangeRate || exchangeRate <= 0) return `Exchange rate unavailable for ${fromCurrency.code} → ${toCurrency.code}.`;
    if (rateError) return rateError;
    const fromWallet = userWallets.find((w: any) => w.currency_code === fromCurrency.code);
    if (!fromWallet) return `You don't have a ${fromCurrency.code} wallet. Please deposit first.`;
    const balance = parseFloat(String(fromWallet.balance).replace(/,/g, ''));
    if (numericAmount > balance) {
      return `Insufficient ${fromCurrency.code} balance. Available: ${fromCurrency.symbol}${balance.toLocaleString()}`;
    }
    return null;
  };

  // ── PIN handlers ──────────────────────────────────────────────────────────
  const handlePinChange = (index: number, value: string) => {
    if (!/^\d?$/.test(value)) return;
    const newPin = [...pin];
    newPin[index] = value;
    setPin(newPin);
    if (value && index < 3) {
      setTimeout(() => pinInputRefs.current[index + 1]?.focus(), 10);
    }
  };

  const handlePinKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !pin[index] && index > 0) {
      pinInputRefs.current[index - 1]?.focus();
    }
  };

  const handlePinPaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const numbers = e.clipboardData.getData('text').replace(/\D/g, '').split('').slice(0, 4);
    if (numbers.length === 4) {
      setPin([...numbers, ...Array(4 - numbers.length).fill('')].slice(0, 4));
      setTimeout(() => pinInputRefs.current[3]?.focus(), 0);
    }
  };

  // Auto-submit when all 4 digits entered
  useEffect(() => {
    if (showPinModal && pin.every((d) => d !== '')) {
      const t = setTimeout(() => handlePinSubmit(), 100);
      return () => clearTimeout(t);
    }
  }, [pin, showPinModal]);

  // ── REST swap call ────────────────────────────────────────────────────────
  const handlePinSubmit = async () => {
    const enteredPin = pin.join('');
    if (enteredPin.length !== 4 || isSubmittingPin) return;

    setIsSubmittingPin(true);
    setIsProcessing(true);

    try {
      const userId = getUserId();
      const amount = parseFloat(fromAmountRef.current.replace(/,/g, ''));

      const payload = {
        acceptRate:     true,
        currency:       fromCurrencyRef.current.code,
        targetWallet:   toCurrencyRef.current.code,
        amount,
        userId:         Number(userId),
        transactionPin: enteredPin,
        idempotencyKey: `swap-${userId}-${Date.now()}`,
      };

      const response = await walletService.swap(userId, payload);

      // Success: backend returns 2xx
      const snapshot = {
        fromCurrency: fromCurrencyRef.current,
        toCurrency:   toCurrencyRef.current,
        fromAmount:   fromAmountRef.current,
        toAmount:     toAmountRef.current,
        exchangeRate: exchangeRateRef.current,
        feeAmount:    feeAmountRef.current,
      };
      setExchangeSnapshot(snapshot);
      setShowPinModal(false);
      setShowSuccessModal(true);

      updateNotificationContainer({ type: 'PAYMENTS', description: 'Currency swap completed successfully' });
      showToast(`Swap complete! You received ${toCurrencyRef.current.symbol}${toAmountRef.current}`, 'success');

      setFromAmount('');
      setToAmount('');
      setFeeAmount('0.00');
      fetchUserWallets();

    } catch (err: any) {
      const msg = err?.message || err?.error || 'Swap failed. Please try again.';
      setErrorMessage(msg);
      setShowPinModal(false);
      setShowFailModal(true);
      showToast(msg, 'warning');
    } finally {
      setIsProcessing(false);
      setIsSubmittingPin(false);
      setPin(['', '', '', '']);
    }
  };

  // Called by the Swap Now button — validates then opens PIN modal
  const handleExchange = () => {
    const validationError = validateSwap();
    if (validationError) {
      showToast(validationError, 'warning');
      return;
    }
    setPin(['', '', '', '']);
    setShowPinModal(true);
    setTimeout(() => pinInputRefs.current[0]?.focus(), 100);
  };

  const handleSwapCurrencies = () => {
    if (isProcessing) return;
    const prev = fromCurrency;
    setFromCurrency(toCurrency);
    setToCurrency(prev);
    setFromAmount(toAmount);
    setToAmount('');
  };

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  useEffect(() => {
    const t = setTimeout(() => setIsPageLoading(false), 2000);
    fetchUserWallets();
    return () => clearTimeout(t);
  }, []);

  useEffect(() => {
    if (fromCurrency.code !== toCurrency.code) {
      fetchExchangeRate(fromCurrency.code, toCurrency.code);
    }
  }, [fromCurrency.code, toCurrency.code]);

  const handleScroll = () => {
    setIsScrolling(true);
    if (scrollTimer.current) clearTimeout(scrollTimer.current);
    scrollTimer.current = setTimeout(() => setIsScrolling(false), 1000);
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.classList.toggle('dark', newTheme === 'dark');
  };

  const filteredFrom = useMemo(() =>
    currencies.filter((c) =>
      c.name.toLowerCase().includes(fromSearch.toLowerCase()) ||
      c.code.toLowerCase().includes(fromSearch.toLowerCase())
    ), [fromSearch]);

  const filteredTo = useMemo(() =>
    currencies.filter((c) =>
      c.name.toLowerCase().includes(toSearch.toLowerCase()) ||
      c.code.toLowerCase().includes(toSearch.toLowerCase())
    ), [toSearch]);

  if (isPageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">

          {/* Toasts */}
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
            {/* Breadcrumb */}
            <div className="airtime-breadcrumb">
              <Link href="/dashboard" className="breadcrumb-link">Dashboard</Link>
              <ChevronRight size={14} className="breadcrumb-sep" />
              <span className="breadcrumb-current">Swaps</span>
            </div>

            <h1 className="swap-page-title">
              <span className="title-green">Currency</span> Swap
            </h1>

            {/* Main Card */}
            <div className="swap-card">
              {/* Illustration */}
              <div className="swap-illustration-col">
                <Image
                  src="/assets/images/swap-banner.png"
                  alt="Currency Swap"
                  width={320}
                  height={320}
                  priority
                  style={{ objectFit: 'contain', width: '100%', height: 'auto' }}
                />
              </div>

              {/* Form */}
              <div className="swap-form-col">
                {/* Wallet Balances */}
                <div className="swap-wallet-balances">
                  <div className="swap-wallet-item">
                    <span className="swap-wallet-label">{fromCurrency.code} Balance</span>
                    <span className="swap-wallet-value">
                      {fromCurrency.symbol}
                      {getWalletBalance(fromCurrency.code).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                  </div>
                  <div className="swap-wallet-divider" />
                  <div className="swap-wallet-item">
                    <span className="swap-wallet-label">{toCurrency.code} Balance</span>
                    <span className="swap-wallet-value">
                      {toCurrency.symbol}
                      {getWalletBalance(toCurrency.code).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </span>
                  </div>
                </div>

                {/* Live Rate Card */}
                <div className="swap-rate-card">
                  <div className="swap-rate-top">
                    <div className="swap-rate-left"><TrendingUp size={14} /><span>Exchange Rate</span></div>
                    <div className="swap-rate-right"><Clock size={13} /><span>Live</span></div>
                  </div>
                  <div className="swap-rate-value-row">
                    {isLoadingRate ? (
                      <span className="swap-rate-loading"><span className="rate-spinner" />Fetching rate...</span>
                    ) : rateError ? (
                      <span className="swap-rate-error"><AlertCircle size={13} /> {rateError}</span>
                    ) : exchangeRate > 0 ? (
                      <>
                        <span className="swap-rate-main">1 {fromCurrency.code} = {exchangeRate.toFixed(6)} {toCurrency.code}</span>
                        {getRawRate(fromCurrency.code, toCurrency.code) && (
                          <span className="swap-rate-market">Market: {getRawRate(fromCurrency.code, toCurrency.code)?.toFixed(6)}</span>
                        )}
                      </>
                    ) : (
                      <span className="swap-rate-error">Rate unavailable</span>
                    )}
                  </div>
                </div>

                {fromAmount && parseFloat(fromAmount.replace(/,/g, '')) > getWalletBalance(fromCurrency.code) && (
                  <span className="swap-field-error"><AlertCircle size={12} /> Insufficient {fromCurrency.code} balance</span>
                )}

                {/* FROM */}
                <div className="swap-section-label">From</div>
                <div className="swap-field-group">
                  <div className="swap-currency-select" onClick={() => setIsFromModalOpen(true)}>
                    <span className="swap-flag">{fromCurrency.flag}</span>
                    <span className="swap-code">{fromCurrency.code}</span>
                    <span className="swap-currency-name">{fromCurrency.name}</span>
                    <ChevronDown size={16} className="swap-chevron" />
                  </div>
                  <div className="swap-amount-row">
                    <span className="swap-symbol">{fromCurrency.symbol}</span>
                    <input
                      type="text"
                      className="swap-input"
                      placeholder="Enter amount"
                      value={fromAmount}
                      onChange={handleFromAmountChange}
                      disabled={isProcessing}
                    />
                  </div>
                </div>

                {/* Swap Toggle */}
                <div className="swap-toggle-row">
                  <button className="swap-toggle-btn" onClick={handleSwapCurrencies} disabled={isProcessing} title="Swap currencies">
                    <ArrowLeftRight size={16} />
                  </button>
                </div>

                {/* TO */}
                <div className="swap-section-label">To</div>
                <div className="swap-field-group">
                  <div className="swap-currency-select" onClick={() => setIsToModalOpen(true)}>
                    <span className="swap-flag">{toCurrency.flag}</span>
                    <span className="swap-code">{toCurrency.code}</span>
                    <span className="swap-currency-name">{toCurrency.name}</span>
                    <ChevronDown size={16} className="swap-chevron" />
                  </div>
                  <div className="swap-amount-row swap-amount-readonly">
                    <span className="swap-symbol">{toCurrency.symbol}</span>
                    <input type="text" className="swap-input" placeholder="Converted amount" value={toAmount} readOnly />
                  </div>
                  {fromCurrency.code === toCurrency.code && (
                    <span className="swap-field-error"><AlertCircle size={12} /> From and To currencies must be different</span>
                  )}
                </div>

                {/* Summary */}
                {fromAmount && toAmount && (
                  <div className="swap-summary">
                    <div className="swap-summary-row">
                      <span>Exchange Rate</span>
                      <span>1 {fromCurrency.code} = {exchangeRate.toFixed(6)} {toCurrency.code}</span>
                    </div>
                    <div className="swap-summary-row swap-summary-fee">
                      <span>Swap Fee (1.5%)</span>
                      <span>−{toCurrency.symbol}{feeAmount}</span>
                    </div>
                    <div className="swap-summary-divider" />
                    <div className="swap-summary-row swap-summary-total">
                      <span>You'll Receive</span>
                      <span>{toCurrency.symbol}{toAmount} {toCurrency.code}</span>
                    </div>
                  </div>
                )}

                {/* Swap Now Button */}
                <button
                  className="swap-btn"
                  onClick={handleExchange}
                  disabled={
                    isProcessing || !fromAmount || isLoadingRate ||
                    !!rateError || fromCurrency.code === toCurrency.code
                  }
                >
                  {isProcessing ? (
                    <span className="btn-loader-row"><span className="btn-spinner" />Processing...</span>
                  ) : (
                    <span className="btn-loader-row"><ArrowLeftRight size={18} />Swap Now</span>
                  )}
                </button>
              </div>
            </div>

            {/* From Modal */}
            {isFromModalOpen && (
              <div className="modal-overlay" onClick={() => setIsFromModalOpen(false)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header"><h3>Select From Currency</h3></div>
                  <div className="search-container">
                    <i className="fa fa-search" />
                    <input type="text" placeholder="Search currency..." value={fromSearch} onChange={(e) => setFromSearch(e.target.value)} />
                  </div>
                  <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                    {filteredFrom.length === 0 ? <div className="empty-search">No currencies found</div> : filteredFrom.map((c) => (
                      <div key={c.code} className="country-item" onClick={() => { setFromCurrency(c); setIsFromModalOpen(false); setFromSearch(''); }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <span style={{ fontSize: '22px' }}>{c.flag}</span>
                          <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <span style={{ fontWeight: 600 }}>{c.code}</span>
                            <span style={{ fontSize: '11px', color: '#888' }}>{c.name}</span>
                          </div>
                        </div>
                        <div className={`radio-outer ${fromCurrency.code === c.code ? 'checked' : ''}`}><div className="radio-inner" /></div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* To Modal */}
            {isToModalOpen && (
              <div className="modal-overlay" onClick={() => setIsToModalOpen(false)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header"><h3>Select To Currency</h3></div>
                  <div className="search-container">
                    <i className="fa fa-search" />
                    <input type="text" placeholder="Search currency..." value={toSearch} onChange={(e) => setToSearch(e.target.value)} />
                  </div>
                  <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                    {filteredTo.length === 0 ? <div className="empty-search">No currencies found</div> : filteredTo.map((c) => (
                      <div key={c.code} className="country-item" onClick={() => { setToCurrency(c); setIsToModalOpen(false); setToSearch(''); }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                          <span style={{ fontSize: '22px' }}>{c.flag}</span>
                          <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <span style={{ fontWeight: 600 }}>{c.code}</span>
                            <span style={{ fontSize: '11px', color: '#888' }}>{c.name}</span>
                          </div>
                        </div>
                        <div className={`radio-outer ${toCurrency.code === c.code ? 'checked' : ''}`}><div className="radio-inner" /></div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* PIN Modal */}
            {showPinModal && (
              <>
                <div className="status-modal-overlay" onClick={() => { if (!isSubmittingPin) { setShowPinModal(false); setPin(['', '', '', '']); }}} />
                <div className={`status-modal pin-modal ${theme}`}>
                  <div className="status-modal-content">
                    <h3 className="status-modal-title">Enter PIN</h3>
                    <p className="status-modal-message">Enter your 4-digit transfer PIN to confirm the swap</p>

                    {/* Swap summary inside PIN modal */}
                    <div className="pin-summary" style={{ margin: '12px 0', padding: '10px 14px', background: '#f8fafc', borderRadius: 8, fontSize: 13 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                        <span style={{ color: '#6b7280' }}>Swapping</span>
                        <span style={{ fontWeight: 600 }}>{fromCurrency.symbol}{fromAmount} {fromCurrency.code}</span>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: '#6b7280' }}>You receive</span>
                        <span style={{ fontWeight: 600, color: 'var(--bg-main)' }}>{toCurrency.symbol}{toAmount} {toCurrency.code}</span>
                      </div>
                    </div>

                    <div className="pin-inputs">
                      {pin.map((digit, index) => (
                        <input
                          key={index}
                          ref={(el) => { pinInputRefs.current[index] = el; }}
                          type="password"
                          inputMode="numeric"
                          className="pin-input"
                          maxLength={1}
                          value={digit}
                          onChange={(e) => handlePinChange(index, e.target.value)}
                          onKeyDown={(e) => handlePinKeyDown(index, e)}
                          onPaste={handlePinPaste}
                          disabled={isSubmittingPin}
                          autoFocus={index === 0}
                        />
                      ))}
                    </div>

                    <div className="status-modal-actions">
                      <button
                        className="status-modal-btn secondary-btn"
                        onClick={() => { setShowPinModal(false); setPin(['', '', '', '']); }}
                        disabled={isSubmittingPin}
                      >
                        Cancel
                      </button>
                      <button
                        className="status-modal-btn confirm-btn"
                        onClick={handlePinSubmit}
                        disabled={isSubmittingPin || pin.some((d) => d === '')}
                      >
                        {isSubmittingPin
                          ? <><div className="spinner-small" /><span>Processing...</span></>
                          : 'Confirm Swap'}
                      </button>
                    </div>
                  </div>
                </div>
              </>
            )}

            {/* Success Modal */}
            {showSuccessModal && exchangeSnapshot && (
              <>
                <div className="status-modal-overlay" onClick={() => setShowSuccessModal(false)} />
                <div className={`status-modal success-modal ${theme === 'dark' ? 'dark' : ''}`}>
                  <div className="status-modal-content">
                    <div className="status-icon-wrapper success-icon"><CheckCircle size={48} /></div>
                    <h3 className="status-modal-title">Swap Successful!</h3>
                    <p className="status-modal-message">Your currency swap has been completed.</p>
                    <div className="status-modal-details">
                      <div className="status-detail-row">
                        <span className="status-detail-label">From</span>
                        <span className="status-detail-value">{exchangeSnapshot.fromCurrency.symbol}{exchangeSnapshot.fromAmount} {exchangeSnapshot.fromCurrency.code}</span>
                      </div>
                      <div className="status-detail-row">
                        <span className="status-detail-label">Rate</span>
                        <span className="status-detail-value">1 {exchangeSnapshot.fromCurrency.code} = {exchangeSnapshot.exchangeRate.toFixed(6)} {exchangeSnapshot.toCurrency.code}</span>
                      </div>
                      <div className="status-detail-row swap-summary-fee">
                        <span className="status-detail-label">Fee (1.5%)</span>
                        <span className="status-detail-value">−{exchangeSnapshot.toCurrency.symbol}{exchangeSnapshot.feeAmount}</span>
                      </div>
                      <div className="status-detail-row">
                        <span className="status-detail-label">You Received</span>
                        <span className="status-detail-value" style={{ color: 'var(--bg-main)', fontWeight: 700 }}>
                          {exchangeSnapshot.toCurrency.symbol}{exchangeSnapshot.toAmount} {exchangeSnapshot.toCurrency.code}
                        </span>
                      </div>
                    </div>
                    <button className="status-modal-btn success-btn" onClick={() => setShowSuccessModal(false)}>Done</button>
                  </div>
                </div>
              </>
            )}

            {/* Fail Modal */}
            {showFailModal && (
              <>
                <div className="status-modal-overlay" onClick={() => setShowFailModal(false)} />
                <div className={`status-modal fail-modal ${theme === 'dark' ? 'dark' : ''}`}>
                  <div className="status-modal-content">
                    <div className="status-icon-wrapper fail-icon"><AlertCircle size={48} /></div>
                    <h3 className="status-modal-title">Swap Failed</h3>
                    <p className="status-modal-message">{errorMessage}</p>
                    <div className="status-modal-actions">
                      <button className="status-modal-btn secondary-btn" onClick={() => setShowFailModal(false)}>Cancel</button>
                      <button className="status-modal-btn fail-btn" onClick={() => { setShowFailModal(false); handleExchange(); }}>Try Again</button>
                    </div>
                  </div>
                </div>
              </>
            )}

          </div>

          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav activeTab="exchange" onPlusClick={() => setIsDepositOpen(true)} />
      <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
      {!isChatOpen && (
        <button className="chat-fab" onClick={() => setIsChatOpen(true)} aria-label="Open support chat">
          <i className="fa-solid fa-comment-dots"></i>
        </button>
      )}
      <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
    </div>
  );
};

export default SwapPage;
