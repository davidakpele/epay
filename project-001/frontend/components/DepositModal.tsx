'use client';

import React, { useState, useEffect, Suspense, lazy } from 'react';
import { X, Landmark, CreditCard, Smartphone, ArrowRight, ShieldCheck, Copy, CheckCircle, Lock, ArrowLeft, AlertCircle } from 'lucide-react';
import './DepositModal.css';
import { DepositModalProps } from '@/app/types/utils';
import { 
  bankCollectionService, 
  getUserId, 
  getUsername, 
  getUserEmail, 
  getUserWalletId,
  getActiveWallet,
  getFiat,
  getWallet,
  depositService,
  updateNotificationContainer 
} from '@/app/api';
import { eventEmitter } from '@/app/utils/eventEmitter';
import { useRouter } from 'next/navigation';

const BankerLoader = lazy(() => import('@/components/BankerLoader'));

const DepositModal = ({ isOpen, onClose, theme, onDepositSuccess }: DepositModalProps) => {
  const [step, setStep] = useState<'selection' | 'bank' | 'card' | 'ussd'>('selection');
  const [bankStep, setBankStep] = useState<'select' | 'details'>('select');
  const [selectedBank, setSelectedBank] = useState<any>(null);
  const [amount, setAmount] = useState('');
  const [rawAmount, setRawAmount] = useState('');
  const [copied, setCopied] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [errors, setErrors] = useState<{ amount?: string }>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showFailModal, setShowFailModal] = useState(false);
  const [isEmpty, setIsEmpty] = useState(true);
  const [bankList, setBankList] = useState<any[]>([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoadingBanks, setIsLoadingBanks] = useState(false);
  const router = useRouter();
  const fetchBanks = () => {
    setIsLoadingBanks(true);
    const userId = getUserId();
    bankCollectionService.getByUserId(userId)
      .then(async (response) => {
        if (response.data && Array.isArray(response.data) && response.data.length > 0) {
          setBankList(response.data);
          setIsEmpty(false);
          setTimeout(() => {
            setIsLoadingBanks(false);
          }, 2000);
        } else {
          setIsEmpty(true);
          setTimeout(() => {
            setIsLoadingBanks(false);
          }, 2000);
        }
      })
      .catch((error) => {
        setTimeout(() => {
          setIsLoadingBanks(false);
        }, 2000);
        setIsEmpty(true);
      });
  };
  
  const filteredBanks = bankList.filter(bank => {
    if (!bank) return false;
    const bankName = bank.bank_name || '';
    const accountName = bank.account_holder_name || '';
    const accountNumber = bank.account_number || '';
    
    return (
      bankName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      accountName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      accountNumber.includes(searchQuery)
    );
  });

  const formatNumberWithCommas = (value: string): string => {
    const cleanValue = value.replace(/,/g, '');
    if (!cleanValue) return '';
    const parts = cleanValue.split('.');
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return parts.join('.');
  };

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) {
      setStep('selection');
      setBankStep('select');
      setSelectedBank(null);
      setAmount('');
      setRawAmount('');
      setErrors({});
      setSearchQuery('');
      setShowSuccessModal(false);
      setShowFailModal(false);
      setErrorMessage('');
      setBankList([]);
    }
  }, [isOpen]);

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const validateAmount = (): boolean => {
    const newErrors: { amount?: string } = {};
    if (!rawAmount || rawAmount.trim() === '') {
      newErrors.amount = 'Please enter an amount';
      setErrors(newErrors);
      return false;
    }
    const numericAmount = parseFloat(rawAmount);
    if (isNaN(numericAmount) || numericAmount <= 0) {
      newErrors.amount = 'Please enter a valid amount greater than 0';
      setErrors(newErrors);
      return false;
    }
    if (numericAmount < 100) {
      newErrors.amount = 'Minimum deposit amount is ₦100';
      setErrors(newErrors);
      return false;
    }
    setErrors({});
    return true;
  };

  const handleDeposit = async () => {
    if (!validateAmount()) return;
    setIsProcessing(true);
    
    if (!rawAmount || parseFloat(rawAmount) <= 0) return;
    
    try {
      const formattedAmount = parseFloat(rawAmount.replace(/,/g, '')).toFixed(2);
      
      const active_wallet = getActiveWallet();
      const user_id = getUserId();
      const wallet_id = getUserWalletId();
      const username = getUsername();
      const userEmail = getUserEmail();
      const fiat = getFiat();

      const fiatCurrency = fiat || 'NGN';
      const extract_symbol = getWallet(fiatCurrency);  
      
      let payloads: any = {
        userId: user_id,
        email: userEmail,
        username: username,
        walletId: wallet_id,
        amount: formattedAmount,
        type: "DEPOSIT",
        currencyType: active_wallet,
        currencySymbol: extract_symbol?.symbol || getFiat(),
      };

      if (step === 'bank') {
        payloads = {
          ...payloads,
          accountHolderName: selectedBank?.account_holder_name || "",
          accountNumber: selectedBank?.account_number || "",
          bankCode: selectedBank?.bank_code || "",
          bankName: selectedBank?.bank_name || "",
          depositSystem: "PAYSTACK"
        };
      } else if (step === 'card') {
        const cardFee = (parseFloat(rawAmount) * 0.015).toFixed(2);
        const totalAmount = (parseFloat(rawAmount) * 1.015).toFixed(2);
        payloads = {
          ...payloads,
          processingFee: cardFee,
          totalAmount: totalAmount,
          depositSystem: "PAYSTACK_CARD"
        };
      } else if (step === 'ussd') {
        payloads = {
          ...payloads,
          ussdCode: `*737*50*${rawAmount}#`,
          depositSystem: "PAYSTACK_USSD"
        };
      }

      await depositService.create(payloads)
        .then((response) => {
          if (response.status === 'success') {
            setIsProcessing(false);
            if (onDepositSuccess) {
              onDepositSuccess();
            }else{
              eventEmitter.emit('refreshBalance');
            }
            setShowSuccessModal(true);
            
            updateNotificationContainer({
              type: "MESSAGES",
              description: `${extract_symbol?.symbol || getFiat()}${amount}` + " Successfully Deposited.",
              date: new Date().toISOString()
            });
            
            if (typeof window !== 'undefined') {
              if ((window as any).refreshNavbarNotifications) {
                (window as any).refreshNavbarNotifications();
              }
              
              if ((window as any).refreshWalletBalance) {
                (window as any).refreshWalletBalance();
              }
            }
          } else {
            updateNotificationContainer({
              type: "MESSAGES",
              description: "Deposit failed. Please try again."
            });
            setIsProcessing(false);
            setErrorMessage("We couldn't process your deposit. Please try again.");
            setShowFailModal(true);
          }
        })
        .catch((error) => {
          const errorMsg = error?.response?.data?.message || error?.message || "An unexpected error occurred";
          setErrorMessage(errorMsg);
          setShowFailModal(true);
        });
    } catch (error: any) {
      const errorMsg = error?.response?.data?.message || error?.message || "An unexpected error occurred";
      setErrorMessage(errorMsg);
      setShowFailModal(true);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value;
    if (inputValue === '' || /^\d*\.?\d*$/.test(inputValue.replace(/,/g, ''))) {
      const rawValue = inputValue.replace(/,/g, '');
      setRawAmount(rawValue);
      setAmount(formatNumberWithCommas(inputValue));
      if (errors.amount) {
        setErrors({});
      }
    }
  };

  const handleQuickAmount = (value: string) => {
    const rawValue = value.replace(/,/g, '');
    setRawAmount(rawValue);
    setAmount(formatNumberWithCommas(value));
    if (errors.amount) {
      setErrors({});
    }
  };

  const handleBack = () => {
    if (step === 'bank' && bankStep === 'details') {
      setBankStep('select');
      setAmount('');
      setRawAmount('');
      setErrors({});
    } else {
      setStep('selection');
      setBankStep('select');
      setSelectedBank(null);
      setAmount('');
      setRawAmount('');
      setErrors({});
      setSearchQuery('');
    }
  };

  const handleBankClick = (bank: any) => {
    if (selectedBank?.id === bank.id) {
      setSelectedBank(null);
    } else {
      setSelectedBank(bank);
      setBankStep('details');
    }
  };

  const handleSuccessClose = () => {
    setAmount('');
    setRawAmount('');
    setShowSuccessModal(false);
    onClose();
  };

  const handleFailClose = () => {
    setShowFailModal(false);
  };

  const handleRedirect =()=>{
    router.push("/banks");
  }

  if (!isOpen) return null;

  return (
    <>
      <div className={`drawer-overlay ${isOpen ? 'open' : ''}`} onClick={onClose} />
      <div className={`deposit-drawer ${isOpen ? 'open' : ''} ${theme}`}>
        <div className="drawer-header">
          <div className="header-content">
            {(step !== 'selection') && (
              <button className="back-button" onClick={handleBack}>
                <ArrowLeft size={20} />
              </button>
            )}
            <div className="header-icon-wrapper">
              {step === 'selection' ? <Landmark size={20} /> : 
               step === 'bank' ? <Landmark size={20} /> :
               step === 'card' ? <CreditCard size={20} /> :
               <Smartphone size={20} />}
            </div>
            <div>
              <h2 className="drawer-title">
                {step === 'selection' ? 'Fund Your Wallet' :
                 step === 'bank' && bankStep === 'select' ? 'Select Bank Account' :
                 step === 'bank' && bankStep === 'details' ? 'Bank Transfer' :
                 step === 'card' ? 'Debit Card' :
                 'USSD Deposit'}
              </h2>
              <p className="drawer-subtitle">
                {step === 'selection' ? 'Choose your preferred deposit method' :
                 step === 'bank' && bankStep === 'select' ? 'Choose which account to fund' :
                 step === 'bank' && bankStep === 'details' ? 'Transfer to your unique account number' :
                 step === 'card' ? 'Top up instantly using your card' :
                 'Dial a code to deposit from your bank'}
              </p>
            </div>
          </div>
          <button className="close-btn" onClick={onClose}>
            <X size={22} />
          </button>
        </div>
        <div className="drawer-main-section">
            <div className="drawer-body">
              {step === 'selection' ? (
                <div className="options-list">
                  <div className="deposit-option-card" onClick={async () => {
                    setStep('bank');
                    await fetchBanks();
                  }}>
                    <div className="option-icon-wrapper blue-bg">
                      <Landmark size={24} />
                    </div>
                    <div className="option-content">
                      <h4 className="option-title">Bank Deposit</h4>
                      <p className="option-desc">Deposit via your unique account number</p>
                    </div>
                    <ArrowRight size={20} className="option-arrow" />
                  </div>
                  <div className="deposit-option-card" onClick={() => setStep('card')}>
                    <div className="option-icon-wrapper green-bg">
                      <CreditCard size={24} />
                    </div>
                    <div className="option-content">
                      <h4 className="option-title">Debit Card</h4>
                      <p className="option-desc">Top up instantly using your card</p>
                    </div>
                    <ArrowRight size={20} className="option-arrow" />
                  </div>
                  <div className="deposit-option-card" onClick={() => setStep('ussd')}>
                    <div className="option-icon-wrapper orange-bg">
                      <Smartphone size={24} />
                    </div>
                    <div className="option-content">
                      <h4 className="option-title">USSD</h4>
                      <p className="option-desc">Dial a code to deposit from your bank</p>
                    </div>
                    <ArrowRight size={20} className="option-arrow" />
                  </div>
                </div>
              ) : step === 'bank' && bankStep === 'select' ? (
                <>
                  {isLoadingBanks ? (
                      <Suspense>
                        <BankerLoader />
                      </Suspense>
                  ) : (
                    <>
                      <div className="search-container">
                        <div className="search-input-wrapper">
                          <svg className="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="11" cy="11" r="8"/>
                            <path d="m21 21-4.35-4.35"/>
                          </svg>
                          <input 
                            type="text" 
                            className="search-input"
                            placeholder="Search by bank name, account name, or number..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                          />
                        </div>
                      </div>
                      <div className="bank-accounts-list">
                        {filteredBanks.length > 0 ? (
                          filteredBanks.map((bank) => (
                            <div 
                              key={bank.id} 
                              className={`bank-account-item ${selectedBank?.id === bank.id ? 'selected' : ''}`}
                              onClick={() => handleBankClick(bank)}
                            >
                              <div className="bank-icon-box">
                                {selectedBank?.id === bank.id ? <CheckCircle size={20} className="selected-check" /> : <Landmark size={20} />}
                              </div>
                              <div className="bank-account-info">
                                <h4 className="bank-name">{bank.bank_name || 'Unknown Bank'}</h4>
                                <p className="account-holder">{bank.account_holder_name || 'No Name'}</p>
                                <p className="account-number">{bank.account_number || 'No Account Number'}</p>
                              </div>
                              <div className="verified-badge">
                                <ShieldCheck size={14} />
                                <span>verified</span>
                              </div>
                            </div>
                          ))
                        ) : (
                          <div className="no-results">
                            <Landmark size={40} className="no-results-icon" />
                            <p className="no-results-text">No bank accounts found</p>
                            <button type='button' className='add-bank-button' onClick={handleRedirect}>Add new bank</button>
                          </div>
                        )}
                      </div>
                    </>
                  )}
                </>
              ) : step === 'bank' && bankStep === 'details' ? (
                <>
                  <div className="info-card">
                    <div className="info-header">
                      <ShieldCheck size={18} className="shield-icon" />
                      <span>Bank Transfer Details</span>
                    </div>
                    <div className="bank-detail-row">
                      <div className="detail-icon-box">
                        <Landmark size={16} />
                      </div>
                      <div className="detail-content">
                        <span className="detail-label">Bank Name</span>
                        <p className="wallet-account-details">{selectedBank?.bank_name || 'Unknown Bank'}</p>
                      </div>
                    </div>
                    <div className="bank-detail-row">
                      <div className="detail-icon-box">
                        <CreditCard size={16} />
                      </div>
                      <div className="detail-content">
                        <span className="detail-label">Account Number</span>
                        <div className="detail-value-row">
                          <p className="wallet-account-details">{selectedBank?.account_number || 'No Account Number'}</p>
                          <button className="copy-button" onClick={() => handleCopy(selectedBank?.account_number || '')}>
                            {copied ? <CheckCircle size={16} /> : <Copy size={16} />}
                          </button>
                        </div>
                      </div>
                    </div>
                    <div className="bank-detail-row">
                      <div className="detail-icon-box">
                        <ShieldCheck size={16} />
                      </div>
                      <div className="detail-content">
                        <span className="detail-label">Account Name</span>
                        <p className="wallet-account-details">{selectedBank?.account_holder_name || 'No Name'}</p>
                      </div>
                    </div>
                  </div>
                  <div className="amount-section">
                    <label className="section-label">Enter Amount</label>
                    <div className="amount-input-wrapper">
                      <span className="currency-symbol">{getFiat()}</span>
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
                    <div className="quick-amounts">
                      {['50,000', '100,000', '200,000', '500,000', '1,000,000'].map(val => (
                        <button key={val} className="quick-amount-btn" onClick={() => handleQuickAmount(val)} disabled={isProcessing}>{getFiat()}{val}</button>
                      ))}
                    </div>
                  </div>
                  <div className="summary-card">
                    <div className="summary-row">
                      <span className="summary-label">Amount</span>
                      <span className="summary-value">{getFiat()}{amount || '0.00'}</span>
                    </div>
                    <div className="summary-divider" />
                    <div className="summary-row total-row">
                      <span className="summary-label">Total to Deposit</span>
                      <span className="summary-value total-value">{getFiat()}{amount || '0.00'}</span>
                    </div>
                  </div>
                </>
              ) : step === 'card' ? (
                <>
                  <div className="info-card">
                    <div className="info-header">
                      <CreditCard size={18} className="shield-icon" />
                      <span>Card Payment</span>
                    </div>
                    <p className="card-info-text">Enter the amount you want to deposit. You'll be redirected to complete the card payment securely.</p>
                  </div>
                  <div className="amount-section">
                    <label className="section-label">Enter Amount</label>
                    <div className="amount-input-wrapper">
                      <span className="currency-symbol">{getFiat()}</span>
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
                    <div className="quick-amounts">
                      {['50,000', '100,000', '200,000', '500,000', '1,000,000'].map(val => (
                        <button key={val} className="quick-amount-btn" onClick={() => handleQuickAmount(val)} disabled={isProcessing}>{getFiat()}{val}</button>
                      ))}
                    </div>
                  </div>
                  <div className="summary-card">
                    <div className="summary-row">
                      <span className="summary-label">Amount</span>
                      <span className="summary-value">{getFiat()}{amount || '0.00'}</span>
                    </div>
                    <div className="summary-row">
                      <span className="summary-label">Card Processing Fee (1.5%)</span>
                      <span className="summary-value">{getFiat()}{rawAmount ? (parseFloat(rawAmount) * 0.015).toFixed(2) : '0.00'}</span>
                    </div>
                    <div className="summary-divider" />
                    <div className="summary-row total-row">
                      <span className="summary-label">Total to Pay</span>
                      <span className="summary-value total-value">
                        {getFiat()}{rawAmount ? formatNumberWithCommas((parseFloat(rawAmount) * 1.015).toFixed(2)) : '0.00'}
                      </span>
                    </div>
                  </div>
                </>
              ) : (
                <>
                  <div className="info-card">
                    <div className="info-header">
                      <Smartphone size={18} className="shield-icon" />
                      <span>USSD Deposit</span>
                    </div>
                    <p className="card-info-text">Dial the USSD code from your registered phone number to complete this deposit.</p>
                    <div className="ussd-code-box">
                      <span className="ussd-label">USSD Code</span>
                      <div className="ussd-code-display">
                        <span className="ussd-code">*737*50*{rawAmount || 'AMOUNT'}#</span>
                        <button className="copy-button-small" onClick={() => handleCopy(`*737*50*${rawAmount || '0'}#`)}>
                          {copied ? <CheckCircle size={14} /> : <Copy size={14} />}
                        </button>
                      </div>
                    </div>
                  </div>
                  <div className="amount-section">
                    <label className="section-label">Enter Amount</label>
                    <div className="amount-input-wrapper">
                      <span className="currency-symbol">{getFiat()}</span>
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
                    <div className="quick-amounts">
                      {['50,000', '100,000', '200,000', '500,000', '1,000,000'].map(val => (
                        <button key={val} className="quick-amount-btn" onClick={() => handleQuickAmount(val)} disabled={isProcessing}>{getFiat()}{val}</button>
                      ))}
                    </div>
                  </div>
                  <div className="summary-card">
                    <div className="summary-row">
                      <span className="summary-label">Amount</span>
                      <span className="summary-value">{getFiat()}{amount || '0.00'}</span>
                    </div>
                    <div className="summary-divider" />
                    <div className="summary-row total-row">
                      <span className="summary-label">Total to Deposit</span>
                      <span className="summary-value total-value">{getFiat()}{amount || '0.00'}</span>
                    </div>
                  </div>
                </>
              )}
            </div>

            {step !== 'selection' && !(step === 'bank' && bankStep === 'select') && (
              <div className="drawer-footer">
                <div className="footer-buttons">
                  <button className="cancel-btn" onClick={handleBack} disabled={isProcessing}>Back</button>
                  <button className="confirm-btn" onClick={handleDeposit} disabled={isProcessing}>
                    {isProcessing ? (
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

      {/* Success Modal */}
      {showSuccessModal && (
        <>
          <div className="status-modal-overlay" onClick={handleSuccessClose} />
          <div className={`status-modal success-modal ${theme}`}>
            <div className="status-modal-content">
              <div className="status-icon-wrapper success-icon">
                <CheckCircle size={48} />
              </div>
              <h3 className="status-modal-title">Deposit Successful!</h3>
              <p className="status-modal-message">
                Your deposit of {getFiat()}{amount} has been processed successfully. 
                The funds will reflect in your wallet shortly.
              </p>
              <div className="status-modal-details">
                <div className="status-detail-row">
                  <span className="status-detail-label">Amount</span>
                  <span className="status-detail-value">{getFiat()}{amount}</span>
                </div>
                <div className="status-detail-row">
                  <span className="status-detail-label">Method</span>
                  <span className="status-detail-value">
                    {step === 'bank' ? 'Bank Deposit' : step === 'card' ? 'Debit Card' : 'USSD'}
                  </span>
                </div>
              </div>
              <button className="status-modal-btn success-btn" onClick={handleSuccessClose}>
                Done
              </button>
            </div>
          </div>
        </>
      )}

      {/* Failure Modal */}
      {showFailModal && (
        <>
          <div className="status-modal-overlay" onClick={handleFailClose} />
          <div className={`status-modal fail-modal ${theme}`}>
            <div className="status-modal-content">
              <div className="status-icon-wrapper fail-icon">
                <AlertCircle size={48} />
              </div>
              <h3 className="status-modal-title">Deposit Failed</h3>
              <p className="status-modal-message">
                {errorMessage}
              </p>
              <div className="status-modal-actions">
                <button className="status-modal-btn secondary-btn" onClick={handleFailClose}>
                  Cancel
                </button>
                <button className="status-modal-btn fail-btn" onClick={() => {
                  setShowFailModal(false);
                  handleDeposit();
                }}>
                  Try Again
                </button>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
};

export default DepositModal;