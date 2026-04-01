'use client';

import React, { useState, useEffect, useRef, useCallback, Suspense } from 'react';
import { useRouter } from 'next/navigation';
import { 
  Eye, EyeOff, Plus, ArrowDownLeft, 
  CreditCard, Repeat, Search, User2, 
  LucideProps, Smartphone, Wifi, Tv, 
  Lightbulb, Hospital, Trophy, Plane, ShoppingBag 
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
import { getFiat, getToken, getUserId, setActiveWallet, setFiat, setWalletContainer, walletService, historyService, userService, capitalizeFirstLetter, getUserDetails, configService, getUserFullName } from '../api';
import { eventEmitter } from '../utils/eventEmitter';
import KycCheckProgress from '@/components/Kyc/page';
import { UserSettings } from '../types/utils';
import { Toast } from '../types/auth';
import WelcomeModal from '@/components/WelcomeModal';

const Dashboard = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [wallet, setWallet] = useState<any>(null);
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [selectedCurrency, setSelectedCurrency] = useState<Currency | null>(null);
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
    { icon: <Hospital />, label: 'Hospital', color: '#f5d671', route: '/services/bills/hospital' },
    { icon: <Trophy />, label: 'Betting', color: '#b5a1d5', route: '/services/bills/betting' },
    { icon: <Repeat />, label: 'Swap', color: '#8ec5ed', route: '/exchange' },
    { icon: <Plane />, label: 'Book Flight', color: '#f7c978', route: '/services/bills/flight' },
    { icon: <ShoppingBag />, label: 'Shopping', color: '#88e0a3', route: '/services/bills/shopping' },
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
              Welcome back <span className='username-display'>{getUserFullName()}</span>
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

          <KycCheckProgress />
         <div className="service-container">
          <span className='service-header'>Service</span>
            <section className={`feature-grid ${theme === "dark" ? "color-light" : "color-dark"}`}>
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
    </div>
  );
};

export default Dashboard;