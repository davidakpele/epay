'use client';

import React, { useState, useEffect, useRef, useCallback, Suspense } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
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
import { getFiat, getToken, getUserId, setActiveWallet, setFiat, setWalletContainer, walletService, historyService } from '../api';
import { eventEmitter } from '../utils/eventEmitter';
import { get } from 'http';
import KycCheckProgress from '@/components/Kyc/page';

const Dashboard = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [wallet, setWallet] = useState<any>(null);
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [selectedCurrency, setSelectedCurrency] = useState<Currency | null>(null);
  const [historyData, setHistoryData] = useState<any[]>([]);
  const [historyKey, setHistoryKey] = useState(0);

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
  const router = useRouter();

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
    { icon: <Smartphone />, label: 'Buy Airtime', color: '#ff7a5c' },
    { icon: <Wifi />, label: 'Buy Data', color: '#5ecdbf' },
    { icon: <Tv />, label: 'CableTv', color: '#5eb7cd' },
    { icon: <Lightbulb />, label: 'Electricity', color: '#ffac7a' },
    { icon: <CreditCard />, label: 'Virtual Card', color: '#9adbb9' },
    { icon: <Hospital />, label: 'Hospital', color: '#f5d671' },
    { icon: <Trophy />, label: 'Betting', color: '#b5a1d5' },
    { icon: <Repeat />, label: 'Swap', color: '#8ec5ed' },
    { icon: <Plane />, label: 'Book Flight', color: '#f7c978' },
    { icon: <ShoppingBag />, label: 'Shopping', color: '#88e0a3' },
  ];

  if (isPageLoading) {
    return <LoadingScreen />;
  }

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <div className="welcome-message">
            <div className="user-avatar">
                <Image
                  src="/assets/images/user-profile.jpg"
                  alt={'User profile'}
                  width={21}
                  height={21}
                  className="settings-avatar"
                /> 
            </div>
            <span className={`user-name-out ${theme === "dark" ? "color-light" : "color-dark"}`}>
              Welcome back, <span className='username-display'>David</span>
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
                <div className="amount">
                  {selectedCurrency?.symbol || '₦'} {showBalance ? formatBalance(getCurrentBalance()) : '*****'}
                </div>
              </div>
            </div>

            <div className="hero-actions">
              <div className="hero-action-item" onClick={() => setIsDepositOpen(true)} style={{ cursor: 'pointer' }}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <Plus size={20} style={{ color: '#ef4444' }} />
                </div>
                <span>Deposit</span>
              </div>
              <div className="hero-action-item" onClick={() => setIsWithdrawOpen(true)} style={{ cursor: 'pointer' }}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <ArrowDownLeft size={20} style={{ color: '#ef4444' }} />
                </div>
                <span>Withdraw</span>
              </div>
              <div className="hero-action-item" onClick={handleRedirect}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <CreditCard size={20} style={{ color: '#ef4444' }} />
                </div>
                <span>Cards</span>
              </div>
              <div className="hero-action-item" onClick={handleExchangeRoute}>
                <div className="hero-icon-box" style={{ background: '#fff', borderRadius: '50%', border: '1px solid #e2e8f0' }}>
                  <Repeat size={20} style={{ color: '#ef4444' }} />
                </div>
                <span>Exchange</span>
              </div>
            </div>
          </section>

          <KycCheckProgress />

          <section className={`feature-grid ${theme === "dark" ? "color-light" : "color-dark"}`}>
            {quickActions.map((action, idx) => (
              <div key={idx} className="feature-card">
                <div className="feature-icon-wrapper" style={{ background: action.color }}>
                  {React.cloneElement(action.icon as React.ReactElement<LucideProps>, { size: 22 })}
                </div>
                <span className="feature-label">{action.label}</span>
              </div>
            ))}
          </section>

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