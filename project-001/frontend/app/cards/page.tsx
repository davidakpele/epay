'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Sidebar from '@/components/Sidebar';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import { Toast } from '../types/auth';
import WithdrawModal from '@/components/WithdrawModal';
import './cards.css';

interface VirtualCard {
  id: string;
  cardNumber: string;
  holder: string;
  validThru: string;
  balance: number;
  currency: string;
  status: 'active' | 'locked';
  isFeatured?: boolean;
}

function Cards() {
  const router = useRouter();
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [selectedCurrency, setSelectedCurrency] = useState('USD');
  const [isLoader, setIsLoader] = useState(true);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  const cards: VirtualCard[] = [
    {
      id: '1',
      cardNumber: '4321 **** **** 0911',
      holder: 'DAVID APEKELE',
      validThru: '04/28',
      balance: 650.00,
      currency: 'USD',
      status: 'active',
      isFeatured: true
    },
    {
      id: '2',
      cardNumber: '5271 **** **** 0012',
      holder: 'DAVID APEKELE',
      validThru: '04/28',
      balance: 0.00,
      currency: 'USD',
      status: 'locked'
    },
    {
      id: '3',
      cardNumber: '5271 **** **** 3323',
      holder: 'DAVID APEKELE',
      validThru: '04/28',
      balance: 0.00,
      currency: 'USD',
      status: 'locked'
    }
  ];

  const currencies = [
    { code: 'USD', count: 3 },
    { code: 'EUR', count: 2 },
    { code: 'NGN', count: 1 },
    { code: 'GBP', count: 1 },
    { code: 'JPY', count: 1 },
    { code: 'AUD', count: 0 },
    { code: 'CAD', count: 0 }
  ];

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

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.classList.toggle('dark', newTheme === 'dark');
    document.body.classList.toggle('dark-theme', newTheme === 'dark');
  };

  useEffect(() => {
    const loadingTimer = setTimeout(() => setIsLoader(false), 2000);
    return () => clearTimeout(loadingTimer);
  }, []);

  return (
    <>
      <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
        <Sidebar />
        <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
          <Header theme={theme} toggleTheme={toggleTheme} />
          <div className="scrollable-content">
            <div className="virtual-cards-container">
              {/* Header */}
              <div className="vc-header">
                <div>
                  <h1 className="vc-title">Virtual Cards</h1>
                  <p className="vc-subtitle">Manage your virtual debit cards across multiple wallets.</p>
                </div>
                <button className="btn-create-new">
                  <span>+</span> Create New Card
                </button>
              </div>

              {/* Search and Filters */}
              <div className="vc-controls">
                <div className="vc-search">
                  <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                    <circle cx="8" cy="8" r="5.5" stroke="currentColor" strokeWidth="1.5"/>
                    <path d="M12 12L15.5 15.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                  </svg>
                  <input type="text" placeholder="Search virtual cards..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} />
                </div>
                <div className="vc-currency-tabs">
                  {currencies.map((curr) => (
                    <button
                      key={curr.code}
                      className={`currency-tab ${selectedCurrency === curr.code ? 'active' : ''} ${curr.count === 0 ? 'disabled' : ''}`}
                      onClick={() => curr.count > 0 && setSelectedCurrency(curr.code)}
                      disabled={curr.count === 0}
                    >
                      {curr.code} {curr.count > 0 && curr.count}
                    </button>
                  ))}
                </div>
              </div>

              {/* Cards Grid */}
              <div className="vc-grid">
                {cards.map((card) => (
                  <div key={card.id} className="vc-card-wrapper">
                    {/* Card Visual */}
                    {card.isFeatured ? (
                      <div className="card-featured">
                        <div className="card-featured-inner">
                          <div className="card-featured-top">
                            <div className="card-bank-logo">
                              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                                <circle cx="10" cy="10" r="9" stroke="white" strokeWidth="1.5"/>
                                <circle cx="10" cy="10" r="4" fill="white"/>
                              </svg>
                              <span>OpenAI Bank</span>
                            </div>
                            <svg className="visa-logo" width="48" height="16" viewBox="0 0 48 16" fill="white">
                              <path d="M18 2L14 12H16.5L20.5 2H18Z"/>
                              <path d="M12 2L8 12H10.5L14.5 2H12Z"/>
                              <path d="M24 2L28 12H25.5L21.5 2H24Z"/>
                              <path d="M30 2L34 12H31.5L27.5 2H30Z"/>
                            </svg>
                          </div>
                          <h3 className="card-featured-title">USD Virtual Card</h3>
                          <div className="card-featured-number">{card.cardNumber}</div>
                          <div className="card-featured-bottom">
                            <span className="card-holder">{card.holder}</span>
                            <div className="expire-date">
                              <span className="card-expiry">Valid Thru:</span> 
                              <span className='card-expiry-date'> {card.validThru}</span>
                            </div>
                             <span className="card-type">VISA</span> 
                          </div>
                        </div>
                      </div>
                    ) : (
                      <div className="card-simple">
                        <div className="card-simple-top">
                          <span>USD Virtual Card</span>
                          <svg width="40" height="14" viewBox="0 0 40 14" fill="currentColor">
                            <path d="M15 1L12 9H13.5L16.5 1H15Z"/>
                            <path d="M10 1L7 9H8.5L11.5 1H10Z"/>
                            <path d="M20 1L23 9H21.5L18.5 1H20Z"/>
                            <path d="M25 1L28 9H26.5L23.5 1H25Z"/>
                          </svg>
                        </div>
                        <div className="card-simple-number">{card.cardNumber}</div>
                        <div className="card-simple-holder">{card.holder}</div>
                        <svg className="visa-logo-simple" width="40" height="14" viewBox="0 0 40 14" fill="#1A1F71">
                          <path d="M15 1L12 9H13.5L16.5 1H15Z"/>
                          <path d="M10 1L7 9H8.5L11.5 1H10Z"/>
                          <path d="M20 1L23 9H21.5L18.5 1H20Z"/>
                          <path d="M25 1L28 9H26.5L23.5 1H25Z"/>
                        </svg>
                      </div>
                    )}

                    {/* Card Info - REDESIGNED */}
                    <div className="vc-card-info">
                      <div className="info-row-1">
                        <div className="info-left">
                          <span className={`status-pill ${card.status}`}>
                            {card.status === 'active' ? (
                              <>✓ Active</>
                            ) : (
                              <>🔒 Locked</>
                            )}
                          </span>
                          <div className="balance-label">
                            <img src="https://flagcdn.com/w20/us.png" alt="US" width="16" />
                            <span>$ Dollar Balance</span>
                          </div>
                        </div>
                        <div className="info-right">
                          <span className="balance-amt">${card.balance.toFixed(2)}</span>
                          {card.balance > 0 && <span className="balance-sub">68 prceheres..</span>}
                          {card.balance === 0 && <span className="balance-sub-vital">Vital ✓</span>}
                        </div>
                      </div>

                      <div className="info-actions">
                        <button className="btn-details-outline">
                          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                            <rect x="3" y="5" width="8" height="7" rx="1" stroke="currentColor" strokeWidth="1.3"/>
                            <path d="M5 5V4C5 3 5.5 2 7 2C8.5 2 9 3 9 4V5" stroke="currentColor" strokeWidth="1.3"/>
                          </svg>
                          Card Details
                        </button>
                        <button className="btn-details-solid">
                          Card Details
                          <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                            <circle cx="6" cy="3.5" r="0.8" fill="white"/>
                            <circle cx="6" cy="6" r="0.8" fill="white"/>
                            <circle cx="6" cy="8.5" r="0.8" fill="white"/>
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <Footer theme={theme} />
          </div>
          
        </main>
        <MobileNav activeTab="wallet" onPlusClick={() => setIsDepositOpen(true)} />
        <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
        <WithdrawModal isOpen={isWithdrawOpen} onClose={() => setIsWithdrawOpen(false)} theme={theme} />
        <div className="toasts-container">
          {toasts.map((toast) => (
            <div key={toast.id} className={`toast ${toast.type} ${toast.exiting ? 'exiting' : ''}`}>
              {toast.message}
            </div>
          ))}
        </div>
      </div>
    </>
  );
}

export default Cards;