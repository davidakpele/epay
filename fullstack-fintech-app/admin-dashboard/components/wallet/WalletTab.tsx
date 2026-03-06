"use client";

import { useState } from "react";
import { ArrowDownToLine, X, ArrowRightLeft } from "lucide-react";
import styles from "./WalletTab.module.css";

const RECENT_TXN = [
  { id: "TXN123457", label: "Deposit",         amount: "+₦150,000",  type: "Deposit",  date: "Apr 21, 2024", color: "#166701", icon: "N"  },
  { id: "TXN123456", label: "Airtime Purchase", amount: "-₦1,500",   type: "Transfer", date: "Apr 19, 2024", color: "#2d7a14", icon: "👤" },
  { id: "TXN123455", label: "Withdrawal",       amount: "-₦50,000",  type: "Deposit",  date: "Apr 17, 2024", color: "#166701", icon: "N"  },
  { id: "TXN123454", label: "Crypto Exchange",  amount: "-₦100,000", type: "Transfer", date: "Apr 15, 2024", color: "#1a8a50", icon: "↔" },
];

const WALLETS = {
  NGN: { symbol: "₦", balance: "845,600",  flag: "🇳🇬", label: "Nigerian Naira"   },
  USD: { symbol: "$", balance: "531.25",   flag: "🇺🇸", label: "US Dollar"        },
  EUR: { symbol: "€", balance: "102.21",   flag: "eu", label: "Euro"        },
};

type Currency = keyof typeof WALLETS;

export default function WalletTab() {
  const [currency,    setCurrency]    = useState<Currency>("NGN");
  const [monthFilter, setMonthFilter] = useState("All Months");
  const [typeFilter,  setTypeFilter]  = useState("All");

  const wallet = WALLETS[currency];

  return (
    <div className={styles.wrap}>

      {/* ══ MANAGE WALLET BLOCK ══ */}
      <div className={styles.block}>
        <h3 className={styles.blockTitle}>Manage Wallet</h3>

        {/* Currency toggle */}
        <div className={styles.currencyToggle}>
          {(Object.keys(WALLETS) as Currency[]).map(cur => (
            <button
              key={cur}
              className={`${styles.currencyBtn} ${currency === cur ? styles.currencyBtnActive : ""}`}
              onClick={() => setCurrency(cur)}
            >
              <span className={styles.currencyFlag}>{WALLETS[cur].flag}</span>
              {cur}
            </button>
          ))}
        </div>

        {/* Row: balance card LEFT + action buttons RIGHT */}
        <div className={styles.walletRow}>

          {/* Balance card */}
          <div className={styles.balanceCard}>
            <span className={styles.balanceLabel}>Available Balance</span>
            <span className={styles.balanceCur}>{wallet.label}</span>
            <span className={styles.balanceAmount}>
              {wallet.symbol}{wallet.balance}
            </span>
          </div>

          {/* Action buttons pinned to far right */}
          <div className={styles.btnBox}>
            <button className={`${styles.iconBtn} ${styles.iconBtnGreen}`} title="Withdraw">
              <ArrowDownToLine size={18} />
            </button>
            <button className={`${styles.iconBtn} ${styles.iconBtnRed}`} title="Freeze">
              <X size={18} />
            </button>
            <button className={`${styles.iconBtn} ${styles.iconBtnGreen}`} title="Transfer">
              <ArrowRightLeft size={18} />
            </button>
          </div>
        </div>

        {/* User Information */}
        <p className={styles.userInfoText}>User Information</p>
      </div>

      {/* ══ RECENT TRANSACTIONS BLOCK ══ */}
      <div className={styles.block}>
        <div className={styles.txnTopRow}>
          <h3 className={styles.blockTitle}>Recent Transactions</h3>
          <div className={styles.pillFilters}>
            <div className={styles.pill}>All <span className={styles.chevron}>▾</span></div>
            <div className={styles.pill}>All Months <span className={styles.chevron}>▾</span></div>
          </div>
        </div>

        <div className={styles.subRow}>
          <button className={`${styles.subBtn} ${styles.subBtnActive}`}>All</button>
          <div className={styles.selWrap}>
            <select className={styles.sel} value={monthFilter} onChange={e => setMonthFilter(e.target.value)}>
              <option>All Months</option>
              <option>April 2024</option>
              <option>March 2024</option>
            </select>
            <span className={styles.selChev}>▾</span>
          </div>
          <div className={styles.selWrap}>
            <select className={styles.sel} value={typeFilter} onChange={e => setTypeFilter(e.target.value)}>
              <option>All</option>
              <option>Deposit</option>
              <option>Transfer</option>
              <option>Withdrawal</option>
            </select>
            <span className={styles.selChev}>▾</span>
          </div>
        </div>

        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>TXN ID</th>
                <th>TRANSACTION</th>
                <th>AMOUNT</th>
                <th>TYPE</th>
                <th>DATE</th>
              </tr>
            </thead>
            <tbody>
              {RECENT_TXN.map(txn => (
                <tr key={txn.id}>
                  <td className={styles.txnId}>{txn.id}</td>
                  <td>
                    <div className={styles.txnCell}>
                      <div className={styles.txnIcon} style={{ background: txn.color }}>{txn.icon}</div>
                      <span className={styles.txnLabel}>{txn.label}</span>
                    </div>
                  </td>
                  <td className={`${styles.amount} ${txn.amount.startsWith("+") ? styles.pos : styles.neg}`}>
                    {txn.amount}
                  </td>
                  <td><span className={styles.typeBadge}>{txn.type}</span></td>
                  <td className={styles.date}>{txn.date}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <button className={styles.viewAll}>View All &nbsp;›</button>
      </div>

    </div>
  );
}