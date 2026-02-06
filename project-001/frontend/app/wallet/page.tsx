'use client';

import React, {ReactElement, useState, useEffect, useRef, Suspense  } from 'react';
import Link from 'next/link';

import { 
 ArrowDown, ArrowUp, Repeat, Share, CreditCard,
 Eye,
 EyeOff,
 Plus,
 ArrowDownLeft,
 CheckCircle, Clock, XCircle, 
 ChevronUp,
 ChevronDown,
 Copy,
 Search
} from 'lucide-react';
import './WalletStyle.css';
import './WalletHistory.css';
import Sidebar from '@/components/Sidebar';
import { Currency } from '../types/api';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import DepositModal from '@/components/DepositModal';
import WithdrawModal from '@/components/WithdrawModal';
import { StatusInfo, Transaction, TransactionStatus, TransactionType } from '../types/utils';
import { formatAmount } from '../lib/walletCrate';
import LoadingScreen from '@/components/loader/Loadingscreen';
import { getUserId, getWallet, historyService, setActiveWallet, setFiat } from '../api';


const TransactionReceipt = React.lazy(
  () => import('@/components/TransactionReceipt')
);

const DeleteHistoryModal = React.lazy(
  () => import('@/components/DeleteHistoryModal')
);

const Wallet = () => {
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  type DateField = "startDate" | "endDate";
  const [showBalance, setShowBalance] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isHistoryFilterModalOpen, setIsHistoryFilterModalOpen] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [isScrolling, setIsScrolling] = useState(false);
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [isWithdrawOpen, setIsWithdrawOpen] = useState(false);
  const scrollTimer = useRef<NodeJS.Timeout | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [transactionsError, setTransactionsError] = useState('')
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [activeTab, setActiveTab] = useState("All");
  const [loading, setLoading] = useState(false);
  const [showDateFilter, setShowDateFilter] = useState(false);
  const [expandedTransaction, setExpandedTransaction] = useState<string | null>(null);
  const [deleteLoading, setDeleteLoading] = useState<string | null>(null);
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);
  const [currentBalance, setCurrentBalance] = useState<number>(0);
  const [showReceipt, setShowReceipt] = useState(false);
  const isFetchingRef = useRef(false);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [searchHistoryTerm, setSearchHistoryTerm] = useState('');
  const tabs: string[] = ["All", "Swaps", "Withdrawals", "Deposits", "Credited"];
  const [deleteModalTransaction, setDeleteModalTransaction] = useState<Transaction | null>(null);

  const fetchTransactionHistory = async () => {
    if (isFetchingRef.current) {
      return;
    }

    try {
      isFetchingRef.current = true;
      setTransactionsLoading(true);
      setTransactionsError('');
      
      // Get userId from auth
      const userId = getUserId();
      if (!userId) {
        setTransactionsError('Please login to view transaction history');
        setTransactions([]);
        return;
      }
      
      // Fetch real data from API
      const response = await historyService.getHistory(userId);
      
      // Transform API response to Transaction format
      const transformedTransactions = transformApiResponseToTransactions(response);
      setTransactions(transformedTransactions);
      
    } catch (error: any) {
      console.error('Error fetching transaction history:', error);
      setTransactionsError(error?.message || 'Failed to load transaction history');
      setTransactions([]);
    } finally {
      setTransactionsLoading(false);
      isFetchingRef.current = false;
    }
  };

  const transformApiResponseToTransactions = (apiData: any[]): Transaction[] => {
    return apiData.map((item: any) => {
      const type = mapTransactionType(item.type, item.description);
      const status = mapTransactionStatus(item.status);
      
      return {
        id: item.id || item.transactionId || "",
        type,
        currency: item.currencyType || 'NGN',
        amount: Number(item.amount) || 0.0,
        fiatAmount: Math.abs(Number(item.amount) || 0),
        status,
        date: item.timestamp || item.createdOn || "",
        description: item.description || item.message || "",
        transactionId: item.transactionId || undefined,
        sessionId: item.sessionId || undefined,
        referenceNo: item.referenceNo || undefined,
        terminalId: item.terminalId || undefined,
        erId: item.erId || undefined,
        accountHolder: item.accountHolder || undefined,
        previousBalance: item.previousBalance ? Number(item.previousBalance) : undefined,
        availableBalance: item.availableBalance ? Number(item.availableBalance) : undefined,
        icon: getTransactionIcon(type),
        originalData: item,
      };
    });
  };

  useEffect(() => {
  // Handle page loading
  const loadingTimer = setTimeout(() => {
    setIsPageLoading(false);
  }, 2000);

    return () => clearTimeout(loadingTimer);
  }, []);
  
  useEffect(() => {
    fetchTransactionHistory();
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

  const currencies: Currency[] = [
    { name: "US Dollar", code: "USD", symbol: "$" },
    { name: "Euro", code: "EUR", symbol: "€" },
    { name: "Nigerian Naira", code: "NGN", symbol: "₦" },
    { name: "British Pound", code: "GBP", symbol: "£" },
    { name: "Japanese Yen", code: "JPY", symbol: "¥" },
    { name: "Australian Dollar", code: "AUD", symbol: "$" },
    { name: "Canadian Dollar", code: "CAD", symbol: "$" },
    { name: "Swiss Franc", code: "CHF", symbol: "Fr" },
    { name: "Chinese Yuan", code: "CNY", symbol: "¥" },
    { name: "Indian Rupee", code: "INR", symbol: "₹" },
  ];

  const [selectedCurrency, setSelectedCurrency] = useState<Currency>(currencies[2]);

  const filteredCurrencies = currencies.filter(c => 
    c.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    c.code.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const filteredTabs = tabs.filter(tab => 
    tab.toLowerCase().includes(searchHistoryTerm.toLowerCase())
  );

  const [dateFilter, setDateFilter] = useState({
    startDate: '',
    endDate: ''
  });

  const handleDeleteTransaction = async (transactionId: string) => {
    try {
      setDeleteLoading(transactionId);
      // Call your API to delete the transaction
      await historyService.deleteTransaction(transactionId);
      
      // Refresh the transaction list
      await fetchTransactionHistory();
      
      // Show success message (optional)
      console.log('Transaction deleted successfully');
    } catch (error) {
      console.error('Error deleting transaction:', error);
      throw error;
    } finally {
      setDeleteLoading(null);
    }
  };

  const clearDateFilters = () => {
    setDateFilter({
      startDate: '',
      endDate: ''
    });
  };

  const handleTabClick = (tab: string) => {
    setActiveTab(tab);
    setExpandedTransaction(null);
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  const applyQuickDateFilter = (days: number) => {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - days);

    setDateFilter({
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0],
    });
  };

  const mapTransactionStatus = (apiStatus: string | undefined): TransactionStatus => {
    const status = apiStatus?.toLowerCase() || '';

    switch (status) {
      case 'success':
      case 'completed':
      case 'confirmed':
        return 'completed';

      case 'pending':
      case 'processing':
        return 'pending';

      case 'failed':
      case 'rejected':
      case 'cancelled':
        return 'failed';

      default:
        return 'pending';
    }
  };

  const mapTransactionType = (apiType: string | undefined, description: string = ''): TransactionType => {
    const type = String(apiType || '').toLowerCase();
    const desc = description.toLowerCase();

    // Deposit
    if (type.includes('deposit') || desc.includes('deposit') || type === 'deposit') {
      return 'deposit';
    }

    // Credited
    if (type.includes('credited') || type.includes('credit') || desc.includes('received') || type === 'credited') {
      return 'credited';
    }

    // Withdrawal
    if (
      type.includes('withdraw') || type.includes('debit') || type.includes('debited') ||
      desc.includes('withdraw') || desc.includes('withdrawal') || desc.includes('sent') ||
      type === 'withdrawal'
    ) {
      return 'withdrawal';
    }

    // Swap
    if (type.includes('swap') || desc.includes('swap') || desc.includes('exchange') || desc.includes('conversion')) {
      return 'swap';
    }

    // Transfer
    if (type.includes('transfer') || desc.includes('transfer')) {
      return 'transfer';
    }

    // Default fallback
    return 'transfer';
  };

  const getTransactionIcon = (type: TransactionType): ReactElement => {
    switch (type) {
      case 'deposit':
        return <ArrowDown color="#0d6efd" />;     
      case 'withdrawal':
        return <ArrowUp color="#dc3545" />;        
      case 'swap':
        return <Repeat color="#ffc107" />; 
      case 'transfer':
        return <Share color="#6f42c1" />;          
      case 'credited':
        return <CreditCard color="#59b910" />;
      default:
        return <Share color="#6c757d" />; 
    }
  };
  
  const getFilteredTransactions = (): Transaction[] => {
    let filtered = transactions;

    // Filter by transaction type (tab)
    if (activeTab !== 'All') {
      filtered = filtered.filter((transaction: Transaction) => {
        switch (activeTab) {
          case 'Deposits':
            return transaction.type === 'deposit';
          case 'Withdrawals':
            return transaction.type === 'withdrawal';
          case 'Swaps':
            return transaction.type === 'swap';
          case 'Credited':
            return transaction.type === 'credited';
          default:
            return true;
        }
      });
    }

    // Filter by date range
    if (dateFilter.startDate || dateFilter.endDate) {
      filtered = filtered.filter((transaction: Transaction) => {
        if (!transaction.date) return false;

        const transactionDate = new Date(transaction.date);
        if (isNaN(transactionDate.getTime())) return false;

        const transactionDateStr = transactionDate.toISOString().split('T')[0];

        let startCondition = true;
        let endCondition = true;

        if (dateFilter.startDate) {
          startCondition = transactionDateStr >= dateFilter.startDate;
        }

        if (dateFilter.endDate) {
          endCondition = transactionDateStr <= dateFilter.endDate;
        }

        return startCondition && endCondition;
      });
    }

    return filtered;
  };

  const currentTransactions = getFilteredTransactions();

  const handleDateFilterChange = (field: DateField, value: string) => {
    setDateFilter(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const getStatusInfo = (status: TransactionStatus | string): StatusInfo => {
    switch (status) {
      case 'completed':
        return { color: '#10b981', icon: <CheckCircle size={15}/>, text: 'Completed' };
      case 'pending':
        return { color: '#f59e0b', icon: <Clock size={15}/>, text: 'Pending' };
      case 'failed':
        return { color: '#ef4444', icon: <XCircle size={15}/>, text: 'Failed' };
      default:
        return { color: '#6b7280', icon: <Clock size={15}/>, text: 'Processing' };
    }
  };
  
  const toggleDateFilter = () => {
    setShowDateFilter(prev => !prev);
  };

  const closeDateFilter = () => {
    setShowDateFilter(false);
  };

  const getTransactionTypeColor = (type: TransactionType): string => {
    switch (type) {
      case 'deposit':
        return '#dbad15ff';
      case 'withdrawal':
        return '#ef4444';
      case 'swap':
        return '#8b5cf6';
      case 'transfer':
        return '#3b82f6';
      case 'credited':
        return '#31b30dff';
      default:
        return '#6b7280';
    }
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);

    if (isNaN(date.getTime())) {
      return "-"; 
    }

    return (
      date.toLocaleDateString() +
      " " +
      date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
    );
  };

  const toggleTransactionExpansion = (transactionId: string) => {
    if (expandedTransaction === transactionId) {
      setExpandedTransaction(null);
    } else {
      setExpandedTransaction(transactionId);
    }
  };

  const copyToClipboard = (text: string): void => {
    if (!text) return;

    navigator.clipboard.writeText(text)
      .then(() => {
        console.log("Copied to clipboard!");
      })
      .catch(() => {
        console.error("Failed to copy!");
      });
  };

  const handleShowReceipt = (transaction: Transaction) => {
    setSelectedTransaction(transaction);
    setShowReceipt(true);
  };

  const fetchCurrencyBalance = async (currencyCode: string) => {
    try {
      const wallet = getWallet(currencyCode);
      if (wallet && wallet.balance !== undefined) {
        setCurrentBalance(wallet.balance);
      } else {
        setCurrentBalance(0);
      }
    } catch (error) {
      console.error('Error fetching wallet balance:', error);
      setCurrentBalance(0);
    }
  };

  const formatBalance = (balance: string) => {
    const numBalance = parseFloat(balance);
    if (isNaN(numBalance)) return '0.00';
    
    return numBalance.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    });
  };

  useEffect(() => {
    fetchCurrencyBalance(selectedCurrency.code);
  }, [selectedCurrency.code]);

  
  if (isPageLoading) {
    return <LoadingScreen />;
  }
  
  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />

      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
          <section className="hero-banner">
            <div className="wallet-header-wrapper">
              <div className="wallet-main-header">
                {/* Left corner */}
                <div className="wallet-currency-selector">
                  <div ref={dropdownRef} className="currency-pill" onClick={() => {
                    setIsModalOpen(true);
                    setIsDropdownOpen(!isDropdownOpen);
                  }} style={{ cursor: 'pointer' }}>
                    <span className='currency-option'>
                      {selectedCurrency.code} 
                      <i className={`fa ${isDropdownOpen ? 'fa-caret-up' : 'fa-caret-down'} text-light`} 
                        aria-hidden="true"
                        style={{ color: "#fff", fontSize: "15px", marginLeft: "4px", marginTop:"3px" }}></i>
                    </span>
                  </div>
                </div>

                {/* Right corner */}
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
                  {selectedCurrency.symbol} {showBalance ? formatBalance(currentBalance.toString()) : '*****'}
                </div>
              </div>
            </div>
            <div className="wallet-hero-actions">
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
            </div>
          </section>
          <div className="wallet-history-section">
            {/* Header with Title and Controls */}
            <div className="history-header">
              <h2 className="section-title">Transaction History</h2>
              <div className="wallet-header-content">
                <div className="header-controls">
                  {/* Transaction Type Tabs */}
                  
                  <div className="transaction-tabs">
                    {tabs.map((tab) => (
                      <button
                        key={tab}
                        type="button"
                        onClick={() => handleTabClick(tab)}
                        className={`tab-button ${activeTab === tab ? "active" : ""}`}
                      >
                        {tab}
                      </button>
                    ))}
                  </div> 
              <div className="transactions-pg__filter wallet-transactions__filter">
                Filter:
                <div className="select-box" onClick={() =>setIsHistoryFilterModalOpen(true)}>
                  {activeTab}  
                  <svg 
                    xmlns="http://www.w3.org/2000/svg" 
                    className="select-arrow" 
                    viewBox="0 0 512 512"
                    aria-hidden="true"
                  >
                    <path 
                      strokeLinecap="round" 
                      strokeLinejoin="round" 
                      strokeWidth="48" 
                      d="M112 184l144 144 144-144" 
                      className="ionicon-fill-none"
                      fill="none"
                      stroke="currentColor"
                    />
                  </svg>
                </div>
              </div>

                  {/* Date Filter Toggle */}
                  <div className="filter-toggle-csontainer">
                    <button 
                      className="filter-toggle-btn"
                      onClick={toggleDateFilter}
                    >
                      <svg className="filter-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
                        <path d="M3 7C3 6.44772 3.44772 6 4 6H20C20.5523 6 21 6.44772 21 7C21 7.55228 20.5523 8 20 8H4C3.44772 8 3 7.55228 3 7Z" fill="currentColor"/>
                        <path d="M3 12C3 11.4477 3.44772 11 4 11H20C20.5523 11 21 11.4477 21 12C21 12.5523 20.5523 13 20 13H4C3.44772 13 3 12.5523 3 12Z" fill="currentColor"/>
                        <path d="M4 16C3.44772 16 3 16.4477 3 17C3 17.5523 3.44772 18 4 18H20C20.5523 18 21 17.5523 21 17C21 16.4477 20.5523 16 20 16H4Z" fill="currentColor"/>
                      </svg>
                      <span>Date Filter</span>
                        <i className={`fa ${showDateFilter ? 'fa-caret-up' : 'fa-caret-down'} text-light`}  aria-hidden="true"></i>
                    </button>

                    {/* Date Filter Panel - Positioned Absolutely */}
                      {showDateFilter && (
                        <div className="custom-date-panel">
                          <div className="custom-panel-header">
                            <div className="custom-panel-title">
                              <svg className="custom-calendar-icon" width="20" height="20" viewBox="0 0 24 24" fill="none">
                                <path d="M8 2V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <path d="M16 2V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <path d="M3 10H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <rect x="3" y="4" width="18" height="18" rx="3" stroke="currentColor" strokeWidth="2"/>
                              </svg>
                              <h3>Filter by Date</h3>
                            </div>
                            <button 
                              className="custom-close-panel-btn"
                              onClick={closeDateFilter}
                            >
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                                <path d="M18 6L6 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                <path d="M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                              </svg>
                            </button>
                          </div>
                          
                          {/* Quick Filters */}
                          <div className="custom-quick-filters-section">
                            <h4 className="custom-quick-filters-title">Quick Select</h4>
                            <div className="custom-quick-filter-buttons">
                              <button 
                                className="custom-quick-filter-btn"
                                onClick={() => applyQuickDateFilter(0)}
                              >
                                Today
                              </button>
                              <button 
                                className="custom-quick-filter-btn"
                                onClick={() => applyQuickDateFilter(7)}
                              >
                                7 Days
                              </button>
                              <button 
                                className="custom-quick-filter-btn"
                                onClick={() => applyQuickDateFilter(30)}
                              >
                                30 Days
                              </button>
                            </div>
                          </div>
                          
                          {/* Date Range Inputs */}
                          <div className="custom-date-range-sectidon">
                            <div className="custom-date-inputs">
                              <div className="custom-date-input-group">
                                <label className="custom-date-input-label">From Date</label>
                                <div className="custom-date-input-wrapper">
                                  <input
                                    type="date"
                                    value={dateFilter.startDate}
                                    onChange={(e) => handleDateFilterChange('startDate', e.target.value)}
                                    max={dateFilter.endDate || new Date().toISOString().split('T')[0]}
                                    className="custom-date-input"
                                  />
                                  <svg className="custom-date-input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
                                    <path d="M8 2V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <path d="M16 2V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <path d="M3 10H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <rect x="3" y="4" width="18" height="18" rx="3" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </div>
                              </div>
                              
                              <div className="custom-date-input-group">
                                <label className="custom-date-input-label">To Date</label>
                                <div className="custom-date-input-wrapper">
                                  <input
                                    type="date"
                                    value={dateFilter.endDate}
                                    onChange={(e) => handleDateFilterChange('endDate', e.target.value)}
                                    min={dateFilter.startDate}
                                    max={new Date().toISOString().split('T')[0]}
                                    className="custom-date-input"
                                  />
                                  <svg className="custom-date-input-icon" width="16" height="16" viewBox="0 0 24 24" fill="none">
                                    <path d="M8 2V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <path d="M16 2V6" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <path d="M3 10H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                    <rect x="3" y="4" width="18" height="18" rx="3" stroke="currentColor" strokeWidth="2"/>
                                  </svg>
                                </div>
                              </div>
                            </div>
                          </div>
                          
                          {/* Filter Actions */}
                          <div className="custom-filter-actions">
                            <button 
                              className="custom-clear-filters-btn"
                              onClick={clearDateFilters}
                              disabled={!dateFilter.startDate && !dateFilter.endDate}
                            >
                              Clear All
                            </button>
                            <div className="custom-action-buttons">
                              <button 
                                className="custom-cancel-filter-btn"
                                onClick={closeDateFilter}
                              >
                                Cancel
                              </button>
                              <button 
                                className="custom-apply-filter-btn"
                                onClick={closeDateFilter}
                              >
                                Apply
                              </button>
                            </div>
                          </div>
                        </div>
                      )}
                  </div>
                </div>
                {(dateFilter.startDate || dateFilter.endDate) && (
                    <div className="active-filter-badge">
                      <div className="badge-content">
                        <svg className="badge-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                          <path d="M3 7C3 6.44772 3.44772 6 4 6H20C20.5523 6 21 6.44772 21 7C21 7.55228 20.5523 8 20 8H4C3.44772 8 3 7.55228 3 7Z" fill="currentColor"/>
                          <path d="M3 12C3 11.4477 3.44772 11 4 11H20C20.5523 11 21 11.4477 21 12C21 12.5523 20.5523 13 20 13H4C3.44772 13 3 12.5523 3 12Z" fill="currentColor"/>
                          <path d="M4 16C3.44772 16 3 16.4477 3 17C3 17.5523 3.44772 18 4 18H20C20.5523 18 21 17.5523 21 17C21 16.4477 20.5523 16 20 16H4Z" fill="currentColor"/>
                        </svg>
                        <span className="badge-text">
                          {dateFilter.startDate || 'Beginning'} → {dateFilter.endDate || 'Today'}
                        </span>
                      </div>
                      <button 
                        className="clear-badge-btn"
                        onClick={clearDateFilters}
                        title="Clear filter"
                      >
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                          <path d="M18 6L6 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                          <path d="M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                      </button>
                    </div>
                  )}
              </div>
            </div>
    
            {/* Transactions List */}
            <div className="transactions-container">
              {transactionsLoading ? (
                <div className="loading-state">
                  <div className="loading-spinner"></div>
                  <p>Loading transactions...</p>
                </div>
              ) : transactionsError ? (
                <div className="error-state">
                  <svg className="error-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M12 8V12M12 16H12.01M22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 6.47715 6.47715 2 12 2C17.5228 2 22 6.47715 22 12Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                  <p>{transactionsError}</p>
                  <button onClick={fetchTransactionHistory} className="retry-btn">
                    Try Again
                  </button>
                </div>
              ) : currentTransactions.length > 0 ? (
              <div className="transactions-list">
                {currentTransactions.map((transaction) => {
                  const statusInfo = getStatusInfo(transaction.status);
                  const typeColor = getTransactionTypeColor(transaction.type);
                  const isExpanded = expandedTransaction === transaction.id;
                  const isCredit = ['deposit', 'credited', 'swap'].includes(transaction.type);
                  const isDebit = ['withdrawal', 'transfer', 'debit'].includes(transaction.type);
                  
                  const amountColor = isCredit ? '#10b981' : '#ef4444';
                  const amountSign = isCredit ? '+' : '-';
                  
                  return (
                    <div key={transaction.id} className={`transaction-card ${isExpanded ? 'expanded' : ''}`}>
                      <div className="transaction-summary">
                        <div className="transaction-icon" style={{ backgroundColor: `${typeColor}15`, color: typeColor }}>
                          {transaction.icon}
                        </div>
                        
                        <div className="transaction-info">
                          <div className="transaction-main">
                            <span className="transaction-type" style={{ color: typeColor }}>
                              {transaction.type.charAt(0).toUpperCase() + transaction.type.slice(1)}
                            </span>
                            <span className="transaction-amount" style={{ color: amountColor }}>
                              {transaction.currency} {amountSign}{formatAmount(Math.abs(transaction.amount))}
                            </span>
                          </div>
                          
                          <div className="transaction-meta">
                            <span className="transaction-description">
                              {transaction.description}
                            </span>
                            <span className="transaction-date">
                              {formatDate(transaction.date)}
                            </span>
                          </div>
                        </div>

                        <div className="history-transaction-status" style={{ color: statusInfo.color }}>
                          <span className="status-icon">{statusInfo.icon}</span>
                          <span className="status-text">{statusInfo.text}</span>
                        </div>

                        <button className="expand-btn" onClick={() => toggleTransactionExpansion(transaction.id)}>
                          {isExpanded ? <ChevronUp /> : <ChevronDown />}
                        </button>
                      </div>

                      {/* Expandable Details */}
                      {isExpanded && (
                        <div className="transaction-details-panel">
                          <div className="details-grid">
                            <div className="detail-item">
                              <span className="detail-label">Transaction ID:</span>
                              <span className="detail-value">
                                {transaction.transactionId}
                                <button 
                                  className="copy-btn"
                                  onClick={() => copyToClipboard(transaction.transactionId || "")}
                                >
                                  <Copy />
                                </button>
                              </span>
                            </div>
                            <div className="detail-item">
                              <span className="detail-label">Description:</span>
                              <span className="detail-value">{transaction.description}</span>
                            </div>
                            <div className="detail-item">
                              <span className="detail-label">Date:</span>
                              <span className="detail-value">{transaction.date}</span>
                            </div>
                            <div className="detail-item">
                              <span className="detail-label">Status:</span>
                              <span className="detail-value" style={{ color: statusInfo.color }}>
                                {statusInfo.text}
                              </span>
                            </div>
                          </div>
                          
                          <div className="transaction-actions">
                            <button 
                              className="history-action-btn print-btn"
                                onClick={() => handleShowReceipt(transaction)}>
                              <i className="fas fa-print"></i>
                            </button>
                            <button 
                              className="history-action-btn delete-btn"
                              onClick={() => setDeleteModalTransaction(transaction)}
                              disabled={deleteLoading === transaction.id}>
                              {deleteLoading === transaction.id ? (
                                <div className="delete-spinner"></div>
                              ) : (
                                <i className="fas fa-trash"></i>
                              )}
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
              ) : (
                <div className="empty-state">
                  <svg className="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none">
                    <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M14 2V8H20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M16 13H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M16 17H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    <path d="M10 9H9H8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                  <h3 className='header-title'>No transactions found</h3>
                  <p className='subheader-title'>No transactions match your current filters</p>
                </div>
              )}
            </div>
          </div>          
            {showReceipt && selectedTransaction && (
              <Suspense fallback={<div>Loading receipt…</div>}>
                <TransactionReceipt transaction={selectedTransaction} />
              </Suspense>
            )}

            {deleteModalTransaction && (
            <DeleteHistoryModal
              transaction={deleteModalTransaction}
              onClose={() => setDeleteModalTransaction(null)}
              onDelete={handleDeleteTransaction}
              theme={theme}
            />
          )}
        <Footer theme={theme} />
      </div>
    </main>

        {isModalOpen && (
          <div className="wallet-modal-overlay" onClick={() => setIsModalOpen(false)}>
            <div className="wallet-modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="wallet-modal-header"><h3>Select Currency</h3></div>
              <div className="wallet-search-container">
                <span className="wallet-search-icon-inside"><Search size={16} /></span>
                <input type="text" placeholder="Search" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
              </div>
              <div className={`wallet-country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                {filteredCurrencies.map((c) => (
                  <div key={c.code} className="country-item" onClick={async () => { 
                    setSelectedCurrency(c); 
                    setIsModalOpen(false);
                    setFiat(c.code); 
                    setActiveWallet(c.code);
                    await fetchCurrencyBalance(c.code); 
                    setSearchTerm('') 
                    setIsDropdownOpen(false);
                  }}>
                    <span>{c.name} ({c.code})</span>
                    <div className={`wallet-radio-outer ${selectedCurrency.code === c.code ? 'checked' : ''}`}><div className="radio-inner"></div></div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

          {isHistoryFilterModalOpen && (
          <div className="modal-overlay" onClick={() => setIsHistoryFilterModalOpen(false)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h3>Filter Transaction</h3>
              </div>
              
              <div className="search-container">
                <span className="search-icon-inside">
                  <Search size={16} />
                </span>
                <input 
                  type="text" 
                  placeholder="Search" 
                  value={searchHistoryTerm} 
                  onChange={(e) => setSearchHistoryTerm(e.target.value)} 
                  autoFocus
                />
              </div>
              
              <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                {filteredTabs.length > 0 ? (
                  filteredTabs.map((tab) => (
                    <div 
                      key={tab} 
                      className="country-item" 
                      onClick={() => { 
                        setActiveTab(tab);
                        setSearchHistoryTerm('');
                        setIsHistoryFilterModalOpen(false);
                      }}
                    >
                      <span>{tab}</span>
                      <div className={`radio-outer ${tab === activeTab ? 'checked' : ''}`}>
                        <div className="radio-inner"></div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="no-results">
                    No results found for "{searchHistoryTerm}"
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        <MobileNav activeTab="wallet" onPlusClick={() => setIsDepositOpen(true)} />
        <DepositModal 
          isOpen={isDepositOpen} 
          onClose={() => setIsDepositOpen(false)} 
          theme={theme} 
        />
        <WithdrawModal 
          isOpen={isWithdrawOpen} 
          onClose={() => setIsWithdrawOpen(false)} 
          theme={theme} 
        />
    </div>
  );
};

export default Wallet;