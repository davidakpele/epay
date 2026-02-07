'use client';

import React, {useState, useEffect, useRef } from 'react';
import { ArrowLeft, Search, Calendar, Filter, Download, Eye, ArrowUpRight, ArrowDownLeft, SendIcon, X, Copy, Check, AlertTriangle, ChevronDown, ChevronUp } from 'lucide-react';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import "./Statement.css"
import { Currency, SendStatementPayload } from '../../types/api';
import { AccountTransactionStatement } from '../../types/utils';
import LoadingScreen from '@/components/loader/Loadingscreen';
import {  ForwardAccountStatement, StatementItem } from '../../types/errors';
import { formatAmount, getUserFullName, getUserId, getUsername, getWallet, historyService, userService } from '../../api';

interface Toast {
  id: number;
  message: string;
  type: 'warning' | 'success';
  exiting: boolean;
}

const Statements = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [duration, setDuration] = useState('');
    const [toasts, setToasts] = useState<Toast[]>([]);
    
    const [isDurationModalOpen, setIsDurationModalOpen] = useState(false);
    const [isCustomDateModalOpen, setIsCustomDateModalOpen] = useState(false);
    const [isStatemenToEmailModal, setIsStatemenToEmailModal] = useState(false);
    const [isSearchLoading, setIsSearchLoading] = useState(false); 
    const [isSendingEmail, setIsSendingEmail] = useState(false);
    const [durationSearch, setDurationSearch] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [isScrolling, setIsScrolling] = useState(false);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [statements, setStatements] = useState<AccountTransactionStatement[]>([]);
    const [isPageLoading, setIsPageLoading] = useState(true);
    const [emailAddress, setEmailAddress] = useState('');
    const emailRef = useRef<HTMLInputElement>(null);
    const scrollTimer = useRef<NodeJS.Timeout | null>(null);
    const [errors, setErrors] = useState<ForwardAccountStatement>({});
    const [successMessage, setSuccessMessage] = useState('');
    
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
    const [selectedTransaction, setSelectedTransaction] = useState<AccountTransactionStatement | null>(null);
    const [isCopied, setIsCopied] = useState(false);
    const [isStatementPreviewOpen, setIsStatementPreviewOpen] = useState(true);
    
    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage] = useState(10);
    
    const [isDownloading, setIsDownloading] = useState(false);
    const [isReporting, setIsReporting] = useState(false);
    const [downloadStatus, setDownloadStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [reportStatus, setReportStatus] = useState<'idle' | 'success' | 'error'>('idle');
    const [currentBalance, setCurrentBalance] = useState<number>(0);
    const durations = ['Daily', 'Weekly', 'Monthly', 'Last Month', 'Custom'];
    
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
    
    const [selectedCurrency, setSelectedCurrency] = useState<Currency>({ name: "", code: "", symbol: "" });
    
    const [filters, setFilters] = useState({
      transactionType: 'All Types',
      status: 'All Statuses',
    });
    
    const transactionTypes = ['All Types', 'Transfer', 'Deposit', 'Withdrawal', 'Payment', 'Bills Payment', 'Credited'];
    const statuses = ['All Statuses', 'Completed', 'Pending', 'Failed'];
    
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
      const tableContainer = document.querySelector('.ah-table-container');
      if (tableContainer) {
        tableContainer.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }, [currentPage]);

    const fetchCurrencyBalance = async (currencyCode: string) => {
      try {
        if (!currencyCode) {
          console.warn('No currency code provided');
          setCurrentBalance(0);
          return;
        }

        const wallet = getWallet(currencyCode);
        if (wallet && typeof wallet === 'object' && 'balance' in wallet) {
          const balance = Number(wallet.balance);
          setCurrentBalance(isNaN(balance) ? 0 : balance);
        } else {
          console.warn(`No wallet found for currency: ${currencyCode}`);
          setCurrentBalance(0);
        }
      } catch (error) {
        console.error('Error fetching wallet balance:', error);
        setCurrentBalance(0);
      }
    };

    useEffect(() => {
      if (selectedCurrency.code) {
        fetchCurrencyBalance(selectedCurrency.code);
      }
    }, [selectedCurrency.code]);
    
    
    const toggleTheme = () => {
      const newTheme = theme === 'light' ? 'dark' : 'light';
      setTheme(newTheme);
      localStorage.setItem('theme', newTheme);
      document.documentElement.classList.toggle('dark', newTheme === 'dark');
      document.body.classList.toggle('dark-theme', newTheme === 'dark');
    };

    const handleDurationOpenModal = () => {
      setIsDurationModalOpen(true);
    };

    const handleDurationSelect = (selectedDuration: string) => {
      if (selectedDuration === 'Custom') {
        setIsDurationModalOpen(false);
        setIsCustomDateModalOpen(true);
      } else {
        setDuration(selectedDuration);
        setIsDurationModalOpen(false);
        
        const endDate = new Date();
        const startDate = new Date();
        
        switch(selectedDuration) {
          case 'Daily':
            break;
          case 'Weekly':
            startDate.setDate(startDate.getDate() - 7);
            break;
          case 'Monthly':
            startDate.setMonth(startDate.getMonth() - 1);
            break;
          case 'Last Month':
            startDate.setMonth(startDate.getMonth() - 1);
            startDate.setDate(1);
            endDate.setDate(0);
            break;
        }
        
        setStartDate(startDate.toISOString().split('T')[0]);
        setEndDate(endDate.toISOString().split('T')[0]);
      }
    };

    const handleCustomDateApply = () => {
      if (startDate && endDate) {
        setDuration(`Custom: ${formatDate(startDate)} - ${formatDate(endDate)}`);
        setIsCustomDateModalOpen(false);
      } else {
        showToast('Please select both start and end dates', 'warning');
      }
    };

    const formatDate = (dateStr: string) => {
      return new Date(dateStr).toLocaleDateString('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
      });
    };

    const filteredDurations = durations.filter(d => 
      d.toLowerCase().includes(durationSearch.toLowerCase())
    );

    const handleScroll = () => {
      setIsScrolling(true);
      if (scrollTimer.current) clearTimeout(scrollTimer.current);
      scrollTimer.current = setTimeout(() => {
        setIsScrolling(false);
      }, 1000);
    };

    const filteredCurrencies = currencies.filter(c => 
      c.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
      c.code.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleFilterChange = (key: string, value: string) => {
      setFilters(prev => ({ ...prev, [key]: value }));
    };

    const handleSearch = async () => {
      if (!startDate || !endDate) {
        showToast('Please select both FROM and TO dates', 'warning');
        return;
      }

      setIsSearchLoading(true);
      
      try {
        const userId = getUserId();
        if (!userId) {
          showToast('Please login to view statements', 'warning');
          setIsSearchLoading(false);
          return;
        }

        const filterPayload = {
          startDate: startDate,
          endDate: endDate,
          transactionType: filters.transactionType !== 'All Types' ? filters.transactionType.toUpperCase() : 'ALL',
          currency: selectedCurrency.code || 'ALL'
        };

        await new Promise(resolve => setTimeout(resolve, 2000));

        const response = await historyService.getFilteredHistory(userId, filterPayload);
        
        if (response && response.data && response.data.length > 0) {
          const transformedStatements = response.data.map((item: any) => ({
            id: item.id || "",
            date: item.timestamp || item.createdOn || "",
            description: item.description || item.message || "",
            type: item.type || "Transfer",
            amount: Number(item.amount) || 0,
            status: mapStatus(item.status),
            reference: item.referenceNo || item.transactionId || "",
            currencyType: item.currencyType || selectedCurrency.code || 'USD',
            availableBalance: Number(item.availableBalance) || 0,
            previousBalance: Number(item.previousBalance) || 0,
          }));
          
          setCurrentPage(1);
          setStatements(transformedStatements);
          showToast(`Found ${transformedStatements.length} transactions`, 'success');
        } else {
          showToast('No transactions found for the selected filters', 'warning');
          setStatements([]);
          setCurrentPage(1);
        }
      } catch (error: any) {
        console.error('Error fetching statements:', error);
        showToast(error?.message || 'Error loading statements from server', 'warning');
        setStatements([]);
        setCurrentPage(1);
      } finally {
        setIsSearchLoading(false);
      }
    };

    const mapStatus = (apiStatus: string | undefined): string => {
      const status = apiStatus?.toLowerCase() || '';
      
      switch (status) {
        case 'success':
        case 'completed':
        case 'confirmed':
          return 'Completed';
        case 'pending':
        case 'processing':
          return 'Pending';
        case 'failed':
        case 'rejected':
        case 'cancelled':
          return 'Failed';
        default:
          return 'Pending';
      }
    };

    const handleClearFilters = () => {
      setFilters({
        transactionType: 'All Types',
        status: 'All Statuses',
      });
      setSelectedCurrency({ name: "", code: "", symbol: "" });
      setDuration('');
      setStartDate('');
      setEndDate('');
      setStatements([]);
      setCurrentPage(1);
      showToast('All filters cleared', 'success');
    };

    const getStatusClass = (status: string) => {
      switch (status) {
        case 'Completed': return 'status-completed';
        case 'Pending': return 'status-pending';
        case 'Failed': return 'status-failed';
        default: return '';
      }
    };

    const handleBackToDashboard = () => {
      console.log('Back to dashboard');
    };

    const hasActiveFilters = () => {
      return selectedCurrency.code || 
        filters.transactionType !== 'All Types' || 
        filters.status !== 'All Statuses' ||
        duration;
    };

    const handleViewDetails = (transaction: AccountTransactionStatement) => {
      setSelectedTransaction(transaction);
      setIsDetailsModalOpen(true);
      setDownloadStatus('idle');
      setReportStatus('idle');
    };

    const handleCopyReference = (reference: string) => {
      navigator.clipboard.writeText(reference);
      setIsCopied(true);
      setTimeout(() => setIsCopied(false), 2000);
    };

    const handleSendEmail = async (e: React.FormEvent) => {
      e.preventDefault();
      setErrors({});
      setSuccessMessage('');

      const newErrors: ForwardAccountStatement = {};
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      
      if (!emailAddress || !emailRegex.test(emailAddress)) {
        newErrors.email = 'Valid email address is required!';
        setErrors(newErrors);
        return;
      }

      if (!statements || statements.length === 0) {
        newErrors.email = 'No transactions to send. Please apply filters first.';
        setErrors(newErrors);
        return;
      }

      const statementsPayload: StatementItem[] = statements.map(item => ({
        id: item.id ?? '',
        date: item.date ?? '',
        description: item.description ?? '',
        type: item.type ?? '',
        currencyType: item.currencyType || selectedCurrency.code || 'USD',
        amount: item.amount ?? 0,
        balance: item.availableBalance || item.previousBalance || 0, 
        reference: item.reference ?? ''
      }));

      const payload: SendStatementPayload = {
        email: emailAddress,
        statements: statementsPayload
      };

      setIsSendingEmail(true);

      try {
        const userId = getUserId();
        const response = await userService.sendAccountStatement(userId, emailAddress, payload);

        if (response.status === "success") {
          setSuccessMessage(response.message || 'Statement sent successfully!');
          showToast('Statement sent to your email successfully!', 'success');
          setEmailAddress('');
          
          setTimeout(() => {
            setIsStatemenToEmailModal(false);
            setSuccessMessage('');
          }, 2000);
        } else {
          throw new Error(response.message || 'Failed to send email');
        }

      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : 'An error occurred while sending the email';
        newErrors.email = errorMessage;
        setErrors(newErrors);
        showToast(errorMessage, 'warning');
      } finally {
        setIsSendingEmail(false);
      }
    };

    const handleDownloadReceipt = async () => {
      if (!selectedTransaction) return;
      
      setIsDownloading(true);
      setDownloadStatus('idle');
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        const receiptContent = `
TRANSACTION RECEIPT
====================

Transaction ID: ${selectedTransaction.id}
Reference: ${selectedTransaction.reference}
Date: ${selectedTransaction.date}
Type: ${selectedTransaction.type}
Description: ${selectedTransaction.description}
Amount: ${formatAmount(selectedTransaction.amount)}
Status: ${selectedTransaction.status}

Account Details:
Name: Nezer Techy
Account: 5001320096

Generated: ${new Date().toLocaleString()}

This is an official receipt for your records.
        `;
        
        const blob = new Blob([receiptContent], { type: 'text/plain' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        
        link.href = url;
        link.download = `Receipt-${selectedTransaction.reference}.txt`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        setDownloadStatus('success');
        showToast('Receipt downloaded successfully', 'success');
        
        setTimeout(() => {
          setDownloadStatus('idle');
        }, 2000);
        
      } catch (error) {
        console.error('Download failed:', error);
        setDownloadStatus('error');
        showToast('Failed to download receipt', 'warning');
        
        setTimeout(() => {
          setDownloadStatus('idle');
        }, 3000);
        
      } finally {
        setIsDownloading(false);
      }
    };

    const handleReportTransaction = async () => {
      if (!selectedTransaction) return;
      
      setIsReporting(true);
      setReportStatus('idle');
      
      try {
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        console.log('Reporting transaction:', {
          transactionId: selectedTransaction.id,
          reference: selectedTransaction.reference,
          reason: 'User reported suspicious activity',
          reportedAt: new Date().toISOString()
        });
        
        setReportStatus('success');
        showToast('Transaction reported successfully', 'success');
        
        setTimeout(() => {
          setReportStatus('idle');
        }, 2000);
        
      } catch (error) {
        console.error('Report failed:', error);
        setReportStatus('error');
        showToast('Failed to report transaction', 'warning');
        
        setTimeout(() => {
          setReportStatus('idle');
        }, 3000);
        
      } finally {
        setIsReporting(false);
      }
    };

    const getDownloadButtonText = () => {
      if (isDownloading) return 'Downloading...';
      if (downloadStatus === 'success') return 'Downloaded!';
      if (downloadStatus === 'error') return 'Error';
      return 'Download';
    };

    const getReportButtonText = () => {
      if (isReporting) return 'Reporting...';
      if (reportStatus === 'success') return 'Reported!';
      if (reportStatus === 'error') return 'Error';
      return 'Report';
    };

    const getDownloadButtonClass = () => {
      let baseClass = "flex-1 py-2 text-xs rounded-lg font-medium transition flex items-center justify-center gap-2";
      
      if (isDownloading) return baseClass + " bg-blue-600 text-white";
      if (downloadStatus === 'success') return baseClass + " bg-green-600 text-white";
      if (downloadStatus === 'error') return baseClass + " bg-red-600 text-white";
      return baseClass + " bg-white border border-gray-300 text-gray-700 hover:bg-gray-50";
    };

    const getReportButtonClass = () => {
      let baseClass = "flex-1 py-2 text-xs rounded-lg font-medium transition flex items-center justify-center gap-2";
      
      if (isReporting) return baseClass + " bg-yellow-600 text-white";
      if (reportStatus === 'success') return baseClass + " bg-green-600 text-white";
      if (reportStatus === 'error') return baseClass + " bg-green-600 text-white";
      return baseClass + " bg-green-700 text-white hover:bg-green-700";
    };

    const totalCredits = statements
      .filter(s => s.amount >= 0)
      .reduce((sum, s) => sum + s.amount, 0);
      
    const totalDebits = statements
      .filter(s => s.amount < 0)
      .reduce((sum, s) => sum + Math.abs(s.amount), 0);

    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentStatements = statements.slice(indexOfFirstItem, indexOfLastItem);
    const totalPages = Math.ceil(statements.length / itemsPerPage);

    const handlePageChange = (pageNumber: number) => {
      setCurrentPage(pageNumber);
    };

    const handlePreviousPage = () => {
      if (currentPage > 1) {
        handlePageChange(currentPage - 1);
      }
    };

    const handleNextPage = () => {
      if (currentPage < totalPages) {
        handlePageChange(currentPage + 1);
      }
    };

    const getPageNumbers = () => {
      const pages: (number | string)[] = [];
      const maxVisiblePages = 5;

      if (totalPages <= maxVisiblePages) {
        for (let i = 1; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        if (currentPage <= 3) {
          for (let i = 1; i <= 4; i++) {
            pages.push(i);
          }
          pages.push('...');
          pages.push(totalPages);
        } else if (currentPage >= totalPages - 2) {
          pages.push(1);
          pages.push('...');
          for (let i = totalPages - 3; i <= totalPages; i++) {
            pages.push(i);
          }
        } else {
          pages.push(1);
          pages.push('...');
          pages.push(currentPage - 1);
          pages.push(currentPage);
          pages.push(currentPage + 1);
          pages.push('...');
          pages.push(totalPages);
        }
      }

      return pages;
    };

    if (isPageLoading) {
      return <LoadingScreen />;
    }

  return (
    <>
      {toasts.length > 0 && (
        <div className="fixed top-4 right-4 z-50 space-y-2">
          {toasts.map((toast) => (
            <div
              key={toast.id}
              className={`${
                toast.type === 'success' ? 'bg-green-500' : 'bg-yellow-500'
              } text-white px-4 py-3 rounded-lg shadow-lg transition-all duration-300 ${
                toast.exiting ? 'opacity-0 translate-x-full' : 'opacity-100 translate-x-0'
              }`}
            >
              {toast.message}
            </div>
          ))}
        </div>
      )}
      
      <div className={`dashboard-container`}>
        <Sidebar />
        <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
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
          
          <div className={`scrollable-content ${theme === 'dark' ? 'bg-light' : 'bg-dark'}`}>
            <div className="wallet-header-wrapper">
              <div className={`account-history ${theme}`}>
                <div className="breadcrumb">
                  <span>Settings</span>
                  <span className="separator">›</span>
                  <span>Settlement Accounts</span>
                </div>
                <div className="ah-header">
                  <div className="ah-header-content">
                    <h1 className="ah-title">Account History</h1>
                    <p className="ah-subtitle">View your master account transaction history</p>
                  </div>
                  <button className="ah-back-btn" onClick={handleBackToDashboard}>
                    <ArrowLeft size={20} />
                    Back to Dashboard
                  </button>
                </div> 
                <div className="ah-account-card">
                  <div className="ah-account-info">
                    <div className="ah-account-details">
                      <h2 className="ah-account-name">{getUserFullName()}</h2>
                      <p className="ah-account-number">Username:@{getUsername()}</p>
                    </div>
                    <div className="ah-balance-info">
                      <h3 className="ah-balance-amount">{selectedCurrency.symbol}{formatAmount(currentBalance)}</h3>
                      <p className="ah-balance-label credits">
                        Total Credits: {formatAmount(totalCredits)}
                      </p>
                      <p className="ah-balance-label debits">
                        Total Debits: {formatAmount(totalDebits)}
                      </p>
                    </div>
                  </div>
                </div>
          
                <div className="ah-filter-section">
                  <div className="ah-filter-header">
                    <h3 className="ah-filter-title">Filter Transactions</h3>
                    <button className="ah-clear-btn" onClick={handleClearFilters}>
                      Clear Filters
                    </button>
                  </div>

                  <div className="ah-filters-grid">
                    <div className="account-row">
                      <label className="account-ah-filter-label">Select Account:</label>
                      <div className="statement-selectField" onClick={() => setIsModalOpen(true)}>
                        <span className="statement-selectedText">
                          {selectedCurrency.code ? `${selectedCurrency.name} (${selectedCurrency.code})` : "Select Wallet"}
                        </span>
                        <svg className="statement-arrow" width="20" height="20" viewBox="0 0 20 20" fill="none">
                          <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                      </div>
                    </div>
                    <div className="account-divider"></div>
                    <div className="ah-filter-row ah-filter-row-3">
                      <div className="ah-filter-group">
                        <label className="account-ah-filter-label">Transaction Type</label>
                        <select 
                          className="ah-filter-select"
                          value={filters.transactionType}
                          onChange={(e) => handleFilterChange('transactionType', e.target.value)}>
                          {transactionTypes.map(type => (
                            <option key={type} value={type}>{type}</option>
                          ))}
                        </select>
                      </div>

                      <div className="ah-filter-group">
                        <label className="account-ah-filter-label">Status</label>
                        <select 
                          className="ah-filter-select"
                          value={filters.status}
                          onChange={(e) => handleFilterChange('status', e.target.value)}>
                          {statuses.map(status => (
                            <option key={status} value={status}>{status}</option>
                          ))}
                        </select>
                      </div>
                      <div className="ah-filter-group">
                        <label className="account-ah-filter-label">Duration</label>
                        <div className="duration-selectTextView" onClick={handleDurationOpenModal}>
                          <span className="statement-selectedText">
                            {duration || "--Select--"}
                          </span>
                          <svg className="statement-arrow" width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                        </div>
                    </div> 
                    </div>
                           
                    <div className="ah-filter-row ah-filter-row-2">
                      <div className="ah-filter-group ah-filter-actions">
                        <button className="ah-search-btn" onClick={handleSearch} disabled={isSearchLoading} >
                          {isSearchLoading ? (
                            <>
                              <div className="search-loader"></div>
                              Searching...
                            </>
                          ) : (
                            <>
                              <Search size={18} />
                              Search
                            </>
                          )}
                        </button>
                      </div>
                    </div>

                    {hasActiveFilters() && (
                      <div className="active-filters">
                        {selectedCurrency.code && (
                          <span className="active-filter-tag">
                            Currency: {selectedCurrency.name} ({selectedCurrency.code})
                          </span>
                        )}
                        {filters.transactionType !== 'All Types' && (
                          <span className="active-filter-tag">
                            Type: {filters.transactionType}
                          </span>
                        )}
                        {filters.status !== 'All Statuses' && (
                          <span className="active-filter-tag">
                            Status: {filters.status}
                          </span>
                        )}
                        {duration && (
                          <span className="active-filter-tag">
                            Duration: {duration}
                          </span>
                        )}
                      </div>
                    )}
                  </div>
                </div>

                {statements.length > 0 ? (
                  <div className="ah-table-section">
                    <div className="ah-table-header">
                      <div className="ah-table-info">
                        <div className="ah-table-title-wrapper">
                          <h3 className="ah-table-title">Statement Preview</h3>
                          <button 
                            className="ah-collapse-btn" 
                            onClick={() => setIsStatementPreviewOpen(!isStatementPreviewOpen)}
                            aria-label={isStatementPreviewOpen ? "Collapse table" : "Expand table"}
                          >
                            {isStatementPreviewOpen ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                          </button>
                        </div>
                      </div>
                      <div className="ah-action-buttons">
                        <button className="ah-download-btn">
                          <Download size={18} />
                          Export
                        </button>
                        <button className="ah-download-btn" onClick={() => setIsStatemenToEmailModal(true)}>
                          <SendIcon size={18} />
                          Send to Email
                        </button>
                      </div>
                    </div>
          
                    {isStatementPreviewOpen && (
                      <>
                        <div className="ah-table-container">
                          <table className="ah-table">
                            <thead>
                              <tr> 
                                <th>Date</th>
                                <th>Description</th>
                                <th>TYPE</th>
                                <th>AMOUNT</th>
                                <th>STATUS</th>
                                <th>ACTIONS</th>
                              </tr>
                            </thead>
                            <tbody>
                              {currentStatements.map((transaction) => (
                                <tr key={transaction.id}>
                                  <td>
                                    <span className="ah-date">{formatDate(transaction.date)}</span>
                                  </td>
                                  <td>
                                    <div className="ah-transaction-cell">
                                      <div className={`ah-transaction-icon ${transaction.amount >= 0 ? 'credit' : 'debit'}`}>
                                        {transaction.amount >= 0 ? 
                                          <ArrowDownLeft size={18} /> : 
                                          <ArrowUpRight size={18} />
                                        }
                                      </div>
                                      <div className="ah-transaction-details">
                                        <p className="ah-transaction-description">{transaction.description}</p>
                                        <p className="ah-transaction-reference">{transaction.reference}</p>
                                      </div>
                                    </div>
                                  </td>
                                  <td>
                                    <span className="ah-type-badge">{transaction.type}</span>
                                  </td>
                                  <td>
                                    <span className={`ah-amount ${transaction.amount >= 0 ? 'credit' : 'debit'}`}>
                                      {formatAmount(transaction.amount)}
                                    </span>
                                  </td>
                                  <td>
                                    <span className={`ah-status-badge ${getStatusClass(transaction.status)}`}>
                                      <span className="status-dot"></span>
                                      {transaction.status}
                                    </span>
                                  </td>
                                  
                                  <td>
                                    <div className="table-action">
                                      <button  className="ah-view-btn" onClick={() => handleViewDetails(transaction)}>
                                        <Eye size={12} />
                                      </button>
                                      <button className="ah-view-btn" onClick={handleDownloadReceipt} disabled={isDownloading}>
                                        <Download size={12} />
                                      </button>
                                    </div>
                                    
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                        {statements.length > itemsPerPage && (
                          <div className="ah-table-footer">
                            <div className="ah-footer-content">
                              <p className="ah-table-subtitle">
                                Showing {indexOfFirstItem + 1} to {Math.min(indexOfLastItem, statements.length)} of {statements.length} entries
                              </p>
                              
                              <div className="ah-pagination">
                                <button 
                                  className="ah-pagination-btn"
                                  onClick={handlePreviousPage}
                                  disabled={currentPage === 1}
                                >
                                  Previous
                                </button>
                                
                                {getPageNumbers().map((pageNum, index) => (
                                  pageNum === '...' ? (
                                    <span key={`ellipsis-${index}`} className="px-2 text-gray-500 flex items-center">
                                      ...
                                    </span>
                                  ) : (
                                    <button
                                      key={pageNum}
                                      onClick={() => typeof pageNum === 'number' && handlePageChange(pageNum)}
                                      className={`ah-pagination-number ${pageNum === currentPage ? 'active' : ''}`}
                                    >
                                      {pageNum}
                                    </button>
                                  )
                                ))}
                                
                                <button 
                                  className="ah-pagination-btn"
                                  onClick={handleNextPage}
                                  disabled={currentPage === totalPages}
                                >
                                  Next
                                </button>
                              </div>
                            </div>
                          </div>
                        )}
                      </>
                    )}
                  </div>
                ) : (
                  <div className="ah-empty-message">
                    <Filter size={48} />
                    <h3>Apply filters to view transactions</h3>
                    <p>Use the filters above to search for specific transactions</p>
                  </div>
                )}

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
                          <div key={c.code} className="country-item" onClick={async () => { 
                            try {
                              setSelectedCurrency(c); 
                              setIsModalOpen(false);
                              setSearchTerm('');
                              await new Promise(resolve => setTimeout(resolve, 100));
                              await fetchCurrencyBalance(c.code);
                            } catch (error) {
                              console.error('Error selecting currency:', error);
                              showToast('Error loading currency balance', 'warning');
                            }
                          }}>
                            <span>{c.name} ({c.code})</span>
                            <div className={`radio-outer ${selectedCurrency.code === c.code ? 'checked' : ''}`}>
                              <div className="radio-inner"></div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                )}

                {isDurationModalOpen && (
                  <div className="modal-overlay" onClick={() => setIsDurationModalOpen(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                      <div className="modal-header"><h3>Select Duration</h3></div>
                      <div className="search-container">
                        <span className="search-icon-inside"><Search size={16} /></span>
                        <input type="text" placeholder="Search" value={durationSearch} onChange={(e) => setDurationSearch(e.target.value)} />
                      </div>
                      <div className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} onScroll={handleScroll}>
                        {filteredDurations.map((d) => (
                          <div key={d} className="country-item" onClick={() => handleDurationSelect(d)}>
                            <span>{d}</span>
                            <div className={`radio-outer ${duration.includes(d) ? 'checked' : ''}`}>
                              <div className="radio-inner"></div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                )}

                {isCustomDateModalOpen && (
                  <div className="modal-overlay" onClick={() => setIsCustomDateModalOpen(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ width: '24rem', height: 'auto', maxHeight: '26rem' }}>
                      <div className="modal-header" style={{ padding: '16px 20px' }}>
                        <h3 style={{ fontSize: '14px', margin: 0 }}>Select Custom Date Range</h3>
                      </div>
                      <div className="p-4 space-y-4" style={{ padding: '16px 20px' }}>
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2" style={{ fontSize: '12px', marginBottom: '8px' }}>Start Date</label>
                          <div className="relative">
                            <input 
                              type="date" 
                              value={startDate}
                              onChange={(e) => setStartDate(e.target.value)}
                              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 bg-white text-gray-900"
                              style={{
                                appearance: 'none',
                                WebkitAppearance: 'none',
                                MozAppearance: 'textfield',
                                padding: '10px 12px',
                                fontSize: '12px'
                              }}
                            />
                            <Calendar className="absolute right-3 top-3 h-4 w-4 text-gray-400 pointer-events-none" />
                          </div>
                        </div>
                        
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2" style={{ fontSize: '12px', marginBottom: '8px' }}>End Date</label>
                          <div className="relative">
                            <input 
                              type="date" 
                              value={endDate}
                              onChange={(e) => setEndDate(e.target.value)}
                              className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 bg-white text-gray-900"
                              style={{
                                appearance: 'none',
                                WebkitAppearance: 'none',
                                MozAppearance: 'textfield',
                                padding: '10px 12px',
                                fontSize: '12px'
                              }}
                            />
                            <Calendar className="absolute right-3 top-3 h-4 w-4 text-gray-400 pointer-events-none" />
                          </div>
                        </div>
                        <div className="ah-filter-group ah-filter-actions">
                          <button 
                            className="apply-custom-btn" 
                            onClick={handleCustomDateApply}
                            disabled={!startDate || !endDate}
                          >
                            Apply Date Range
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {isStatemenToEmailModal && (
                  <form onSubmit={handleSendEmail}>
                    <div
                      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                      onClick={() => {
                        if (!isSendingEmail) {
                          setIsStatemenToEmailModal(false);
                          setErrors({});
                          setSuccessMessage('');
                        }
                      }}
                    >
                      <div
                        className="w-full max-w-sm bg-white rounded-xl shadow-lg max-h-[90vh] overflow-y-auto"
                        onClick={(e) => e.stopPropagation()}
                      >
                        <div className="px-5 py-4 border-b border-gray-200">
                          <h3 className="text-lg sm:text-xl font-semibold text-gray-800 text-center sm:text-left">
                            Send Statement to Email
                          </h3>
                        </div>

                        <div className="p-5 space-y-4">
                          <div className="flex justify-center">
                            <SendIcon size={40} className="text-slate-500" />
                          </div>

                          <p className="text-xs text-slate-500 text-center">
                            Enter the email address where you would like to receive your account statement.
                          </p>

                          {successMessage && (
                            <div className="bg-green-50 border border-green-200 text-green-700 px-3 py-2 rounded-lg text-xs">
                              {successMessage}
                            </div>
                          )}

                          {errors.email && (
                            <div className="bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded-lg text-xs">
                              {errors.email}
                            </div>
                          )}

                          <div>
                            <label className="block text-xs font-medium text-gray-700 mb-2">
                              Email Address
                            </label>
                            <input
                              type="email"
                              placeholder="Enter email address"
                              value={emailAddress}
                              ref={emailRef}
                              onChange={(e) => {
                                setEmailAddress(e.target.value);
                                setErrors({});
                              }}
                              disabled={isSendingEmail}
                              className="w-full rounded-lg border border-gray-300 px-3 py-3 text-xs text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-1 focus:ring-green-700 focus:border-green-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
                            />
                          </div>

                          <button 
                            type='submit'
                            className="w-full rounded-lg bg-green-800 py-3 text-sm font-medium text-white hover:bg-green-800 transition disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center justify-center gap-2" 
                            disabled={isSendingEmail}
                          >
                            {isSendingEmail ? (
                              <>
                                <div className="search-loader"></div>
                                Sending...
                              </>
                            ) : (
                              'Send'
                            )}
                          </button>
                        </div>
                      </div>
                    </div>
                  </form>
                )}

                {isDetailsModalOpen && selectedTransaction && (
                  <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                    onClick={() => {
                      setIsDetailsModalOpen(false);
                      setSelectedTransaction(null);
                      setIsCopied(false);
                    }}
                  >
                    <div
                      className="w-full max-w-md bg-white rounded-2xl shadow-2xl max-h-[85vh] overflow-hidden flex flex-col"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <div className="flex items-center justify-between px-5 py-3 border-b border-gray-200 bg-gray-50">
                        <h3 className="text-base font-semibold text-gray-800">
                          Transaction Details
                        </h3>
                        <button
                          onClick={() => {
                            setIsDetailsModalOpen(false);
                            setSelectedTransaction(null);
                            setIsCopied(false);
                          }}
                          className="p-1.5 hover:bg-gray-200 rounded-full transition"
                        >
                          <X size={18} className="text-gray-600" />
                        </button>
                      </div>

                      <div className="flex-1 overflow-y-auto p-5 space-y-5">
                        <div className="flex items-center gap-4 p-4 bg-gradient-to-br from-gray-50 to-gray-100 rounded-xl">
                          <div className={`w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0 ${
                            selectedTransaction.amount >= 0 ? 'bg-green-100' : 'bg-red-100'
                          }`}>
                            {selectedTransaction.amount >= 0 ? 
                              <ArrowDownLeft size={20} className="text-green-600" /> : 
                              <ArrowUpRight size={20} className="text-red-600" />
                            }
                          </div>
                          <div className="flex-1 min-w-0">
                            <h2 className={`text-2xl font-bold truncate ${
                              selectedTransaction.amount >= 0 ? 'text-green-600' : 'text-red-600'
                            }`}>
                              {formatAmount(selectedTransaction.amount)}
                            </h2>
                            <p className="text-xs text-gray-500 mt-0.5">
                              {selectedTransaction.amount >= 0 ? 'Money In' : 'Money Out'}
                            </p>
                          </div>
                        </div>

                        <div className="space-y-3">
                          <div className="flex items-center justify-between py-2 border-b border-gray-100">
                            <span className="text-xs font-medium text-gray-600">Status</span>
                            <span className={`ah-status-badge text-xs ${getStatusClass(selectedTransaction.status)}`}>
                              <span className="status-dot"></span>
                              {selectedTransaction.status}
                            </span>
                          </div>

                          <div className="flex items-start justify-between py-2 border-b border-gray-100 gap-3">
                            <span className="text-xs font-medium text-gray-600 flex-shrink-0">Description</span>
                            <span className="text-xs text-gray-900 text-right break-words">
                              {selectedTransaction.description}
                            </span>
                          </div>

                          <div className="flex items-center justify-between py-2 border-b border-gray-100">
                            <span className="text-xs font-medium text-gray-600">Type</span>
                            <span className="text-xs px-2 py-1 bg-gray-100 text-gray-700 rounded-md font-medium">
                              {selectedTransaction.type}
                            </span>
                          </div>

                          <div className="flex items-center justify-between py-2 border-b border-gray-100">
                            <span className="text-xs font-medium text-gray-600">Date & Time</span>
                            <span className="text-xs text-gray-900">{formatDate(selectedTransaction.date)}</span>
                          </div>

                          <div className="flex items-center justify-between py-2 border-b border-gray-100 gap-2">
                            <span className="text-xs font-medium text-gray-600 flex-shrink-0">Reference</span>
                            <div className="flex items-center gap-1.5 min-w-0">
                              <span className="text-xs text-gray-900 font-mono truncate">
                                {selectedTransaction.reference}
                              </span>
                              <button
                                onClick={() => handleCopyReference(selectedTransaction.reference)}
                                className="p-1 hover:bg-gray-100 rounded transition flex-shrink-0"
                                title="Copy"
                              >
                                {isCopied ? (
                                  <Check size={14} className="text-green-600" />
                                ) : (
                                  <Copy size={14} className="text-gray-500" />
                                )}
                              </button>
                            </div>
                          </div>

                          <div className="flex items-center justify-between py-2">
                            <span className="text-xs font-medium text-gray-600">ID</span>
                            <span className="text-xs text-gray-900 font-mono">
                              {selectedTransaction.id}
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="flex gap-2 p-4 border-t border-gray-200 bg-gray-50">
                        <button 
                          onClick={handleDownloadReceipt}
                          disabled={isDownloading || isReporting}
                          className={getDownloadButtonClass()}
                        >
                          {isDownloading ? (
                            <>
                              <div className="search-loader small"></div>
                              {getDownloadButtonText()}
                            </>
                          ) : downloadStatus === 'success' ? (
                            <>
                              <Check size={14} />
                              {getDownloadButtonText()}
                            </>
                          ) : downloadStatus === 'error' ? (
                            <>
                              <X size={14} />
                              {getDownloadButtonText()}
                            </>
                          ) : (
                            <>
                              <Download size={14} />
                              {getDownloadButtonText()}
                            </>
                          )}
                        </button>
                        <button 
                          onClick={handleReportTransaction}
                          disabled={isReporting || isDownloading}
                          className={getReportButtonClass()}
                        >
                          {isReporting ? (
                            <>
                              <div className="search-loader small"></div>
                              {getReportButtonText()}
                            </>
                          ) : reportStatus === 'success' ? (
                            <>
                              <Check size={14} />
                              {getReportButtonText()}
                            </>
                          ) : reportStatus === 'error' ? (
                            <>
                              <X size={14} />
                              {getReportButtonText()}
                            </>
                          ) : (
                            <>
                              <AlertTriangle size={14} className="inline mr-1" />
                              {getReportButtonText()}
                            </>
                          )}
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
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
  )
}

export default Statements;