'use client';

import React, { useState, useEffect, useRef } from 'react';
import ReactSwitch from 'react-switch';
import { 
  Bell, X, Menu, ChevronDown, User, Settings, LogOut, 
  History, Headphones, CreditCard, Wallet, Receipt, 
  FileText, UserPlus, LayoutDashboard, 
  Smartphone, Repeat, Send, Landmark, UserStarIcon,
  ChevronRight
} from 'lucide-react';
import './Header.css';
import Link from 'next/link';
import Image from 'next/image';
import { getNotificationContainer } from '@/app/api/utils';

interface HeaderProps {
  theme: 'light' | 'dark';
  toggleTheme: () => void;
}

interface Notification {
  id?: string;
  type: string;
  description: string;
  date: string;
  read?: boolean;
}

const Header = ({ theme, toggleTheme }: HeaderProps) => {
  const [showNotif, setShowNotif] = useState(false);
  const [isNotifLoading, setIsNotifLoading] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [showAccountDropdown, setShowAccountDropdown] = useState(false);
  const [dropdownHover, setDropdownHover] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [filteredNotifications, setFilteredNotifications] = useState<Notification[]>([]);
  const [filter, setFilter] = useState('all');
  const [profileImage, setProfileImage] = useState('/assets/images/user-profile.jpg');
  const [expandedMobileMenus, setExpandedMobileMenus] = useState<string[]>([]);
  
  const notifRef = useRef<HTMLDivElement>(null);
  const sidebarRef = useRef<HTMLDivElement>(null);
  const accountDropdownRef = useRef<HTMLDivElement>(null);
  const accountTriggerRef = useRef<HTMLButtonElement>(null);
  const dropdownTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    const storedNotifications = getNotificationContainer() || [];
    setNotifications(storedNotifications);
    setFilteredNotifications(storedNotifications);
  }, []);

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth <= 1000);
    };
    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (notifRef.current && !notifRef.current.contains(event.target as Node)) {
        setShowNotif(false);
      }
      if (sidebarRef.current && !sidebarRef.current.contains(event.target as Node) && isSidebarOpen) {
        setIsSidebarOpen(false);
      }
      if (
        accountDropdownRef.current && 
        !accountDropdownRef.current.contains(event.target as Node) &&
        accountTriggerRef.current && 
        !accountTriggerRef.current.contains(event.target as Node)
      ) {
        setShowAccountDropdown(false);
        setDropdownHover(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isSidebarOpen]);

  useEffect(() => {
    if (filter === 'all') {
      setFilteredNotifications(notifications);
    } else {
      const typeMap: Record<string, string[]> = {
        'messages': ['message', 'chat', 'communication', 'profile_update'],
        'payments': ['payment', 'transaction', 'billing'],
        'system': ['system', 'system_alert', 'update', 'maintenance']
      };
      
      const typesToInclude = typeMap[filter] || [filter];
      const filtered = notifications.filter(notification => 
        typesToInclude.includes(notification.type)
      );
      setFilteredNotifications(filtered);
    }
  }, [filter, notifications]);

  const toggleMobileMenu = (label: string) => {
    setExpandedMobileMenus(prev => 
      prev.includes(label) 
        ? prev.filter(item => item !== label)
        : [...prev, label]
    );
  };

  const handleToggleNotif = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!showNotif) {
      setIsNotifLoading(true);
      setShowNotif(true);
      setTimeout(() => setIsNotifLoading(false), 2000);
    } else {
      setShowNotif(false);
    }
  };

  const handleAccountMouseEnter = () => {
    if (dropdownTimeoutRef.current) clearTimeout(dropdownTimeoutRef.current);
    setShowAccountDropdown(true);
  };

  const handleAccountMouseLeave = () => {
    dropdownTimeoutRef.current = setTimeout(() => {
      if (!dropdownHover) setShowAccountDropdown(false);
    }, 300);
  };

  const formatNotificationDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));
    const diffInDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24));

    if (diffInMinutes < 1) return 'Just now';
    if (diffInMinutes < 60) return `${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''} ago`;
    if (diffInHours < 24) return `${diffInHours} hour${diffInHours > 1 ? 's' : ''} ago`;
    if (diffInDays < 7) return `${diffInDays} day${diffInDays > 1 ? 's' : ''} ago`;
    
    return date.toLocaleDateString();
  };

  const getNotificationIcon = (type: string) => {
    const iconMap: Record<string, string> = {
      profile_update: '🔄',
      welcome: '👋',
      system_alert: '⚠️',
      payment: '💰',
      transaction: '💳',
      billing: '🧾',
      message: '✉️',
      chat: '💬',
      communication: '📞',
      system: '🔧',
      update: '🔄',
      maintenance: '🔧',
    };
    return iconMap[type] || '🔔';
  };

  const handleFilterChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setFilter(event.target.value);
  };

  const handleClearAllNotifications = () => {
    setNotifications([]);
    setFilteredNotifications([]);
  };

  const getFilterCount = (filterType: string) => {
    if (filterType === 'all') return notifications.length;
    
    const typeMap: Record<string, string[]> = {
      'messages': ['message', 'chat', 'communication', 'profile_update'],
      'payments': ['payment', 'transaction', 'billing'],
      'system': ['system', 'system_alert', 'update', 'maintenance']
    };
    
    const typesToInclude = typeMap[filterType] || [filterType];
    return notifications.filter(n => typesToInclude.includes(n.type)).length;
  };

  const desktopNavItems = [
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'bills', label: 'Paybills' },
    { id: 'cards', label: 'Virtual Cards' },
  ];

  const accountDropdownItems = [
    { id: 'settings/profile', label: 'Profile', icon: <User size={16} /> },
    { id: 'settings/general', label: 'Settings', icon: <Settings size={16} /> },
    { id: 'auth/logout', label: 'Logout', icon: <LogOut size={16} /> },
  ];

  const mobileMenuGroups = [
    {
      title: "DASHBOARD",
      items: [
        { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard size={20} />, href: '/dashboard' }
      ]
    },
    {
      title: "ACCOUNT",
      items: [
        { 
          id: 'wallet', 
          label: 'Wallets', 
          icon: <Wallet size={20} />, 
          href: '/wallet',
          submenu: [
            { label: 'Accounts', href: '/wallet/accounts' },
            { label: 'Account Statements', href: '/wallet/statements' },
            { label: 'Beneficiary', href: '/wallet/beneficiary' },
            { label: 'Deposit Banks', href: '/wallet/deposit-banks' }
          ]
        },
        { id: 'exchange', label: 'Exchange', icon: <Repeat size={20} />, href: '/exchange' },
      ]
    },
    {
      title: "UTILITIES",
      items: [
        { id: 'cards', label: 'Cards', icon: <CreditCard size={20} />, href: '/cards' },
        { id: 'bills', label: 'Bills', icon: <Receipt size={20} />, href: '/bills' },
        { id: 'refer', label: 'Refer and Earn', icon: <UserPlus size={20} />, href: '/refer' },
      ]
    },
    {
      title: "USER",
      items: [
        { id: 'support', label: 'Support', icon: <Headphones size={20} />, href: '/support' },
        { 
          id: 'settings', 
          label: 'Settings', 
          icon: <Settings size={20} />, 
          href: '/settings',
          submenu: [
            { label: 'General', href: '/settings/general' },
            { label: 'Profile Settings', href: '/settings/profile' },
            { label: 'Security', href: '/settings/security' }
          ]
        },
      ]
    }
  ];

  return (
    <>
      <header className={`header sticky-header ${isMobile ? 'mobile-header' : ''}`}>
        <div className="header-left">
          {isMobile && (
            <button className="mobile-menu-button" onClick={() => setIsSidebarOpen(true)}>
              <Menu size={24} />
            </button>
          )}
          {!isMobile && (
            <nav className="desktop-nav">
              <ul className="nav-list">
                {desktopNavItems.map((item) => (
                  <li key={item.id}>
                    <Link href={`/${item.id}`} className="nav-link">{item.label}</Link>
                  </li>
                ))}
                <li className="nav-item-dropdown">
                  <button 
                    ref={accountTriggerRef}
                    className="nav-link account-dropdown-trigger"
                    onClick={() => setShowAccountDropdown(!showAccountDropdown)}
                    onMouseEnter={handleAccountMouseEnter}
                    onMouseLeave={handleAccountMouseLeave}
                  >
                    Account
                    <ChevronDown size={16} className={`dropdown-chevron ${showAccountDropdown ? 'rotate' : ''}`} />
                  </button>
                  {showAccountDropdown && (
                    <div 
                      className="account-dropdown"
                      onMouseEnter={() => { if (dropdownTimeoutRef.current) clearTimeout(dropdownTimeoutRef.current); setDropdownHover(true); }}
                      onMouseLeave={() => { setDropdownHover(false); dropdownTimeoutRef.current = setTimeout(() => setShowAccountDropdown(false), 200); }}
                    >
                      {accountDropdownItems.map((item) => (
                        <Link key={item.id} href={`/${item.id}`} className="dropdown-item">
                          <span className="dropdown-icon">{item.icon}</span>
                          {item.label}
                        </Link>
                      ))}
                    </div>
                  )}
                </li>
              </ul>
            </nav>
          )}
        </div>

        <div className="header-right">
          <button className="notification-icon" onClick={handleToggleNotif}>
            <Bell size={24} />
            <span className="notification-badge">{notifications.length > 0 ? notifications.length : '0'}</span>
          </button>
          <div className="user-avatar">
            <Image src="/assets/images/user-profile.jpg" alt={'User profile'} width={21} height={21} className="settings-avatar" />
          </div>
        </div>

        {showNotif && (
          <div className="notification-dropdown" ref={notifRef}>
            {isNotifLoading ? (
              <div className="notif-loader-container">
                <div className="notif-spinner"></div>
              </div>
            ) : (
              <>
                <div className="notif-header">
                  <div className="notif-title">
                    <Bell size={18} style={{ color: 'var(--bg-main)' }} />
                    <span style={{ fontWeight: '600', color: '#111827' }}>
                      Notifications {filteredNotifications.length > 0 && `(${filteredNotifications.length})`}
                    </span>
                  </div>
                  <X 
                    size={18} 
                    style={{ cursor: 'pointer', color: 'var(--bg-main)' }} 
                    onClick={() => setShowNotif(false)} 
                  />
                </div>

                <div className="notif-body">
                  <div className="notif-filter">
                    <select 
                      className="notif-select" 
                      value={filter} 
                      onChange={handleFilterChange}
                    >
                      <option value="all">All ({getFilterCount('all')})</option>
                      <option value="messages">Messages ({getFilterCount('messages')})</option>
                      <option value="payments">Payments ({getFilterCount('payments')})</option>
                      <option value="system">System ({getFilterCount('system')})</option>
                    </select>
                  </div>

                  <div className="notif-list">
                    {filteredNotifications.length > 0 ? (
                      filteredNotifications.map((notification, index) => (
                        <div key={notification.id || index} className="notif-item">
                          <div className="notif-icon">
                            {getNotificationIcon(notification.type)}
                          </div>
                          <div className="notif-text">
                            <small>{formatNotificationDate(notification.date)}</small>
                            <span className="notif-description">
                              {notification.description.length > 40 
                                ? `${notification.description.substring(0, 40)}...`
                                : notification.description
                              }
                            </span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="notif-empty">
                        <Bell size={40} style={{ color: 'var(--bg-main)' }} />
                        <p>No {filter !== 'all' ? filter : ''} notifications</p>
                        <small>
                          {filter === 'all' 
                            ? "We'll notify you when something arrives" 
                            : `No ${filter} notifications found`
                          }
                        </small>
                      </div>
                    )}
                  </div>
                </div>

                {notifications.length > 0 && <div className="notif-divider"></div>}

                {notifications.length > 0 && (
                  <div className="notif-footer">
                    <button 
                      className="clear-all-btn"
                      onClick={handleClearAllNotifications}
                    >
                      Clear All Notifications
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        )}
      </header>

      {isMobile && isSidebarOpen && (
        <>
          <div className="sidebar-overlay" onClick={() => setIsSidebarOpen(false)} />
          <div className={`mobile-sidebar ${theme === 'dark' ? 'dark-sidebar' : ''}`} ref={sidebarRef}>
            <div className="sidebar-header">
              <div className="sidebar-logo-container">
                <div className="mobile-logo-icon">
                  <Smartphone size={22} color="white" />
                </div>
                <span className="mobile-logo-text">ePay</span>
              </div>
              <button className={`sidebar-close ${theme === 'dark' ? 'color-light' : 'color-dark'}`} onClick={() => setIsSidebarOpen(false)}>
                <X size={24} />
              </button>
            </div>

            <nav className="mobile-nav-container">
              {mobileMenuGroups.map((group, gIdx) => (
                <div key={gIdx} className="mobile-nav-group">
                  <h3 className="mobile-group-title">{group.title}</h3>
                  {group.items.map((item, idx) => {
                    const isExpanded = expandedMobileMenus.includes(item.label);
                    const hasSubmenu = item.submenu && item.submenu.length > 0;

                    return (
                      <div key={idx}>
                        {hasSubmenu ? (
                          <div
                            className="mobile-nav-link mobile-nav-expandable"
                            onClick={() => toggleMobileMenu(item.label)}
                          >
                            <span className="nav-icon">{item.icon}</span>
                            <span>{item.label}</span>
                            {isExpanded ? (
                              <ChevronDown size={16} className="mobile-chevron-icon" />
                            ) : (
                              <ChevronRight size={16} className="mobile-chevron-icon" />
                            )}
                          </div>
                        ) : (
                          <Link
                            href={item.href || `/${item.id}`}
                            className="mobile-nav-link"
                            onClick={() => setIsSidebarOpen(false)}
                          >
                            <span className="nav-icon">{item.icon}</span>
                            <span>{item.label}</span>
                          </Link>
                        )}
                        
                        {hasSubmenu && isExpanded && (
                          <div className="mobile-submenu">
                            {item.submenu.map((subItem, subIdx) => (
                              <Link
                                key={subIdx}
                                href={subItem.href}
                                className="mobile-submenu-item"
                                onClick={() => setIsSidebarOpen(false)}
                              >
                                <span>{subItem.label}</span>
                              </Link>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              ))}
              <div className="mobile-nav-group">
                <Link
                  href="/auth/logout"
                  className="mobile-nav-link logout-link-mobile"
                  onClick={() => setIsSidebarOpen(false)}
                >
                  <span className="nav-icon"><LogOut size={20} /></span>
                  <span>Logout</span>
                </Link>
              </div>
            </nav>
          </div>
        </>
      )}
    </>
  );
};

export default Header;