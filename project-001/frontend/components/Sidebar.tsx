'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Wallet,
  CreditCard,
  Receipt,
  Repeat,
  Send,
  HandCoins,
  FileText,
  UserPlus,
  Landmark,
  User,
  Headphones,
  Settings,
  LogOut,
  Smartphone,
  ScanEyeIcon,
  UserStarIcon,
  CreditCardIcon,
  ChevronDown,
  ChevronRight
} from 'lucide-react';
import "./Sidebar.css";

const Sidebar = () => {
  const pathname = usePathname();
  const [expandedMenus, setExpandedMenus] = useState<string[]>([]);

  const toggleMenu = (label: string) => {
    setExpandedMenus(prev => 
      prev.includes(label) 
        ? prev.filter(item => item !== label)
        : [...prev, label]
    );
  };

  const menuGroups = [
    {
      title: "DASHBOARD",
      items: [
        { icon: <LayoutDashboard size={20} />, label: 'Dashboard', href: '/dashboard' },
      ]
    },
    {
      title: "ACCOUNT",
      items: [
        { 
          icon: <Wallet size={20} />, 
          label: 'Wallets', 
          href: '/wallet',
          submenu: [
            { label: 'Accounts', href: '/wallet/accounts' },
            { label: 'Account Statements', href: '/wallet/statements' },
            { label: 'Beneficiary', href: '/wallet/beneficiary' },
            { label: 'Deposit Banks', href: '/wallet/deposit-banks' }
          ]
        },
        { icon: <Repeat size={20} />, label: 'Exchange', href: '/exchange' },
      ]
    },
    {
      title: "UNTILITIES",
      items: [
        { icon: <CreditCard size={20} />, label: 'Cards', href: '/cards' },
        { icon: <Receipt size={20} />, label: 'Bills', href: '/bills' },
        { icon: <UserPlus size={20} />, label: 'Refer and Earn', href: '/refer' },
      ]
    },
    {
      title: "USER",
      items: [

        { icon: <Headphones size={20} />, label: 'Support', href: '/support' },
        { 
          icon: <Settings size={20} />, 
          label: 'Settings', 
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
    <aside className="sidebar">
      <div className="logo-section">
        <div className="logo-icon-wrapper">
          <Smartphone size={24} color="white" />
        </div>
        <span className="logo-text">ePay</span>
      </div>

      <nav className="sidebar-nav-scrollable">
        {menuGroups.map((group, gIdx) => (
          <div key={gIdx} className="nav-group">
            <h3 className="group-title">{group.title}</h3>
            {group.items.map((item, idx) => {
              const isActive = pathname === item.href;
              const isExpanded = expandedMenus.includes(item.label);
              const hasSubmenu = item.submenu && item.submenu.length > 0;

              return (
                <div key={idx}>
                  {hasSubmenu ? (
                    <div
                      className={`nav-item nav-item-expandable ${isActive ? 'active' : ''}`}
                      onClick={() => toggleMenu(item.label)}
                    >
                      {item.icon}
                      <span>{item.label}</span>
                      {isExpanded ? (
                        <ChevronDown size={16} className="chevron-icon" />
                      ) : (
                        <ChevronRight size={16} className="chevron-icon" />
                      )}
                    </div>
                  ) : (
                    <Link
                      href={item.href}
                      className={`nav-item ${isActive ? 'active' : ''}`}
                    >
                      {item.icon}
                      <span>{item.label}</span>
                    </Link>
                  )}
                  
                  {hasSubmenu && isExpanded && (
                    <div className="submenu">
                      {item.submenu.map((subItem, subIdx) => {
                        const isSubActive = pathname === subItem.href;
                        return (
                          <Link
                            key={subIdx}
                            href={subItem.href}
                            className={`submenu-item ${isSubActive ? 'active' : ''}`}
                          >
                            <span>{subItem.label}</span>
                          </Link>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        <Link href="/auth/logout" className="nav-item logout-link">
          <LogOut size={20} />
          <span>Logout</span>
        </Link>
      </div>
    </aside>
  );
};

export default Sidebar;