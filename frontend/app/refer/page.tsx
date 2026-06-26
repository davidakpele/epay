'use client';

import { useState, useEffect, useMemo } from 'react';
import DepositModal from '@/components/DepositModal';
import Footer from '@/components/Footer';
import Header from '@/components/Header';
import MobileNav from '@/components/MobileNav';
import Sidebar from '@/components/Sidebar';
import Link from 'next/link';
import {
  Search,
  Filter,
  ChevronRight,
  ChevronLeft,
  X,
  Link2,
  Wallet2,
  Megaphone,
} from 'lucide-react';
import './Refer.css';
import { referralData } from '../lib/referralData';
import { ReferredUser } from '../types/utils';
import LoadingScreen from '@/components/loader/Loadingscreen';
import SupportChatBot from '@/components/SupportChatBot';

const PAGE_SIZE = 5;

const Refer = () => {
  const [isDepositOpen, setIsDepositOpen] = useState(false);
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [searchTerm, setSearchTerm] = useState('');
  const [activeModal, setActiveModal] = useState<'none' | 'filter-main' | 'year-range' | 'details' | 'month-select'>('none');
  const [selectedUser, setSelectedUser] = useState<ReferredUser | null>(null);
  const [isPageLoading, setIsPageLoading] = useState(true);
  const [yearRange, setYearRange] = useState({ from: '2020', to: '2026' });
  const [selectedMonth, setSelectedMonth] = useState<string>('All');
  const [currentPage, setCurrentPage] = useState(1);
  const [sortField, setSortField] = useState<'joinedDate' | 'totalEarningsFromUser' | null>(null);
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  const [isChatOpen, setIsChatOpen] = useState(false);
  useEffect(() => {
    const t = setTimeout(() => setIsPageLoading(false), 2000);
    return () => clearTimeout(t);
  }, []);

  const months = [
    'All', 'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.classList.toggle('dark', newTheme === 'dark');
  };

  const handleFilterSelect = (type: string) => {
    if (type === 'all') {
      setYearRange({ from: '2020', to: '2026' });
      setSelectedMonth('All');
      setActiveModal('none');
    } else if (type === 'year') {
      setActiveModal('year-range');
    } else if (type === 'month') {
      setActiveModal('month-select');
    }
  };

  const handleSort = (field: 'joinedDate' | 'totalEarningsFromUser') => {
    if (sortField === field) setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    else { setSortField(field); setSortDir('asc'); }
    setCurrentPage(1);
  };

  const filteredUsers = useMemo(() => {
    let list = referralData.filter(user => {
      const dateObj = new Date(user.joinedDate);
      const userYear = dateObj.getFullYear();
      const userMonth = dateObj.toLocaleString('default', { month: 'long' });
      const matchesSearch = user.username.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesYear = userYear >= parseInt(yearRange.from) && userYear <= parseInt(yearRange.to);
      const matchesMonth = selectedMonth === 'All' || userMonth === selectedMonth;
      return matchesSearch && matchesYear && matchesMonth;
    });
    if (sortField) {
      list = [...list].sort((a, b) => {
        let av: number, bv: number;
        if (sortField === 'joinedDate') {
          av = new Date(a.joinedDate).getTime(); bv = new Date(b.joinedDate).getTime();
        } else {
          av = a.totalEarningsFromUser; bv = b.totalEarningsFromUser;
        }
        return sortDir === 'asc' ? av - bv : bv - av;
      });
    }
    return list;
  }, [searchTerm, yearRange, selectedMonth, sortField, sortDir]);

  const totalPages = Math.max(1, Math.ceil(filteredUsers.length / PAGE_SIZE));
  const paginated = filteredUsers.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);
  const totalEarnings = referralData.reduce((s, u) => s + u.totalEarningsFromUser, 0);
  const totalWithdrawals = Math.floor(totalEarnings * 0.515);
  const hasActiveFilter = selectedMonth !== 'All' || yearRange.from !== '2020' || yearRange.to !== '2026';

  const getSortIcon = (field: 'joinedDate' | 'totalEarningsFromUser') => {
    if (sortField !== field) return '⇅';
    return sortDir === 'asc' ? '↑' : '↓';
  };

  const visiblePages = () => {
    const max = 5, half = Math.floor(max / 2);
    let start = Math.max(1, currentPage - half);
    let end = Math.min(totalPages, start + max - 1);
    if (end - start < max - 1) start = Math.max(1, end - max + 1);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  };

  if (isPageLoading) return <LoadingScreen />;

  return (
    <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
      <Sidebar />

      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />

        <div className="scrollable-content">
          <div className="referral-container">

            {/* Breadcrumb */}
            <div className="referral-breadcrumb">
              <Link href="/dashboard" className="referral-bc-link">Dashboard</Link>
              <ChevronRight size={13} className="referral-bc-sep" />
              <span className="referral-bc-current">Referral Program</span>
            </div>

            {/* Page header */}
            <div className="referral-header">
              <div>
                <h1 className="referral-title">Referral Program</h1>
                <p className="referral-subtitle">Manage your referrals and track your earnings.</p>
              </div>
              <div className="referral-link-btn">
                <Link2 size={14} />
                Ref ID: <span>EPAY-7721</span>
              </div>
            </div>

            {/* Stats */}
            <div className="referral-stats-grid">
              <div className="referral-stat-card">
                <div className="referral-stat-icon rsi-blue"><Megaphone style={{color:"var(--bg-main)"}}/></div>
                <div className="referral-stat-info">
                  <p className="referral-stat-label">Total Referrals</p>
                  <h3 className="referral-stat-value">{referralData.length}</h3>
                </div>
              </div>
              <div className="referral-stat-card">
                <div className="referral-stat-icon rsi-green"><Wallet2 style={{color:"var(--bg-main)"}}/></div>
                <div className="referral-stat-info">
                  <p className="referral-stat-label">Total Earnings</p>
                  <h3 className="referral-stat-value green">
                    ${totalEarnings.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </h3>
                </div>
              </div>
              <div className="referral-stat-card">
                <div className="referral-stat-icon rsi-amber"><Wallet2 style={{color:"var(--bg-main)"}}/></div>
                <div className="referral-stat-info">
                  <p className="referral-stat-label">Total Withdrawals</p>
                  <h3 className="referral-stat-value green">
                    ${totalWithdrawals.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                  </h3>
                </div>
              </div>
            </div>

            {/* Table card */}
            <div className="referral-table-card">

              {/* Card header */}
              <div className="referral-table-header">
                <h2 className="referral-table-title">Referred Users</h2>
                <div className="referral-table-controls">
                  <div className="refer-search-wrap">
                    <Search size={15} className="refer-search-icon" />
                    <input
                      type="text"
                      className="refer-search-input"
                      placeholder="Search users..."
                      value={searchTerm}
                      onChange={e => { setSearchTerm(e.target.value); setCurrentPage(1); }}
                    />
                  </div>
                  <button
                    className={`refer-filter-btn${hasActiveFilter ? ' active' : ''}`}
                    onClick={() => setActiveModal('filter-main')}
                  >
                    <Filter size={14} /> Filter
                  </button>
                </div>
              </div>

              {/* Table */}
              <div className="refer-table-wrap">
                <table className="refer-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th
                        className={`refer-th-sort${sortField === 'joinedDate' ? ' active' : ''}`}
                        onClick={() => handleSort('joinedDate')}
                      >
                        Date Referred <span className="refer-sort-icon">{getSortIcon('joinedDate')}</span>
                      </th>
                      <th
                        className={`refer-th-sort${sortField === 'totalEarningsFromUser' ? ' active' : ''}`}
                        onClick={() => handleSort('totalEarningsFromUser')}
                      >
                        Earnings <span className="refer-sort-icon">{getSortIcon('totalEarningsFromUser')}</span>
                      </th>
                      <th>Withdrawn</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {paginated.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="refer-empty-cell">No referrals found.</td>
                      </tr>
                    ) : paginated.map(user => (
                      <tr
                        key={user.id}
                        className="refer-table-row"
                        onClick={() => { setSelectedUser(user); setActiveModal('details'); }}
                      >
                        <td>
                          <div className="refer-name-cell">
                            <div className="refer-avatar">{user.username.charAt(0).toUpperCase()}</div>
                            <span className="refer-username">{user.username}</span>
                          </div>
                        </td>
                        <td className="refer-cell-muted">
                          {`${user.username.toLowerCase().replace(/\s+/g, '.')}@email.com`}
                        </td>
                        <td className="refer-cell-muted">
                          {new Date(user.joinedDate).toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', year: 'numeric' })}
                        </td>
                        <td className="refer-cell-green">${user.totalEarningsFromUser.toFixed(2)}</td>
                        <td className="refer-cell-green">${(user.totalEarningsFromUser * 0.5).toFixed(2)}</td>
                        <td><span className="refer-status-badge">Active</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className="refer-pagination">
                <span className="refer-pagination-info">
                  Showing {filteredUsers.length === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1} to{' '}
                  {Math.min(currentPage * PAGE_SIZE, filteredUsers.length)} of {filteredUsers.length} entries
                </span>
                <div className="refer-pagination-btns">
                  <button className="refer-pg-btn" onClick={() => setCurrentPage(p => Math.max(1, p - 1))} disabled={currentPage === 1}>
                    <ChevronLeft size={13} />
                  </button>
                  {visiblePages().map(p => (
                    <button key={p} className={`refer-pg-btn${currentPage === p ? ' active' : ''}`} onClick={() => setCurrentPage(p)}>
                      {p}
                    </button>
                  ))}
                  <button className="refer-pg-btn" onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))} disabled={currentPage === totalPages}>
                    <ChevronRight size={13} />
                  </button>
                </div>
              </div>

            </div>
          </div>

          {/* Modals */}
          {activeModal !== 'none' && (
            <div className="refer-modal-overlay" onClick={() => setActiveModal('none')}>
              <div className="refer-modal-box" onClick={e => e.stopPropagation()}>

                <div className="refer-modal-head">
                  <h3>
                    {activeModal === 'filter-main' && 'Filter By'}
                    {activeModal === 'year-range' && 'Select Year Range'}
                    {activeModal === 'month-select' && 'Select Month'}
                    {activeModal === 'details' && 'User Transactions'}
                  </h3>
                  <button className="refer-modal-close" onClick={() => setActiveModal('none')}>
                    <X size={16} />
                  </button>
                </div>

                {activeModal === 'filter-main' && (
                  <div className="refer-filter-opts">
                    <button className="refer-filter-opt" onClick={() => handleFilterSelect('all')}>Show All Referrals</button>
                    <button className="refer-filter-opt" onClick={() => handleFilterSelect('year')}>Filter by Year Range</button>
                    <button className="refer-filter-opt" onClick={() => handleFilterSelect('month')}>Filter by Month</button>
                  </div>
                )}

                {activeModal === 'month-select' && (
                  <div className="refer-month-grid">
                    {months.map(m => (
                      <button
                        key={m}
                        className={`refer-month-btn${selectedMonth === m ? ' active' : ''}`}
                        onClick={() => { setSelectedMonth(m); setActiveModal('none'); setCurrentPage(1); }}
                      >
                        {m}
                      </button>
                    ))}
                  </div>
                )}

                {activeModal === 'year-range' && (
                  <div className="refer-year-form">
                    <div className="refer-year-row">
                      <div className="refer-year-field">
                        <label>From (Year)</label>
                        <input type="number" value={yearRange.from} onChange={e => setYearRange({ ...yearRange, from: e.target.value })} />
                      </div>
                      <div className="refer-year-field">
                        <label>To (Year)</label>
                        <input type="number" value={yearRange.to} onChange={e => setYearRange({ ...yearRange, to: e.target.value })} />
                      </div>
                    </div>
                    <button className="refer-apply-btn" onClick={() => { setCurrentPage(1); setActiveModal('none'); }}>Apply Filter</button>
                  </div>
                )}

                {activeModal === 'details' && selectedUser && (
                  <div className="refer-details-view">
                    <p className="refer-details-meta">Activity for <strong>{selectedUser.username}</strong></p>
                    <div className="refer-details-stats">
                      <div className="refer-detail-stat">
                        <span className="rds-label">Total Earnings</span>
                        <span className="rds-val green">${selectedUser.totalEarningsFromUser.toFixed(2)}</span>
                      </div>
                      <div className="refer-detail-stat">
                        <span className="rds-label">Withdrawn</span>
                        <span className="rds-val green">${(selectedUser.totalEarningsFromUser * 0.5).toFixed(2)}</span>
                      </div>
                    </div>
                    <div className="refer-tx-list">
                      {selectedUser.transactions.map(tx => (
                        <div key={tx.id} className="refer-tx-item">
                          <div>
                            <p className="refer-tx-type">{tx.type}</p>
                            <p className="refer-tx-date">{tx.date}</p>
                          </div>
                          <div className="refer-tx-right">
                            <p className="refer-tx-amount">₦{tx.amount.toLocaleString()}</p>
                            <p className="refer-tx-comm">+₦{tx.commission} earned</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

              </div>
            </div>
          )}

          <Footer theme={theme} />
        </div>
      </main>

      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
      <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
      {!isChatOpen && (
        <button
          className="chat-fab"
          onClick={() => setIsChatOpen(true)}
          aria-label="Open support chat"
        >
          <i className="fa-solid fa-comment-dots"></i>
        </button>
      )}

      <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
    </div>
  );
};

export default Refer;