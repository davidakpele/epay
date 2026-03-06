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
import { Building2, ChevronRight, CreditCard,  Grip, ShoppingBag, ShoppingCart, Wallet } from 'lucide-react';
import './Shopping.css';
import { Toast } from '@/app/types/auth';
import { shoppingProviders } from '@/app/lib/ImageProvider';

const paymentOptions = [
  { id: 'wallet',   label: 'Wallet Balance', icon: <Wallet style={{color:"var(--bg-main)"}}/> },
  { id: 'bank',     label: 'Bank Transfer',  icon: <Building2 style={{color:"var(--warning-color)"}}/> },
  { id: 'card',     label: 'Card Payment',   icon: <CreditCard style={{color:"var(--bg-input-border)"}}/>},
];

const quickAmounts = [5000, 10000, 20000];

const ShoppingBill = () => {
  const [isDepositOpen, setIsDepositOpen]         = useState(false);
  const [theme, setTheme]                         = useState<'light' | 'dark'>('light');
  const [isPageLoading, setIsPageLoading]         = useState(true);
  const [isSubmitting, setIsSubmitting]           = useState(false);
  const [toasts, setToasts]                       = useState<Toast[]>([]);

  // Form
  const [orderId, setOrderId]                     = useState('');
  const [selectedPayment, setSelectedPayment]     = useState<string | null>(null);
  const [amount, setAmount]                       = useState('');
  const [selectedAmount, setSelectedAmount]       = useState<number | null>(null);
  const [selectedProvider, setSelectedProvider]   = useState<string | null>(null);

  // ─── Helpers ─────────────────────────────────────────────────────────────────

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
    if (!orderId || orderId.trim().length < 3) {
      showToast('Please enter a valid Order ID', 'warning');
      return;
    }
    if (!selectedPayment) {
      showToast('Please select a payment option', 'warning');
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
      showToast(`Order ${orderId} paid successfully via ${paymentOptions.find(p => p.id === selectedPayment)?.label}!`, 'success');
      setOrderId('');
      setAmount('');
      setSelectedAmount(null);
      setSelectedPayment(null);
    } catch {
      showToast('Payment failed. Please try again.', 'warning');
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
            <div className="shopping-breadcrumb">
              <Link href="/dashboard" className="breadcrumb-link">Dashboard</Link>
              <ChevronRight size={14} className="breadcrumb-sep" />
              <span className="breadcrumb-current">Shopping</span>
            </div>

            {/* ── Page Title ── */}
            <h1 className="shopping-page-title">Shopping & Payment</h1>

            {/* ── Main Card ── */}
            <div className="shopping-card">

              {/* Left — Illustration */}
              <div className="shopping-illustration-col">
                <div className="shopping-illustration-img">
                  <Image
                    src="/assets/images/shopping-banner.png"
                    alt="Shopping & Payment"
                    width={280}
                    height={300}
                    priority
                  />
                  <div className="shopping-promo-banner">
                    <span>🛍️</span>
                    <div>
                      <p className="promo-text">
                        Shop from top stores and <strong>pay instantly</strong> from your wallet!
                      </p>
                      <a href="#" className="promo-link">Learn more</a>
                    </div>
                  </div>
                </div>
              </div>

              {/* Right — Form */}
              <div className="shopping-form-col">

                {/* Section header */}
                <div className="shop-form-header">
                  <ShoppingCart size={18} />
                  <span>Place New Order</span>
                </div>

                {/* Order ID */}
                <div className="shopping-field">
                  <label className="shopping-label">
                    Order ID
                  </label>
                  <input
                    type="text"
                    className="shopping-input"
                    placeholder="Enter Order ID (e.g. 123456789)"
                    value={orderId}
                    onChange={(e) => setOrderId(e.target.value)}
                  />
                  {orderId && orderId.trim().length < 3 && (
                    <span className="validation-error">Please enter a valid Order ID</span>
                  )}
                </div>

                {/* Payment Option */}
                <div className="shopping-field">
                  <label className="shopping-label">
                    Payment Option
                  </label>
                  <div className="payment-options-row">
                    {paymentOptions.map((opt) => (
                      <button
                        key={opt.id}
                        className={`payment-option-btn ${selectedPayment === opt.id ? 'selected' : ''}`}
                        onClick={() => setSelectedPayment(opt.id)}
                      >
                        <span>{opt.icon}</span>
                        <span>{opt.label}</span>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Amount */}
                <div className="shopping-field">
                  <label className="shopping-label"> Amount</label>
                  <div className="shop-page-amount-input-row">
                    <span className="naira-prefix">₦</span>
                    <input
                      type="text"
                      className="shopping-input shop-page-amount-input"
                      placeholder="Enter amount (₦)"
                      value={getFormattedAmount()}
                      onChange={handleAmountChange}
                    />
                  </div>
                  <div className="shopping-quick-amounts">
                    {quickAmounts.map((q) => (
                      <button
                        key={q}
                        className={`quick-amount-btn ${selectedAmount === q ? 'selected' : ''}`}
                        onClick={() => handleAmountSelect(q)}
                      >
                        ₦{q.toLocaleString()}
                      </button>
                    ))}
                    <button
                      className={`quick-amount-btn ${!selectedAmount && amount && !quickAmounts.includes(Number(amount)) ? 'selected' : ''}`}
                      onClick={() => { setSelectedAmount(null); setAmount(''); }}
                    >
                      Other
                    </button>
                  </div>
                </div>

                {/* Pay Button */}
                <button
                  className="shopping-buy-btn shop-pay-btn"
                  onClick={handleSubmit}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <span className="btn-loader-row">
                      <span className="btn-spinner" />
                      Processing...
                    </span>
                  ) : (
                    <span className="btn-loader-row">
                      <ShoppingBag size={18} />
                      Pay for Order
                    </span>
                  )}
                </button>
              </div>
            </div>

            {/* ── Available Providers ── */}
            <div className="shop-providers">
              <h3 className="shop-providers-title">
                Available Providers <span className="title-line" />
              </h3>
              <div className="shop-providers-grid">
                {shoppingProviders.map((provider) => (
                  <div
                    key={provider.id}
                    className={`shop-provider-card ${selectedProvider === provider.id ? 'shop-provider-active' : ''}`}
                    onClick={() => setSelectedProvider(provider.id)}
                  >
                    <Image
                      src={provider.logo}
                      alt={provider.name}
                      width={120}
                      height={40}
                      style={{ objectFit: 'contain', maxWidth: '100%', height: 'auto' }}
                    />
                  </div>
                ))}
                <div className="shop-provider-card shop-more-stores">
                  <div className="more-stores-icon"><Grip/></div>
                  <span>More Stores</span>
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
    </div>
  );
};

export default ShoppingBill;