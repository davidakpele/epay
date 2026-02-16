'use client';

import { useState, useEffect, FormEvent, useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import Select from 'react-select';
import { ArrowLeft, User, CheckCircle, Search } from 'lucide-react';
import Image from 'next/image';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Sidebar from '@/components/Sidebar';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import "./CreateCard.css";
import LoadingScreen from '@/components/loader/Loadingscreen';
import { eventEmitter } from '@/app/utils/eventEmitter';
import { getFiat, getToken, getUserFullName, getUserId, getUsername, setActiveWallet, setFiat, setWalletContainer, virtualCardService, walletService } from '@/app/api';
import { Currency } from '@/app/types/api';

// Extended Currency interface with balance
interface CurrencyWithBalance extends Currency {
  balance?: string;
}

interface WalletOption {
  value: string;
  label: string;
}

interface Wallet {
  currency_code: string;
  symbol: string;
  balance: string;
}

interface FormData {
  accountCurrency: string;
  cardType: 'MASTER' | 'VISA';
  spendingLimit: string;
  cardTheme: string;
  cardHolderName: string;
}

interface FormErrors {
  accountCurrency?: string;
  cardHolderName?: string;
}

interface CardTheme {
  name: string;
  imageUrl: any;
  label: string;
}

interface Toast {
  id: number;
  message: string;
  type: 'warning' | 'success';
  exiting: boolean;
}

const mastercardThemes: CardTheme[] = [
  { name: 'theme1', imageUrl: '/assets/images/darkcard.png', label: 'Black' },
  { name: 'theme2', imageUrl: '/assets/images/greenCard.png', label: 'Green' },
  { name: 'theme3', imageUrl: '/assets/images/darkGreenCard.png', label: 'Dark Green' },
];

const visaThemes: CardTheme[] = [
  { name: 'theme1', imageUrl: '/assets/images/visaCardBlur.png', label: 'Green' },
  { name: 'theme2', imageUrl: '/assets/images/visaCardBlue.png', label: 'Blue' },
];

const CreateCard = () => {
  const router = useRouter();
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [cardType, setCardType] = useState<'MASTER' | 'VISA'>('MASTER');
  const [selectedTheme, setSelectedTheme] = useState('theme1');
  const [options, setOptions] = useState<WalletOption[]>([]);
  const [isLoader, setIsLoader] = useState(true);
  const [formErrors, setFormErrors] = useState<FormErrors>({});
  const [walletData, setWalletData] = useState<Wallet[]>([]);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isVerified, setIsVerified] = useState(false);
  const [userName, setUserName] = useState('');
  const [userHandle, setUserHandle] = useState('');
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [wallet, setWallet] = useState<any>(null);
  const [currencies, setCurrencies] = useState<CurrencyWithBalance[]>([]);
  const [isScrolling, setIsScrolling] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCurrency, setSelectedCurrency] = useState<CurrencyWithBalance | null>(null);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const scrollTimer = useRef<NodeJS.Timeout | null>(null);
  const [formData, setFormData] = useState<FormData>({
    accountCurrency: '',
    cardType: 'MASTER',
    spendingLimit: '10000.00',
    cardTheme: 'theme1',
    cardHolderName: '',
  });

  const refreshBalance = useCallback(async () => {
      try {
        const token = getToken();
        const userId = getUserId();
        
        if (!token || !userId) {
          return;
        }
        
        const response = await walletService.getByUserId(userId, token);
        if (!response || response.status === 401 || response.status === 500) {
          router.push('/auth/logout');
          return;
        }
        setWallet(response);
        
        if (response && response.wallet_balances) {
          const apiCurrencies: CurrencyWithBalance[] = response.wallet_balances.map((balance: any) => {
            return {
              name: balance.currency_code,
              code: balance.currency_code,
              symbol: balance.symbol,
              balance: balance.balance
            };
          });
          
          setCurrencies(apiCurrencies);
          if (getFiat() !== '' && getFiat() != null) {
            const fiatCurrency = apiCurrencies.find(c => c.code === getFiat());
            if (fiatCurrency) {
              setSelectedCurrency(fiatCurrency);
              setFormData(prev => ({...prev, accountCurrency: fiatCurrency.code}));
            }
          }
          else {
            if (!selectedCurrency && apiCurrencies.length > 0) {
              const defaultCurrency = apiCurrencies.find(c => c.code === 'NGN') || apiCurrencies[0];
              setSelectedCurrency(defaultCurrency);
              setActiveWallet(defaultCurrency.code);
              setFiat(defaultCurrency.code);
              setFormData(prev => ({...prev, accountCurrency: defaultCurrency.code}));
            }
          }
            
          setWalletContainer(response.wallet_balances, response.hasTransferPin, response.walletId);
        }
      } catch (e) {
        console.log(e);
      }
    }, []); 
  

   useEffect(() => {
      document.title = 'Create Card - ePay Online Business Banking';
      const handleBalanceRefresh = () => {
        refreshBalance();
      };
  
      eventEmitter.on('refreshBalance', handleBalanceRefresh);
      
      return () => {
        eventEmitter.off('refreshBalance', handleBalanceRefresh);
      };
    }, [refreshBalance]);

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

  const handleInputChange = (field: keyof FormData, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));

    if (formErrors[field as keyof FormErrors]) {
      setFormErrors(prev => ({
        ...prev,
        [field]: ''
      }));
    }
  };

  const validateForm = () => {
    const errors: FormErrors = {};

    if (!formData.accountCurrency) {
      errors.accountCurrency = 'Please select an account currency';
    }

    if (!getUserFullName()?.toString().trim() ) {
      errors.cardHolderName = 'Card holder name is required';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const getSelectedWalletBalance = () => {
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

  const generateMerchantId = () => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < 15; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  };

  const generateMerchantCategoryCode = () => {
    return Math.floor(1000 + Math.random() * 9000).toString();
  };

  const preparePayload = async () => {
    const userId = getUserId();
    const fullName = getUserFullName();
    
    const payload = {
      userId: userId,
      accountHolderName: fullName?.toUpperCase() || formData.cardHolderName.toUpperCase(),
      cardType: formData.cardType,
      currency: formData.accountCurrency,
      initialBalance: parseFloat(getSelectedWalletBalance()),
      spendingLimit: parseFloat(formData.spendingLimit),
      limitPeriod: "TRANSACTION",
      plan: "SINGLE_USE",
      allowInternational: true,
      allowOnline: true,
      allowAtm: true,
      allowContactless: true,
      merchantName: "Standard Chartered Bank",
      merchantId: generateMerchantId(),
      merchantCategoryCode: generateMerchantCategoryCode(),
      merchantCountry: "US",
      merchantCity: "Washington DC"
    };
    try {
  
      const response = await virtualCardService.createCard(payload)
    } catch (error) {
      showToast('Sorry, something went wrong!', 'warning');
    }
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      showToast('Please fix the form errors before submitting', 'warning');
      return;
    }
    preparePayload();
  };

  useEffect(() => {
    refreshBalance();
    const storedUserName = getUserFullName()?.toString() || '';
    const storedUserHandle = getUsername() || '';
    const userDataString = localStorage.getItem('data');
    
    let storedVerified = false;
    
    if (userDataString) {
      try {
        
        const userData = JSON.parse(userDataString);
        storedVerified = userData.user?.is_verify === true;
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    }
    
    setUserName(storedUserName);
    setUserHandle(storedUserHandle);
    setIsVerified(storedVerified);
  }, []);

  useEffect(() => {
    handleInputChange('cardType', cardType);
  }, [cardType]);

  useEffect(() => {
    handleInputChange('cardTheme', selectedTheme);
  }, [selectedTheme]);

  const handleThemeChange = (theme: string) => {
    setSelectedTheme(theme);
  };

  const handleGoBack = () => {
    router.back();
  };

  const hasError = (field: keyof FormErrors) => {
    return formErrors[field] && formErrors[field] !== '';
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

  useEffect(() => {
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);

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
            
            <div className="create-card-container">
              <form onSubmit={handleSubmit} className="form-content">
                <div className="card-header-title">
                  <button type="button" className="back-button" onClick={handleGoBack}>
                    <ArrowLeft size={24} />
                  </button>
                  <h1 className="title">Create new card</h1>
                </div>
                <p className="info-text">
                  There is a $4.00 (NGN 6,170.00) fee to create a card and KYC verified to be eligible to create a virtual card.
                </p>

                <div className="section">
                  <label htmlFor="accountCurrency" className="section-title">
                    Select account
                  </label>
                  <div className="account-selector-container" onClick={() => {setIsModalOpen(true)}}>
                    <span>{selectedCurrency ? `${selectedCurrency.name} (${selectedCurrency.code})` : 'Select Wallet'}</span>
                  </div>
                
                  {formErrors.accountCurrency && (
                    <div className="error-message">{formErrors.accountCurrency}</div>
                  )}
                  <div className="account-selector" style={{ marginTop: "10px" }}>
                    <span>Selected account balance:</span>
                    <span className="balance-amount">
                      {selectedCurrency?.symbol}{formatBalance(getSelectedWalletBalance())} {formData.accountCurrency}
                    </span>
                  </div>
                </div>

                <div className="section">
                  <div className="user-profile-box">
                    <span className="user-icon">
                      <User size={24} />
                    </span>
                    <div className="user-details">
                      <p className="user-name">{userName}</p>
                      <p className="username">@{userHandle}</p>
                    </div>
                    {isVerified ? (
                      <div className="verification-status">
                        Verified
                      </div>
                    ) : (
                      <div className="unverification-status">
                        Unverified
                      </div>
                    )}
                  </div>
                </div>

                <div className="section">
                  <p className="section-title">Card type</p>
                  <div className="radio-group">
                    <label className="radio-option">
                      <input
                        type="radio"
                        name="cardType"
                        value="MASTER"
                        checked={cardType === 'MASTER'}
                        onChange={(e) => setCardType(e.target.value as 'MASTER' | 'VISA')}
                      />
                      <span className="radio-custom"></span>
                      <span>Master card</span>
                    </label>
                    <label className="radio-option">
                      <input
                        type="radio"
                        name="cardType"
                        value="VISA"
                        checked={cardType === 'VISA'}
                        onChange={(e) => setCardType(e.target.value as 'MASTER' | 'VISA')}
                      />
                      <span className="radio-custom"></span>
                      <span>Visa card</span>
                    </label>
                  </div>
                </div>

                <div className="section">
                  <p className="section-title">Card alias</p>
                  <input
                    type="text"
                    className={`alias-input ${hasError('cardHolderName') ? 'error-form' : ''}`}
                    placeholder="Enter card alias"
                    value={getUserFullName()?.toString()}
                    onChange={(e) => handleInputChange('cardHolderName', e.target.value)}
                  />
                  {formErrors.cardHolderName && (
                    <div className="error-message">{formErrors.cardHolderName}</div>
                  )}
                </div>

                <div className="section">
                  <p className="section-title">Choose card theme</p>
                  <div className="theme-selector">
                    {(cardType === 'MASTER' ? mastercardThemes : visaThemes).map((themeOption) => (
                      <div
                        key={themeOption.name}
                        className={`card-preview ${selectedTheme === themeOption.name ? 'selected' : ''}`}
                        onClick={() => handleThemeChange(themeOption.name)}
                      >
                        <Image
                          src={themeOption.imageUrl}
                          alt={themeOption.label}
                          fill
                          className="card-theme-image"
                          style={{ objectFit: 'cover' }}
                        />
                        {selectedTheme === themeOption.name && (
                          <div className="check-icon-overlay">
                            <CheckCircle size={24} color="white" />
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                <button type="submit" className="cta-button">
                  Proceed to verification
                </button>
              </form>
            </div>
            
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
                          setFormData(prev => ({...prev, accountCurrency: c.code}));
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
  );
};

export default CreateCard;