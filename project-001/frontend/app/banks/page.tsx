'use client';

import { useState, useEffect, MouseEvent } from 'react';
import DepositModal from '@/components/DepositModal';
import Footer from '@/components/Footer';
import Header from '@/components/Header';
import MobileNav from '@/components/MobileNav';
import Sidebar from '@/components/Sidebar';
import "./Banks.css";
import { CheckCircle, Trash2Icon, Trash2 } from 'lucide-react';
import LoadingScreen from '@/components/loader/Loadingscreen';
import { Toast } from '../types/auth';
import { bankCollectionService, getUserId } from '../../app/api/index';
import Select from 'react-select';

type BankAccount = {
  id: string;
  bankName: string;
  accountNumber: string;
  accountName: string;
  currency: string;
  addedDate: string;
};

type NewBankForm = {
  bankName: string;
  bankCode: string;
  accountNumber: string;
  accountName: string;
  currency: string;
};

type BankOption = {
  value: string;
  label: string;
  code: string;
};

const customStyles = {
  control: (base: any, state: any) => ({
    ...base,
    backgroundColor: 'white',
    borderColor: state.isFocused ? '#764ba2' : '#e2e8f0',
    borderRadius: '8px',
    padding: '2px 8px',
    minHeight: '45px',
    boxShadow: state.isFocused ? '0 0 0 3px rgba(118, 75, 162, 0.1)' : 'none',
    borderWidth: '2px',
    '&:hover': {
      borderColor: state.isFocused ? '#764ba2' : '#cbd5e1'
    }
  }),
  option: (base: any, state: any) => ({
    ...base,
    backgroundColor: state.isSelected ? '#764ba2' : state.isFocused ? '#f5f0ff' : 'white',
    color: state.isSelected ? 'white' : state.isFocused ? '#764ba2' : '#334155',
    padding: '10px 12px',
    cursor: 'pointer',
    '&:active': {
      backgroundColor: '#5d3a8c'
    }
  }),
  menu: (base: any) => ({
    ...base,
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
    zIndex: 9999
  }),
  menuList: (base: any) => ({
    ...base,
    padding: '8px',
    backgroundColor: 'white',
    borderRadius: '8px'
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
    color: state.isFocused ? '#764ba2' : '#94a3b8',
    '&:hover': {
      color: '#764ba2'
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
    backgroundColor: '#1a1a2e',
    borderColor: state.isFocused ? '#764ba2' : '#2d2d44',
    borderRadius: '8px',
    padding: '2px 8px',
    minHeight: '45px',
    boxShadow: state.isFocused ? '0 0 0 3px rgba(118, 75, 162, 0.2)' : 'none',
    borderWidth: '2px',
    '&:hover': {
      borderColor: state.isFocused ? '#764ba2' : '#3d3d5c'
    }
  }),
  option: (base: any, state: any) => ({
    ...base,
    backgroundColor: state.isSelected ? '#764ba2' : state.isFocused ? '#2d2d44' : '#1a1a2e',
    color: state.isSelected ? 'white' : state.isFocused ? '#d1c4e9' : '#cbd5e1',
    padding: '10px 12px',
    cursor: 'pointer',
    '&:active': {
      backgroundColor: '#5d3a8c'
    }
  }),
  menu: (base: any) => ({
    ...base,
    backgroundColor: '#1a1a2e',
    borderRadius: '8px',
    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
    zIndex: 9999
  }),
  menuList: (base: any) => ({
    ...base,
    padding: '8px',
    backgroundColor: '#1a1a2e',
    borderRadius: '8px'
  }),
  singleValue: (base: any) => ({
    ...base,
    color: '#f1f5f9'
  }),
  placeholder: (base: any) => ({
    ...base,
    color: '#64748b'
  }),
  dropdownIndicator: (base: any, state: any) => ({
    ...base,
    color: state.isFocused ? '#764ba2' : '#64748b',
    '&:hover': {
      color: '#764ba2'
    }
  }),
  indicatorSeparator: (base: any) => ({
    ...base,
    backgroundColor: '#2d2d44'
  })
};

const Banks = () => {
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [bankToDelete, setBankToDelete] = useState<string | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [verify, setVerify] = useState(false);
  const [bankOptions, setBankOptions] = useState<BankOption[]>([]);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [banks, setBanks] = useState<BankAccount[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [selectedBankOption, setSelectedBankOption] = useState<any>(null);
  const [newBank, setNewBank] = useState<NewBankForm>({
    bankName: '',
    bankCode: '',
    accountNumber: '',
    accountName: '',
    currency: 'NGN'
  });
  const [isValid, setIsValid] = useState<boolean | null>(null);
  const [loading, setLoading] = useState(false);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [accountExist, setAccountExist] = useState(false);

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

  useEffect(() => {
    const loadingTimer = setTimeout(() => {
      setIsPageLoading(false);
    }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark';
    if (savedTheme) {
      setTheme(savedTheme);
    }
  }, []);

  useEffect(() => {
    fetchUserBanks();
    fetchBankList();
  }, []);

  const fetchUserBanks = async () => {
    try {
      const userId = getUserId();
      setLoading(true);

      const result = await bankCollectionService.getByUserId(userId);

      if (result?.data && Array.isArray(result.data)) {
        const formattedBanks = result.data.map((bank: any) => ({
          id: bank.id,
          bankCode: bank.bank_code,
          bankName: bank.bank_name,
          accountNumber: bank.account_number,
          accountName: bank.account_holder_name,
          currency: "NGN",
          addedDate: bank.created_on
            ? new Date(bank.created_on).toISOString().split("T")[0]
            : new Date().toISOString().split("T")[0],
        }));

        setBanks(formattedBanks);
      } else {
        showToast("Failed to load your bank accounts", "warning");
      }
    } catch (error) {
      showToast("Network error. Please try again.", "warning");
    } finally {
      setLoading(false);
    }
  };

  const fetchBankList = async () => {
    try {
      const response = await bankCollectionService.getBankList();
      if (response && response.length > 0) {
        const options = response.map((bank: any) => ({
          value: bank.code,
          label: bank.name,
          code: bank.code
        }));
        setBankOptions(options);
      } else {
        showToast('No banks found', 'warning');
      }
    } catch (error) {
      showToast('Failed to load bank list. Please refresh.', 'warning');
    }
  };

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
  };

  const handleBankChange = (selectedOption: any) => {
    setSelectedBankOption(selectedOption);
    if (selectedOption) {
      setNewBank({
        ...newBank,
        bankName: selectedOption.label,
        bankCode: selectedOption.value
      });
    } else {
      setNewBank({
        ...newBank,
        bankName: '',
        bankCode: ''
      });
    }
  };

  const validate = (): boolean => {
    if (!newBank.bankCode) {
      showToast('Please select your bank', 'warning');
      return false;
    }
    if (!newBank.accountNumber.trim() || newBank.accountNumber.length !== 10) {
      showToast('Valid account number (10 digits) is required', 'warning');
      return false;
    }
    return true;
  };

  const handleSearch = async () => {
    if (!validate()) return;

    try {
      setVerifying(true);

      const result = await bankCollectionService.getUserBanks(newBank.accountNumber, newBank.bankCode);
      
      if (result?.status) {
        setVerify(true);
        setNewBank({
          ...newBank,
          accountName: result.data.account_name
        });
        setIsValid(true);
        showToast('Account verified successfully!', 'success');
      } else {
        setVerify(false);
        setIsValid(false);
        showToast(result?.message || "Invalid Account Details", 'warning');
      }
    } catch (error: any) {
      setVerify(false);
      setIsValid(false);
      showToast(error?.message || "Network error. Please try again.", 'warning');
    } finally {
      setVerifying(false);
    }
  };

  const handleAddBank = async () => {
    if (banks.length >= 5) {
      showToast('Maximum 5 banks allowed per currency', 'warning');
      return;
    }

    if (!verify) {
      showToast('Please verify account details first', 'warning');
      return;
    }

    try {
      setLoading(true);

      const payload = { 
        bank_code: newBank.bankCode, 
        bank_name: newBank.bankName, 
        account_number: newBank.accountNumber,
        account_holder_name: newBank.accountName,
        user_id: getUserId(),
        currency: newBank.currency
      };

      const result = await bankCollectionService.create(payload);
      
      if (result?.status === 201) {
        setShowSuccessModal(true);
        
        const newBankAccount: BankAccount = {
          id: result.data?.id || Date.now().toString(),
          bankName: newBank.bankName,
          accountNumber: newBank.accountNumber,
          accountName: newBank.accountName,
          currency: newBank.currency,
          addedDate: new Date().toISOString().split('T')[0]
        };
        
        setBanks([...banks, newBankAccount]);
        setAccountExist(false);
        showToast("Bank successfully added!", 'success');
        
        setTimeout(() => {
          resetForm();
        }, 2000);
      } else {
        setAccountExist(true);
        showToast(result?.response?.message || "Error adding bank.", 'warning');
      }
    } catch (error: any) {
      showToast(error?.message || "Network error. Please try again.", 'warning');
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setNewBank({
      bankName: '',
      bankCode: '',
      accountNumber: '',
      accountName: '',
      currency: 'NGN'
    });
    setSelectedBankOption(null);
    setShowForm(false);
    setIsValid(null);
    setVerify(false);
    setAccountExist(false);
  };

  const handleCancelForm = () => {
    resetForm();
  };

  const handleDeleteClick = (bankId: string, e: MouseEvent) => {
    e.stopPropagation();
    setBankToDelete(bankId);
    setShowDeleteConfirm(true);
  };

  const confirmDelete = async () => {
    bankCollectionService.getByUserId
    if (bankToDelete) {
      try {
        const result = await bankCollectionService.delete(bankToDelete);
        
        if (result?.status) {
          setBanks(banks.filter(bank => bank.id !== bankToDelete));
          showToast('Bank account deleted successfully', 'success');
        } else {
          showToast('Failed to delete bank account', 'warning');
        }
      } catch (error) {
        showToast('Network error. Please try again.', 'warning');
      }
    }
    setShowDeleteConfirm(false);
    setBankToDelete(null);
  };

  const cancelDelete = () => {
    setShowDeleteConfirm(false);
    setBankToDelete(null);
  };

  const handleBankClick = (bank: BankAccount) => {
    console.log('Selected bank:', bank);
  };

   if (isPageLoading) {
    return <LoadingScreen />;
  }

  return (
    <div className="dashboard-container">
      <Sidebar />

      <main className={`main-content ${isDepositOpen ? 'blur-sm' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        
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

        <div className="scrollable-content">
          <div className="banks-page">
            {/* 2. Page Title & Primary Action */}
          <div className="page-header">
            <div>
              <h1 className="page-title">Deposit Accounts</h1>
              <p className="page-description">
                Manage the bank accounts where your Deposit are made.
              </p>
            </div>
          </div>
            <div className="banks-container">
              <div className="banks-grid">
                <div className="banks-card">
                  <h1 className="banks-title">Deposit Bank Account</h1>
                  <p className="banks-subtitle">Deposit your funds into your bank account.</p>
                  <p className="banks-note">
                    <span className="note-bold">Note:</span> You are only allowed to add a total of 5 banks per currency
                  </p>

                  {!showForm ? (
                    <button onClick={() => setShowForm(true)} className="add-bank-placeholder">
                      <span className="placeholder-plus">+</span>
                      <span className="placeholder-title">Add New Bank Account</span>
                      <span className="placeholder-subtitle">Click to add a new bank account</span>
                      <span className="placeholder-count">{banks.length}/5 banks added</span>
                    </button>
                  ) : (
                    <div className="add-bank-form">
                      <div className="form-header">
                        <h2 className="form-title">Add New Bank Account</h2>
                        <button onClick={handleCancelForm} className="bank-cancel-btn">Cancel</button>
                      </div>

                      <p className="form-note">
                        <span className="note-bold">NOTE:</span> Accounts added here are solely for deposits and not for withdrawals. 
                        Make sure it is your own account and the account name matches
                      </p>

                      {!verify ? (
                        <>
                          <div className="form-group">
                            <label className="form-label">Select Your Bank</label>
                            <Select
                              options={bankOptions}
                              value={selectedBankOption}
                              onChange={handleBankChange}
                              styles={theme === 'dark' ? customStylesDark : customStyles}
                              placeholder="Select Bank..."
                              className="bank-select"
                            />
                          </div>

                          <div className="form-group">
                            <label className="form-label">Account Number</label>
                            <input
                              type="text"
                              className="form-input"
                              placeholder="Enter account number"
                              value={newBank.accountNumber}
                              onChange={(e) => {
                                const value = e.target.value.replace(/\D/g, '');
                                if (value.length <= 10) {
                                  setNewBank({...newBank, accountNumber: value});
                                }
                              }}
                              maxLength={10}
                            />
                          </div>
                        </>
                      ) : (
                        <div className="verified-bank-display">
                          <div className={`currency1 mb-3 bank-item ${theme === "dark" ? "light-background" : "dark-background"}`}>
                            <div className="currency1__img-block">
                              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 67 67" width="30" height="30">
                                <path d="M7.722 57.657a.75.75 0 0 0-.75.75V64.5c0 .414.336.75.75.75h51.556a.75.75 0 0 0 .75-.75v-6.093a.75.75 0 0 0-.75-.75h-4.325V27.442h4.325a.75.75 0 0 0 .75-.75v-5.35a.75.75 0 0 0-.75-.75h-.187L33.94 1.899a.75.75 0 0 0-.895 0L7.91 20.593h-.187a.75.75 0 0 0-.75.75v5.349c0 .414.336.75.75.75h4.325v30.215H7.722zm50.806 6.093H8.472v-4.593h50.056v4.593zM24.214 27.442v30.215h-5.761V27.442h5.761zm12.167 0v30.215H30.62V27.442h5.761zm12.166 0v30.215h-5.761V27.442h5.761zm-7.261 30.215H37.88V27.442h3.405v30.215zm-12.166 0h-3.406V27.442h3.406v30.215zm24.333 0h-3.406V27.442h3.406v30.215zM33.493 3.435l23.083 17.158H10.423l23.07-17.158zM8.474 22.098h50.055v3.844H8.472v-3.844zm5.074 5.344h3.406v30.215h-3.406V27.442z"></path>
                                <path d="M33.5 12.448a1.167 1.167 0 0 1-1.166-1.166c0-.653.512-1.165 1.166-1.165.642 0 1.166.523 1.166 1.165a.75.75 0 0 0 1.5 0 2.66 2.66 0 0 0-1.916-2.544v-.415a.75.75 0 0 0-1.5 0v.415a2.66 2.66 0 0 0-1.916 2.544 2.668 2.668 0 0 0 2.666 2.666c.642 0 1.166.523 1.166 1.165s-.524 1.166-1.166 1.166-1.166-.523-1.166-1.166a.75.75 0 0 0-1.5 0 2.66 2.66 0 0 0 1.916 2.545v.477a.75.75 0 0 0 1.5 0v-.477a2.66 2.66 0 0 0 1.916-2.545 2.669 2.669 0 0 0-2.666-2.665z"></path>
                              </svg>
                            </div>
                            <div className="currency1__main">
                              <span className="currency1__abbr">{newBank.bankName}</span>
                              <div className="d-flex justify-content-between">
                                <span className="currency1__name">{newBank.accountName}</span>
                                <span className="verified-text">verified</span>
                              </div>
                              <span className="currency1__name">{newBank.accountNumber}</span>
                            </div>
                            <span className="currency1__check">
                              <CheckCircle className="check-icon" />
                            </span>
                          </div>
                        </div>
                      )}

                      <button
                        onClick={verify ? handleAddBank : handleSearch}
                        disabled={verifying || loading || !newBank.bankName || !newBank.accountNumber}
                        className="mt-3 submit-button"
                      >
                        {verifying ? "Checking..." : loading ? "Saving..." : verify ? "Add Bank Details" : "Search"}
                      </button>
                    </div>
                  )}
                </div>

                <div className="banks-card">
                  <div className="banks-list-header">
                    <h2 className="banks-title">Your Bank Accounts</h2>
                    <span className="bank-count">{banks.length}/5</span>
                  </div>

                  {loading && banks.length === 0 ? (
                    <div className="empty-state">
                      <p className="empty-title">Loading bank accounts...</p>
                    </div>
                  ) : banks.length === 0 ? (
                    <div className="empty-state">
                      <p className="empty-title">No bank accounts added yet</p>
                      <p className="empty-subtitle">Add your first bank account using the form</p>
                    </div>
                  ) : (
                    <div className="bank-list-container">
                      <div className="list-section">
                        <div className="mb-3">
                          {banks.map((bank) => (
                            <div key={bank.id} onClick={() => handleBankClick(bank)}>
                              <div className={`currency1 mb-3 bank-item ${theme === "dark" ? "light-background" : "dark-background"}`}>
                                <div 
                                  className="delete-icon-wrapper" 
                                  onClick={(e) => handleDeleteClick(bank.id, e)}
                                  title="Delete bank account"
                                >
                                  <Trash2Icon size={18} />
                                </div>
                                
                                <div className="currency1__img-block">
                                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 67 67" width="30" height="30">
                                    <path d="M7.722 57.657a.75.75 0 0 0-.75.75V64.5c0 .414.336.75.75.75h51.556a.75.75 0 0 0 .75-.75v-6.093a.75.75 0 0 0-.75-.75h-4.325V27.442h4.325a.75.75 0 0 0 .75-.75v-5.35a.75.75 0 0 0-.75-.75h-.187L33.94 1.899a.75.75 0 0 0-.895 0L7.91 20.593h-.187a.75.75 0 0 0-.75.75v5.349c0 .414.336.75.75.75h4.325v30.215H7.722zm50.806 6.093H8.472v-4.593h50.056v4.593zM24.214 27.442v30.215h-5.761V27.442h5.761zm12.167 0v30.215H30.62V27.442h5.761zm12.166 0v30.215h-5.761V27.442h5.761zm-7.261 30.215H37.88V27.442h3.405v30.215zm-12.166 0h-3.406V27.442h3.406v30.215zm24.333 0h-3.406V27.442h3.406v30.215zM33.493 3.435l23.083 17.158H10.423l23.07-17.158zM8.474 22.098h50.055v3.844H8.472v-3.844zm5.074 5.344h3.406v30.215h-3.406V27.442z"></path>
                                    <path d="M33.5 12.448a1.167 1.167 0 0 1-1.166-1.166c0-.653.512-1.165 1.166-1.165.642 0 1.166.523 1.166 1.165a.75.75 0 0 0 1.5 0 2.66 2.66 0 0 0-1.916-2.544v-.415a.75.75 0 0 0-1.5 0v.415a2.66 2.66 0 0 0-1.916 2.544 2.668 2.668 0 0 0 2.666 2.666c.642 0 1.166.523 1.166 1.165s-.524 1.166-1.166 1.166-1.166-.523-1.166-1.166a.75.75 0 0 0-1.5 0 2.66 2.66 0 0 0 1.916 2.545v.477a.75.75 0 0 0 1.5 0v-.477a2.66 2.66 0 0 0 1.916-2.545 2.669 2.669 0 0 0-2.666-2.665z"></path>
                                  </svg>
                                </div>
                                <div className="currency1__main">
                                  <span className="currency1__abbr">{bank.bankName}</span>
                                  <div className="d-flex justify-content-between">
                                    <span className="currency1__name">
                                      {bank.accountName}
                                    </span>
                                    <span className="verified-text">verified</span>
                                  </div>
                                  <span className="currency1__name">{bank.accountNumber}</span>
                                </div>
                                <span className="currency1__check">
                                  <CheckCircle className="check-icon" />
                                </span>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          {showSuccessModal && (
            <div className="modal-overlay">
              <div className="modal success-modal">
                <div className="success-icon">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <h3 className="modal-title">Bank Account Added Successfully!</h3>
                <p className="modal-text">Your bank account has been added and verified.</p>
                <button className="modal-btn success-btn" onClick={() => setShowSuccessModal(false)}>Done</button>
              </div>
            </div>
          )}

          {showDeleteConfirm && (
            <div className="modal-overlay">
              <div className="modal delete-modal">
                 <div className="bm-modal-icon delete">
                  <Trash2 size={50} style={{justifyItems:"center",
                     justifyContent:"center",
                      justifySelf:"center",
                      textAlign:"center",}}/>
                    </div>
                 <h3 className="modal-title" style={{textAlign:"center"}}> Delete Bank Account</h3>
                <p className="modal-text">Are you sure you want to delete this bank account? This action cannot be undone.</p>
                <div className="modal-actions">
                  <button className="modal-btn cancel-modal-btn" onClick={cancelDelete}>Cancel</button>
                  <button className="modal-btn delete-btn" onClick={confirmDelete}>Delete</button>
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
  );
};

export default Banks;