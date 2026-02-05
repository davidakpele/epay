'use client';

import { useState, FormEvent, ChangeEvent, useEffect } from 'react';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import "./Cards.css"
import { CheckCircle, CreditCard, XCircle } from 'lucide-react';
import LoadingScreen from '@/components/loader/Loadingscreen';

type CardType = 'mastercard' | 'visa';

type VirtualCard = {
  id: string;
  type: CardType;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  holderName: string;
  price: number;
  requestedDate: string;
};

type CardOption = {
  type: CardType;
  name: string;
  price: number;
  color: string;
  logo: string;
};


const Cards = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [userCards, setUserCards] = useState<VirtualCard[]>([]);
    const [selectedCard, setSelectedCard] = useState<CardType | null>(null);
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [showFailModal, setShowFailModal] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
     const [isPageLoading, setIsPageLoading] = useState(true);
    const toggleTheme = () => {
        const newTheme = theme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
        document.documentElement.classList.toggle('dark', newTheme === 'dark');
        document.body.classList.toggle('dark-theme', newTheme === 'dark');
    };

  const cardOptions: CardOption[] = [
    {
      type: 'mastercard',
      name: 'Mastercard',
      price: 5000,
      color: '#eb001b',
      logo: 'MC'
    },
    {
      type: 'visa',
      name: 'Visa Card',
      price: 4500,
      color: '#1a1f71',
      logo: 'VISA'
    }
  ];
  useEffect(() => {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark';
    if (savedTheme) {
      setTheme(savedTheme);
    }
  }, []);

  useEffect(() => {
    // Handle page loading
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);
  const hasCardType = (type: CardType) => {
    return userCards.some(card => card.type === type);
  };

  const handleCardSelect = (type: CardType) => {
    if (!hasCardType(type)) {
      setSelectedCard(type);
    }
  };

  const generateCardNumber = () => {
    return Array.from({ length: 16 }, () => Math.floor(Math.random() * 10)).join('');
  };

  const generateCVV = () => {
    return Array.from({ length: 3 }, () => Math.floor(Math.random() * 10)).join('');
  };

  const generateExpiryDate = () => {
    const now = new Date();
    const year = now.getFullYear() + 3;
    const month = String(now.getMonth() + 1).padStart(2, '0');
    return `${month}/${year.toString().slice(-2)}`;
  };

  const handleRequestCard = () => {
    if (!selectedCard) return;

    setIsProcessing(true);

    // Simulate API call
    setTimeout(() => {
      // 80% success rate simulation
      const isSuccess = Math.random() > 0.2;

      if (isSuccess) {
        const newCard: VirtualCard = {
          id: Date.now().toString(),
          type: selectedCard,
          cardNumber: generateCardNumber(),
          expiryDate: generateExpiryDate(),
          cvv: generateCVV(),
          holderName: 'Alan Bola',
          price: cardOptions.find(c => c.type === selectedCard)?.price || 0,
          requestedDate: new Date().toISOString().split('T')[0]
        };

        setUserCards([...userCards, newCard]);
        setShowSuccessModal(true);
      } else {
        setShowFailModal(true);
      }

      setIsProcessing(false);
      setSelectedCard(null);
    }, 2000);
  };

  const formatCardNumber = (number: string) => {
    return number.replace(/(\d{4})/g, '$1 ').trim();
  };

  if (isPageLoading) {
    return <LoadingScreen />;
  }
  
  return (
    <>
      <div className="dashboard-container">
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'blur-sm' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <div className="virtual-cards-page">
            <div className="virtual-cards-container">
              <h1 className="page-title">Virtual Cards</h1>
              <p className="page-subtitle">Request and manage your virtual payment cards</p>

              <div className="cards-grid">
                {/* Left Column - Card Options */}
                <div className="cards-section">
                  <h2 className="section-title">Available Cards</h2>
                  <p className="section-subtitle">Select a card type to request</p>

                  <div className="card-options-list">
                    {cardOptions.map((option) => {
                      const hasCard = hasCardType(option.type);
                      const isSelected = selectedCard === option.type;

                      return (
                        <div
                          key={option.type}
                          className={`card-option ${hasCard ? 'card-disabled' : ''} ${isSelected ? 'card-selected' : ''}`}
                          onClick={() => handleCardSelect(option.type)}
                        >
                          <div className="card-option-header">
                            <div className="card-logo" style={{ backgroundColor: option.color }}>
                              {option.logo}
                            </div>
                            <div className="card-info">
                              <h3 className="card-name">{option.name}</h3>
                              <p className="card-price">₦{option.price.toLocaleString()}</p>
                            </div>
                          </div>

                          {hasCard && (
                            <div className="card-status-badge owned">
                              <CheckCircle size={14} />
                              <span>Already Owned</span>
                            </div>
                          )}

                          {isSelected && !hasCard && (
                            <div className="card-status-badge selected-badge">
                              <CheckCircle size={14} />
                              <span>Selected</span>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>

                  {selectedCard && (
                    <div className="request-section">
                      <div className="request-info">
                        <p className="request-text">
                          You are about to request a <strong>{cardOptions.find(c => c.type === selectedCard)?.name}</strong>
                        </p>
                        <p className="request-price">
                          Amount: <strong>₦{cardOptions.find(c => c.type === selectedCard)?.price.toLocaleString()}</strong>
                        </p>
                      </div>
                      <button
                        onClick={handleRequestCard}
                        disabled={isProcessing}
                        className="submit-button"
                      >
                        {isProcessing ? 'Processing...' : 'Request Card'}
                      </button>
                    </div>
                  )}
                </div>

                {/* Right Column - User Cards */}
                <div className="cards-section">
                  <h2 className="section-title">My Cards</h2>
                  <p className="section-subtitle">Your active virtual cards</p>

                  {userCards.length === 0 ? (
                    <div className="empty-state">
                      <CreditCard size={48} className="empty-icon" />
                      <p className="empty-title">No cards yet</p>
                      <p className="empty-subtitle">Request your first virtual card to get started</p>
                    </div>
                  ) : (
                    <div className="user-cards-list">
                      {userCards.map((card) => {
                        const cardOption = cardOptions.find(c => c.type === card.type);
                        return (
                          <div key={card.id} className="virtual-card">
                            <div className="virtual-card-bg" style={{ background: cardOption?.color }}>
                              <div className="card-chip"></div>
                              <div className="card-logo-text">{cardOption?.logo}</div>
                              <div className="card-number">{formatCardNumber(card.cardNumber)}</div>
                              <div className="card-details">
                                <div className="card-detail-item">
                                  <span className="card-label">CARD HOLDER</span>
                                  <span className="card-value">{card.holderName}</span>
                                </div>
                                <div className="card-detail-item">
                                  <span className="card-label">EXPIRES</span>
                                  <span className="card-value">{card.expiryDate}</span>
                                </div>
                                <div className="card-detail-item">
                                  <span className="card-label">CVV</span>
                                  <span className="card-value">{card.cvv}</span>
                                </div>
                              </div>
                            </div>
                            <div className="card-footer">
                              <span className="card-type-name">{cardOption?.name}</span>
                              <span className="card-date">Issued: {card.requestedDate}</span>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Success Modal */}
          {showSuccessModal && (
            <div className="modal-overlay">
              <div className="modal success-modal">
                <div className="success-icon">
                  <CheckCircle size={48} />
                </div>
                <h3 className="modal-title">Card Request Successful!</h3>
                <p className="modal-text">
                  Your virtual card has been created successfully. You can now use it for online payments.
                </p>
                <button className="modal-btn success-btn" onClick={() => setShowSuccessModal(false)}>
                  Done
                </button>
              </div>
            </div>
          )}

          {/* Fail Modal */}
          {showFailModal && (
            <div className="modal-overlay">
              <div className="modal fail-modal">
                <div className="fail-icon">
                  <XCircle size={48} />
                </div>
                <h3 className="modal-title">Card Request Failed</h3>
                <p className="modal-text">
                  We couldn't process your card request at this time. Please try again later or contact support.
                </p>
                <button className="modal-btn fail-btn" onClick={() => setShowFailModal(false)}>
                  Close
                </button>
              </div>
            </div>
          )}

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
    </>
  )
}

export default Cards;