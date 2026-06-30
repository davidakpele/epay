'use client';

import React, { useState, useEffect, useRef, useCallback, Suspense } from 'react';
import { useRouter } from 'next/navigation';
import { 
  Eye, EyeOff, Plus, ArrowDownLeft, 
  CreditCard, Repeat, Search, User2, 
  LucideProps, Smartphone, Wifi, Tv, 
  Lightbulb, Hospital, Trophy, Plane, ShoppingBag, Hand 
} from 'lucide-react';
import './Dashboard.css';
import Sidebar from '@/components/Sidebar';
import { Currency } from '../types/api';
import Header from '@/components/Header';
import News from '@/components/News';
import History from '@/components/History';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import WithdrawModal from '@/components/WithdrawModal';
import LoadingScreen from '@/components/loader/Loadingscreen';
import { getFiat, getToken, getUserId, setActiveWallet, setFiat, setWalletContainer, walletService, historyService, userService, capitalizeFirstLetter, getUserDetails, configService, getUserFullName, calculateProfileCompletion, getHasSeenMetaMap, updateHasSeenMetaMap } from '../api';
import { eventEmitter } from '../utils/eventEmitter';
import { UserSettings } from '../types/utils';
import { Toast } from '../types/auth';
import WelcomeModal from '@/components/WelcomeModal';
import SupportChatBot from '@/components/SupportChatBot';

const Dashboard = () => {
  const [loading, setLoading] = useState(false);
  const [isChatOpen, setIsChatOpen] = useState(false);
  
  const [error, setError] = useState('');
  const [wallet, setWallet] = useState<any>(null);
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [selectedCurrency, setSelectedCurrency] = useState<Currency | null>(null);
  const sliderRef = useRef<HTMLElement>(null);
  const [activeDot, setActiveDot] = useState(0);  
  const [historyData, setHistoryData] = useState<any[]>([]);
  const [historyKey, setHistoryKey] = useState(0);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [showBalance, setShowBalance] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [isScrolling, setIsScrolling] = useState(false);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [showTierUpgradeModal, setShowTierUpgradeModal] = useState(false);
  const scrollTimer = useRef<NodeJS.Timeout | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [userProfile, setUserProfile] = useState<any>(null);
  const [profileImage, setProfileImage] = useState('/assets/images/user-profile.jpg');
  const router = useRouter();
  const user_details = getUserDetails();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    gender: '',
    telephone: '',
    dob: '',
    email: ''
  });

  const [settings, setSettings] = useState<UserSettings>({
    profile: {
      fullName: '',
      email: '',
      phone: '',
      username: '',
      profileImage: '/assets/images/user-profile.jpg'
    },
    security: {
      twoFactorEnabled: false,
      biometricEnabled: false,
      sessionTimeout: 30
    },
    notifications: {
      email: true,
      push: true,
      sms: false,
      transactionAlerts: true,
      loginAlerts: true,
      marketingEmails: false
    },
    preferences: {
      language: 'English',
      currency: 'NGN',
      theme: 'light',
      timezone: 'Africa/Lagos'
    }
  });

  const fetchHistory = useCallback(async () => {
      const userId = getUserId();
      if (!userId) return;
      
      const response = await historyService.getHistory(userId);
      setHistoryData(response);
      setHistoryKey(prev => prev + 1);
  }, []);

  const refreshBalance = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const token = getToken();
      const userId = getUserId();
      
      if (!token || !userId) {
        setError('Please login to view wallet');
        setLoading(false);
        return;
      }
      
      const response = await walletService.getByUserId(userId, token);
      if (!response || response.status === 401 || response.status === 500) {
        setError('Failed to fetch wallet data');
        setLoading(false);
        router.push('/auth/logout');
        return;
      }
      setWallet(response);
      
      if (response && response.wallet_balances) {
        const apiCurrencies: Currency[] = response.wallet_balances.map((balance: any) => {
          const currencyName = balance.currency_code;
          return {
            name: currencyName,
            code: balance.currency_code,
            symbol: balance.symbol
          };
        });
        
        setCurrencies(apiCurrencies);
        if (getFiat() !== '' && getFiat() != null) {
          const fiatCurrency = apiCurrencies.find(c => c.code === getFiat());
          if (fiatCurrency) {
            setSelectedCurrency(fiatCurrency);
          }
        }
        else {
          if (!selectedCurrency && apiCurrencies.length > 0) {
            const defaultCurrency = apiCurrencies.find(c => c.code === 'NGN') || apiCurrencies[0];
            setSelectedCurrency(defaultCurrency);
            setActiveWallet(defaultCurrency.code);
            setFiat(defaultCurrency.code);
          }
        }
          
        setWalletContainer(response.wallet_balances, response.hasTransferPin, response.walletId);
      }
    } catch (e) {
      console.log(e);
    } finally {
      setLoading(false);
    }
  }, []); 

  const handleTransactionSuccess = useCallback(async () => {
    await refreshBalance();
    await fetchHistory();
  }, [refreshBalance, fetchHistory]);

  useEffect(() => {
    document.title = 'Dashboard - ePay Online Business Banking';
    const handleBalanceRefresh = () => {
      refreshBalance();
    };

    eventEmitter.on('refreshBalance', handleBalanceRefresh);
    
    return () => {
      eventEmitter.off('refreshBalance', handleBalanceRefresh);
    };
  }, [refreshBalance]);

  useEffect(() => {
    refreshBalance();
    fetchHistory();
    fetchUserProfile();
  }, []);

  useEffect(() => {
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };

    if (isDropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isDropdownOpen]);

  useEffect(() => {
    try {
      const storedData = localStorage.getItem('data');
      if (storedData) {
        const parsedData = JSON.parse(storedData);
        const userPhoto = parsedData?.user?.photo;
        if (userPhoto && userPhoto !== '/assets/images/user-profile.jpg') {
          setProfileImage(userPhoto);
        }
      }
    } catch (error) {
      console.error('Error loading profile image:', error);
    }
  }, []);

  useEffect(() => {
    try {
        const storedData = localStorage.getItem('data');
        if (storedData) {
            const parsedData = JSON.parse(storedData);
            const userPhoto = parsedData?.user?.photo;
            if (userPhoto && userPhoto !== '/assets/images/user-profile.jpg') {
                setProfileImage(userPhoto);
            }
        }
    } catch (error) {
        console.error('Error loading profile image:', error);
    }
  }, []);

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

  const handleSliderScroll = () => {
    if (!sliderRef.current) return;
    const { scrollLeft, scrollWidth, clientWidth } = sliderRef.current;
    const dot = scrollLeft > scrollWidth / 2 - clientWidth / 2 ? 1 : 0;
    setActiveDot(dot);
  };

  const scrollToPage = (page: number) => {
    if (!sliderRef.current) return;
    const { scrollWidth, clientWidth } = sliderRef.current;
    sliderRef.current.scrollTo({
      left: page === 0 ? 0 : scrollWidth - clientWidth,
      behavior: 'smooth',
    });
    setActiveDot(page);
  };

  const fetchUserSettings = async () => {
    try {
      const userId = getUserId();
      const response = await configService.getUserSettings(userId);
      
      if (response?.status === 'success' && response?.data) {
        const settingsData = response.data;
        setSettings(prev => ({
          ...prev,
          security: {
            ...prev.security,
            biometricEnabled: settingsData.isBiometric || false,
            sessionTimeout: parseInt(settingsData.sessionTimeOut) || 30
          },
          notifications: {
            email: settingsData.isEmailAlert || false,
            push: prev.notifications.push,
            sms: settingsData.isReceiveSmsMessage || false,
            transactionAlerts: settingsData.isTransactionAlert || false,
            loginAlerts: settingsData.isLoginAlert || false,
            marketingEmails: settingsData.isReceiveMarketingNews || false
          },
          preferences: {
            ...prev.preferences,
            language: settingsData.preferredLanguage || 'English',
            timezone: settingsData.timeZone || 'Africa/Lagos'
          }
        }));
      }
    } catch (error) {
      console.error('Error fetching user settings:', error);
      showToast("Error fetching user settings");
    }
  };
    
  const fetchUserProfile = async () => {
    try {
      const userId = getUserId();
      
      const response = await userService.getById(userId);
      const API_BASE_URL = 'http://localhost:8187';  
      setUserProfile(response);
      const userRecord = response.records?.[0] || {};
      
      setFormData({
        firstName: userRecord.firstName || '',
        lastName: userRecord.lastName || '',
        gender: userRecord.gender
          ? capitalizeFirstLetter(userRecord.gender)
          : '',
        telephone: userRecord.telephone || '',
        dob: userRecord.dob || user_details?.dob || '',
        email: response.email || ''
      });

      setSettings(prev => ({
        ...prev,
        profile: {
          fullName: `${userRecord.firstName || ''} ${userRecord.lastName || ''}`.trim(),
          email: response.email || '',
          phone: userRecord.telephone || '',
          username: response.username || '',
          profileImage: userRecord.photo 
            ? `http://localhost:8292/api${userRecord.photo}` 
            : '/assets/images/user-profile.jpg'
        },
        security: {
          twoFactorEnabled: response.twoFactorAuth || false,
          biometricEnabled: prev.security.biometricEnabled,
          sessionTimeout: prev.security.sessionTimeout
        },
        preferences: {
          language: userRecord.language || 'English',
          currency: userRecord.currency || 'NGN',
          theme: prev.preferences.theme,
          timezone: userRecord.timezone || 'Africa/Lagos'
        }
      }));

      if (userRecord.photo) {
        const fullImageUrl = `http://localhost:8292/api${userRecord.photo}`;
        setProfileImage(fullImageUrl);
        
        try {
            const storedData = localStorage.getItem('data');
            if (storedData) {
                const parsedData = JSON.parse(storedData);
                if (parsedData?.user) {
                    parsedData.user.photo = fullImageUrl;
                    localStorage.setItem('data', JSON.stringify(parsedData));
                }
            }
        } catch (err) {
            console.error('Error updating profile image in storage:', err);
        }
    }
    await fetchUserSettings();

    // ── Tier upgrade modal: show once when KYC is incomplete ─────────────
    const profileProgress = calculateProfileCompletion();
    const hasSeenTierModal = getHasSeenMetaMap();
    if (profileProgress < 100 && !hasSeenTierModal) {
      setTimeout(() => setShowTierUpgradeModal(true), 1800);
    }
    // ─────────────────────────────────────────────────────────────────────

    } catch (error) {
      console.error('Error fetching user profile:', error);
      showToast("Failed to load profile data");
    } finally {
      setLoading(false);
    }
  };

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

  const filteredCurrencies = currencies.filter(c => 
    c.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    c.code.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleRedirect = () => {
    router.push('/cards');
  };

  const handleExchangeRoute = () => {
    router.push('/exchange');
  };

  const getCurrentBalance = () => {
    if (!wallet || !wallet.wallet_balances || !selectedCurrency) return '0.00';
    
    const balanceData = wallet.wallet_balances.find(
      (item: any) => item.currency_code === selectedCurrency.code
    );
    
    return balanceData ? balanceData.balance : '0.00';
  };

  const formatBalance = (balance: string) => {
    const numBalance = parseFloat(balance);
    if (isNaN(numBalance)) return '0.00';
    
    return numBalance.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  };

  const quickActions = [
    { icon: <Smartphone />, label: 'Airtime', color: '#ff7a5c', route: '/services/bills/airtime' },
    { icon: <Wifi />, label: 'Data', color: '#5ecdbf', route: '/services/bills/data' },
    { icon: <Tv />, label: 'CableTv', color: '#5eb7cd', route: '/services/bills/cabletv' },
    { icon: <Lightbulb />, label: 'Electricity', color: '#ffac7a', route: '/services/bills/electricity' },
    { icon: <CreditCard />, label: 'Virtual Card', color: '#9adbb9', route: '/cards' },
    { icon: <Trophy />, label: 'Betting', color: '#b5a1d5', route: '/services/bills/betting' },
    { icon: <Repeat />, label: 'Swap', color: '#8ec5ed', route: '/exchange' },
  ];

  const userRecord = userProfile?.records?.[0] || {};

  if (isPageLoading) {
    return <LoadingScreen />;
  }

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <WelcomeModal
          userName={getUserFullName()?.split(' ')[0] || 'David'}
          imageSrc="/assets/images/welcome-img.png"
        />
       <div className="scrollable-content">
          <div className="welcome-message">
            <div className="user-avatar">
              <img
                src={profileImage}
                alt="User profile"
                width={21}
                height={21}
                className="settings-avatar"
                onError={(e) => {
                  e.currentTarget.src = '/assets/images/user-profile.jpg';
                }}
              />
            </div>
            <span className={`user-name-out ${theme === "dark" ? "color-light" : "color-dark"}`}>
              <span className="welcome-text-container">Welcome back,</span>
                <span className="username-display">
                  {getUserFullName()} <span className="wave-icon"><Hand size={16} className="wave-icon" /></span>
                </span>
            </span>
          </div>
          
          <section className="hero-banner">
            <div className="wallet-header-wrapper">
              <div className="wallet-main-header">
                <div className="wallet-currency-selector">
                  <div ref={dropdownRef} className="currency-pill" onClick={() => {
                    setIsModalOpen(true);
                    setIsDropdownOpen(!isDropdownOpen);
                  }} style={{ cursor: 'pointer' }}>
                    <span className='currency-option'>
                      {selectedCurrency?.code || 'NGN'} 
                      <i className={`fa ${isDropdownOpen ? 'fa-caret-up' : 'fa-caret-down'} text-light`} 
                        aria-hidden="true"
                        style={{ color: "#fff", fontSize: "15px", marginLeft: "4px", marginTop:"3px" }}></i>
                    </span>
                  </div>
                </div>

                <div className="wallet-visibility-toggle">
                  <button onClick={() => setShowBalance(!showBalance)} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer' }}>
                    <span className='eye-view'>
                      {showBalance ? <Eye size={20} /> : <EyeOff size={20} />}
                    </span>
                  </button>
                </div>
              </div>
              
              <div className="balance-row">
                <div className="wallet-balance-label">Available Balance</div>
                <div className={`amount ${!showBalance ? 'has-blur' : ''}`}>
                    {selectedCurrency?.symbol || '₦'}{' '}
                    {showBalance ? (
                      formatBalance(getCurrentBalance())
                    ) : (
                      <span className="blurred-balance">*****</span>
                    )}
                  </div>
              </div>
            </div>

            <div className="hero-actions">
              <div className="hero-action-item" onClick={() => setIsDepositOpen(true)} style={{ cursor: 'pointer' }}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <Plus size={20} style={{ color: 'var(--bg-main)' }} />
                </div>
                <span>Deposit</span>
              </div>
              <div className="hero-action-item" onClick={() => setIsWithdrawOpen(true)} style={{ cursor: 'pointer' }}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <ArrowDownLeft size={20} style={{ color: 'var(--bg-main)' }} />
                </div>
                <span>Withdraw</span>
              </div>
              <div className="hero-action-item" onClick={handleRedirect}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <CreditCard size={20} style={{ color: 'var(--bg-main)' }} />
                </div>
                <span>Cards</span>
              </div>
              <div className="hero-action-item" onClick={handleExchangeRoute}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <Repeat size={20} style={{ color: 'var(--bg-main)' }} />
                </div>
                <span>Exchange</span>
              </div>
            </div>
          </section>

          <div className="service-container">
            <div className="service-top-bar">
              <span className="service-title">Quick Services</span>
              <span className="service-view-all" onClick={() => router.push('/services')}>View All</span>
            </div>

            <section
              className={`feature-grid ${theme === "dark" ? "color-light" : "color-dark"}`}
              ref={sliderRef}
              onScroll={handleSliderScroll}
            >
              {quickActions.map((action, idx) => (
                <div
                  key={idx}
                  className="feature-card"
                  onClick={() => router.push(action.route)}
                >
                  <div className="feature-icon-wrapper" style={{ background: action.color }}>
                    {React.cloneElement(action.icon as React.ReactElement<LucideProps>, { size: 22 })}
                  </div>
                  <span className="feature-label">{action.label}</span>
                </div>
              ))}
            </section>

            <div className="service-dots">
              {[0, 1].map((dot) => (
                <span
                  key={dot}
                  className={`service-dot ${activeDot === dot ? 'active' : ''}`}
                  onClick={() => scrollToPage(dot)}
                />
              ))}
            </div>
          </div>
          

          <div className="bottom-sections-grid">
            <div className="history-column">
              <History key={historyKey} theme={theme} historyData={historyData} />
            </div>
            <div className="news-column">
              <News theme={theme} />
            </div>
          </div>

          <Footer theme={theme} />
        </div>
      </main>

      {isModalOpen && (
        <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header"><h3>Select Currency</h3></div>
            <div className="search-container">
              <span className="search-icon-inside"><Search size={16} /></span>
              <input type="text" placeholder="Search" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
            </div>
            <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
              {filteredCurrencies.map((c) => (
                <div key={c.code} className="country-item" onClick={() => { 
                  setSelectedCurrency(c); 
                  setIsModalOpen(false);
                  setFiat(c.code); 
                  setActiveWallet(c.code);
                  setSearchTerm('') 
                  setIsDropdownOpen(false);
                }}>
                  <span>{c.name} ({c.code})</span>
                  <div className={`radio-outer ${selectedCurrency?.code === c.code ? 'checked' : ''}`}>
                    <div className="radio-inner"></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      <MobileNav activeTab="home" onPlusClick={() => setIsDepositOpen(true)} />
        <Suspense>
           <DepositModal 
              isOpen={isDepositOpen} 
              onClose={() => setIsDepositOpen(false)} 
              theme={theme}
              onDepositSuccess={handleTransactionSuccess}
            />
        </Suspense>
      <Suspense>
      <WithdrawModal 
        isOpen={isWithdrawOpen} 
        onClose={() => setIsWithdrawOpen(false)} 
        theme={theme}
        onWithdrawReloadSuccess={handleTransactionSuccess}/> 
      </Suspense>

      {/* ── Tier 1 → Tier 2 KYC Upgrade Modal ── */}
      {showTierUpgradeModal && (
        <>
          <div
            style={{
              position: 'fixed', inset: 0,
              background: 'rgba(0,0,0,0.55)', backdropFilter: 'blur(3px)',
              zIndex: 99998, animation: 'tierBackdropIn 0.25s ease'
            }}
            onClick={() => { setShowTierUpgradeModal(false); updateHasSeenMetaMap(true); }}
          />
          <div style={{
            position: 'fixed', top: '50%', left: '50%',
            transform: 'translate(-50%,-50%)',
            background: '#fff', borderRadius: 24, width: '92%', maxWidth: 440,
            zIndex: 99999, boxShadow: '0 24px 80px rgba(43,15,86,0.22)',
            overflow: 'hidden', animation: 'tierModalIn 0.3s cubic-bezier(.22,1,.36,1)'
          }}>
            {/* Purple gradient header */}
            <div style={{
              background: 'linear-gradient(135deg, #2b0f56, #4a1a8c)',
              padding: '32px 28px 24px', textAlign: 'center', position: 'relative'
            }}>
              {/* Close button */}
              <button
                onClick={() => { setShowTierUpgradeModal(false); updateHasSeenMetaMap(true); }}
                style={{
                  position: 'absolute', top: 14, right: 16,
                  background: 'rgba(255,255,255,0.15)', border: 'none',
                  borderRadius: '50%', width: 30, height: 30, cursor: 'pointer',
                  color: '#fff', fontSize: 18, lineHeight: 1, display: 'flex',
                  alignItems: 'center', justifyContent: 'center'
                }}
              >×</button>

              {/* Tier badges */}
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 12, marginBottom: 18 }}>
                <div style={{
                  background: 'rgba(255,255,255,0.15)', borderRadius: 12,
                  padding: '8px 18px', color: '#fff', fontSize: 13, fontWeight: 700
                }}>
                  <i className="fas fa-user" style={{ marginRight: 6, fontSize: 11 }} />
                  Tier 1
                </div>
                <div style={{ color: 'rgba(255,255,255,0.6)', fontSize: 18 }}>→</div>
                <div style={{
                  background: 'linear-gradient(135deg, #fbbf24, #f59e0b)',
                  borderRadius: 12, padding: '8px 18px',
                  color: '#78350f', fontSize: 13, fontWeight: 700,
                  boxShadow: '0 4px 12px rgba(251,191,36,0.4)'
                }}>
                  <i className="fas fa-crown" style={{ marginRight: 6, fontSize: 11 }} />
                  Tier 2
                </div>
              </div>
              <h2 style={{ margin: 0, color: '#fff', fontSize: 22, fontWeight: 700, lineHeight: 1.3 }}>
                Upgrade to Tier 2
              </h2>
              <p style={{ margin: '8px 0 0', color: 'rgba(255,255,255,0.75)', fontSize: 13 }}>
                Unlock transfers, deposits & withdrawals
              </p>
            </div>

            {/* Body */}
            <div style={{ padding: '24px 28px 28px' }}>
              {/* Feature list */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12, marginBottom: 24 }}>
                {[
                  { icon: 'fa-paper-plane', text: 'Send & receive money instantly', color: '#2b0f56' },
                  { icon: 'fa-arrow-down', text: 'Deposit funds from any bank', color: '#059669' },
                  { icon: 'fa-arrow-up', text: 'Withdraw to your bank account', color: '#d97706' },
                  { icon: 'fa-shield-alt', text: 'Higher transaction limits', color: '#7c3aed' },
                ].map((item, i) => (
                  <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div style={{
                      width: 36, height: 36, borderRadius: '50%', flexShrink: 0,
                      background: `${item.color}18`,
                      display: 'flex', alignItems: 'center', justifyContent: 'center'
                    }}>
                      <i className={`fas ${item.icon}`} style={{ color: item.color, fontSize: 14 }} />
                    </div>
                    <span style={{ fontSize: 14, color: '#374151', fontWeight: 500 }}>{item.text}</span>
                  </div>
                ))}
              </div>

              {/* KYC requirement note */}
              <div style={{
                background: '#f5f0ff', border: '1px solid #ddd6fe', borderRadius: 12,
                padding: '12px 16px', marginBottom: 22, display: 'flex', gap: 10, alignItems: 'flex-start'
              }}>
                <i className="fas fa-info-circle" style={{ color: '#2b0f56', marginTop: 2, flexShrink: 0 }} />
                <p style={{ margin: 0, fontSize: 13, color: '#4b5563', lineHeight: 1.6 }}>
                  To upgrade to Tier 2, complete your <strong style={{ color: '#2b0f56' }}>KYC verification</strong> by
                  filling in your personal details on the profile page.
                </p>
              </div>

              {/* CTA buttons */}
              <button
                onClick={() => {
                  setShowTierUpgradeModal(false);
                  updateHasSeenMetaMap(true);
                  router.push('/settings/profile');
                }}
                style={{
                  width: '100%', padding: '14px', background: '#2b0f56',
                  color: '#fff', border: 'none', borderRadius: 12, fontSize: 15,
                  fontWeight: 700, cursor: 'pointer', marginBottom: 10,
                  display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                  transition: 'background 0.2s'
                }}
              >
                <i className="fas fa-id-card" />
                Complete KYC & Upgrade
              </button>
              <button
                onClick={() => { setShowTierUpgradeModal(false); updateHasSeenMetaMap(true); }}
                style={{
                  width: '100%', padding: '12px', background: 'transparent',
                  color: '#9ca3af', border: '1px solid #e5e7eb', borderRadius: 12,
                  fontSize: 14, fontWeight: 500, cursor: 'pointer'
                }}
              >
                Remind me later
              </button>
            </div>
          </div>
        </>
      )}

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

export default Dashboard;