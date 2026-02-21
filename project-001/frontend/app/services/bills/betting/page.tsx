'use client';

import { useState, useEffect, useRef } from 'react';
import DepositModal from '@/components/DepositModal';
import Footer from '@/components/Footer';
import Header from '@/components/Header';
import MobileNav from '@/components/MobileNav';
import Sidebar from '@/components/Sidebar';
import LoadingScreen from '@/components/loader/Loadingscreen';
import Link from 'next/link';
import Image from 'next/image';
import { ChevronDown, ChevronRight, CircleDollarSign, Trophy } from 'lucide-react';
import './Betting.css';
import { Toast } from '@/app/types/auth';
import { BettingProviders } from '@/app/lib/BettingProvider';


const quickAmounts = [500, 1000, 2000, 5000, 10000];

const BettingPage = () => {
  const [isDepositOpen, setIsDepositOpen]       = useState(false);
  const [theme, setTheme]                       = useState<'light' | 'dark'>('light');
  const [isPageLoading, setIsPageLoading]       = useState(true);
  const [isSubmitting, setIsSubmitting]         = useState(false);
  const [toasts, setToasts]                     = useState<Toast[]>([]);

  // Provider modal
  const [isModalOpen, setIsModalOpen]           = useState(false);
  const [searchTerm, setSearchTerm]             = useState('');
  const [selectedProvider, setSelectedProvider] = useState<string | null>(null);
  const [selectedProvider2, setSelectedProvider2] = useState(BettingProviders[0]);

  // Form fields
  const [recipient, setRecipient]               = useState('');
  const [amount, setAmount]                     = useState('');
  const [selectedAmount, setSelectedAmount]     = useState<number | null>(null);

  // Scroll
  const [isScrolling, setIsScrolling]           = useState(false);
  const scrollTimer                             = useRef<NodeJS.Timeout | null>(null);

  // ─── Helpers ────────────────────────────────────────────────────────────────

  const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
    setToasts((prev) => {
      if (prev.length >= 5) return prev;
      const id = Date.now();
      const newToast: Toast = { id, message: msg, type, exiting: false };
      setTimeout(() => {
        setToasts((cur) => cur.map((t) => (t.id === id ? { ...t, exiting: true } : t)));
        setTimeout(() => setToasts((cur) => cur.filter((t) => t.id !== id)), 300);
      }, 3000);
      return [...prev, newToast];
    });
  };

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

  const handleAmountSelect = (val: number) => {
    setSelectedAmount(val);
    setAmount(String(val));
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value.replace(/,/g, '').replace(/\D/g, '');
    setAmount(raw);
    setSelectedAmount(null);
  };

  const getFormattedAmount = () => {
    if (!amount) return '';
    try {
      return BigInt(amount).toLocaleString('en-US');
    } catch {
      return amount;
    }
  };

  // ─── Submit ──────────────────────────────────────────────────────────────────

  const handleSubmit = async () => {
    if (!selectedProvider2) {
      showToast('Please select a betting provider', 'warning');
      return;
    }
    if (!recipient || recipient.trim().length < 3) {
      showToast('Please enter a valid user ID / account number', 'warning');
      return;
    }
    if (!amount || Number(amount) <= 0) {
      showToast('Please enter a valid amount', 'warning');
      return;
    }
    if (Number(amount) < 100) {
      showToast('Minimum amount is ₦100', 'warning');
      return;
    }

    try {
      setIsSubmitting(true);
      await new Promise((res) => setTimeout(res, 2000)); // replace with real API call
      showToast(`₦${Number(amount).toLocaleString()} funded to ${selectedProvider2.name} successfully!`, 'success');
      setRecipient('');
      setAmount('');
      setSelectedAmount(null);
    } catch {
      showToast('Funding failed. Please try again.', 'warning');
    } finally {
      setIsSubmitting(false);
    }
  };

  // ─── Effects ─────────────────────────────────────────────────────────────────

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

          {/* ── Toasts ── */}
          <div className="toastrs">
            {toasts.map((toast) => (
              <div
                key={toast.id}
                className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}
              >
                <div className="toast-icon">
                  <i
                    className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`}
                    aria-hidden="true"
                  />
                </div>
                <div className="toast-message">{toast.message}</div>
              </div>
            ))}
          </div>

          <div className="main-container">

            {/* ── Breadcrumb ── */}
            <div className="airtime-breadcrumb">
              <Link href="/dashboard" className="breadcrumb-link">Dashboard</Link>
              <ChevronRight size={14} className="breadcrumb-sep" />
              <span className="breadcrumb-current">Betting</span>
            </div>

            {/* ── Page Title ── */}
            <h1 className="airtime-page-title">Fund Betting Account</h1>

            {/* ── Main Card ── */}
            <div className="airtime-card">

              {/* Left — Illustration */}
              <div className="airtime-illustration-col">
                <div className="airtime-illustration-img">
                  <Image
                    src="/assets/images/betting-background.png"
                    alt="Fund Betting Account"
                    width={280}
                    height={300}
                    priority
                  />
                  <div className="airtime-promo-banner">
                    <span>🏆</span>
                    <div>
                      <p className="promo-text">
                        Fund your account instantly — <strong>no hidden charges!</strong>
                      </p>
                      <a href="#" className="promo-link">Learn more</a>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right — Form */}
              <div className="airtime-form-col">

                {/* Select Provider */}
                <div className="airtime-field">
                  <label className="airtime-label">
                    <Trophy size={14} /> Select Betting Provider
                  </label>
                  <div className="airtime-select-box" onClick={() => setIsModalOpen(true)}>
                    <div
                      className="platform-logo-img"
                      style={{ width: '32px', height: '32px', borderRadius: '8px', flexShrink: 0 }}
                    >
                      <Image
                        src={selectedProvider2.logo.replace('./', '/')}
                        alt={selectedProvider2.name}
                        width={32}
                        height={32}
                        style={{ objectFit: 'contain', width: '100%', height: '100%' }}
                      />
                    </div>
                    <span className="network-name">{selectedProvider2.name}</span>
                    <ChevronDown size={18} className="select-chevron" />
                  </div>
                </div>

                {/* User ID / Account Number */}
                <div className="airtime-field">
                  <label className="airtime-label">
                    <span>🎯</span> User ID / Account Number
                  </label>
                  <input
                    type="text"
                    className="airtime-input"
                    placeholder={`Enter your ${selectedProvider2.name} user ID`}
                    value={recipient}
                    onChange={(e) => setRecipient(e.target.value)}
                  />
                  {recipient && recipient.trim().length < 3 && (
                    <span className="validation-error">
                      Please enter a valid user ID or account number
                    </span>
                  )}
                </div>

                {/* Amount */}
                <div className="airtime-field">
                  <label className="airtime-label">
                    <CircleDollarSign size={14} /> Amount
                  </label>
                  <div className="airtime-quick-amounts">
                    {quickAmounts.map((q) => (
                      <button
                        key={q}
                        className={`quick-amount-btn ${selectedAmount === q ? 'selected' : ''}`}
                        onClick={() => handleAmountSelect(q)}
                      >
                        ₦{q.toLocaleString()}
                      </button>
                    ))}
                  </div>
                  <input
                    type="text"
                    className="airtime-input amount-input"
                    placeholder="Or enter custom amount"
                    value={getFormattedAmount()}
                    onChange={handleAmountChange}
                  />
                </div>

                {/* Fund Button */}
                <button
                  className="airtime-buy-btn"
                  onClick={handleSubmit}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <span className="btn-loader-row">
                      <span className="btn-spinner" />
                      Processing...
                    </span>
                  ) : (
                    'Fund Account'
                  )}
                </button>

                <p className="airtime-cashback-note">
                  Instant funding — available 24/7.{' '}
                  <a href="#">Learn more</a>
                </p>
              </div>
            </div>

            {/* ── Common Platforms ── */}
            <div className="airtime-platforms">
              <h3 className="platforms-title">Common Platforms</h3>
              <div className="platforms-grid">
                {BettingProviders.map((provider) => (
                  <div
                    key={provider.id}
                    className={`platform-card ${selectedProvider === provider.id ? 'platform-card-active' : ''}`}
                    onClick={() => {
                      setSelectedProvider(provider.id);
                      setSelectedProvider2(
                        BettingProviders.find((p) => p.id === provider.id) || selectedProvider2
                      );
                    }}
                  >
                    <div className="platform-logo-img">
                      <Image
                        src={provider.logo.replace('./', '/')}
                        alt={provider.name}
                        width={64}
                        height={64}
                        style={{ objectFit: 'contain' }}
                      />
                    </div>
                    <span className="platform-name">{provider.name}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* ── Provider Modal ── */}
            {isModalOpen && (
              <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header"><h3>Select Betting Provider</h3></div>
                  <div className="search-container">
                    <i className="fa fa-search" />
                    <input
                      type="text"
                      placeholder="Search provider..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                    />
                  </div>
                  <div
                    className={`country-list ${isScrolling ? 'is-scrolling' : ''}`}
                    onScroll={handleScroll}
                  >
                    {BettingProviders
                      .filter((p) => p.name.toLowerCase().includes(searchTerm.toLowerCase()))
                      .map((p) => (
                        <div
                          key={p.id}
                          className="country-item"
                          onClick={() => {
                            setSelectedProvider2(p);
                            setSelectedProvider(p.id);
                            setIsModalOpen(false);
                            setSearchTerm('');
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <div
                              className="platform-logo-img"
                              style={{ width: '36px', height: '36px', borderRadius: '8px', flexShrink: 0 }}
                            >
                              <Image
                                src={p.logo.replace('./', '/')}
                                alt={p.name}
                                width={36}
                                height={36}
                                style={{ objectFit: 'contain', width: '100%', height: '100%' }}
                              />
                            </div>
                            <span>{p.name}</span>
                          </div>
                          <div className={`radio-outer ${selectedProvider2.id === p.id ? 'checked' : ''}`}>
                            <div className="radio-inner" />
                          </div>
                        </div>
                      ))}
                  </div>
                </div>
              </div>
            )}

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
    </div>
  );
};

export default BettingPage;