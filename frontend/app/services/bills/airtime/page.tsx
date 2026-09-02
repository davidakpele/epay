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
import { Smartphone, ChevronDown, Phone, CircleDollarSign, ChevronRight, Search } from 'lucide-react';
import "./Airtime.css"
import { providers } from '@/app/lib/BillsData';
import { countryCodes, getMaxPhoneLength, getPhonePlaceholder, isValidPhoneForCountry } from '@/app/lib/CountryCode';
import { Toast } from '@/app/types/auth';
import SupportChatBot from '@/components/SupportChatBot';

const networks = [
  { id: 'mtn', name: 'MTN', bg: '#FFC300', color: '#000', abbr: 'MTN' },
  { id: 'airtel', name: 'Airtel', bg: '#E8001C', color: '#fff', abbr: 'AIR' },
  { id: 'glo', name: 'Glo', bg: '#008000', color: '#fff', abbr: 'GLO' },
  { id: '9mobile', name: '9mobile', bg: '#006633', color: '#fff', abbr: '9MB' },
  { id: 'smile', name: 'Smile', bg: '#2ea613', color: '#fff', abbr: 'Smile' },
];

const quickAmounts = [100, 200, 500, 1000, 2000];

const AirtimePage = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [isPageLoading, setIsPageLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [isScrolling, setIsScrolling] = useState(false);
    const scrollTimer = useRef<NodeJS.Timeout | null>(null);
    const [selectedProvider, setSelectedProvider] = useState<string | null>(null);
    const [selectedNetwork, setSelectedNetwork] = useState(networks[0]);
    const [phone, setPhone] = useState('');
    const [amount, setAmount] = useState('');
    const [selectedAmount, setSelectedAmount] = useState<number | null>(null);
    const [selectedCountryCode, setSelectedCountryCode] = useState(countryCodes[0]);
    const [isCountryModalOpen, setIsCountryModalOpen] = useState(false);
    const [countrySearchTerm, setCountrySearchTerm] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [toasts, setToasts] = useState<Toast[]>([]);
    const [isChatOpen, setIsChatOpen] = useState(false);
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
        if (!selectedNetwork) {
            showToast('Please select a network', 'warning');
            return;
        }
        if (!phone || !isValidPhoneForCountry(phone, selectedCountryCode)) {
            showToast('Please enter a valid phone number', 'warning');
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
            showToast('Airtime purchased successfully!', 'success');
            setPhone('');
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
                    <span className="breadcrumb-current">Airtime</span>
                </div>

                {/* Page Title */}
                <h1 className="airtime-page-title">Buy Airtime</h1>

                {/* Main Card */}
                <div className="airtime-card">

                    {/* Left — Illustration */}
                    <div className="airtime-illustration-col">
                        <div className="airtime-illustration-img">
                            <Image
                            src="/assets/images/airtime-banner.png"
                            alt="Buy Airtime"
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

                  {/* Select Network */}
                    <div className="airtime-field">
                        <label className="airtime-label">
                            <span className="label-icon">📡</span> Select Network
                        </label>
                        <div className="airtime-select-box" onClick={() => setIsModalOpen(true)}>
                            <span
                            className="network-logo-pill"
                            style={{ background: selectedNetwork.bg, color: selectedNetwork.color }}
                            >
                            {selectedNetwork.abbr}
                            </span>
                            <span className="network-name">{selectedNetwork.name}</span>
                            <ChevronDown size={18} className="select-chevron" />
                        </div>
                    </div>

                    {/* Phone Number */}
                    <div className="airtime-field">
                        <label className="airtime-label">
                            <Phone size={14} /> Phone Number
                        </label>
                        <div className="airtime-phone-row">
                            <div className="phone-prefix" onClick={() => setIsCountryModalOpen(true)}>
                            <span>{selectedCountryCode.flag} {selectedCountryCode.code}</span>
                            <ChevronDown size={14} />
                            </div>
                            <input
                            type="tel"
                            className="airtime-input"
                            placeholder={getPhonePlaceholder(selectedCountryCode)}
                            value={phone}
                            maxLength={getMaxPhoneLength(selectedCountryCode)}
                            onChange={e => {
                                const value = e.target.value.replace(/\D/g, '');
                                setPhone(value);
                            }}
                            />
                        </div>
                        {/* Optional: Show validation hint */}
                        {phone && !isValidPhoneForCountry(phone, selectedCountryCode) && (
                            <span className="validation-error">
                            Invalid number for {selectedCountryCode.name}
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
                            'Buy Airtime'
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
                        {providers.map(provider => (
                        <div
                            key={provider.id}
                            className={`platform-card ${selectedProvider === provider.id ? 'platform-card-active' : ''}`}
                            onClick={() => {
                            setSelectedProvider(provider.id);
                            setSelectedNetwork(networks.find(n => n.id === provider.id) || selectedNetwork);
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
                    <div className="airtime-modal-overlay" onClick={() => setIsModalOpen(false)}>
                        <div className="airtime-modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="airtime-modal-header"><h3>Select Network</h3></div>
                        <div className="airtime-search-container">
                            <div className="airtime-search-container">
                                <i className="fa fa-search"></i>
                                <input type="text" placeholder="Search" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                                </div>
                        
                        </div>
                        <div
                            className={`airtime-country-list ${isScrolling ? 'is-scrolling' : ''}`}
                            onScroll={handleScroll}
                        >
                            {networks
                            .filter(n => n.name.toLowerCase().includes(searchTerm.toLowerCase()))
                            .map((n) => (
                                <div
                                key={n.id}
                                className="airtime-country-item"
                                onClick={() => {
                                    setSelectedNetwork(n);
                                    setIsModalOpen(false);
                                    setSearchTerm('');
                                }}
                                >
                                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                    <span
                                    className="network-logo-pill"
                                    style={{ background: n.bg, color: n.color }}
                                    >
                                    {n.abbr}
                                    </span>
                                    <span>{n.name}</span>
                                </div>
                                <div className={`airtime-radio-outer ${selectedNetwork.id === n.id ? 'checked' : ''}`}>
                                    <div className="airtime-radio-inner"></div>
                                </div>
                                </div>
                            ))}
                        </div>
                        </div>
                    </div>
                )}

                {isCountryModalOpen && (
                    <div className="airtime-modal-overlay" onClick={() => setIsCountryModalOpen(false)}>
                        <div className="airtime-modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="airtime-modal-header"><h3>Select Country Code</h3></div>
                        <div className="airtime-search-container">
                            <i className="fa fa-search"></i>
                            <input
                            type="text"
                            placeholder="Search country..."
                            value={countrySearchTerm}
                            onChange={(e) => setCountrySearchTerm(e.target.value)}/>
                        </div>
                        <div className={`airtime-country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                        {countryCodes
                            .filter(c =>
                                c.name.toLowerCase().includes(countrySearchTerm.toLowerCase()) ||
                                c.code.includes(countrySearchTerm)
                            )
                            .map((c) => (
                                <div
                                key={`${c.name}-${c.code}`}
                                className="airtime-country-item"
                                onClick={() => {
                                    setSelectedCountryCode(c);
                                    setIsCountryModalOpen(false);
                                    setCountrySearchTerm('');
                                    setPhone('');
                                }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flex: 1 }}>
                                    <span style={{ fontSize: '20px' }}>{c.flag}</span>
                                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                                    <span>{c.name}</span>
                                    <span style={{ color: '#888', fontSize: '11px' }}>
                                        e.g., {c.placeholder || '123 456 789'}
                                    </span>
                                    </div>
                                    <span style={{ color: '#888', fontSize: '12px', marginLeft: 'auto', marginRight:"-10" }}>{c.code}</span>
                                </div>
                                <div className={`airtime-radio-outer ${selectedCountryCode.name === c.name ? 'checked' : ''}`}>
                                    <div className="airtime-radio-inner"></div>
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

export default AirtimePage;