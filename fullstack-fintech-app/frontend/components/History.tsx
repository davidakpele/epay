'use client';

import React, { useState, useEffect } from 'react';
import { ArrowUpRight, ArrowDownLeft, ShoppingBag, Landmark, X, Copy, CreditCard } from 'lucide-react';
import './History.css';
import { formatAmount } from '@/app/api';

interface Transaction {
  id: number;
  type: 'credit' | 'debit';
  title: string;
  category: string;
  amount: string;
  date: string;
  icon: React.ReactNode;
  ref: string;
  status: string;
  currency: string;
}

interface HistoryProps {
  theme: 'light' | 'dark';
  historyData: any[];
}

const History = ({ theme, historyData }: HistoryProps) => {
  const [selectedTx, setSelectedTx] = useState<Transaction | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [displayLimit, setDisplayLimit] = useState(4);

  useEffect(() => {
    if (historyData && Array.isArray(historyData)) {
      const transformedTx = transformTransactions(historyData);
      setTransactions(transformedTx);
    } else {
      setTransactions([]);
    }
  }, [historyData]);

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case 'deposit':
        return <ArrowDownLeft size={18} />;
      case 'withdrawal':
        return <ArrowUpRight size={18} />;
      case 'credited':
        return <CreditCard size={18} />;
      case 'swap':
        return <ShoppingBag size={18} />;
      default:
        return <Landmark size={18} />;
    }
  };

  const mapTransactionType = (apiType: string, description = '') => {
    const type = String(apiType || '').toLowerCase();
    const desc = String(description || '').toLowerCase();
    
    if (type.includes('deposit') || desc.includes('deposit') || type === 'deposit') {
      return 'deposit';
    }
    if (type.includes('credited') || type.includes('credit') || desc.includes('received') || type === 'credited') {
      return 'credited';
    }
    if (type.includes('withdraw') || type.includes('debit') || type.includes('debited') || desc.includes('withdraw') || desc.includes('withdrawal') || desc.includes('sent') || type === 'withdrawal') {
      return 'withdrawal';
    }
    if (type.includes('swap') || desc.includes('swap') || desc.includes('exchange') || desc.includes('conversion')) {
      return 'swap';
    }
    return 'transfer';
  };

  const transformTransactions = (apiTransactions: any[]): Transaction[] => {
    return apiTransactions.map(tx => {
      const type = mapTransactionType(tx.type, tx.description);
      const isCredit = ['deposit', 'credited', 'swap'].includes(type);
      const amount = Math.abs(tx.amount || 0);
      
      return {
        id: tx.id || tx.transactionId,
        type: isCredit ? 'credit' : 'debit',
        title: tx.description || 'Transaction',
        category: type.charAt(0).toUpperCase() + type.slice(1),
        amount: `${isCredit ? '+' : '-'}${tx.currencyType || '₦'}${formatAmount(amount)}`,
        date: formatDate(tx.timestamp || tx.createdOn),
        icon: getTransactionIcon(type),
        ref: tx.transactionId || tx.referenceNo || 'N/A',
        status: mapStatus(tx.status),
        currency: tx.currencyType || 'NGN'
      };
    });
  };

  const mapStatus = (status: string) => {
    const s = status?.toLowerCase() || '';
    if (s === 'success' || s === 'completed' || s === 'confirmed') return 'Successful';
    if (s === 'pending' || s === 'processing') return 'Pending';
    if (s === 'failed' || s === 'rejected' || s === 'cancelled') return 'Failed';
    return 'Pending';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  };

  const handleViewAll = () => {
    setDisplayLimit(transactions.length);
  };

  const themeClass = theme === "dark" ? "color-dark" : "color-light";

  return (
    <div className="history-container">
      <div className="section-header">
        <h3 className={`history-title ${theme === "dark" ? "color-light" : "color-dark"}`}>Recent Transactions</h3>
        <button className="view-all" onClick={handleViewAll}>View All</button>
      </div>

      <div className="transaction-list">
        {transactions.length > 0 ? (
          transactions.slice(0, displayLimit).map((tx) => (
            <div key={tx.id} className="transaction-item" onClick={() => setSelectedTx(tx)}>
              <div className={`tx-icon-box ${tx.type}`}>
                {tx.icon}
              </div>
              <div className={`tx-details ${themeClass}`}>
                <p className="tx-title">{tx.title}</p>
                <p className="tx-meta">{tx.category} • {tx.date}</p>
              </div>
              <div className="tx-amount-box">
                <p className={`tx-amount ${tx.type}`}>{tx.amount}</p>
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <p>No transactions found</p>
          </div>
        )}
      </div>

      {selectedTx && (
        <div className="tx-modal-overlay" onClick={() => setSelectedTx(null)}>
          <div className="tx-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="tx-modal-close" onClick={() => setSelectedTx(null)}>
              <X size={20} />
            </button>

            <div className="tx-modal-header">
              <div className={`tx-modal-icon-wrapper ${selectedTx.type}`}>
                {selectedTx.icon}
              </div>
              <h4 className={`${theme === "dark" ? "color-light" : "color-dark"}`}>Transaction Details</h4>
              <p className={`tx-status-badge ${selectedTx.status.toLowerCase()}`}>
                {selectedTx.status}
              </p>
            </div>

            <div className="tx-modal-body">
              <div className="tx-receipt-amount">
                <span className={selectedTx.type}>{selectedTx.amount}</span>
              </div>

              <div className="tx-info-grid">
                <div className="tx-info-row">
                  <span className="label">Transaction Type</span>
                  <span className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}>{selectedTx.type === 'credit' ? 'Inbound' : 'Outbound'}</span>
                </div>
                <div className="tx-info-row">
                  <span className="label">Category</span>
                  <span className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}>{selectedTx.category}</span>
                </div>
                <div className="tx-info-row">
                  <span className="label">Date & Time</span>
                  <span className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}>{selectedTx.date}</span>
                </div>
                <div className="tx-info-row">
                  <span className="label">Reference</span>
                  <span className={`value ${theme === "dark" ? "color-light" : "color-dark"} ref-code`}>
                    {selectedTx.ref} <Copy size={14} style={{ marginLeft: '4px', cursor: 'pointer' }} />
                  </span>
                </div>
              </div>

              <button className="tx-download-btn">
                Download Receipt
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default History;