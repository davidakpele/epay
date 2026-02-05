'use client';

import { useState, useEffect, useMemo } from 'react';
import DepositModal from '@/components/DepositModal'
import Footer from '@/components/Footer'
import Header from '@/components/Header'
import MobileNav from '@/components/MobileNav'
import Sidebar from '@/components/Sidebar'
import { 
  Users, 
  TrendingUp, 
  Search, 
  Filter, 
  ChevronRight, 
  X
} from 'lucide-react';
import "./Refer.css"
import { referralData } from '../lib/referralData';
import { ReferredUser } from '../types/utils';
import LoadingScreen from '@/components/loader/Loadingscreen';

const Refer = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [searchTerm, setSearchTerm] = useState('');
    const [activeModal, setActiveModal] = useState<'none' | 'filter-main' | 'year-range' | 'details' | 'month-select'>('none');
    const [selectedUser, setSelectedUser] = useState<ReferredUser | null>(null);
    const [isPageLoading, setIsPageLoading] = useState(true);
    const [yearRange, setYearRange] = useState({ from: '2020', to: '2026' });
    const [selectedMonth, setSelectedMonth] = useState<string>('All');

    useEffect(() => {
        // Handle page loading
        const loadingTimer = setTimeout(() => {
          setIsPageLoading(false);
        }, 2000);
    
        return () => clearTimeout(loadingTimer);
    }, []);

    const months = [
        "All", "January", "February", "March", "April", "May", "June", 
        "July", "August", "September", "October", "November", "December"
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

    
    if (isPageLoading) {
        return <LoadingScreen />;
    }

    const filteredUsers = useMemo(() => {
        return referralData.filter(user => {
            const dateObj = new Date(user.joinedDate);
            const userYear = dateObj.getFullYear();
            const userMonth = dateObj.toLocaleString('default', { month: 'long' });

            const matchesSearch = user.username.toLowerCase().includes(searchTerm.toLowerCase());
            
            const matchesYear = userYear >= parseInt(yearRange.from) && userYear <= parseInt(yearRange.to);
            
            const matchesMonth = selectedMonth === 'All' || userMonth === selectedMonth;

            return matchesSearch && matchesYear && matchesMonth;
        });
    }, [searchTerm, yearRange, selectedMonth]);

    return (
        <div className={`dashboard-container ${theme === 'dark' ? 'dark' : ''}`}>
            <Sidebar />
            <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
                <Header theme={theme} toggleTheme={toggleTheme} />
                <div className="scrollable-content">
                <div className={`referral-container ${theme === "dark" ? "bg-light" : "bg-dark"}`}>
                        <div className="referral-header">
                            <div>
                                <h1 className={`${theme === "dark" ? "color-light" : "color-dark"}`}>Referral Program</h1>
                                <p className={`${theme === "dark" ? "color-light" : "color-dark"}`}>Manage your network and track commissions</p>
                            </div>
                            <div className="referral-link-btn">
                                Ref ID: <span>EPAY-7721</span>
                            </div>
                        </div>

                        <div className="stats-grid">
                            <div className="stat-card total-earned">
                                <div className="stat-info">
                                    <p>Total Commission</p>
                                    <h3>₦ 6,700.00</h3>
                                </div>
                                <div className="stat-icon red-bg"><TrendingUp size={24} /></div>
                            </div>
                            <div className={`stat-card ${theme === "dark" ? "bg-dark" : "bg-light"}`}>
                                <div className="stat-info">
                                    <p>Direct Referrals</p>
                                    <h3>{referralData.length} Users</h3>
                                </div>
                                <div className="stat-icon gray-bg"><Users size={24} /></div>
                            </div>
                        </div>

                        <div className="refer-filter-row">
                            <div className="refer-search-container">
                                <Search size={18} className="refer-search-icon" />
                                <input 
                                    type="text" 
                                    placeholder="Search username..." 
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </div>
                            <button className="refer-filter-main-btn" onClick={() => setActiveModal('filter-main')}>
                                <Filter size={18} />
                                <span>Filter</span>
                            </button>
                        </div>

                        <div className="table-outer-wrapper">
                            <div className="table-inner-scroll">
                                <table className="referral-table">
                                    <thead>
                                        <tr>
                                            <th>Username</th>
                                            <th>Joined Date</th>
                                            <th>Earnings (₦)</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredUsers.map((user) => (
                                            <tr key={user.id}>
                                                <td>
                                                    <div className="user-profile-cell">
                                                        <div className="avatar-circle">{user.username.charAt(0).toUpperCase()}</div>
                                                        <span className="username-text">{user.username}</span>
                                                    </div>
                                                </td>
                                                <td>{user.joinedDate}</td>
                                                <td className="earning-cell-text">₦{user.totalEarningsFromUser.toLocaleString()}</td>
                                                <td>
                                                    <button className="details-action-btn" onClick={() => { setSelectedUser(user); setActiveModal('details'); }}>
                                                        Details <ChevronRight size={14} />
                                                    </button>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div> 

                        {activeModal !== 'none' && (
                            <div className="refer-modal-overlay" onClick={() => setActiveModal('none')}>
                                <div className="refer-modal-content" onClick={e => e.stopPropagation()}>
                                    <div className="refer-modal-header">
                                        <h3>
                                            {activeModal === 'filter-main' && 'Filter By'}
                                            {activeModal === 'year-range' && 'Select Year Range'}
                                            {activeModal === 'month-select' && 'Select Month'}
                                            {activeModal === 'details' && 'User Transactions'}
                                        </h3>
                                        <X className="close-icon" onClick={() => setActiveModal('none')} />
                                    </div>

                                    {activeModal === 'filter-main' && (
                                        <div className="refer-filter-options">
                                            <button onClick={() => handleFilterSelect('all')}>Show All Referrals</button>
                                            <button onClick={() => handleFilterSelect('year')}>Filter by Year Range</button>
                                            <button onClick={() => handleFilterSelect('month')}>Filter by Month</button>
                                        </div>
                                    )}

                                    {activeModal === 'month-select' && (
                                        <div className="refer-filter-options month-grid">
                                            {months.map(m => (
                                                <button 
                                                    key={m} 
                                                    className={selectedMonth === m ? 'active-opt' : ''}
                                                    onClick={() => { setSelectedMonth(m); setActiveModal('none'); }}
                                                >
                                                    {m}
                                                </button>
                                            ))}
                                        </div>
                                    )}

                                    {activeModal === 'year-range' && (
                                        <div className="refer-range-picker">
                                            <div className="input-row">
                                                <div className="input-group">
                                                    <label>From (Year)</label>
                                                    <input 
                                                        type="number" 
                                                        value={yearRange.from} 
                                                        onChange={(e) => setYearRange({...yearRange, from: e.target.value})} 
                                                    />
                                                </div>
                                                <div className="input-group">
                                                    <label>To (Year)</label>
                                                    <input 
                                                        type="number" 
                                                        value={yearRange.to} 
                                                        onChange={(e) => setYearRange({...yearRange, to: e.target.value})} 
                                                    />
                                                </div>
                                            </div>
                                            <button className="apply-btn" onClick={() => setActiveModal('none')}>Apply Filter</button>
                                        </div>
                                    )}

                                    {activeModal === 'details' && selectedUser && (
                                        <div className="refer-details-view">
                                            <p className="subtitle">Activity for <strong>{selectedUser.username}</strong></p>
                                            <div className="tx-list">
                                                {selectedUser.transactions.map(tx => (
                                                    <div key={tx.id} className="tx-item">
                                                        <div>
                                                            <p className="tx-type">{tx.type}</p>
                                                            <p className="tx-date">{tx.date}</p>
                                                        </div>
                                                        <div className="tx-amounts">
                                                            <p className="tx-main">₦{tx.amount.toLocaleString()}</p>
                                                            <p className="tx-comm">+₦{tx.commission} earned</p>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                    <Footer theme={theme} />
                </div>
            </main>
            <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
            <DepositModal isOpen={isDepositOpen} onClose={() => setIsDepositOpen(false)} theme={theme} />
        </div>
    );
};

export default Refer;