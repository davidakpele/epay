'use client';

import React, { useState, useEffect, useMemo, useRef } from 'react';
import { ArrowDownUp, TrendingUp, Clock, CheckCircle, AlertCircle, Info, ChevronDown, Search, X } from 'lucide-react';
import './ExchangePage.css';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import { Currency } from '../types/api';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import LoadingScreen from '@/components/loader/Loadingscreen';
import { getUserId, getWalletList, setWalletContainer, updateNotificationContainer } from '../api';

interface Toast {
  id: number;
  message: string;
  type: 'warning' | 'success' | 'error';
  exiting: boolean;
}

const ExchangePage = () => {
  const [fromCurrency, setFromCurrency] = useState<any>({
    code: 'NGN',
    name: 'Nigerian Naira',
    symbol: '₦',
    flag: '🇳🇬'
  });
  
  const [toCurrency, setToCurrency] = useState<any>({
    code: 'USD',
    name: 'US Dollar',
    symbol: '$',
    flag: '🇺🇸'
  });
  
  const currencies = [
    { code: 'USD', name: 'US Dollar', symbol: '$', flag: '🇺🇸' },
    { code: 'EUR', name: 'Euro', symbol: '€', flag: '🇪🇺' },
    { code: 'NGN', name: 'Nigerian Naira', symbol: '₦', flag: '🇳🇬' },
    { code: 'GBP', name: 'British Pound', symbol: '£', flag: '🇬🇧' },
    { code: 'JPY', name: 'Japanese Yen', symbol: '¥', flag: '🇯🇵' },
    { code: 'AUD', name: 'Australian Dollar', symbol: '$', flag: '🇦🇺' },
    { code: 'CAD', name: 'Canadian Dollar', symbol: '$', flag: '🇨🇦' },
    { code: 'CHF', name: 'Swiss Franc', symbol: 'CHF', flag: '🇨🇭' },
    { code: 'CNY', name: 'Chinese Yuan', symbol: '¥', flag: '🇨🇳' },
    { code: 'INR', name: 'Indian Rupee', symbol: '₹', flag: '🇮🇳' },
  ];

  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [fromAmount, setFromAmount] = useState('');
  const [toAmount, setToAmount] = useState('');
  const [isLoadingRate, setIsLoadingRate] = useState(false);
  const [exchangeRate, setExchangeRate] = useState<number>(0.0013);
  const [isProcessing, setIsProcessing] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showFailModal, setShowFailModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [errors, setErrors] = useState<{ amount?: string }>({});
  const [feeAmount, setFeeAmount] = useState<string>('0.00');

  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [selectedCurrency, setSelectedCurrency] = useState<Currency>(currencies[2]);
  const [isCurrencyModalOpen, setIsCurrencyModalOpen] = useState(false);
  const [modalTarget, setModalTarget] = useState<'from' | 'to'>('from');
  const [searchQuery, setSearchQuery] = useState('');
  const [isScrolling, setIsScrolling] = useState(false);
  const [exchangeSnapshot, setExchangeSnapshot] = useState<any>(null);
  const scrollTimer = useRef<NodeJS.Timeout | null>(null);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const feePercentage = 0.015;

  // WebSocket state
  const [websocket, setWebsocket] = useState<WebSocket | null>(null);
  const [userWallets, setUserWallets] = useState<any[]>([]);
  const [toasts, setToasts] = useState<Toast[]>([]);

  const mockExchangeRates: { [key: string]: { [key: string]: number } } = {
    USD: { EUR: 0.92, GBP: 0.80, JPY: 147.11, AUD: 1.52, CAD: 1.35, CHF: 0.88, CNY: 7.25, INR: 83.12, NGN: 1500.50 },
    EUR: { USD: 1.09, GBP: 0.87, JPY: 159.25, AUD: 1.65, CAD: 1.47, CHF: 0.96, CNY: 7.88, INR: 90.35, NGN: 1630.75 },
    GBP: { USD: 1.25, EUR: 1.15, JPY: 184.22, AUD: 1.90, CAD: 1.69, CHF: 1.10, CNY: 9.06, INR: 103.89, NGN: 1875.30 },
    JPY: { USD: 0.0068, EUR: 0.0063, GBP: 0.0054, AUD: 0.0103, CAD: 0.0092, CHF: 0.0060, CNY: 0.0493, INR: 0.565, NGN: 10.20 },
    AUD: { USD: 0.66, EUR: 0.61, GBP: 0.53, JPY: 97.10, CAD: 0.89, CHF: 0.58, CNY: 4.77, INR: 54.68, NGN: 987.45 },
    CAD: { USD: 0.74, EUR: 0.68, GBP: 0.59, JPY: 108.75, AUD: 1.12, CHF: 0.65, CNY: 5.37, INR: 61.55, NGN: 1111.11 },
    CHF: { USD: 1.14, EUR: 1.04, GBP: 0.91, JPY: 166.67, AUD: 1.72, CAD: 1.54, CNY: 8.24, INR: 94.50, NGN: 1705.88 },
    CNY: { USD: 0.14, EUR: 0.13, GBP: 0.11, JPY: 20.28, AUD: 0.21, CAD: 0.19, CHF: 0.12, INR: 11.46, NGN: 206.90 },
    INR: { USD: 0.012, EUR: 0.011, GBP: 0.0096, JPY: 1.77, AUD: 0.018, CAD: 0.016, CHF: 0.0106, CNY: 0.087, NGN: 18.05 },
    NGN: { USD: 0.00067, EUR: 0.00061, GBP: 0.00053, JPY: 0.098, AUD: 0.00101, CAD: 0.00090, CHF: 0.00059, CNY: 0.0048, INR: 0.055 }
  };

  const showToast = (msg: string, type: 'warning' | 'success' | 'error' = 'warning') => {
    setToasts((prev) => {
      if (prev.length >= 5) return prev;

      const id = Date.now();
      const newToast: Toast = { id, message: msg, type, exiting: false };

      setTimeout(() => {
        setToasts((currentToasts) =>
          currentToasts.map((t) => (t.id === id ? { ...t, exiting: true } : t))
        );

        setTimeout(() => {
          setToasts((currentToasts) => currentToasts.filter((t) => t.id !== id));
        }, 300);
      }, 5000);

      return [...prev, newToast];
    });
  };

  const getExchangeRateWithMargin = (fromCurr: string, toCurr: string) => {
    const rawRate = mockExchangeRates[fromCurr]?.[toCurr];
    if (!rawRate) return null;
    
    const margin = 0.005; // 0.5% margin
    return rawRate * (1 - margin); 
  };

  const getRawExchangeRate = (fromCurr: string, toCurr: string) => {
    return mockExchangeRates[fromCurr]?.[toCurr] || null;
  };

  const connectWebSocket = () => {
    const userId = getUserId();
    if (!userId) {
      console.warn('No userId found, cannot connect WebSocket');
      return null;
    }

    try {
      const ws = new WebSocket(`ws://localhost:8292/api/ws/wallet?userId=${userId}`);
      
      ws.onopen = () => {
        console.log('WebSocket connected for currency exchange');
        setWebsocket(ws);
      };
      
      ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        handleWebSocketMessage(message);
      };
      
      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        showToast('WebSocket connection error', 'error');
      };
      
      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setWebsocket(null);
      };

      return ws;
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      showToast('Failed to connect to server', 'error');
      return null;
    }
  };

  const handleWebSocketMessage = (message: any) => {
    if (message.type === 'swap_response') {
      setIsProcessing(false);
      
      if (message.status === 'COMPLETED') {
        setExchangeSnapshot({ 
          fromCurrency, 
          toCurrency, 
          fromAmount, 
          toAmount, 
          exchangeRate, 
          feeAmount 
        });
        setShowSuccessModal(true);
        updateNotificationContainer({
          type: "PAYMENTS",
          description: "Currency exchange completed successfully"
        });
        showToast(`Currency exchange completed successfully! You received ${message.toAmount} ${toCurrency.code}`, 'success');
        setFromAmount('');
        setToAmount('');
        setFeeAmount('0.00');
        fetchUserWallets();
      } else {
        updateNotificationContainer({
          type: "MESSAGES",
          description: "Exchange failed. Please try again."
        });
        setErrorMessage(message.message || 'Exchange failed. Please try again.');
        setShowFailModal(true);
        showToast(message.message || 'Exchange failed', 'error');
      }
    }
    
    if (message.type === 'wallet_update' || message.type === 'wallet_update_response') {
      setWalletContainer(
        message.data.wallet.wallet_balances, 
        message.data.wallet.hasTransferPin, 
        message.data.wallet.walletId
      );
      fetchUserWallets();
    }
  };

  const fetchUserWallets = async () => {
    try {
      const walletList = getWalletList();
      setUserWallets(walletList || []);
  
      if (walletList && walletList.length > 0) {
        const hasNGN = walletList.find((w: any) => w.currency_code === 'NGN');
        const hasUSD = walletList.find((w: any) => w.currency_code === 'USD');
        
        if (hasNGN) {
          const ngnCurrency = currencies.find(c => c.code === 'NGN');
          if (ngnCurrency) setFromCurrency(ngnCurrency);
        }
        if (hasUSD) {
          const usdCurrency = currencies.find(c => c.code === 'USD');
          if (usdCurrency) setToCurrency(usdCurrency);
        }
      }
    } catch (error) {
      console.error('Error fetching wallets:', error);
      setUserWallets([]);
    }
  };

  const fetchExchangeRate = async (from: string, to: string) => {
    setIsLoadingRate(true);
    try {
      await new Promise(resolve => setTimeout(resolve, 1000));
      const rate = getExchangeRateWithMargin(from, to);
      if (rate) {
        setExchangeRate(rate);
      }
    } catch (error) {
      console.error('Failed to fetch exchange rate:', error);
    } finally {
      setIsLoadingRate(false);
    }
  };

  useEffect(() => {
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    fetchUserWallets();
    const ws = connectWebSocket();

    return () => {
      clearTimeout(loadingTimer);
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, []);

  useEffect(() => {
    if (fromCurrency.code !== toCurrency.code) {
      fetchExchangeRate(fromCurrency.code, toCurrency.code);
    }
  }, [fromCurrency.code, toCurrency.code]);

  const calculateSwapAmounts = (amount: string) => {
    if (!amount || !exchangeRate) {
      setToAmount('');
      setFeeAmount('0.00');
      return;
    }
    const numericAmount = parseFloat(amount.replace(/,/g, ''));
    if (isNaN(numericAmount)) {
      setToAmount('');
      setFeeAmount('0.00');
      return;
    }
    const baseConverted = numericAmount * exchangeRate;
    const fee = baseConverted * feePercentage;
    const finalAmount = baseConverted - fee;
    setToAmount(formatNumberWithCommas(finalAmount.toFixed(2)));
    setFeeAmount(fee.toFixed(2));
  };

  useEffect(() => {
    calculateSwapAmounts(fromAmount);
  }, [fromAmount, exchangeRate]);

  const formatNumberWithCommas = (value: string): string => {
    const cleanValue = value.replace(/,/g, '');
    if (!cleanValue) return '';
    const parts = cleanValue.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  };

  const handleFromAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue.replace(/,/g, ''))) {
      setFromAmount(formatNumberWithCommas(inputValue));
      if (errors.amount) setErrors({});
    }
  };

  const handleSwapCurrencies = () => {
    const tempCurrency = fromCurrency;
    setFromCurrency(toCurrency);
    setToCurrency(tempCurrency);
    setFromAmount(toAmount);
  };

  const getWalletBalance = (currencyCode: string) => {
    const wallet = userWallets.find((w: any) => w.currency_code === currencyCode);
    return wallet ? parseFloat(wallet.balance.replace(/,/g, '')) : 0;
  };

  const handleExchange = async () => {
    if (!fromAmount || parseFloat(fromAmount.replace(/,/g, '')) <= 0) {
      showToast('Please enter a valid amount', 'error');
      return;
    }

    if (fromCurrency.code === toCurrency.code) {
      showToast('Please select different currencies for exchange', 'error');
      return;
    }

    const fromWallet = userWallets.find((w: any) => w.currency_code === fromCurrency.code);
    if (!fromWallet) {
      showToast('Source wallet not found', 'error');
      return;
    }

    const fromBalance = parseFloat(fromWallet.balance.replace(/,/g, ''));
    const exchangeAmount = parseFloat(fromAmount.replace(/,/g, ''));

    if (exchangeAmount > fromBalance) {
      showToast('Insufficient balance for exchange', 'error');
      return;
    }

    setIsProcessing(true);

    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const swapRequest = {
        type: "swap_currency",
        fromCurrency: fromCurrency.code,
        toCurrency: toCurrency.code,
        amount: fromAmount.replace(/,/g, ''),
        acceptRate: true,
        userId: getUserId()
      };
      
      websocket.send(JSON.stringify(swapRequest));
    } else {
      showToast('WebSocket connection not available. Please refresh the page.', 'error');
      setIsProcessing(false);
    }
  };

  const openCurrencyModal = (target: 'from' | 'to') => {
    setModalTarget(target);
    setSearchQuery('');
    setIsCurrencyModalOpen(true);
  };

  const selectCurrency = (currency: any) => {
    if (modalTarget === 'from') {
      setFromCurrency(currency);
    } else {
      setToCurrency(currency);
    }
    setIsCurrencyModalOpen(false);
  };

  const filteredCurrencies = useMemo(() => {
    return currencies.filter(c => 
      c.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
      c.code.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [searchQuery]);

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.classList.toggle('dark', newTheme === 'dark');
    document.body.classList.toggle('dark-theme', newTheme === 'dark');
  };

  const handleScroll = () => {
    setIsScrolling(true);
    if (scrollTimer.current) clearTimeout(scrollTimer.current);
    scrollTimer.current = setTimeout(() => setIsScrolling(false), 1000);
  };

  const getToastColor = (type: string) => {
    switch(type) {
      case 'success': return 'bg-green-500';
      case 'error': return 'bg-red-500';
      default: return 'bg-yellow-500';
    }
  };

  if (isPageLoading) {
    return <LoadingScreen />;
  }

  return (
    <>
      {toasts.length > 0 && (
        <div className="fixed top-4 right-4 z-50 space-y-2">
          {toasts.map((toast) => (
            <div
              key={toast.id}
              className={`${getToastColor(toast.type)} text-white px-4 py-3 rounded-lg shadow-lg transition-all duration-300 ${
                toast.exiting ? 'opacity-0 translate-x-full' : 'opacity-100 translate-x-0'
              }`}
            >
              {toast.message}
            </div>
          ))}
        </div>
      )}

      <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
        <Sidebar />
        <main className={`main-content`}>
          <Header theme={theme} toggleTheme={toggleTheme} />
          <div className="scrollable-content">
            <div className={`exchange-page ${theme === "dark" ? "bg-light" : "bg-dark"}`}>
              <div className="exchange-container">
                <div className="exchange-header">
                  <h1 className={`exchange-title ${theme === "dark" ? "color-light" : "color-dark"}`}>Currency Exchange</h1>
                  <p className={`exchange-subtitle ${theme === "dark" ? "color-light" : "color-dark"}`}>Convert between different currencies instantly</p>
                </div>

                {/* Wallet Balances */}
                <div className="wallet-balances" style={{ 
                  display: 'grid', 
                  gridTemplateColumns: '1fr 1fr', 
                  gap: '1rem', 
                  marginBottom: '1.5rem',
                  padding: '1rem',
                  background: theme === 'dark' ? '#2a2a2a' : '#f8f9fa',
                  borderRadius: '12px'
                }}>
                  <div style={{color:"#0f172a", textAlign: 'center' }}>
                    <p style={{color:"#0f172a", fontSize: '0.875rem', opacity: 0.7, marginBottom: '0.25rem' }}>
                      {fromCurrency.code} Balance
                    </p>
                    <p style={{color:"#0f172a", fontSize: '1.25rem', fontWeight: '600' }}>
                      {fromCurrency.symbol}{getWalletBalance(fromCurrency.code).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </p>
                  </div>
                  <div style={{ textAlign: 'center' }}>
                    <p style={{color:"#0f172a", fontSize: '0.875rem', opacity: 0.7, marginBottom: '0.25rem' }}>
                      {toCurrency.code} Balance
                    </p>
                    <p style={{color:"#0f172a", fontSize: '1.25rem', fontWeight: '600' }}>
                      {toCurrency.symbol}{getWalletBalance(toCurrency.code).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </p>
                  </div>
                </div>

                <div className="exchange-rate-card">
                  <div className="rate-header">
                    <div className="rate-label">
                      <TrendingUp size={16} />
                      <span>Current Exchange Rate</span>
                    </div>
                    <div className="rate-updated">
                      <Clock size={14} />
                      <span>Updated now</span>
                    </div>
                  </div>
                  <div className="rate-display">
                    {isLoadingRate ? (
                      <div className="rate-loader">
                        <div className="rate-spinner" />
                        <span>Fetching rate...</span>
                      </div>
                    ) : (
                      <>
                        <span className="rate-value">
                          1 {fromCurrency.code} = {exchangeRate.toFixed(6)} {toCurrency.code}
                        </span>
                        {getRawExchangeRate(fromCurrency.code, toCurrency.code) && (
                          <span style={{color:"#0f172a", fontSize: '0.8em', opacity: 0.6, marginLeft: '0.5rem' }}>
                            (Market: {getRawExchangeRate(fromCurrency.code, toCurrency.code)?.toFixed(6)})
                          </span>
                        )}
                      </>
                    )}
                  </div>
                </div>

                <div className="exchange-form">
                  <div className="currency-input-group">
                    <label className="input-label">From</label>
                    <div className="currency-input-wrapper">
                      <div className="custom-select-trigger" onClick={() => openCurrencyModal('from')}>
                        <span className="trigger-flag">{fromCurrency.flag}</span>
                        <span className="trigger-code">{fromCurrency.code}</span>
                        <ChevronDown size={16} className="trigger-icon" />
                      </div>
                      <input
                        type="text"
                        className="currency-input"
                        placeholder="0.00"
                        value={fromAmount}
                        onChange={handleFromAmountChange}
                        disabled={isProcessing}
                      />
                      <span className="exchange-currency-symbol">{fromCurrency.symbol}</span>
                    </div>
                  </div>

                  <div className="swap-button-container">
                    <button className="swap-button" onClick={handleSwapCurrencies} disabled={isProcessing}>
                      <ArrowDownUp size={20} />
                    </button>
                  </div>

                  <div className="currency-input-group">
                    <label className="input-label">To</label>
                    <div className="currency-input-wrapper">
                      <div className="custom-select-trigger" onClick={() => openCurrencyModal('to')}>
                        <span className="trigger-flag">{toCurrency.flag}</span>
                        <span className="trigger-code">{toCurrency.code}</span>
                        <ChevronDown size={16} className="trigger-icon" />
                      </div>
                      <input
                        type="text"
                        className="currency-input readonly"
                        placeholder="0.00"
                        value={toAmount}
                        readOnly
                      />
                      <span className="exchange-currency-symbol">{toCurrency.symbol}</span>
                    </div>
                  </div>

                  {fromAmount && toAmount && (
                    <div className="exchange-summary">
                      <div className="summary-row">
                        <span className="summary-label">Exchange Rate</span>
                        <span className="summary-value">1 {fromCurrency.code} = {exchangeRate.toFixed(6)} {toCurrency.code}</span>
                      </div>
                      <div className="summary-row fee-row">
                        <span className="summary-label">Swap Fee (1.5%)</span>
                        <span className="summary-value fee-value">-{toCurrency.symbol}{feeAmount} {toCurrency.code}</span>
                      </div>
                      <div className="summary-divider" />
                      <div className="summary-row highlight">
                        <span className="summary-label">You'll Receive</span>
                        <span className="summary-value">{toCurrency.symbol}{toAmount} {toCurrency.code}</span>
                      </div>
                    </div>
                  )}

                  <button className="exchange-button" onClick={handleExchange} disabled={isProcessing || !fromAmount}>
                    {isProcessing ? <div className="button-spinner" /> : <span>Confirm Exchange</span>}
                  </button>
                </div>
              </div>
            </div>

            {isCurrencyModalOpen && (
              <div className="modal-overlay" onClick={() => setIsCurrencyModalOpen(false)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header"><h3>Select Currency</h3></div>
                  <div className="search-container">
                    <span className="search-icon-inside"><Search size={16} /></span>
                    <input type="text" placeholder="Search" value={searchQuery} onChange={e => setSearchQuery(e.target.value)}/>
                  </div>
                  <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                    {filteredCurrencies.map((c) => (
                      <div key={c.code} className="country-item"  onClick={() => selectCurrency(c)}>
                        <span>{c.flag} {c.name} ({c.code})</span>
                        <div className={`radio-outer ${selectedCurrency.code === c.code ? 'checked' : ''}`}><div className="radio-inner"></div></div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {showSuccessModal && exchangeSnapshot && (
              <>
                <div className={`status-modal-overlay`} onClick={() => setShowSuccessModal(false)} />
                <div className={`status-modal success-modal ${theme === "dark" ? "dark" : "light"}`}>
                  <div className="status-modal-content">
                    <div className="status-icon-wrapper success-icon">
                      <CheckCircle size={48} />
                    </div>
                    <h3 className="status-modal-title">Exchange Successful!</h3>
                    <p className="status-modal-message">
                      Your currency exchange has been completed successfully.
                    </p>
                    <div className="status-modal-details">
                      <div className="status-detail-row">
                        <span className="status-detail-label">From</span>
                        <span className="status-detail-value">{exchangeSnapshot.fromCurrency.symbol}{exchangeSnapshot.fromAmount} {exchangeSnapshot.fromCurrency.code}</span>
                      </div>
                      <div className="status-detail-row">
                        <span className="status-detail-label">Exchange Rate</span>
                        <span className="status-detail-value">1 {exchangeSnapshot.fromCurrency.code} = {exchangeSnapshot.exchangeRate.toFixed(6)} {exchangeSnapshot.toCurrency.code}</span>
                      </div>
                      <div className="status-detail-row fee-detail">
                        <span className="status-detail-label">Swap Fee (1.5%)</span>
                        <span className="status-detail-value">-{exchangeSnapshot.toCurrency.symbol}{exchangeSnapshot.feeAmount} {exchangeSnapshot.toCurrency.code}</span>
                      </div>
                      <div className="status-detail-row">
                        <span className="status-detail-label">To</span>
                        <span className="status-detail-value">{exchangeSnapshot.toCurrency.symbol}{exchangeSnapshot.toAmount} {exchangeSnapshot.toCurrency.code}</span>
                      </div>
                    </div>
                    <button className="status-modal-btn success-btn" onClick={() => setShowSuccessModal(false)}>
                      Done
                    </button>
                  </div>
                </div>
              </>
            )}

            {showFailModal && (
              <>
                <div className={`status-modal-overlay`} onClick={() => setShowFailModal(false)}/>
                <div className={`status-modal  fail-modal ${theme === "dark" ? "dark" : "light"}`}>
                  <div className="status-modal-content">
                    <div className="status-icon-wrapper fail-icon">
                      <AlertCircle size={48} />
                    </div>
                    <h3 className="status-modal-title">Exchange Failed</h3>
                    <p className="status-modal-message">
                      {errorMessage}
                    </p>
                    <div className="status-modal-actions">
                      <button className="status-modal-btn secondary-btn" onClick={() => setShowFailModal(false)}>
                        Cancel
                      </button>
                      <button className="status-modal-btn fail-btn" onClick={() => {
                        setShowFailModal(false);
                        handleExchange();
                      }}>
                        Try Again
                      </button>
                    </div>
                  </div>
                </div>
              </>
            )}

            <Footer theme={theme} />
          </div>
        </main>
        <MobileNav activeTab="exchange" onPlusClick={() => setIsDepositOpen(true)} />
        <DepositModal 
          isOpen={isDepositOpen} 
          onClose={() => setIsDepositOpen(false)} 
          theme={theme} 
        />
      </div>
    </>
  );
};

export default ExchangePage;