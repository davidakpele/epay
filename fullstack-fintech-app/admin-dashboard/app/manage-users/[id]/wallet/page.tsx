"use client";

import { useState } from "react";
import Link from "next/link";
import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles from "./page.module.css";
import { ChevronRight, CheckCircle } from "lucide-react";
import WalletTab from "@/components/wallet/WalletTab";
import TransactionsTab from "@/components/wallet/TransactionsTab";
import VirtualCardsTab from "@/components/wallet/VirtualCardsTab";
import AccountSettingsTab from "@/components/wallet/AccountSettingsTab";
import WalletActions from "@/components/wallet/WalletActions";

const TABS = ["Wallet", "Transactions", "Virtual Cards", "Account Settings"] as const;
type Tab = typeof TABS[number];

// Mock user — in real app you'd fetch by params.id
const USER = {
  id:     "USR001",
  name:   "David Alex",
  email:  "david.alex@email.com",
  avatar: "DA",
  kyc:    "Verified" as const,
  status: "Active"   as const,
};

export default function ManageWalletPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [activeTab,   setActiveTab]   = useState<Tab>("Wallet");

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />
        <main className={styles.main}>

          {/* Breadcrumb */}
          <nav className={styles.breadcrumb}>
            <Link href="/manage-users" className={styles.breadcrumbLink}>Manage Users</Link>
            <ChevronRight size={13} className={styles.breadcrumbSep} />
            <span className={styles.breadcrumbLink}>{USER.name}</span>
            <ChevronRight size={13} className={styles.breadcrumbSep} />
            <span className={styles.breadcrumbCurrent}>Manage Wallet</span>
          </nav>

          <div className={styles.body}>
            {/* ── Left column ── */}
            <div className={styles.left}>

              {/* User hero card */}
              <div className={styles.heroCard}>
                <div className={styles.heroLeft}>
                  <div className={styles.heroAvatar}>{USER.avatar}</div>
                  <div className={styles.heroInfo}>
                    <h2 className={styles.heroName}>{USER.name}</h2>
                    <div className={styles.heroMeta}>
                      <span className={styles.heroEmail}>{USER.email}</span>
                      <span className={styles.verifiedBadge}>
                        <CheckCircle size={13} />
                        Verified
                      </span>
                    </div>
                  </div>
                </div>
                <button className={styles.kycBtn}>
                  🛡 KYC Status ▾
                </button>
              </div>

              {/* Tab bar */}
              <div className={styles.tabBar}>
                {TABS.map(tab => (
                  <button
                    key={tab}
                    className={`${styles.tab} ${activeTab === tab ? styles.tabActive : ""}`}
                    onClick={() => setActiveTab(tab)}
                  >
                    {tab}
                  </button>
                ))}
              </div>

              {/* Tab content */}
              <div className={styles.tabContent}>
                {activeTab === "Wallet"           && <WalletTab />}
                {activeTab === "Transactions"     && <TransactionsTab />}
                {activeTab === "Virtual Cards"    && <VirtualCardsTab />}
                {activeTab === "Account Settings" && <AccountSettingsTab user={USER} />}
              </div>

            </div>

            {/* ── Right sidebar ── */}
            <aside className={styles.right}>
              <WalletActions />
            </aside>
          </div>

        </main>
      </div>
    </div>
  );
}