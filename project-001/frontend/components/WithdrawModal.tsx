'use client';

import React, { useState, useEffect, Suspense, lazy, useRef} from 'react';
import { X, Landmark, Wallet, User, ArrowRight, ShieldCheck, Lock, ArrowLeft, AlertCircle, CheckCircle, ChevronDown, Star } from 'lucide-react';
import Select from 'react-select';
import './WithdrawModal.css';
import { Toast } from '@/app/types/auth';
import { WalletType, WithdrawModalProps } from '@/app/types/utils';
import { bankCollectionService, beneficiaryService, getFiat, getToken, getUserId, getUsername, getUserWalletId, getWallet, getWalletList, setFiat, setWalletContainer, updateNotificationContainer, uuidv4, walletService, withdrawService } from '@/app/api';
import { eventEmitter } from '@/app/utils/eventEmitter';

const customStyles = {
  control: (base: any, state: any) => ({
    ...base,
    backgroundColor: 'white',
    borderColor: state.isFocused ? '#dc2626' : '#e2e8f0',
    borderRadius: '10px',
    padding: '2px 8px',
    minHeight: '48px',
    boxShadow: state.isFocused ? '0 0 0 3px rgba(220, 38, 38, 0.1)' : 'none',
    borderWidth: '2px',
    '&:hover': {
      borderColor: state.isFocused ? '#dc2626' : '#cbd5e1'
    }
  }),
  option: (base: any, state: any) => ({
    ...base,
    backgroundColor: state.isSelected ? '#dc2626' : state.isFocused ? '#fef2f2' : 'white',
    color: state.isSelected ? 'white' : state.isFocused ? '#dc2626' : '#334155',
    padding: '10px 12px',
    cursor: 'pointer',
    '&:active': {
      backgroundColor: '#b91c1c'
    }
  }),
  menu: (base: any) => ({
    ...base,
    backgroundColor: 'white',
    borderRadius: '10px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
    zIndex: 9999
  }),
  menuList: (base: any) => ({
    ...base,
    padding: '8px',
    backgroundColor: 'white',
    borderRadius: '10px'
  }),
  singleValue: (base: any) => ({
    ...base,
    color: '#0f172a'
  }),
  placeholder: (base: any) => ({
    ...base,
    color: '#94a3b8'
  }),
  dropdownIndicator: (base: any, state: any) => ({
    ...base,
    color: state.isFocused ? '#dc2626' : '#94a3b8',
    '&:hover': {
      color: '#dc2626'
    }
  }),
  indicatorSeparator: (base: any) => ({
    ...base,
    backgroundColor: '#e2e8f0'
  })
};

const customStylesDark = {
  control: (base: any, state: any) => ({
    ...base,
    backgroundColor: '#0f172a',
    borderColor: state.isFocused ? '#dc2626' : '#334155',
    borderRadius: '10px',
    padding: '2px 8px',
    minHeight: '48px',
    boxShadow: state.isFocused ? '0 0 0 3px rgba(220, 38, 38, 0.2)' : 'none',
    borderWidth: '2px',
    '&:hover': {
      borderColor: state.isFocused ? '#dc2626' : '#475569'
    }
  }),
  option: (base: any, state: any) => ({
    ...base,
    backgroundColor: state.isSelected ? '#dc2626' : state.isFocused ? '#1e293b' : '#0f172a',
    color: state.isSelected ? 'white' : state.isFocused ? '#f1f5f9' : '#cbd5e1',
    padding: '10px 12px',
    cursor: 'pointer',
    '&:active': {
      backgroundColor: '#b91c1c'
    }
  }),
  menu: (base: any) => ({
    ...base,
    backgroundColor: '#1e293b',
    borderRadius: '10px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
    zIndex: 9999
  }),
  menuList: (base: any) => ({
    ...base,
    padding: '8px',
    backgroundColor: '#1e293b',
    borderRadius: '10px'
  }),
  singleValue: (base: any) => ({
    ...base,
    color: '#f1f5f9'
  }),
  placeholder: (base: any) => ({
    ...base,
    color: '#64748b'
  })
};

const WithdrawModal = ({ isOpen, onClose, theme, onWithdrawReloadSuccess }: WithdrawModalProps) => {
  const [step, setStep] = useState<'selection' | 'bank' | 'epay'>('selection');
  const [showWalletModal, setShowWalletModal] = useState(false);
  const [amount, setAmount] = useState('');
  const [rawAmount, setRawAmount] = useState('');
  const [accountNumber, setAccountNumber] = useState('');
  const [recipientUsername, setRecipientUsername] = useState('');
  const [selectedWallet, setSelectedWallet] = useState<WalletType | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [showPinModal, setShowPinModal] = useState(false);
  const [pin, setPin] = useState(['', '', '', '']);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const pinInputRefs = useRef<(HTMLInputElement | null)[]>([]);
  const [showFailModal, setShowFailModal] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoadingBanks, setIsLoadingBanks] = useState(false);
  const [bankName, setBankName] = useState('');
  const [accountName, setAccountName] = useState('');
  const [selectedBankOption, setSelectedBankOption] = useState<any>(null);
  const [isSubmittingPin, setIsSubmittingPin] = useState(false);
  const [isVerifyingAccount, setIsVerifyingAccount] = useState(false);
  const idempotencyKeyRef = useRef<string | null>(null);
  const [pendingTransaction, setPendingTransaction] = useState(false);
  const [bankOptions, setBankOptions] = useState<any[]>([]);
  const [wallets, setWallets] = useState<WalletType[]>([]);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [showBeneficiaryModal, setShowBeneficiaryModal] = useState(false);
  const [isSavingBeneficiary, setIsSavingBeneficiary] = useState(false);

  const formatNumberWithCommas = (value: string): string => {
    const cleanValue = value.replace(/,/g, '');
    if (!cleanValue) return '';
    const parts = cleanValue.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    if (parts[1]) parts[1] = parts[1].slice(0, 2);
    return parts.join('.');
  };

  const formatBalance = (balance: number, symbol: string): string => {
    return `${symbol}${balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const calculateProcessingFee = () => {
    if (!rawAmount || !selectedWallet) return 0;
    const amountNum = parseFloat(rawAmount);
    if (isNaN(amountNum)) return 0;
    return 0;
  };

  const calculateTotal = () => {
    if (!rawAmount) return 0;
    const amountNum = parseFloat(rawAmount);
    if (isNaN(amountNum)) return 0;
    return amountNum + calculateProcessingFee();
  };

  const generateIdempotencyKey = () => {
    const newKey = uuidv4();
    idempotencyKeyRef.current = newKey;
    return newKey;
  };

  const clearIdempotencyKey = () => {
    idempotencyKeyRef.current = null;
    setPendingTransaction(false);
  };

  const hasPendingTransaction = () => {
    return pendingTransaction && idempotencyKeyRef.current !== null;
  };

  const resetTransactionState = () => {
    clearIdempotencyKey();
    generateIdempotencyKey();
    setPendingTransaction(false);
  };

  const fetchBankList = async () => {
    setIsLoadingBanks(true);
    try {
      const response = await bankCollectionService.getBankList();
      if (response && response.length > 0) {
        const options = response.map((bank: any) => ({
          value: bank.code,
          label: bank.name
        }));
        setBankOptions(options);
      } else {
        setBankOptions([]);
      }
    } catch (error) {
      setBankOptions([]);
      setErrorMessage('Failed to load banks');
    } finally {
      setIsLoadingBanks(false);
    }
  };

  const verifyAccountNumber = async (accountNumber: string, bankCode: string) => {
    if (!accountNumber || !bankCode || accountNumber.length !== 10) return;
    
    setIsVerifyingAccount(true);
    try {
      const response = await bankCollectionService.getUserBanks(accountNumber, bankCode);
      
      if (response && response.status === true) {
        setAccountName(response.data?.account_name || '');
        setErrors(prev => ({ ...prev, accountName: '', accountNumber: '' }));
      } else {
        setErrors(prev => ({ ...prev, accountNumber: response?.message || 'Account verification failed' }));
        setAccountName('');
      }
    } catch (error) {
      setErrors(prev => ({ ...prev, accountNumber: 'Failed to verify account' }));
      setAccountName('');
    } finally {
      setIsVerifyingAccount(false);
    }
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

  const saveBeneficiaryUser = async () => {
    setIsSavingBeneficiary(true);
    try {
      const userId = getUserId();
      const token = getToken();

      if (!userId || !token) {
        showToast('Authentication required', 'warning');
        setIsSavingBeneficiary(false);
        return;
      }

      let beneficiaryData;

      if (step === 'bank') {
        beneficiaryData = {
          userId: userId,
          beneficiaryType: 'bank',
          beneficiaryName: accountName, 
          accountNumber: accountNumber,
          accountName: accountName,
          bankCode: selectedBankOption.value,
          bankName: selectedBankOption.label,
          currency: selectedWallet?.currency || 'NGN',
          recipientUsername: null
        };
      } else {
        beneficiaryData = {
          userId: userId,
          beneficiaryType: 'user',
          beneficiaryName: recipientUsername, 
          recipientUsername: recipientUsername,
          currency: selectedWallet?.currency || 'NGN',
          accountNumber: null,
          accountName: null,
          bankCode: null,
          bankName: null
        };
      }

      const response = await beneficiaryService.saveBeneficiary(beneficiaryData);

      if (response && (response.status === 'success' || response.status === 'OK')) {
        showToast('Beneficiary saved successfully', 'success');
        setShowBeneficiaryModal(false);
        setTimeout(() => onClose(), 1000);
      } else {
        showToast(response?.message || 'Failed to save beneficiary', 'warning');
      }
    } catch (error: any) {
      console.error('Error saving beneficiary:', error);
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to save beneficiary';
      showToast(errorMessage, 'warning');
    } finally {
      setIsSavingBeneficiary(false);
    }
  };

  useEffect(() => {
    if (selectedBankOption && accountNumber && accountNumber.length === 10) {
      const timer = setTimeout(() => {
        verifyAccountNumber(accountNumber, selectedBankOption.value);
      }, 1000);
      return () => clearTimeout(timer);
    } else {
      setAccountName('');
    }
  }, [selectedBankOption, accountNumber]);

  useEffect(() => {
    if (isOpen) {
      generateIdempotencyKey();
      setPendingTransaction(false);
      
      const walletData = getWalletList();
      if (walletData && Array.isArray(walletData)) {
        const formattedWallets: WalletType[] = walletData.map((wallet, index) => ({
          id: index + 1,
          name: `${wallet.currency_code} Wallet`,
          balance: parseFloat(wallet.balance.toString()) || 0,
          currency: wallet.currency_code,
          isDefault: index === 0
        }));
        setWallets(formattedWallets);
        const defaultWallet = formattedWallets.find(w => w.isDefault);
        if (defaultWallet) setSelectedWallet(defaultWallet);
      }
      
      if (step === 'bank') fetchBankList();
    }
  }, [isOpen, step]);

  useEffect(() => {
    if (showPinModal && pin.every(digit => digit !== '') && pin.length === 4) {
      const timer = setTimeout(() => handlePinSubmit(), 100);
      return () => clearTimeout(timer);
    }
  }, [pin, showPinModal]);

  useEffect(() => {
    let timeout: NodeJS.Timeout;
    if (pendingTransaction) {
      timeout = setTimeout(() => {
        if (pendingTransaction) {
          resetTransactionState();
          setErrorMessage('Transaction timeout');
        }
      }, 2 * 60 * 1000);
    }
    return () => { if (timeout) clearTimeout(timeout); };
  }, [pendingTransaction]);

  useEffect(() => {
    if (!isOpen) {
      setStep('selection');
      setAmount('');
      setRawAmount('');
      setAccountNumber('');
      setRecipientUsername('');
      setErrors({});
      setShowPinModal(false);
      setPin(['', '', '', '']);
      setShowSuccessModal(false);
      setShowFailModal(false);
      setErrorMessage('');
      setBankName('');
      setAccountName('');
      setSelectedBankOption(null);
      setShowBeneficiaryModal(false);
      clearIdempotencyKey();
    }
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => { document.body.style.overflow = 'unset'; };
  }, [isOpen]);

  const handleQuickAmount = (value: string) => {
    const rawValue = value.replace(/,/g, '');
    setRawAmount(rawValue);
    setAmount(formatNumberWithCommas(value));
    if (errors.amount) setErrors(prev => ({ ...prev, amount: '' }));
  };

  const validateForm = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!selectedWallet) newErrors.wallet = 'Please select a wallet';

    if (!rawAmount || rawAmount.trim() === '') {
      newErrors.amount = 'Please enter an amount';
    } else {
      const numericAmount = parseFloat(rawAmount);
      if (isNaN(numericAmount) || numericAmount <= 0) {
        newErrors.amount = 'Please enter a valid amount';
      } else if (numericAmount > (selectedWallet?.balance || 0)) {
        newErrors.amount = 'Insufficient balance';
      }
    }

    if (step === 'bank') {
      if (!selectedBankOption || !bankName) newErrors.bankName = 'Please select a bank';
      if (!accountNumber || !/^\d{10}$/.test(accountNumber)) {
        newErrors.accountNumber = 'Please enter a valid account number';
      }
      if (!accountName || accountName.trim().length < 2) {
        newErrors.accountName = 'Please verify account name';
      }
    } else if (step === 'epay') {
      if (!recipientUsername || recipientUsername.trim().length < 3) {
        newErrors.recipientUsername = 'Please enter a valid username';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue.replace(/,/g, ''))) {
      const rawValue = inputValue.replace(/,/g, '');
      setRawAmount(rawValue);
      setAmount(formatNumberWithCommas(inputValue));
      if (errors.amount) setErrors(prev => ({ ...prev, amount: '' }));
    }
  };

  const handleBankChange = (selectedOption: any) => {
    setSelectedBankOption(selectedOption);
    setBankName(selectedOption ? selectedOption.label : '');
    setAccountName('');
    if (errors.bankName) setErrors(prev => ({ ...prev, bankName: '' }));
  };

  const handlePinChange = (index: number, value: string) => {
    if (!/^\d?$/.test(value)) return;
    const newPin = [...pin];
    newPin[index] = value;
    setPin(newPin);
    if (value && index < 3) {
      setTimeout(() => pinInputRefs.current[index + 1]?.focus(), 10);
    }
  };

  const handlePinKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !pin[index] && index > 0) {
      pinInputRefs.current[index - 1]?.focus();
    }
  };

  const handlePinPaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const numbers = e.clipboardData.getData('text').replace(/\D/g, '').split('').slice(0, 4);
    if (numbers.length === 4) {
      setPin([...numbers]);
      setTimeout(() => pinInputRefs.current[3]?.focus(), 0);
    }
  };

  const handleBack = () => {
    if (hasPendingTransaction()) {
      setErrorMessage('Cannot close while transaction is in progress');
      return;
    }
    if (showPinModal) {
      setShowPinModal(false);
      setPin(['', '', '', '']);
    } else if (step !== 'selection') {
      setStep('selection');
      setAmount('');
      setRawAmount('');
      setAccountNumber('');
      setRecipientUsername('');
      setErrors({});
      setSelectedBankOption(null);
      setAccountName('');
    } else {
      onClose();
    }
  };

  const handleWithdraw = async () => {
    if (!validateForm()) return;
    if (hasPendingTransaction()) {
      setErrorMessage('Transaction already in progress');
      return;
    }
    setShowPinModal(true);
    setTimeout(() => pinInputRefs.current[0]?.focus(), 100);
  };

  const handlePinSubmit = async () => {
    const enteredPin = pin.join('');
    if (enteredPin.length !== 4) return;
    if (isSubmittingPin || hasPendingTransaction()) return;

    setIsSubmittingPin(true);
    setPendingTransaction(true);
    setIsProcessing(true);
    
    try {
      const rawAmountValue = rawAmount.replace(/,/g, '');
      const numberValue = Number(rawAmountValue);
      const bigDecimalString = numberValue.toFixed(2);

      if (!bigDecimalString || parseFloat(bigDecimalString) <= 0) {
        setErrorMessage('Invalid amount');
        setPendingTransaction(false);
        setIsProcessing(false);
        setIsSubmittingPin(false);
        return;
      }

      if (!selectedWallet) {
        setErrorMessage('Wallet not found');
        setPendingTransaction(false);
        setIsProcessing(false);
        setIsSubmittingPin(false);
        return;
      }

      const walletInfo = getWallet(selectedWallet.currency);
      const idempotencyKey = idempotencyKeyRef.current;
      let response;

      if (step === 'bank') {
        response = await withdrawService.withdrawToBank({
          accountNumber,
          bankCode: selectedBankOption.value,
          accountName,
          username: getUsername(),
          amount: bigDecimalString,
          currency: selectedWallet.currency,
          walletId: getUserWalletId(),
          senderUserId: getUserId(), 
          currencySymbol: walletInfo?.symbol || '',
          idempotencyKey: idempotencyKey,
          password: enteredPin
        });
      } else {
        response = await withdrawService.transferToUser({
          username: getUsername(),
          recipientUsername,
          amount: bigDecimalString,
          currency: selectedWallet.currency,
          walletId: getUserWalletId(),
          senderUserId: getUserId(),
          currencySymbol: walletInfo?.symbol || '',
          password: enteredPin,
          idempotencyKey: idempotencyKey,
          note: `Transfer to ${recipientUsername}`
        });
      }

      // Check if response exists and has status
      if (response) {
        if (response.status === 'success' || response.status ==="OK") {
          const token = getToken();
          const userId = getUserId();
          
          if (token && userId) {
            const walletResponse = await walletService.getByUserId(userId, token);
            setWalletContainer(walletResponse.wallet_balances, walletResponse.hasTransferPin, walletResponse.walletId);
          }

          const successMessage = step === 'bank'
            ? `Withdrawal of ${walletInfo?.symbol}${formatNumberWithCommas(bigDecimalString)} to bank successful`
            : `Transfer of ${walletInfo?.symbol}${formatNumberWithCommas(bigDecimalString)} to ${recipientUsername} successful`;
          
          updateNotificationContainer({
            type: "PAYMENTS",
            description: successMessage,
            date: new Date().toISOString()
          });
          if (onWithdrawReloadSuccess) {
            onWithdrawReloadSuccess();
          } else {
            eventEmitter.emit('refreshBalance');
          }

          if (typeof window !== 'undefined') {
            if ((window as any).refreshNavbarNotifications) (window as any).refreshNavbarNotifications();
            if ((window as any).refreshWalletBalance) (window as any).refreshWalletBalance();
            if ((window as any).refreshTransactionHistory) (window as any).refreshTransactionHistory();
          }

          setShowSuccessModal(true);
          setShowPinModal(false);
          clearIdempotencyKey();
          setTimeout(() => {
            setShowBeneficiaryModal(true);
          }, 500);
        } else {
          const errorMessage = response.message || 'Transaction failed';
          showToast(errorMessage, 'warning');
          setErrorMessage(errorMessage);
          setShowFailModal(true);
          setShowPinModal(false);
          setPendingTransaction(true);
          throw new Error(errorMessage);
        }
      } else {
        const errorMessage = 'No response from server';
        showToast(errorMessage, 'warning');
        setErrorMessage(errorMessage);
        setShowFailModal(true);
        setShowPinModal(false);
        setPendingTransaction(true);
        throw new Error(errorMessage);
      }
    } catch (error: any) {
      let errorMessage = 'Transaction failed';
      if (error?.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error?.data?.message) {
        errorMessage = error.data.message;
      } else if (error?.message) {
        errorMessage = error.message;
      } else if (typeof error === 'string') {
        errorMessage = error;
      }
      generateIdempotencyKey();
      setPendingTransaction(false);
    
      if (errorMessage.toLowerCase().includes('user') && errorMessage.toLowerCase().includes('not found')) {
        setErrorMessage(errorMessage);
        setShowFailModal(true);
        setShowPinModal(false);
        showToast(errorMessage, 'warning');
      } else {
        setErrorMessage(errorMessage);
        setShowFailModal(true);
        setShowPinModal(false);
        showToast(errorMessage, 'warning');
      }
    } finally {
      setIsProcessing(false);
      setIsSubmittingPin(false);
      setPin(['', '', '', '']);
    }
  };

  const renderTransactionStatus = () => {
    if (hasPendingTransaction()) {
      return (
        <div className="wallet-transaction-status">
          <div className="wallet-status-warning">
            <div className="wallet-spinner-small" />
            <span>Transaction in progress...</span>
          </div>
          <button 
            className="wallet-reset-transaction-btn"
            onClick={resetTransactionState}
            type="button"
            disabled={isProcessing}
          >
            Reset
          </button>
        </div>
      );
    }
    return null;
  };

  const availableWallets = wallets.filter(wallet => !wallet.name.includes('Savings'));

  if (!isOpen) return null;

  return (
    <>
      <div className={`drawer-overlay ${isOpen ? 'open' : ''}`} onClick={onClose} />
        <div className={`withdraw-drawer ${isOpen ? 'open' : ''} ${theme}`}>
          <div className="toastrs">
            {toasts.map((toast) => (
              <div key={toast.id} className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}>
                <div className="toast-icon">
                  <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`} aria-hidden="true"></i>
                </div>
                <div className="toast-message">{toast.message}</div>
              </div>
            ))}
          </div>
        <div className="drawer-header">
          <div className="header-content">
            {step !== 'selection' && (
              <button className="back-button" onClick={handleBack}>
                <ArrowLeft size={20} />
              </button>
            )}
            <div className="header-icon-wrapper">
              {step === 'selection' ? <Wallet size={20} /> : step === 'bank' ? <Landmark size={20} /> : <User size={20} />}
            </div>
            <div>
              <h2 className="drawer-title">
                {step === 'selection' ? 'Withdraw Funds' : step === 'bank' ? 'Transfer to Bank' : 'Transfer to Epay Account'}
              </h2>
              <p className="drawer-subtitle">
                {step === 'selection' ? 'Choose your withdrawal method' : step === 'bank' ? 'Withdraw funds to your bank account' : 'Transfer to another epay user'}
              </p>
            </div>
          </div>
          <button className="close-btn" onClick={onClose}>
            <X size={22} />
          </button>
        </div>
        
        <div className="drawer-main-section">
          {/* {renderTransactionStatus()} */}
          <div className="drawer-body">
            {step === 'selection' ? (
              <div className="options-list">
                <div className="withdraw-option-card" onClick={() => setStep('bank')}>
                  <div className="option-icon-wrapper blue-bg">
                    <Landmark size={24} />
                  </div>
                  <div className="option-content">
                    <h4 className="option-title">Transfer to Bank</h4>
                    <p className="option-desc">Withdraw funds to your bank account</p>
                  </div>
                  <ArrowRight size={20} className="option-arrow" />
                </div>
                <div className="withdraw-option-card" onClick={() => setStep('epay')}>
                  <div className="option-icon-wrapper green-bg">
                    <User size={24} />
                  </div>
                  <div className="option-content">
                    <h4 className="option-title">Transfer to Epay Account</h4>
                    <p className="option-desc">Send funds to another epay user</p>
                  </div>
                  <ArrowRight size={20} className="option-arrow" />
                </div>
              </div>
            ) : step === 'bank' ? (
              <>
                <div className="wallet-selector">
                  <label className="section-label">From Wallet</label>
                  <button className="wallet-select-btn" onClick={() => setShowWalletModal(true)}>
                    {selectedWallet ? (
                      <div className="wallet-info">
                        <Wallet size={16} />
                        <span className="wallet-name">{selectedWallet.name}</span>
                        <span className="wallet-balance">
                          {formatBalance(selectedWallet.balance, getFiat() || '')}
                        </span>
                      </div>
                    ) : (
                      <div className="wallet-info">
                        <Wallet size={16} />
                        <span>Select Wallet</span>
                      </div>
                    )}
                    <ChevronDown size={18} />
                  </button>
                  {errors.wallet && <span className="error-message">{errors.wallet}</span>}
                </div>

                <div className="form-section">
                  <label className="section-label">Bank Name</label>
                  <Select
                    options={bankOptions}
                    value={selectedBankOption}
                    onChange={handleBankChange}
                    isDisabled={isProcessing || isLoadingBanks}
                    isLoading={isLoadingBanks}
                    styles={theme === 'dark' ? customStylesDark : customStyles}
                    placeholder={isLoadingBanks ? "Loading..." : "Select Bank"}
                    noOptionsMessage={() => "No banks available"}
                    className={`react-select-container ${errors.bankName ? 'error-form' : ''}`}
                  />
                  {errors.bankName && <span className="error-message">{errors.bankName}</span>}
                </div>

                <div className="form-section">
                  <label className="section-label">Account Number</label>
                  <input
                    type="text"
                    className={`form-input ${errors.accountNumber ? 'error' : ''}`}
                    placeholder="Enter account number"
                    value={accountNumber}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      setAccountNumber(value);
                      if (errors.accountNumber) setErrors(prev => ({ ...prev, accountNumber: '' }));
                    }}
                    maxLength={10}
                    disabled={isProcessing}
                  />
                  {errors.accountNumber && <span className="error-message">{errors.accountNumber}</span>}
                </div>

                <div className="form-section">
                  <label className="section-label">Account Name</label>
                  <input
                    type="text"
                    className={`form-input disabled ${accountName ? 'verified' : ''}`}
                    value={accountName}
                    placeholder={isVerifyingAccount ? "Verifying..." : "Account holder name"}
                    disabled
                    readOnly
                  />
                  {errors.accountName && <span className="error-message">{errors.accountName}</span>}
                </div>
              </>
            ) : step === 'epay' ? (
              <>
                <div className="wallet-selector">
                  <label className="section-label">From Wallet</label>
                  <button className="wallet-select-btn" onClick={() => setShowWalletModal(true)}>
                    {selectedWallet ? (
                      <div className="wallet-info">
                        <Wallet size={16} />
                        <span className="wallet-name">{selectedWallet.name}</span>
                        <span className="wallet-balance">
                          {formatBalance(selectedWallet.balance, getFiat() || '')}
                        </span>
                      </div>
                    ) : (
                      <div className="wallet-info">
                        <Wallet size={16} />
                        <span>Select Wallet</span>
                      </div>
                    )}
                    <ChevronDown size={18} />
                  </button>
                  {errors.wallet && <span className="error-message">{errors.wallet}</span>}
                </div>

                <div className="form-section">
                  <label className="section-label">Recipient Username</label>
                  <input
                    type="text"
                    className={`form-input ${errors.recipientUsername ? 'error' : ''}`}
                    placeholder="Enter recipient's username"
                    value={recipientUsername}
                    onChange={(e) => {
                      setRecipientUsername(e.target.value);
                      if (errors.recipientUsername) setErrors(prev => ({ ...prev, recipientUsername: '' }));
                    }}
                    disabled={isProcessing}
                  />
                  {errors.recipientUsername && <span className="error-message">{errors.recipientUsername}</span>}
                </div>
              </>
            ) : null}

            {(step === 'bank' || step === 'epay') && (
              <>
                <div className="amount-section">
                  <label className="section-label">Amount</label>
                  <div className="amount-input-wrapper">
                    <span className="currency-symbol">
                      {selectedWallet ? getFiat() || '' : '₦'}
                    </span>
                    <input
                      type="text"
                      className={`amount-input ${errors.amount ? 'error' : ''}`}
                      placeholder="0.00"
                      value={amount}
                      onChange={handleAmountChange}
                      disabled={isProcessing}
                    />
                  </div>
                  {errors.amount && <span className="error-message">{errors.amount}</span>}
                  
                  {selectedWallet?.currency === 'NGN' && (
                    <div className="quick-amounts">
                      {['50,000', '100,000', '200,000', '500,000'].map(val => (
                        <button key={val} className="quick-amount-btn" onClick={() => handleQuickAmount(val)} disabled={isProcessing}>
                          ₦{val}
                        </button>
                      ))}
                    </div>
                  )}
                </div>

                <div className="summary-card">
                  <div className="summary-row">
                    <span className="summary-label">Amount</span>
                    <span className="summary-value">
                      {selectedWallet ? getFiat() || '' : '₦'}{amount || '0.00'}
                    </span>
                  </div>
                  <div className="summary-row">
                    <span className="summary-label">Fee</span>
                    <span className="summary-value">
                      {selectedWallet ? getFiat() || '' : '₦'}{calculateProcessingFee().toFixed(2)}
                    </span>
                  </div>
                  <div className="summary-divider" />
                  <div className="summary-row total-row">
                    <span className="summary-label">Total</span>
                    <span className="summary-value total-value">
                      {selectedWallet ? getFiat() || '' : '₦'}{formatNumberWithCommas(calculateTotal().toFixed(2))}
                    </span>
                  </div>
                </div>
              </>
            )}
          </div>

          {step !== 'selection' && (
            <div className="drawer-footer">
              <div className="security-note">
                <ShieldCheck size={14} />
                <span>Secured with encryption</span>
              </div>
              <div className="footer-buttons">
                <button className="cancel-btn" onClick={handleBack} disabled={isProcessing}>
                  Cancel
                </button>
                <button 
                  className="confirm-btn" 
                  onClick={handleWithdraw} 
                  disabled={isProcessing || !selectedWallet || isVerifyingAccount || hasPendingTransaction()}
                >
                  {isProcessing || hasPendingTransaction() ? (
                    <>
                      <div className="spinner" />
                      <span>Processing...</span>
                    </>
                  ) : (
                    <>
                      <Lock size={16} />
                      <span>Complete</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
      
      {showWalletModal && (
        <>
          <div className="status-modal-overlay" onClick={() => setShowWalletModal(false)} />
          <div className={`status-modal wallet-modal ${theme}`}>
            <div className="status-modal-content">
              <h3 className="status-modal-title">Select Wallet</h3>
              <div className="wallets-list">
                {availableWallets.map(wallet => (
                  <div
                    key={wallet.id}
                    className={`wallet-item ${selectedWallet?.id === wallet.id ? 'selected' : ''}`}
                    onClick={() => {
                      setSelectedWallet(wallet);
                      setFiat(wallet.currency);
                      setShowWalletModal(false);
                      if (errors.wallet) setErrors(prev => ({ ...prev, wallet: '' }));
                    }}
                  >
                    <div className="wallet-icon">
                      <Wallet size={20} />
                    </div>
                    <div className="wallet-details">
                      <h4>{wallet.name}</h4>
                      <p>{formatBalance(wallet.balance, getWallet(wallet.currency)?.symbol || '')}</p>
                    </div>
                    {selectedWallet?.id === wallet.id && <CheckCircle size={20} className="selected-icon" />}
                  </div>
                ))}
              </div>
              <button className="status-modal-btn secondary-btn" onClick={() => setShowWalletModal(false)}>
                Close
              </button>
            </div>
          </div>
        </>
      )}

      {showPinModal && (
        <>
          <div className="status-modal-overlay" onClick={() => !isProcessing && setShowPinModal(false)} />
          <div className={`status-modal pin-modal ${theme}`}>
            <div className="status-modal-content">
              <h3 className="status-modal-title">Enter PIN</h3>
              <p className="status-modal-message">Enter your 4-digit transfer PIN</p>
              <div className="pin-inputs">
                {pin.map((digit, index) => (
                  <input
                    key={index}
                    ref={(el) => {pinInputRefs.current[index] = el;}}
                    type="password"
                    inputMode="numeric"
                    className="pin-input"
                    maxLength={1}
                    value={digit}
                    onChange={(e) => handlePinChange(index, e.target.value)}
                    onKeyDown={(e) => handlePinKeyDown(index, e)}
                    onPaste={handlePinPaste}
                    disabled={isProcessing}
                    autoFocus={index === 0}
                  />
                ))}
              </div>
              <div className="status-modal-actions">
                <button className="status-modal-btn secondary-btn" onClick={() => { setShowPinModal(false); setPin(['', '', '', '']); }} disabled={isProcessing}>
                  Cancel
                </button>
                <button className="status-modal-btn confirm-btn" onClick={handlePinSubmit} disabled={isProcessing || pin.some(d => d === '')}>
                  {isProcessing ? <><div className="spinner-small" /><span>Processing...</span></> : 'Confirm'}
                </button>
              </div>
            </div>
          </div>
        </>
      )}

      {showSuccessModal && (
        <>
          <div className="status-modal-overlay" onClick={() => { setShowSuccessModal(false); onClose(); }} />
          <div className={`status-modal success-modal ${theme}`}>
            <div className="status-modal-content">
              <div className="status-icon-wrapper success-icon">
                <CheckCircle size={48} />
              </div>
              <h3 className="status-modal-title">Success!</h3>
              <p className="status-modal-message">
                {step === 'bank' ? 'Withdrawal successful' : 'Transfer successful'}
              </p>
              <button className="status-modal-btn confirm-btn" onClick={() => { setShowSuccessModal(false); onClose(); }}>
                Done
              </button>
            </div>
          </div>
        </>
      )}

      {showBeneficiaryModal && (
        <>
          <div className="status-modal-overlay" onClick={() => !isSavingBeneficiary && setShowBeneficiaryModal(false)} />
          <div className={`status-modal beneficiary-modal ${theme}`}>
            <div className="status-modal-content">
              <div className="status-icon-wrapper info-icon">
                <Star size={48} />
              </div>
              <h3 className="status-modal-title">Save as Beneficiary?</h3>
              <p className="status-modal-message">
                {step === 'bank' 
                  ? `Would you like to save ${accountName} for future transfers?`
                  : `Would you like to save ${recipientUsername} for future transfers?`
                }
              </p>

              <div className="status-modal-actions" style={{ marginTop: '30px' }}>
                <button 
                  className="status-modal-btn secondary-btn" 
                  onClick={() => {
                    setShowBeneficiaryModal(false);
                    setTimeout(() => onClose(), 300);
                  }} 
                  disabled={isSavingBeneficiary}
                >
                  Skip
                </button>
                <button 
                  className="status-modal-btn confirm-btn" 
                  onClick={saveBeneficiaryUser}
                  disabled={isSavingBeneficiary}
                >
                  {isSavingBeneficiary ? (
                    <>
                      <div className="spinner-small" />
                      <span>Saving...</span>
                    </>
                  ) : (
                    <>
                      <Star size={16} />
                      <span>Save</span>
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </>
      )}

      {showFailModal && (
        <>
          <div className="status-modal-overlay" onClick={() => setShowFailModal(false)} />
          <div className={`status-modal fail-modal ${theme}`}>
            <div className="status-modal-content">
              <div className="status-icon-wrapper fail-icon">
                <AlertCircle size={48} />
              </div>
              <h3 className="status-modal-title">Failed</h3>
              <p className="status-modal-message">{errorMessage}</p>
              <div className="status-modal-actions">
                <button className="status-modal-btn secondary-btn" onClick={() => setShowFailModal(false)}>
                  Cancel
                </button>
                <button className="status-modal-btn fail-btn" onClick={() => {
                  setShowFailModal(false);
                  setShowPinModal(true);
                  setPin(['', '', '', '']);
                  setTimeout(() => pinInputRefs.current[0]?.focus(), 100);
                }}>
                  Retry
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
};

export default WithdrawModal;