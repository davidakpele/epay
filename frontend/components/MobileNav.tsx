'use client';

import { useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { Home, Wallet, TrendingUp, Repeat, User, PiggyBank, X } from 'lucide-react';
import "./MobileNav.css";

interface MobileNavProps {
  activeTab?: string;
  onPlusClick?: () => void;
}

const MobileNav = ({ activeTab, onPlusClick }: MobileNavProps) => {
  const router   = useRouter();
  const pathname = usePathname();
  const [showSavingsMenu, setShowSavingsMenu] = useState(false);

  // Derive active tab from pathname if not explicitly passed
  const currentTab = activeTab ?? (
    pathname.startsWith('/dashboard')        ? 'home'    :
    pathname.startsWith('/wallet')           ? 'wallet'  :
    pathname.startsWith('/savings')          ? 'invest'  :
    pathname.startsWith('/exchange')         ? 'exchange':
    pathname.startsWith('/settings/profile') ? 'profile' : 'none'
  );

  const isActive = (tab: string) => currentTab === tab;

  const savingsOptions = [
    {
      icon: <TrendingUp size={20} />,
      label: 'Investments',
      sub: 'Grow your money',
      href: '/savings/invest',
      color: '#7c3aed',
      bg: '#f5f0ff',
    },
    {
      icon: <PiggyBank size={20} />,
      label: 'Target Savings',
      sub: 'Save toward a goal',
      href: '/savings/target',
      color: '#059669',
      bg: '#f0fdf4',
    },
  ];

  return (
    <>
      {/* ── Savings sub-menu drawer ── */}
      {showSavingsMenu && (
        <>
          <div
            className="mobile-savings-backdrop"
            onClick={() => setShowSavingsMenu(false)}
          />
          <div className="mobile-savings-drawer">
            <div className="mobile-savings-drawer-header">
              <span className="mobile-savings-drawer-title">
                <TrendingUp size={16} style={{ marginRight: 6, verticalAlign: 'middle' }} />
                Invest &amp; Save
              </span>
              <button
                className="mobile-savings-close"
                onClick={() => setShowSavingsMenu(false)}
                aria-label="Close"
              >
                <X size={18} />
              </button>
            </div>
            <div className="mobile-savings-options">
              {savingsOptions.map((opt) => (
                <button
                  key={opt.href}
                  className={`mobile-savings-option ${pathname.startsWith(opt.href) ? 'mobile-savings-option--active' : ''}`}
                  onClick={() => {
                    setShowSavingsMenu(false);
                    router.push(opt.href);
                  }}
                >
                  <div className="mobile-savings-opt-icon" style={{ background: opt.bg, color: opt.color }}>
                    {opt.icon}
                  </div>
                  <div className="mobile-savings-opt-text">
                    <span className="mobile-savings-opt-label">{opt.label}</span>
                    <span className="mobile-savings-opt-sub">{opt.sub}</span>
                  </div>
                  {pathname.startsWith(opt.href) && (
                    <div className="mobile-savings-opt-active-dot" />
                  )}
                </button>
              ))}
            </div>
          </div>
        </>
      )}

      {/* ── Bottom nav bar ── */}
      <footer className="mobile-footer">
        <div
          className={`footer-tab ${isActive('home') ? 'active' : ''}`}
          onClick={() => router.push('/dashboard')}
        >
          <Home size={22} />
          <span>Home</span>
        </div>

        <div
          className={`footer-tab ${isActive('wallet') ? 'active' : ''}`}
          onClick={() => router.push('/wallet/accounts')}
        >
          <Wallet size={22} />
          <span>Wallet</span>
        </div>

        {/* Invest & Save — opens sub-menu drawer */}
        <div
          className={`footer-tab ${isActive('invest') ? 'active' : ''}`}
          onClick={() => setShowSavingsMenu(prev => !prev)}
        >
          <TrendingUp size={22} />
          <span>Invest</span>
          <span className="footer-tab-caret">
            {showSavingsMenu ? '▴' : '▾'}
          </span>
        </div>

        <div
          className={`footer-tab ${isActive('exchange') ? 'active' : ''}`}
          onClick={() => router.push('/exchange')}
        >
          <Repeat size={20} />
          <span>Swap</span>
        </div>

        <div
          className={`footer-tab ${isActive('profile') ? 'active' : ''}`}
          onClick={() => router.push('/settings/profile')}
        >
          <User size={22} />
          <span>Profile</span>
        </div>
      </footer>
    </>
  );
};

export default MobileNav;
