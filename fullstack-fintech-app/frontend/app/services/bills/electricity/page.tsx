'use client';

import { useState, useEffect, useRef } from 'react';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import LoadingScreen from '@/components/loader/Loadingscreen';
import Link from 'next/link';
import Image from 'next/image';
import { ChevronDown,CircleDollarSign, ChevronRight, Search } from 'lucide-react';
import "./Electricity.css"
import { Toast } from '@/app/types/auth';
import { ElectricyProviders } from '@/app/lib/ElectricityProvider';

const networks = [
  { id: 'mtn', name: 'MTN', bg: '#FFC300', color: '#000', abbr: 'MTN' },
  { id: 'airtel', name: 'Airtel', bg: '#E8001C', color: '#fff', abbr: 'AIR' },
  { id: 'glo', name: 'Glo', bg: '#008000', color: '#fff', abbr: 'GLO' },
  { id: '9mobile', name: '9mobile', bg: '#006633', color: '#fff', abbr: '9MB' },
  { id: 'smile', name: 'Smile', bg: '#2ea613', color: '#fff', abbr: 'Smile' },
];

const quickAmounts = [5000, 10000, 20000, 30000, 50000];

const ElectricitySubscription = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [isPageLoading, setIsPageLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [isScrolling, setIsScrolling] = useState(false);
    const scrollTimer = useRef<NodeJS.Timeout | null>(null);
    const [selectedProvider, setSelectedProvider] = useState<string | null>(null);
    const [selectedProvider2, setSelectedProvider2] = useState(ElectricyProviders[0]);
    const [recipient, setRecipient] = useState('');
    const [amount, setAmount] = useState('');
    const [selectedAmount, setSelectedAmount] = useState<number | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [toasts, setToasts] = useState<Toast[]>([]);
    
    const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
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
            }, 3000);

            return [...prev, newToast];
        });
    };

    useEffect(() => {
        const loadingTimer = setTimeout(() => {
          setIsPageLoading(false);
        }, 2000);
    
        return () => clearTimeout(loadingTimer);
      }, []);

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
    
    const handleSubmit = async () => {
        if (!selectedProvider2) {
          showToast('Please select a provider', 'warning');
          return;
        }
        if (!recipient || recipient.length < 6) {
          showToast('Please enter a valid meter/account number', 'warning');
          return;
        }
        if (!amount || Number(amount) <= 0) {
            showToast('Please enter a valid amount', 'warning');
            return;
        }
        if (Number(amount) < 50) {
            showToast('Minimum amount is ₦50', 'warning');
            return;
        }

        try {
            setIsSubmitting(true);
            // await your API call here e.g: await airtimeService.buy(...)
            await new Promise(res => setTimeout(res, 2000)); // remove this dummy await
            showToast('Data Bundle purchased successfully!', 'success');
            setRecipient('');
            setAmount('');
            setSelectedAmount(null);
        } catch (err) {
            showToast('Purchase failed. Please try again.', 'warning');
        } finally {
            setIsSubmitting(false);
        }
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

    const handleScroll = () => {
        setIsScrolling(true);
        if (scrollTimer.current) clearTimeout(scrollTimer.current);
        scrollTimer.current = setTimeout(() => {
        setIsScrolling(false);
        }, 1000);
    };

    if (isPageLoading) return <LoadingScreen />;

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
                {/* Breadcrumb */}
                <div className="airtime-breadcrumb">
                    <Link href="/dashboard" className="breadcrumb-link">
                    Dashboard
                    </Link>
                    <ChevronRight size={14} className="breadcrumb-sep" />
                    <span className="breadcrumb-current">Pay Electricity Bill</span>
                </div>

                {/* Page Title */}
                <h1 className="airtime-page-title">Pay Electricity Bill</h1>

                {/* Main Card */}
                <div className="airtime-card">

                    {/* Left — Illustration */}
                    <div className="airtime-illustration-col">
                        <div className="airtime-illustration-img">
                            <Image
                            src="/assets/images/electricity_bill.png"
                            alt="Buy Data Bundle"
                            width={280}
                            height={300}
                            priority
                            />
                            {/* Banner overlaid ON TOP of image */}
                            <div className="airtime-promo-banner">
                            <span>🎁</span>
                            <div>
                                <p className="promo-text">Earn up to <strong>3% cashback</strong> when you buy airtime!</p>
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
                      <span className="label-icon">⚡</span> Select Provider
                    </label>
                    <div className="airtime-select-box" onClick={() => setIsModalOpen(true)}>
                      <div className="platform-logo-img" style={{ width: '32px', height: '32px', borderRadius: '8px', flexShrink: 0 }}>
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

                    <div className="airtime-field">
                      <label className="airtime-label">
                      Meter / Account Number
                      </label>
                      <input
                        type="text"
                        className="airtime-input"
                        placeholder="Enter meter or account number"
                        value={recipient}
                        onChange={e => setRecipient(e.target.value.replace(/\D/g, ''))}
                      />
                      {recipient && recipient.length < 6 && (
                        <span className="validation-error">
                          Please enter a valid meter/account number
                        </span>
                      )}
                    </div>

                    {/* Amount */}
                    <div className="airtime-field">
                        <label className="airtime-label">
                        <CircleDollarSign size={14} /> Amount
                        </label>
                        <div className="airtime-quick-amounts">
                        {quickAmounts.map(q => (
                            <button
                            key={q}
                            className={`quick-amount-btn ${selectedAmount === q ? 'selected' : ''}`}
                            onClick={() => handleAmountSelect(q)}
                            >
                            ₦ {q.toLocaleString()}
                            </button>
                        ))}
                        </div>
                        <input
                            type="text"
                            className="airtime-input page-amount-input"
                            placeholder="Or enter custom amount"
                            value={getFormattedAmount()}
                            onChange={handleAmountChange}
                            />
                    </div>

                    {/* Buy Button */}
                    <button
                        className="airtime-buy-btn"
                        onClick={handleSubmit}
                        disabled={isSubmitting}
                        >
                        {isSubmitting ? (
                            <span className="btn-loader-row">
                            <span className="btn-spinner"></span>
                            Processing...
                            </span>
                        ) : (
                            'Pay Electricity Bill'
                        )}
                    </button>

                    <p className="airtime-cashback-note">
                        Earn up to 3% cashback when you buy airtime! <a href="#">Learn more</a>
                    </p>
                    </div>
                </div>

                {/* Common Platforms */}
                <div className="airtime-platforms">
                    <h3 className="platforms-title">Common Platforms</h3>
                    <div className="platforms-grid">
                        {ElectricyProviders.map(provider => (
                        <div
                          key={provider.id}
                          className={`platform-card ${selectedProvider === provider.id ? 'platform-card-active' : ''}`}
                          onClick={() => {
                            setSelectedProvider(provider.id);
                            setSelectedProvider2(provider); 
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

                 {isModalOpen && (
                    <div className="eletricity-modal-overlay" onClick={() => setIsModalOpen(false)}>
                      <div className="eletricity-modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="eletricity-modal-header"><h3>Select Provider</h3></div>
                        <div className="eletricity-search-container">
                          <i className="fa fa-search"></i>
                          <input
                            type="text"
                            placeholder="Search provider..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                          />
                        </div>
                        <div
                          className={`eletricity-country-list ${isScrolling ? 'is-scrolling' : ''}`}
                          onScroll={handleScroll}>
                          {ElectricyProviders
                            .filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()))
                            .map((p) => (
                              <div
                                key={p.id}
                                className="eletricity-country-item"
                                onClick={() => {
                                  setSelectedProvider2(p);
                                  setSelectedProvider(p.id);
                                  setIsModalOpen(false);
                                  setSearchTerm('');
                                }}
                              >
                                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                  <div className="platform-logo-img" style={{ width: '36px', height: '36px', borderRadius: '8px', flexShrink: 0 }}>
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
                                <div className={`eletricity-radio-outer ${selectedProvider2.id === p.id ? 'checked' : ''}`}>
                                  <div className="eletricity-radio-inner"></div>
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
      <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
    </div>
  );
};

export default ElectricitySubscription;