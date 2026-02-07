'use client';

import { useState, useEffect, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Select from 'react-select';
import { ArrowLeft, User, CheckCircle } from 'lucide-react';
import Image from 'next/image';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import Sidebar from '@/components/Sidebar';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import "./CreateCard.css";

const customStyles = {
  control: (base: any, state: any) => ({
    ...base,
    backgroundColor: 'white',
    borderColor: '#e9ecef',
    borderRadius: '8px',
    padding: '4px 8px',
    minHeight: '44px',
    boxShadow: state.isFocused ? '0 0 0 3px rgba(0, 123, 255, 0.1)' : 'none',
    borderWidth: '1px',
    '&:hover': {
      borderColor: '#007bff'
    }
  }),
  option: (base: any, state: any) => ({
    ...base,
    backgroundColor: state.isSelected ? '#6c757d' : state.isFocused ? '#495057' : 'white',
    color: state.isSelected ? 'white' : state.isFocused ? 'white' : '#333',
    padding: '10px 12px',
    cursor: 'pointer',
    '&:active': {
      backgroundColor: '#6c757d'
    }
  }),
  menu: (base: any) => ({
    ...base,
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    zIndex: 9999
  }),
  menuList: (base: any) => ({
    ...base,
    padding: '4px',
    backgroundColor: '#f8f9fa'
  }),
  singleValue: (base: any) => ({
    ...base,
    color: '#333'
  }),
  placeholder: (base: any) => ({
    ...base,
    color: '#999'
  }),
  dropdownIndicator: (base: any) => ({
    ...base,
    color: '#666',
    '&:hover': {
      color: '#333'
    }
  }),
  indicatorSeparator: (base: any) => ({
    ...base,
    backgroundColor: '#e9ecef'
  }),
  loadingIndicator: (base: any) => ({
    ...base,
    color: '#666'
  })
};

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
  cardType: 'mastercard' | 'visa';
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
  const [cardType, setCardType] = useState<'mastercard' | 'visa'>('mastercard');
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

  const [formData, setFormData] = useState<FormData>({
    accountCurrency: '',
    cardType: 'mastercard',
    spendingLimit: '10000.00',
    cardTheme: 'theme1',
    cardHolderName: '',
  });

  // Custom toast function
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

  const handleBankChange = (selectedOption: WalletOption | null) => {
    handleInputChange('accountCurrency', selectedOption ? selectedOption.value : '');
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

  const fetchBanklist = async () => {
    setIsLoader(true);
    
    try {
      // Mock wallet data - replace with actual API call
      const mockWallets: Wallet[] = [
        { currency_code: 'NGN', symbol: '₦', balance: '0.00' },
        { currency_code: 'USD', symbol: '$', balance: '0.00' },
        { currency_code: 'GBP', symbol: '£', balance: '0.00' }
      ];

      setWalletData(mockWallets);
      const walletOptions = mockWallets.map(wallet => ({
        value: wallet.currency_code,
        label: `${wallet.symbol} ${wallet.currency_code} - Balance: ${wallet.balance}`
      }));
      setOptions(walletOptions);
      setIsLoader(false);
    } catch (error) {
      console.error('Error fetching wallets:', error);
      setOptions([]);
      setIsLoader(false);
    }
  };

  const validateForm = () => {
    const errors: FormErrors = {};

    if (!formData.accountCurrency) {
      errors.accountCurrency = 'Please select an account currency';
    }

    if (!formData.cardHolderName.trim()) {
      errors.cardHolderName = 'Card holder name is required';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const getSelectedWalletBalance = () => {
    if (!formData.accountCurrency || !walletData.length) return '0.00';
    const selectedWallet = walletData.find(wallet => 
      wallet.currency_code === formData.accountCurrency
    );
    return selectedWallet ? selectedWallet.balance : '0.00';
  };

  const generateUUID = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  };

  const preparePayload = async () => {
    const payload = {
      id: generateUUID(),
      user_id: 'user_123', // Replace with actual user ID
      user_wallet_id: 'wallet_123', // Replace with actual wallet ID
      accountCurrency: formData.accountCurrency,
      cardNumber: '',
      cvv: '',
      expiryMonth: new Date().getMonth() + 1,
      expiryYear: new Date().getFullYear() + 3,
      card_holder_name: formData.cardHolderName,
      card_type: formData.cardType,
      card_theme: formData.cardTheme,
      status: 'pending',
      spendingLimit: 10000.00,
      currentBalance: 0,
      currency: formData.accountCurrency,
      createdAt: new Date().toISOString(),
      metadata: {
        theme: formData.cardTheme,
        isVirtual: true,
        cardNetwork: formData.cardType
      }
    };

    try {
      // Replace with your actual API call
      // const response = await bankCollectionService.createVirtualCard(payload);
      console.log('Payload:', payload);
      showToast('Card created successfully!', 'success');
      setTimeout(() => {
        router.push('/cards');
      }, 2000);
    } catch (error) {
      console.error('Error creating card:', error);
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
    fetchBanklist();
    
    // Load user data from localStorage or API
    const storedUserName = localStorage.getItem('userName') || 'Angel Mike';
    const storedUserHandle = localStorage.getItem('userHandle') || 'angelmike';
    const storedVerified = localStorage.getItem('isVerified') === 'true';
    
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

  return (
    <>
      <div className="dashboard-container">
        <Sidebar />
        <main className={`main-content ${isDepositOpen ? 'blur-sm' : ''}`}>
          <Header theme={theme} toggleTheme={toggleTheme} />
          <div className="scrollable-content">
            {/* Custom Toast Container */}
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
                  <Select
                    options={options}
                    onChange={handleBankChange}
                    isDisabled={isLoader}
                    isLoading={isLoader}
                    styles={customStyles}
                    placeholder={isLoader ? "Loading accounts..." : "Select Account Currency"}
                    noOptionsMessage={() => "No accounts available"}
                    className={`react-select-container ${hasError('accountCurrency') ? 'error-form' : ''}`}
                    classNamePrefix="react-select"
                  />
                  {formErrors.accountCurrency && (
                    <div className="error-message">{formErrors.accountCurrency}</div>
                  )}
                  <div className="account-selector" style={{ marginTop: "10px" }}>
                    <span>Selected account balance:</span>
                    <span className="balance-amount">
                      {getSelectedWalletBalance()} {formData.accountCurrency}
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
                  <p className="section-title">User verification</p>
                  {isVerified ? (
                    <button type="button" className="verification-badge verified">
                      Verified
                    </button>
                  ) : (
                    <button type="button" className="verification-badge unverified">
                      Unverified
                    </button>
                  )}
                </div>

                <div className="section">
                  <p className="section-title">Card type</p>
                  <div className="radio-group">
                    <label className="radio-option">
                      <input
                        type="radio"
                        name="cardType"
                        value="mastercard"
                        checked={cardType === 'mastercard'}
                        onChange={(e) => setCardType(e.target.value as 'mastercard' | 'visa')}
                      />
                      <span className="radio-custom"></span>
                      <span>Master card</span>
                    </label>
                    <label className="radio-option">
                      <input
                        type="radio"
                        name="cardType"
                        value="visa"
                        checked={cardType === 'visa'}
                        onChange={(e) => setCardType(e.target.value as 'mastercard' | 'visa')}
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
                    value={formData.cardHolderName}
                    onChange={(e) => handleInputChange('cardHolderName', e.target.value)}
                  />
                  {formErrors.cardHolderName && (
                    <div className="error-message">{formErrors.cardHolderName}</div>
                  )}
                </div>

                <div className="section">
                  <p className="section-title">Choose card theme</p>
                  <div className="theme-selector">
                    {(cardType === 'mastercard' ? mastercardThemes : visaThemes).map((themeOption) => (
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