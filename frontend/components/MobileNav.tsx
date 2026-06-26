'use client';

import { usePathname, useRouter } from 'next/navigation';
import { Home, Wallet, TrendingUp, Repeat, User } from 'lucide-react';
import "./MobileNav.css"

interface MobileNavProps {
  activeTab?: string;
  onPlusClick?: () => void;
}

const MobileNav = ({ activeTab, onPlusClick }: MobileNavProps) => {
  const router   = useRouter();
  const pathname = usePathname();

  // Derive active tab from pathname if not explicitly passed
  const currentTab = activeTab ?? (
    pathname.startsWith('/dashboard')        ? 'home'    :
    pathname.startsWith('/wallet')           ? 'wallet'  :
    pathname.startsWith('/savings')          ? 'invest'  :
    pathname.startsWith('/exchange')         ? 'exchange':
    pathname.startsWith('/settings/profile') ? 'profile' : 'none'
  );

  const isActive = (tab: string) => currentTab === tab;

  return (
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

      <div
        className={`footer-tab ${isActive('invest') ? 'active' : ''}`}
        onClick={() => router.push('/savings/invest')}
      >
        <TrendingUp size={22} />
        <span>Invest</span>
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
  );
};

export default MobileNav;
