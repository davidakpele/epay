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
import { ChevronDown, CircleDollarSign, ChevronRight, Tv } from 'lucide-react';
import './Cabletv.css';
import { Toast } from '@/app/types/auth';
import { CableProviders, DSTV_OPTIONS, GOTV_OPTIONS, STARTIME_OPTIONS } from '@/app/lib/CableService';

const CableSubscription = () => {
  const [isDepositOpen, setIsDepositOpen]     = useState(false);
  const [theme, setTheme]                     = useState<'light' | 'dark'>('light');
  const [isPageLoading, setIsPageLoading]     = useState(true);
  const [isSubmitting, setIsSubmitting]       = useState(false);
  const [toasts, setToasts]                   = useState<Toast[]>([]);

  // Provider modal
  const [isModalOpen, setIsModalOpen]         = useState(false);
  const [searchTerm, setSearchTerm]           = useState('');
  const [selectedProvider, setSelectedProvider]   = useState<string | null>(null);
  const [selectedProvider2, setSelectedProvider2] = useState(CableProviders[0]);

  // Plan modal
  const [isPlanModalOpen, setIsPlanModalOpen] = useState(false);
  const [planSearchTerm, setPlanSearchTerm]   = useState('');
  const [selectedPlan, setSelectedPlan]       = useState<{ name: string; price: number; value: string } | null>(null);

  // Form fields
  const [smartCardNumber, setSmartCardNumber] = useState('');

  // Scroll
  const [isScrolling, setIsScrolling]         = useState(false);
  const scrollTimer                           = useRef<NodeJS.Timeout | null>(null);

  // ─── Helpers ────────────────────────────────────────────────────────────────

  const getPlansForProvider = () => {
    switch (selectedProvider2.id) {
      case 'dstv':      return DSTV_OPTIONS;
      case 'Gotv':      return GOTV_OPTIONS;
      case 'startimes': return STARTIME_OPTIONS;
      default:          return [];
    }
  };

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

  // ─── Submit ──────────────────────────────────────────────────────────────────

  const handleSubmit = async () => {
    if (!selectedProvider2) {
      showToast('Please select a provider', 'warning');
      return;
    }
    if (!selectedPlan) {
      showToast('Please select a plan', 'warning');
      return;
    }
    if (!smartCardNumber || smartCardNumber.length < 6) {
      showToast('Please enter a valid smart card / IUC number', 'warning');
      return;
    }
    try {
      setIsSubmitting(true);
      await new Promise((res) => setTimeout(res, 2000)); // replace with real API call
      showToast(`${selectedProvider2.name} — ${selectedPlan.name} subscription successful!`, 'success');
      setSmartCardNumber('');
      setSelectedPlan(null);
    } catch {
      showToast('Subscription failed. Please try again.', 'warning');
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
          <div className="main-container">
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
            {/* ── Breadcrumb ── */}
            <div className="cabletv-breadcrumb">
              <Link href="/dashboard" className="breadcrumb-link">Dashboard</Link>
              <ChevronRight size={14} className="breadcrumb-sep" />
              <span className="breadcrumb-current">Cable TV</span>
            </div>

            {/* ── Page Title ── */}
            <h1 className="cabletv-page-title">Cable TV Subscription</h1>

            {/* ── Main Card ── */}
            <div className="cabletv-card">

              {/* Left — Illustration */}
              <div className="cabletv-illustration-col">
                <div className="cabletv-illustration-img">
                  <Image
                    src="/assets/images/cabletv-banner.png"
                    alt="Cable TV Subscription"
                    width={280}
                    height={300}
                    priority
                  />
                  <div className="cabletv-promo-banner">
                    <span>🎁</span>
                    <div>
                      <p className="promo-text">
                        Earn up to <strong>3% cashback</strong> on every subscription!
                      </p>
                      <a href="#" className="promo-link">Learn more</a>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right — Form */}
              <div className="cabletv-form-col">

                {/* Select Provider */}
                <div className="cabletv-field">
                  <label className="cabletv-label">
                    <Tv size={14} /> Select Provider
                  </label>
                  <div className="cabletv-select-box" onClick={() => setIsModalOpen(true)}>
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

                {/* Select Plan */}
                <div className="cabletv-field">
                  <label className="cabletv-label">
                    <CircleDollarSign size={14} /> Select Plan
                  </label>
                  <div className="cabletv-select-box" onClick={() => setIsPlanModalOpen(true)}>
                    <span
                      className="network-name"
                      style={{ color: selectedPlan ? '#333' : '#bbb' }}
                    >
                      {selectedPlan
                        ? `${selectedPlan.name} — ₦${selectedPlan.price.toLocaleString()}`
                        : 'Select a plan'}
                    </span>
                    <ChevronDown size={18} className="select-chevron" />
                  </div>
                </div>
                 {/* Smart Card Number */}
                <div className="cabletv-field">
                  <label className="cabletv-label">
                    <span>💳</span> Smart Card / IUC Number
                  </label>
                  <input
                    type="text"
                    className="cabletv-input"
                    placeholder="Enter smart card number"
                    value={smartCardNumber}
                    onChange={(e) => setSmartCardNumber(e.target.value.replace(/\D/g, ''))}
                  />
                  {smartCardNumber && smartCardNumber.length < 6 && (
                    <span className="validation-error">
                      Please enter a valid smart card number
                    </span>
                  )}
                </div>

                {/* Amount (read-only, populated from plan) */}
                {selectedPlan && (
                  <div className="cabletv-field">
                    <label className="cabletv-label">Amount</label>
                    <div className="cabletv-input amount-display">
                      ₦ {selectedPlan.price.toLocaleString('en-NG', { minimumFractionDigits: 2 })}
                    </div>
                  </div>
                )}

                {/* Subscribe Button */}
                <button
                  className="cabletv-buy-btn"
                  onClick={handleSubmit}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <span className="btn-loader-row">
                      <span className="btn-spinner" />
                      Processing...
                    </span>
                  ) : (
                    'Subscribe Now'
                  )}
                </button>

                <p className="cabletv-cashback-note">
                  Earn up to 3% cashback on subscriptions!{' '}
                  <a href="#">Learn more</a>
                </p>
              </div>
            </div>

            {/* ── Common Platforms ── */}
            <div className="cabletv-platforms">
              <h3 className="platforms-title">Common Platforms</h3>
              <div className="platforms-grid">
                {CableProviders.map((provider) => (
                  <div
                    key={provider.id}
                    className={`platform-card ${selectedProvider === provider.id ? 'platform-card-active' : ''}`}
                    onClick={() => {
                      setSelectedProvider(provider.id);
                      setSelectedProvider2(
                        CableProviders.find((p) => p.id === provider.id) || selectedProvider2
                      );
                      setSelectedPlan(null); // reset plan on provider switch
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
              <div className="cable-modal-overlay" onClick={() => setIsModalOpen(false)}>
                <div className="cable-modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="cable-modal-header"><h3>Select Provider</h3></div>
                  <div className="cable-search-container">
                    <i className="fa fa-search" />
                    <input
                      type="text"
                      placeholder="Search provider..."
                      value={searchTerm}
                      onChange={(e) => setSearchTerm(e.target.value)}
                    />
                  </div>
                  <div
                    className={`cable-country-list ${isScrolling ? 'is-scrolling' : ''}`}
                    onScroll={handleScroll}
                  >
                    {CableProviders
                      .filter((p) => p.name.toLowerCase().includes(searchTerm.toLowerCase()))
                      .map((p) => (
                        <div
                          key={p.id}
                          className="cable-country-item"
                          onClick={() => {
                            setSelectedProvider2(p);
                            setSelectedProvider(p.id);
                            setSelectedPlan(null);
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
                          <div className={`cable-radio-outer ${selectedProvider2.id === p.id ? 'checked' : ''}`}>
                            <div className="cable-radio-inner" />
                          </div>
                        </div>
                      ))}
                  </div>
                </div>
              </div>
            )}

            {/* ── Plan Modal ── */}
            {isPlanModalOpen && (
              <div className="cable-modal-overlay" onClick={() => setIsPlanModalOpen(false)}>
                <div className="cable-modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="cable-modal-header">
                    <h3>Select Plan — {selectedProvider2.name}</h3>
                  </div>
                  <div className="cable-search-container">
                    <i className="fa fa-search" />
                    <input
                      type="text"
                      placeholder="Search plan..."
                      value={planSearchTerm}
                      onChange={(e) => setPlanSearchTerm(e.target.value)}
                    />
                  </div>
                  <div
                    className={`cable-country-list ${isScrolling ? 'is-scrolling' : ''}`}
                    onScroll={handleScroll}
                  >
                    {getPlansForProvider()
                      .filter((p) => p.name.toLowerCase().includes(planSearchTerm.toLowerCase()))
                      .map((p) => (
                        <div
                          key={p.value}
                          className="cable-country-item"
                          onClick={() => {
                            setSelectedPlan(p);
                            setIsPlanModalOpen(false);
                            setPlanSearchTerm('');
                          }}
                        >
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                            <span style={{ fontSize: '13px', fontWeight: 600 }}>{p.name}</span>
                            <span style={{ fontSize: '12px', color: 'var(--bg-main)' }}>
                              ₦{p.price.toLocaleString('en-NG', { minimumFractionDigits: 2 })}
                            </span>
                          </div>
                          <div className={`cable-radio-outer ${selectedPlan?.value === p.value ? 'checked' : ''}`}>
                            <div className="cable-radio-inner" />
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

export default CableSubscription;