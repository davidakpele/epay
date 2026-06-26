"use client";

import { useState } from "react";
import {
  ArrowRightLeft, Plus, ArrowUpFromLine,
  Lock, RefreshCw, ChevronRight, ShieldAlert,
} from "lucide-react";
import styles from "./WalletActions.module.css";

const STATS = [
  { label: "Total Deposits",     value: "₦450,000", color: styles.statGreen  },
  { label: "Total Withdrawals",  value: "₦150,000", color: styles.statOrange },
  { label: "Total Spent",        value: "₦143,400", color: styles.statNeutral },
  { label: "Crypto Holdings",    value: "₦200,600", color: styles.statNeutral },
];

const QUICK_ACTIONS = [
  { label: "Wallet Transfer",  Icon: ArrowRightLeft  },
  { label: "Add Funds",        Icon: Plus            },
  { label: "Withdraw Funds",   Icon: ArrowUpFromLine },
  { label: "Freeze Wallet",    Icon: Lock            },
  { label: "Reset KYC Level",  Icon: RefreshCw       },
];

export default function WalletActions() {
  const [suspended, setSuspended] = useState(false);

  return (
    <div className={styles.wrap}>

      {/* Wallet Actions */}
      <div className={styles.card}>
        <h3 className={styles.cardTitle}>Wallet Actions</h3>

        <div className={styles.statsList}>
          {STATS.map(s => (
            <div key={s.label} className={styles.statRow}>
              <span className={styles.statLabel}>{s.label}</span>
              <span className={`${styles.statValue} ${s.color}`}>{s.value}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Actions */}
      <div className={styles.card}>
        <h3 className={styles.cardTitle}>Quick Actions</h3>

        <div className={styles.quickList}>
          {QUICK_ACTIONS.map(({ label, Icon }) => (
            <button key={label} className={styles.quickItem}>
              <span className={styles.quickIconWrap}>
                <Icon size={16} />
              </span>
              <span className={styles.quickLabel}>{label}</span>
              <ChevronRight size={14} className={styles.quickArrow} />
            </button>
          ))}
        </div>

        <button
          className={`${styles.suspendBtn} ${suspended ? styles.suspendBtnActive : ""}`}
          onClick={() => setSuspended(p => !p)}
        >
          <ShieldAlert size={16} />
          {suspended ? "Unsuspend Wallet" : "Suspend Wallet"}
        </button>
      </div>

      {/* Security note */}
      <div className={styles.securityCard}>
        <div className={styles.securityHead}>
          <span className={styles.securityIcon}>🔒</span>
          <span className={styles.securityTitle}>Security</span>
        </div>
        <p className={styles.securityText}>
          For security purposes, transfers can only be made to verified wallets.
        </p>
      </div>

    </div>
  );
}