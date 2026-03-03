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
import Link from 'next/link';
import { getUserId, virtualCardService } from '../api';
import LoadingScreen from '@/components/loader/Loadingscreen';

interface VirtualCard {
  id: string;
  cardId: string;
  cardNumber: string;
  maskedCardNumber: string;
  hashedCardNumber: string;
  holder: string;
  validThru: string;
  expirationMonth: string;
  expirationYear: string;
  balance: number;
  currency: string;
  status: string;
  cardType: string;
  cardPlan: string;
  merchantName: string;
  merchantCity: string;
  merchantCountry: string;
  spendingLimit: number;
  allowInternational: boolean;
  allowOnline: boolean;
  allowAtm: boolean;
  allowContactless: boolean;
  createdAt: string;
  expiresAt: string;
}

function Cards() {
  const router = useRouter();
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [selectedCurrency, setSelectedCurrency] = useState('USD');
  const [isLoader, setIsLoader] = useState(true);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [allCards, setAllCards] = useState<VirtualCard[]>([]);
  const [isLoadingCards, setIsLoadingCards] = useState(true);
  const [isCardDetailsOpen, setIsCardDetailsOpen] = useState(false);
  const [selectedCard, setSelectedCard] = useState<VirtualCard | null>(null);

  useEffect(() => {
    const fetchCards = async () => {
      setIsLoadingCards(true);
      try {
        const userId = getUserId();
        if (!userId) return;
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        const response = virtualCardService.fetchUserVirtualCardsByUserId(userId);
        const data = await response;
      
        const transformedCards: VirtualCard[] = data.map((card: any) => ({
          id: card.id,
          cardId: card.cardId,
          cardNumber: card.maskedCardNumber,
          maskedCardNumber: card.maskedCardNumber,
          hashedCardNumber: card.hashedCardNumber,
          holder: card.cardHolderName,
          validThru: `${card.expirationMonth}/${card.expirationYear.slice(-2)}`,
          expirationMonth: card.expirationMonth,
          expirationYear: card.expirationYear,
          balance: card.balance,
          currency: card.currency,
          status: card.status,
          cardType: card.cardType,
          cardPlan: card.cardPlan,
          merchantName: card.merchantName,
          merchantCity: card.merchantCity,
          merchantCountry: card.merchantCountry,
          spendingLimit: card.spendingLimit,
          allowInternational: card.allowInternational,
          allowOnline: card.allowOnline,
          allowAtm: card.allowAtm,
          allowContactless: card.allowContactless,
          createdAt: card.createdAt,
          expiresAt: card.expiresAt
        }));
        
        setAllCards(transformedCards);
        if (transformedCards.length > 0) {
          setSelectedCurrency(transformedCards[0].currency);
        }
      } catch (error) {
        console.error('Error fetching cards:', error);
        showToast('Failed to load cards. Please try again.', 'warning');
      } finally {
        setIsLoadingCards(false);
      }
    };

    fetchCards();
  }, []);
  const cards = allCards.filter(card => card.currency === selectedCurrency);
  const availableCurrencies = Array.from(new Set(allCards.map(card => card.currency)));

  const allPossibleCurrencies = ['USD', 'EUR', 'NGN', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY', 'INR'];
  const currencies = allPossibleCurrencies.map(code => ({
    code,
    count: allCards.filter(c => c.currency === code).length
  }));

  const getCurrencyInfo = (currencyCode: string) => {
    const currencyMap: Record<string, { symbol: string; name: string }> = {
      USD: { symbol: '$', name: 'Dollar' },
      EUR: { symbol: '€', name: 'Euro' },
      NGN: { symbol: '₦', name: 'Naira' },
      GBP: { symbol: '£', name: 'Pound' },
      JPY: { symbol: '¥', name: 'Yen' }
    };
    return currencyMap[currencyCode] || { symbol: '$', name: 'Dollar' };
  };

  const getCardTypeName = (cardType: string) => {
    if (cardType === 'MASTER' || cardType === 'MASTERCARD') {
      return 'MASTER';
    }
    return 'VISA';
  };
  const handleCardDetailsClick = (card: VirtualCard) => {
    setSelectedCard(card);
    setIsCardDetailsOpen(true);
  };

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
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);;

  if (isPageLoading) {
    return <LoadingScreen />;
  }
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
                <Link href={"/cards/create"} className="btn-create-new">
                  <span>+</span> Request for New Card
                </Link>
              </div>

              {/* Search and Filters */}
              <div className="vc-controls">
                
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

              {/* Loading State */}
              {isLoadingCards ? (
                <div className="cards-loader">
                  <div className="loader-spinner"></div>
                  <p>Loading your cards...</p>
                </div>
              ) : allCards.length === 0 ? (
                <div className="no-cards">
                  <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                    <rect x="8" y="20" width="48" height="32" rx="4" stroke="#9ca3af" strokeWidth="2"/>
                    <rect x="8" y="28" width="48" height="8" fill="#e5e7eb"/>
                    <circle cx="16" cy="40" r="2" fill="#9ca3af"/>
                  </svg>
                  <h3>No Cards Available</h3>
                  <p>You haven't created any virtual cards yet.</p>
                  <Link href={"/cards/create"} className="btn-create-new">
                    <span>+</span> Create Your First Card
                  </Link>
                </div>
              ) : (
                /* Cards Grid */
                <div className="vc-grid">
                  {cards.map((card) => {
                    const currencyInfo = getCurrencyInfo(card.currency);
                    return (
                      <div key={card.id} className="vc-card-wrapper">
                        {/* Card Visual */}
                        {card.cardType === 'MASTER' || card.cardType === 'MASTERCARD' ? (
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
                                <span className="card-type">{getCardTypeName(card.cardType)}</span>
                              </div>
                              <h3 className="card-featured-title">{card.currency} Virtual Card</h3>
                              <div className="card-featured-number">{card.cardNumber}</div>
                              <div className="card-featured-bottom">
                                <span className="card-holder">{card.holder}</span>
                                <div className="expire-date">
                                  <span className="card-expiry">Valid Thru:</span> 
                                  <span className='card-expiry-date'> {card.validThru}</span>
                                </div>
                                <span className="card-type">{getCardTypeName(card.cardType)}</span>
                              </div>
                            </div>
                          </div>
                        ) : (
                          <div className="card-simple">
                            <div className="card-simple-top">
                              <span>{card.currency} Virtual Card</span>
                            </div>
                            <h3 className="card-simple-title">{card.currency} Virtual Card</h3>
                            <div className="card-simple-number">{card.cardNumber}</div>
                            <div className="card-simple-bottom">
                              <span className="card-simple-holder">{card.holder}</span>
                              <div className="card-simple-expiry">
                                <span className="expiry-label">Valid Thru:</span>
                                <span className="expiry-value"> {card.validThru}</span>
                              </div>
                              <span className="card-simple-type">{getCardTypeName(card.cardType)}</span>
                            </div>
                          </div>
                        )}

                        {/* Card Info - REDESIGNED */}
                        <div className="vc-card-info">
                          <div className="info-row-1">
                            <div className="info-left">
                              <span className={`status-pill ${card.status}`}>
                                {card.status}
                              </span>
                              <div className="balance-label">
                                <span>{currencyInfo.symbol} {currencyInfo.name} Balance</span>
                              </div>
                            </div>
                            <div className="info-right">
                              <span className="balance-amt">{currencyInfo.symbol}{card.balance.toFixed(2)}</span>
                              {card.balance > 0 && <span className="balance-sub">68 prceheres..</span>}
                              {card.balance === 0 && <span className="balance-sub-vital">Vital ✓</span>}
                            </div>
                          </div>

                          <div className="info-actions">
                            <button 
                              className="btn-details-outline"
                              onClick={() => handleCardDetailsClick(card)}
                            >
                              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                                <rect x="3" y="5" width="8" height="7" rx="1" stroke="currentColor" strokeWidth="1.3"/>
                                <path d="M5 5V4C5 3 5.5 2 7 2C8.5 2 9 3 9 4V5" stroke="currentColor" strokeWidth="1.3"/>
                              </svg>
                              Card Details
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
            <Footer theme={theme} />
          </div>
          
        </main>
        <MobileNav activeTab="wallet" onPlusClick={() => setIsDepositOpen(true)} />
        <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
        <WithdrawModal isOpen={isWithdrawOpen} onClose={() => setIsWithdrawOpen(false)} theme={theme} />
        
        {/* Card Details Modal */}
        {isCardDetailsOpen && selectedCard && (
          <div className="card-modal-overlay" onClick={() => setIsCardDetailsOpen(false)}>
            <div className="card-card-details-modal" onClick={(e) => e.stopPropagation()}>
              <div className="card-modal-header">
                <h2>Card Details</h2>
                <button className="card-modal-close" onClick={() => setIsCardDetailsOpen(false)}>
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M18 6L6 18M6 6l12 12"/>
                  </svg>
                </button>
              </div>

              <div className="card-modal-body">
                {/* Card Preview */}
                <div className={selectedCard.cardType === 'MASTER' || selectedCard.cardType === 'MASTERCARD' ? "card-featured-modal" : "card-simple-modal"}>
                  <div className="card-modal-inner">
                    <div className="card-modal-top">
                      <div className="card-bank-logo">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                          <circle cx="10" cy="10" r="9" stroke="white" strokeWidth="1.5"/>
                          <circle cx="10" cy="10" r="4" fill="white"/>
                        </svg>
                        <span>{selectedCard.merchantName}</span>
                      </div>
                      <span className="card-type-badge">{getCardTypeName(selectedCard.cardType)}</span>
                    </div>
                    <h3 className="card-modal-title">{selectedCard.currency} Virtual Card</h3>
                    <div className="card-modal-number">{selectedCard.hashedCardNumber}</div>
                    <div className="card-modal-bottom">
                      <span className="card-modal-holder">{selectedCard.holder}</span>
                      <div className="card-modal-expiry">
                        <span>Valid Thru:</span>
                        <span>{selectedCard.expirationMonth}/{selectedCard.expirationYear}</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Card Information */}
                <div className="card-info-grid">
                  <div className="info-section">
                    <h3>Card Information</h3>
                    <div className="info-row">
                      <span className="info-label">Card Number</span>
                      <span className="info-value">{selectedCard.hashedCardNumber}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Card Holder</span>
                      <span className="info-value">{selectedCard.holder}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Expiration Date</span>
                      <span className="info-value">{selectedCard.expirationMonth}/{selectedCard.expirationYear}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Card Type</span>
                      <span className="info-value">{getCardTypeName(selectedCard.cardType)}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Card Plan</span>
                      <span className="info-value">{selectedCard.cardPlan.replace('_', ' ')}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Status</span>
                      <span className={`info-value status-${selectedCard.status}`}>
                        {selectedCard.status.toUpperCase()}
                      </span>
                    </div>
                  </div>

                  <div className="info-section">
                    <h3>Balance & Limits</h3>
                    <div className="info-row">
                      <span className="info-label">Current Balance</span>
                      <span className="info-value balance-highlight">
                        {getCurrencyInfo(selectedCard.currency).symbol}{selectedCard.balance.toFixed(2)}
                      </span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Spending Limit</span>
                      <span className="info-value">
                        {getCurrencyInfo(selectedCard.currency).symbol}{selectedCard.spendingLimit.toFixed(2)}
                      </span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Currency</span>
                      <span className="info-value">{selectedCard.currency}</span>
                    </div>
                  </div>

                  <div className="info-section">
                    <h3>Merchant Details</h3>
                    <div className="info-row">
                      <span className="info-label">Merchant Name</span>
                      <span className="info-value">{selectedCard.merchantName}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Location</span>
                      <span className="info-value">{selectedCard.merchantCity}, {selectedCard.merchantCountry}</span>
                    </div>
                  </div>

                  <div className="info-section">
                    <h3>Card Permissions</h3>
                    <div className="permissions-grid">
                      <div className={`permission-item ${selectedCard.allowInternational ? 'enabled' : 'disabled'}`}>
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                          <circle cx="10" cy="10" r="8" stroke="currentColor" strokeWidth="1.5"/>
                          <path d="M10 2C10 2 6 6 6 10C6 14 10 18 10 18C10 18 14 14 14 10C14 6 10 2 10 2Z" stroke="currentColor" strokeWidth="1.5"/>
                        </svg>
                        <span>International</span>
                        <span className="status-badge">{selectedCard.allowInternational ? '✓' : '✗'}</span>
                      </div>
                      <div className={`permission-item ${selectedCard.allowOnline ? 'enabled' : 'disabled'}`}>
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                          <rect x="3" y="5" width="14" height="10" rx="2" stroke="currentColor" strokeWidth="1.5"/>
                          <path d="M7 9h6M7 11h4" stroke="currentColor" strokeWidth="1.5"/>
                        </svg>
                        <span>Online Shopping</span>
                        <span className="status-badge">{selectedCard.allowOnline ? '✓' : '✗'}</span>
                      </div>
                      <div className={`permission-item ${selectedCard.allowAtm ? 'enabled' : 'disabled'}`}>
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                          <rect x="4" y="3" width="12" height="14" rx="2" stroke="currentColor" strokeWidth="1.5"/>
                          <rect x="6" y="6" width="8" height="5" stroke="currentColor" strokeWidth="1.5"/>
                        </svg>
                        <span>ATM Withdrawal</span>
                        <span className="status-badge">{selectedCard.allowAtm ? '✓' : '✗'}</span>
                      </div>
                      <div className={`permission-item ${selectedCard.allowContactless ? 'enabled' : 'disabled'}`}>
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                          <path d="M8 10c0-1.1.9-2 2-2s2 .9 2 2-.9 2-2 2-2-.9-2-2z" stroke="currentColor" strokeWidth="1.5"/>
                          <path d="M5 10c0-2.8 2.2-5 5-5s5 2.2 5 5-2.2 5-5 5-5-2.2-5-5z" stroke="currentColor" strokeWidth="1.5"/>
                        </svg>
                        <span>Contactless</span>
                        <span className="status-badge">{selectedCard.allowContactless ? '✓' : '✗'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="info-section">
                    <h3>Card Timeline</h3>
                    <div className="info-row">
                      <span className="info-label">Created</span>
                      <span className="info-value">{new Date(selectedCard.createdAt).toLocaleString()}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">Expires</span>
                      <span className="info-value">{new Date(selectedCard.expiresAt).toLocaleString()}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
        
        <div className="toasts-container">{toasts.map((toast) => (
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