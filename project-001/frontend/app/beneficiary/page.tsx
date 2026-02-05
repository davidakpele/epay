'use client';

import DepositModal from "@/components/DepositModal";
import Footer from "@/components/Footer";
import Header from "@/components/Header";
import MobileNav from "@/components/MobileNav";
import Sidebar from "@/components/Sidebar";
import { useState, useMemo, useEffect } from "react";
import "./BeneficiaryManager.css";
import LoadingScreen from "@/components/loader/Loadingscreen";
import { beneficiaryService, getUserId } from "@/app/api";

interface Transaction {
    id: number;
    date: string;
    amount: string;
    type: string;
    status: string;
}

interface Beneficiary {
    id: number;
    userId: number;
    beneficiaryType: 'bank' | 'user';
    beneficiaryName: string;
    currency: string;
    accountNumber?: string;
    accountName?: string;
    bankCode?: string;
    bankName?: string;
    recipientUsername?: string;
    isActive: boolean;
    createdOn: string;
    updatedOn: string;
    // UI-specific fields
    initial: string;
    detail: string;
    category: string;
    isEpay: boolean;
    lastTransaction: string;
    amount: string;
    transactions: Transaction[];
}

const BeneficiaryManager = () => {
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [activeFilter, setActiveFilter] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
    const [showHistoryModal, setShowHistoryModal] = useState(false);
    const [selectedBeneficiary, setSelectedBeneficiary] = useState<Beneficiary | null>(null);
    const [expandedTransaction, setExpandedTransaction] = useState<number | null>(null);
    const [historyFilter, setHistoryFilter] = useState('all');
    const [dateFrom, setDateFrom] = useState('');
    const [dateTo, setDateTo] = useState('');
    const [isPageLoading, setIsPageLoading] = useState(true);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [beneficiaryToDelete, setBeneficiaryToDelete] = useState<number | null>(null);
    const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([]);
    const [isLoadingBeneficiaries, setIsLoadingBeneficiaries] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Format beneficiary data from API to UI format
    const formatBeneficiary = (apiBeneficiary: any): Beneficiary => {
        const isEpay = apiBeneficiary.beneficiaryType === 'user';
        
        // Generate initials
        const nameParts = apiBeneficiary.beneficiaryName.split(' ');
        const initial = nameParts.map((part: string) => part[0]).join('').substring(0, 2).toUpperCase();

        // Generate detail string
        let detail = '';
        if (isEpay) {
            detail = `ePay Wallet • @${apiBeneficiary.recipientUsername}`;
        } else {
            detail = `${apiBeneficiary.bankName} • ${apiBeneficiary.accountNumber}`;
        }

        // Determine category
        const category = isEpay ? 'epay' : 'banks';

        // Calculate last transaction (placeholder - you can update this with real transaction data)
        const createdDate = new Date(apiBeneficiary.createdOn);
        const daysDiff = Math.floor((Date.now() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
        let lastTransaction = '';
        if (daysDiff === 0) lastTransaction = 'Today';
        else if (daysDiff === 1) lastTransaction = 'Yesterday';
        else if (daysDiff < 7) lastTransaction = `${daysDiff} days ago`;
        else if (daysDiff < 30) lastTransaction = `${Math.floor(daysDiff / 7)} week${Math.floor(daysDiff / 7) > 1 ? 's' : ''} ago`;
        else lastTransaction = `${Math.floor(daysDiff / 30)} month${Math.floor(daysDiff / 30) > 1 ? 's' : ''} ago`;

        return {
            ...apiBeneficiary,
            initial,
            detail,
            category,
            isEpay,
            lastTransaction,
            amount: '₦0.00', // Placeholder - update with real transaction total
            transactions: [], // Placeholder - update with real transaction history
        };
    };

    // Fetch beneficiaries from API
    const fetchBeneficiaries = async () => {
        setIsLoadingBeneficiaries(true);
        setError(null);

        try {
            const userId = getUserId();
            if (!userId) {
                throw new Error('User not authenticated');
            }

            const response = await beneficiaryService.getAllByUserId(userId);
            
            if (response && response.data) {
                const formattedBeneficiaries = response.data.map(formatBeneficiary);
                setBeneficiaries(formattedBeneficiaries);
            } else {
                setBeneficiaries([]);
            }
        } catch (err: any) {
            console.error('Error fetching beneficiaries:', err);
            setError(err.message || 'Failed to load beneficiaries');
            setBeneficiaries([]);
        } finally {
            setIsLoadingBeneficiaries(false);
        }
    };

    // Delete beneficiary
    const deleteBeneficiary = async (id: number) => {
        try {
            const userId = getUserId();
            if (!userId) {
                throw new Error('User not authenticated');
            }

            await beneficiaryService.deleteBeneficiary(id);
            
            // Remove from local state
            setBeneficiaries(beneficiaries.filter(b => b.id !== id));
            setShowDeleteConfirm(false);
            setBeneficiaryToDelete(null);
        } catch (err: any) {
            console.error('Error deleting beneficiary:', err);
            setError(err.message || 'Failed to delete beneficiary');
        }
    };

    useEffect(() => {
        // Handle page loading and fetch data
        const initializePage = async () => {
            setIsPageLoading(true);
            await fetchBeneficiaries();
            setIsPageLoading(false);
        };

        initializePage();
    }, []);

    const toggleTheme = () => {
        const newTheme = theme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
        document.documentElement.classList.toggle('dark', newTheme === 'dark');
    };

    const filteredBeneficiaries = useMemo(() => {
        return beneficiaries.filter((b) => {
            const matchesTab = activeFilter === 'all' || b.category === activeFilter;
            const matchesSearch = b.beneficiaryName.toLowerCase().includes(searchQuery.toLowerCase()) || 
                                 b.detail.toLowerCase().includes(searchQuery.toLowerCase());
            return matchesTab && matchesSearch;
        });
    }, [activeFilter, searchQuery, beneficiaries]);

    const getCategoryCount = (category: string) => {
        if (category === 'all') return beneficiaries.length;
        return beneficiaries.filter(b => b.category === category).length;
    };

    const openHistoryModal = (beneficiary: Beneficiary) => {
        setSelectedBeneficiary(beneficiary);
        setShowHistoryModal(true);
        setHistoryFilter('all');
        setDateFrom('');
        setDateTo('');
    };

    const handleDeleteClick = (id: number) => {
        setBeneficiaryToDelete(id);
        setShowDeleteConfirm(true);
    };

    const confirmDelete = () => {
        if (beneficiaryToDelete) {
            deleteBeneficiary(beneficiaryToDelete);
        }
    };

    const getFilteredTransactions = () => {
        if (!selectedBeneficiary) return [];
        
        const now = new Date();
        const transactions = selectedBeneficiary.transactions;

        if (historyFilter === 'custom' && dateFrom && dateTo) {
            return transactions.filter((t: Transaction) => {
                const tDate = new Date(t.date);
                return tDate >= new Date(dateFrom) && tDate <= new Date(dateTo);
            });
        }

        return transactions.filter((t: Transaction) => {
            const tDate = new Date(t.date);
            const diffTime = now.getTime() - tDate.getTime();
            const diffDays = diffTime / (1000 * 3600 * 24);

            switch(historyFilter) {
                case 'day':
                    return diffDays <= 1;
                case 'week':
                    return diffDays <= 7;
                case 'month':
                    return diffDays <= 30;
                default:
                    return true;
            }
        });
    };

    // Get frequent beneficiaries (top 5 by most recent)
    const frequentBeneficiaries = useMemo(() => {
        return [...beneficiaries]
            .sort((a, b) => new Date(b.updatedOn).getTime() - new Date(a.updatedOn).getTime())
            .slice(0, 5);
    }, [beneficiaries]);

    // Generate random gradient colors for frequent beneficiaries
    const getGradientColor = (index: number) => {
        const colors = [
            "bg-gradient-to-br from-orange-400 to-orange-600",
            "bg-gradient-to-br from-blue-400 to-blue-600",
            "bg-gradient-to-br from-purple-400 to-purple-600",
            "bg-gradient-to-br from-green-400 to-green-600",
            "bg-gradient-to-br from-pink-400 to-pink-600"
        ];
        return colors[index % colors.length];
    };

    if (isPageLoading) {
        return <LoadingScreen />;
    }

    return (
        <div className="dashboard-container">
            <Sidebar />
            <main className={`main-content ${isDepositOpen ? 'blur-sm' : ''}`}>
                <Header theme={theme} toggleTheme={toggleTheme} />
                <div className="scrollable-content">
                    <div className="pt-20 px-4 sm:px-6 lg:px-8">
                        <div className="max-w-7xl mx-auto w-full">
                            
                            {/* Error Message */}
                            {error && (
                                <div className="bg-red-50 border-2 border-red-200 rounded-2xl p-4 mb-6 flex items-center gap-3">
                                    <i className="fas fa-exclamation-circle text-red-600 text-xl"></i>
                                    <div className="flex-1">
                                        <h4 className="font-bold text-red-900">Error</h4>
                                        <p className="text-sm text-red-700">{error}</p>
                                    </div>
                                    <button 
                                        onClick={() => setError(null)}
                                        className="text-red-600 hover:text-red-800"
                                    >
                                        <i className="fas fa-times"></i>
                                    </button>
                                </div>
                            )}

                            {/* Header Banner */}
                            <div className="bg-gradient-to-br from-red-600 via-red-700 to-red-800 rounded-3xl sm:rounded-[2rem] p-6 sm:p-8 lg:p-10 mb-6 shadow-xl">
                                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-3 mb-3">
                                            <div className="w-12 h-12 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center">
                                                <i className="fas fa-users text-white text-xl"></i>
                                            </div>
                                            <div>
                                                <h1 className="text-2xl sm:text-3xl lg:text-4xl font-bold text-white">Saved Beneficiaries</h1>
                                            </div>
                                        </div>
                                        <p className="text-red-100 text-sm sm:text-base max-w-xl">Manage your trusted contacts and send money instantly to your favorite recipients.</p>
                                    </div>
                                    
                                    <div className="flex flex-wrap gap-4">
                                        <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 min-w-[140px] border border-white/20">
                                            <div className="text-white/80 text-xs font-medium mb-1">Total Contacts</div>
                                            <div className="text-white text-2xl sm:text-3xl font-bold">{beneficiaries.length}</div>
                                        </div>
                                        <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 min-w-[140px] border border-white/20">
                                            <div className="text-white/80 text-xs font-medium mb-1">ePay Users</div>
                                            <div className="text-white text-2xl sm:text-3xl font-bold">{beneficiaries.filter(b => b.isEpay).length}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Search Bar */}
                            <div className="bg-white rounded-2xl sm:rounded-3xl shadow-sm border border-gray-100 p-4 sm:p-6 mb-6">
                                <div className="flex flex-col lg:flex-row gap-4 items-stretch lg:items-center">
                                    <div className="flex-1 relative">
                                        <i className="fas fa-search absolute left-4 top-1/2 -translate-y-1/2 text-gray-400"></i>
                                        <input 
                                            type="text" 
                                            value={searchQuery}
                                            onChange={(e) => setSearchQuery(e.target.value)}
                                            placeholder="Search contacts..." 
                                            className="w-full pl-12 pr-4 py-3.5 bg-gray-50 border border-gray-200 rounded-xl sm:rounded-2xl focus:ring-2 focus:ring-red-500 focus:bg-white outline-none transition-all text-sm sm:text-base text-gray-900 placeholder:text-gray-400"
                                        />
                                        {searchQuery && (
                                            <button 
                                                onClick={() => setSearchQuery('')}
                                                className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                                            >
                                                <i className="fas fa-times"></i>
                                            </button>
                                        )}
                                    </div>

                                    <div className="flex gap-2 bg-gray-100 rounded-xl p-1.5">
                                        <button 
                                            onClick={() => setViewMode('grid')}
                                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${viewMode === 'grid' ? 'bg-white text-red-600 shadow-sm' : 'text-gray-600'}`}
                                        >
                                            <i className="fas fa-th mr-2"></i>
                                            <span className="hidden sm:inline">Grid</span>
                                        </button>
                                        <button 
                                            onClick={() => setViewMode('list')}
                                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${viewMode === 'list' ? 'bg-white text-red-600 shadow-sm' : 'text-gray-600'}`}
                                        >
                                            <i className="fas fa-list mr-2"></i>
                                            <span className="hidden sm:inline">List</span>
                                        </button>
                                    </div>
                                </div>
                            </div>

                            {/* Filter Tabs */}
                            <div className="bg-white rounded-2xl sm:rounded-3xl shadow-sm border border-gray-100 mb-6 overflow-hidden">
                                <div className="flex overflow-x-auto no-scrollbar">
                                    {[
                                        { key: 'all', label: 'All', icon: 'users' },
                                        { key: 'banks', label: 'Banks', icon: 'university' },
                                        { key: 'epay', label: 'ePay', icon: 'wallet' },
                                    ].map((tab) => (
                                        <button 
                                            key={tab.key}
                                            onClick={() => setActiveFilter(tab.key)} 
                                            className={`flex-1 min-w-[140px] px-4 sm:px-6 py-4 sm:py-5 flex items-center justify-center gap-2 sm:gap-3 font-bold text-xs sm:text-sm transition-all border-b-4 ${
                                                activeFilter === tab.key 
                                                    ? 'text-red-600 bg-red-50/50 border-red-600' 
                                                    : 'text-gray-500 border-transparent hover:bg-gray-50'
                                            }`}
                                        >
                                            <i className={`fas fa-${tab.icon} text-sm sm:text-base`}></i>
                                            <span>{tab.label}</span>
                                            <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${
                                                activeFilter === tab.key ? 'bg-red-600 text-white' : 'bg-gray-200 text-gray-600'
                                            }`}>
                                                {getCategoryCount(tab.key)}
                                            </span>
                                        </button>
                                    ))}
                                </div>
                            </div>

                            {/* Frequent Recipients */}
                            {frequentBeneficiaries.length > 0 && (
                                <div className="mb-6">
                                    <div className="flex items-center justify-between mb-4">
                                        <h3 className="text-sm sm:text-base font-bold text-gray-700 flex items-center gap-2">
                                            <i className="fas fa-star text-yellow-500"></i>
                                            Frequent Recipients
                                        </h3>
                                    </div>
                                    <div className="flex gap-3 sm:gap-4 overflow-x-auto pb-4 no-scrollbar">
                                        {frequentBeneficiaries.map((person, idx) => (
                                            <div 
                                                key={person.id} 
                                                className="flex-shrink-0 flex flex-col items-center gap-2 cursor-pointer group"
                                                onClick={() => openHistoryModal(person)}
                                            >
                                                <div className={`w-16 h-16 sm:w-20 sm:h-20 rounded-2xl ${getGradientColor(idx)} flex items-center justify-center text-white text-lg sm:text-xl font-bold shadow-lg group-hover:scale-110 transition-transform`}>
                                                    {person.initial}
                                                </div>
                                                <span className="text-xs font-semibold text-gray-700 text-center max-w-[80px] truncate">{person.beneficiaryName}</span>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Loading State */}
                            {isLoadingBeneficiaries && (
                                <div className="flex items-center justify-center py-12">
                                    <div className="text-center">
                                        <div className="w-16 h-16 border-4 border-red-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
                                        <p className="text-gray-600">Loading beneficiaries...</p>
                                    </div>
                                </div>
                            )}

                            {/* Grid View */}
                            {!isLoadingBeneficiaries && viewMode === 'grid' && (
                                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-6 mb-8">
                                    {filteredBeneficiaries.map((b) => (
                                        <div key={b.id} className={`bg-white rounded-2xl sm:rounded-3xl border-2 transition-all hover:shadow-xl hover:-translate-y-1 group ${
                                            b.isEpay ? 'border-red-200 hover:border-red-400' : 'border-gray-100 hover:border-gray-300'
                                        }`}>
                                            <div className="p-5 sm:p-6">
                                                <div className="flex justify-between items-start mb-4">
                                                    <div className={`w-14 h-14 sm:w-16 sm:h-16 rounded-2xl flex items-center justify-center font-bold text-xl ${
                                                        b.isEpay 
                                                            ? 'bg-gradient-to-br from-red-500 to-red-700 text-white shadow-lg shadow-red-500/30' 
                                                            : 'bg-gradient-to-br from-gray-100 to-gray-200 text-gray-700'
                                                    }`}>
                                                        {b.initial}
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        {b.isEpay && (
                                                            <span className="text-[9px] sm:text-[10px] bg-red-600 text-white px-2 py-1 rounded-full font-extrabold uppercase tracking-wider">
                                                                ePay
                                                            </span>
                                                        )}
                                                        <button 
                                                            onClick={() => handleDeleteClick(b.id)}
                                                            className="w-8 h-8 rounded-lg hover:bg-red-50 flex items-center justify-center text-gray-400 hover:text-red-600 transition"
                                                        >
                                                            <i className="fas fa-trash"></i>
                                                        </button>
                                                    </div>
                                                </div>
                                                
                                                <h4 className="font-bold text-gray-900 text-base sm:text-lg mb-1 truncate">{b.beneficiaryName}</h4>
                                                <p className="text-xs sm:text-sm text-gray-500 mb-4 truncate">{b.detail}</p>
                                                
                                                <div className="bg-gray-50 rounded-xl p-3 mb-4">
                                                    <div className="flex justify-between items-center text-xs">
                                                        <span className="text-gray-500">Added</span>
                                                        <span className="font-bold text-gray-700">{b.lastTransaction}</span>
                                                    </div>
                                                    <div className="flex justify-between items-center text-xs mt-1">
                                                        <span className="text-gray-500">Currency</span>
                                                        <span className="font-bold text-red-600">{b.currency}</span>
                                                    </div>
                                                </div>

                                                <button 
                                                    onClick={() => openHistoryModal(b)}
                                                    className="w-full py-3 text-xs sm:text-sm font-bold bg-red-600 text-white rounded-xl hover:bg-red-700 transition flex items-center justify-center gap-2 shadow-lg shadow-red-600/30"
                                                >
                                                    <i className="fas fa-eye text-xs"></i>
                                                    View Details
                                                </button>
                                            </div>
                                        </div>
                                    ))}
                                    
                                    {filteredBeneficiaries.length === 0 && (
                                        <div className="col-span-full bg-white rounded-3xl border-2 border-dashed border-gray-200 p-12 text-center">
                                            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                                <i className="fas fa-search text-3xl text-gray-400"></i>
                                            </div>
                                            <h3 className="text-lg font-bold text-gray-700 mb-2">No beneficiaries found</h3>
                                            <p className="text-sm text-gray-500">Try adjusting your search or filter criteria</p>
                                        </div>
                                    )}
                                </div>
                            )}

                            {/* List View */}
                            {!isLoadingBeneficiaries && viewMode === 'list' && (
                                <div className="space-y-3 mb-8">
                                    {filteredBeneficiaries.map((b) => (
                                        <div key={b.id} className={`bg-white rounded-2xl border-2 transition-all hover:shadow-lg ${
                                            b.isEpay ? 'border-red-200 hover:border-red-400' : 'border-gray-100 hover:border-gray-300'
                                        }`}>
                                            <div className="p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center gap-4">
                                                <div className="flex items-center gap-4 flex-1 min-w-0">
                                                    <div className={`w-12 h-12 sm:w-14 sm:h-14 flex-shrink-0 rounded-xl flex items-center justify-center font-bold ${
                                                        b.isEpay 
                                                            ? 'bg-gradient-to-br from-red-500 to-red-700 text-white' 
                                                            : 'bg-gradient-to-br from-gray-100 to-gray-200 text-gray-700'
                                                    }`}>
                                                        {b.initial}
                                                    </div>
                                                    
                                                    <div className="flex-1 min-w-0">
                                                        <div className="flex items-center gap-2 mb-1">
                                                            <h4 className="font-bold text-gray-900 text-sm sm:text-base truncate">{b.beneficiaryName}</h4>
                                                            {b.isEpay && (
                                                                <span className="text-[9px] bg-red-600 text-white px-2 py-0.5 rounded-full font-extrabold uppercase tracking-wider flex-shrink-0">
                                                                    ePay
                                                                </span>
                                                            )}
                                                        </div>
                                                        <p className="text-xs sm:text-sm text-gray-500 truncate">{b.detail}</p>
                                                    </div>
                                                </div>

                                                <div className="flex items-center gap-3 sm:gap-4 justify-between sm:justify-end">
                                                    <div className="text-right">
                                                        <div className="text-xs text-gray-500">{b.lastTransaction}</div>
                                                        <div className="text-sm font-bold text-red-600">{b.currency}</div>
                                                    </div>

                                                    <div className="flex gap-2">
                                                        <button 
                                                            onClick={() => openHistoryModal(b)}
                                                            className="px-4 py-2 bg-red-600 text-white rounded-xl hover:bg-red-700 transition flex items-center justify-center gap-2 text-sm font-bold"
                                                        >
                                                            <i className="fas fa-eye"></i>
                                                            <span className="hidden sm:inline">Details</span>
                                                        </button>
                                                        <button 
                                                            onClick={() => handleDeleteClick(b.id)}
                                                            className="w-10 h-10 bg-red-50 text-red-600 rounded-xl hover:bg-red-100 transition flex items-center justify-center"
                                                        >
                                                            <i className="fas fa-trash"></i>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                    
                                    {filteredBeneficiaries.length === 0 && (
                                        <div className="bg-white rounded-3xl border-2 border-dashed border-gray-200 p-12 text-center">
                                            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                                                <i className="fas fa-search text-3xl text-gray-400"></i>
                                            </div>
                                            <h3 className="text-lg font-bold text-gray-700 mb-2">No beneficiaries found</h3>
                                            <p className="text-sm text-gray-500">Try adjusting your search or filter criteria</p>
                                        </div>
                                    )}
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

            {/* Delete Confirmation Modal */}
            {showDeleteConfirm && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-3xl max-w-md w-full p-6 sm:p-8 shadow-2xl animate-in fade-in zoom-in duration-200">
                        <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i className="fas fa-trash text-red-600 text-2xl"></i>
                        </div>
                        <h3 className="text-xl sm:text-2xl font-bold text-gray-900 text-center mb-2">Delete Beneficiary?</h3>
                        <p className="text-gray-600 text-center mb-6">This action cannot be undone. Are you sure you want to remove this beneficiary from your list?</p>
                        <div className="flex gap-3">
                            <button 
                                onClick={() => setShowDeleteConfirm(false)}
                                className="flex-1 py-3 bg-gray-100 text-gray-700 rounded-xl font-bold hover:bg-gray-200 transition"
                            >
                                Cancel
                            </button>
                            <button 
                                onClick={confirmDelete}
                                className="flex-1 py-3 bg-red-600 text-white rounded-xl font-bold hover:bg-red-700 transition"
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* History Modal */}
            {showHistoryModal && selectedBeneficiary && (
                <>
                    <div 
                        className="history-backdrop"
                        onClick={() => setShowHistoryModal(false)}
                    />
                    
                    <div className="history-panel">
                        <div className="history-panel-header">
                            <div className="history-panel-header-content">
                                <div className="history-panel-back-button">
                                    <button 
                                        onClick={() => setShowHistoryModal(false)}
                                        className="history-back-button"
                                    >
                                        <i className="fas fa-arrow-left"></i>
                                    </button>
                                </div>
                                <div className="history-panel-title-section">
                                    <h2 className="history-panel-title">
                                        <i className="fas fa-user-circle history-panel-title-icon"></i>
                                        Beneficiary Details
                                    </h2>
                                    <p className="history-panel-subtitle">{selectedBeneficiary.beneficiaryName}</p>
                                </div>
                                <div className="history-panel-header-actions">
                                    <button 
                                        onClick={() => setShowHistoryModal(false)}
                                        className="history-panel-send-button"
                                    >
                                        <i className="fas fa-times"></i>
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="p-6 space-y-4">
                            <div className="bg-gray-50 rounded-2xl p-6">
                                <h3 className="font-bold text-gray-900 mb-4 flex items-center gap-2">
                                    <i className="fas fa-info-circle text-red-600"></i>
                                    Beneficiary Information
                                </h3>
                                <div className="space-y-3">
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Name</span>
                                        <span className="font-semibold text-gray-900">{selectedBeneficiary.beneficiaryName}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Type</span>
                                        <span className="font-semibold text-gray-900">
                                            {selectedBeneficiary.isEpay ? 'ePay User' : 'Bank Account'}
                                        </span>
                                    </div>
                                    {selectedBeneficiary.isEpay ? (
                                        <div className="flex justify-between">
                                            <span className="text-gray-600">Username</span>
                                            <span className="font-semibold text-gray-900">@{selectedBeneficiary.recipientUsername}</span>
                                        </div>
                                    ) : (
                                        <>
                                            <div className="flex justify-between">
                                                <span className="text-gray-600">Bank</span>
                                                <span className="font-semibold text-gray-900">{selectedBeneficiary.bankName}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-gray-600">Account Number</span>
                                                <span className="font-semibold text-gray-900">{selectedBeneficiary.accountNumber}</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-gray-600">Account Name</span>
                                                <span className="font-semibold text-gray-900">{selectedBeneficiary.accountName}</span>
                                            </div>
                                        </>
                                    )}
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Currency</span>
                                        <span className="font-semibold text-gray-900">{selectedBeneficiary.currency}</span>
                                    </div>
                                    <div className="flex justify-between">
                                        <span className="text-gray-600">Added</span>
                                        <span className="font-semibold text-gray-900">
                                            {new Date(selectedBeneficiary.createdOn).toLocaleDateString('en-GB', {
                                                day: 'numeric',
                                                month: 'short',
                                                year: 'numeric'
                                            })}
                                        </span>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-yellow-50 border border-yellow-200 rounded-2xl p-4 flex items-start gap-3">
                                <i className="fas fa-info-circle text-yellow-600 mt-0.5"></i>
                                <div className="flex-1">
                                    <h4 className="font-bold text-yellow-900 text-sm mb-1">Transaction History Coming Soon</h4>
                                    <p className="text-xs text-yellow-800">Transaction history for beneficiaries will be available in the next update.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

export default BeneficiaryManager;