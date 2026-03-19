"use client";

import { useState } from "react";
import styles from "./TransactionsTab.module.css";

const ALL_TXN = [
  { id: "TXN123457", label: "Deposit",          amount: "+₦150,000",  type: "Deposit",    date: "Apr 21, 2024", icon: "N",  color: "#166701" },
  { id: "TXN123456", label: "Airtime Purchase",  amount: "-₦1,500",   type: "Transfer",   date: "Apr 19, 2024", icon: "👤", color: "#2d7a14" },
  { id: "TXN123455", label: "Withdrawal",        amount: "-₦50,000",  type: "Withdrawal", date: "Apr 17, 2024", icon: "↑",  color: "#c47f00" },
  { id: "TXN123454", label: "Crypto Exchange",   amount: "-₦100,000", type: "Transfer",   date: "Apr 15, 2024", icon: "↔", color: "#1a8a50" },
  { id: "TXN123453", label: "Deposit",           amount: "+₦200,000", type: "Deposit",    date: "Apr 10, 2024", icon: "N",  color: "#166701" },
  { id: "TXN123452", label: "Bill Payment",      amount: "-₦8,000",   type: "Transfer",   date: "Apr 8, 2024",  icon: "📄", color: "#444"    },
];

export default function TransactionsTab() {
  const [typeFilter,  setTypeFilter]  = useState("All Types");
  const [monthFilter, setMonthFilter] = useState("All Months");

  const filtered = ALL_TXN.filter(t => {
    const matchType  = typeFilter  === "All Types"  || t.type === typeFilter;
    const matchMonth = monthFilter === "All Months" || t.date.startsWith(monthFilter.split(" ")[0]);
    return matchType && matchMonth;
  });

  return (
    <div className={styles.wrap}>

      {/* ── Header row ── */}
      <div className={styles.header}>
        <h3 className={styles.title}>All Transactions</h3>

        <div className={styles.filters}>
          <div className={styles.selWrap}>
            <select
              className={styles.sel}
              value={typeFilter}
              onChange={e => setTypeFilter(e.target.value)}
            >
              <option>All Types</option>
              <option>Deposit</option>
              <option>Withdrawal</option>
              <option>Transfer</option>
            </select>
            <svg className={styles.selIcon} viewBox="0 0 16 16" fill="none">
              <path d="M4 6l4 4 4-4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>

          <div className={styles.selWrap}>
            <select
              className={styles.sel}
              value={monthFilter}
              onChange={e => setMonthFilter(e.target.value)}
            >
              <option>All Months</option>
              <option>April 2024</option>
              <option>March 2024</option>
              <option>February 2024</option>
            </select>
            <svg className={styles.selIcon} viewBox="0 0 16 16" fill="none">
              <path d="M4 6l4 4 4-4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
        </div>
      </div>

      {/* ── Table ── */}
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
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={5} className={styles.empty}>No transactions found.</td>
              </tr>
            ) : filtered.map(txn => (
              <tr key={txn.id}>
                <td className={styles.txnId}>{txn.id}</td>
                <td>
                  <div className={styles.txnCell}>
                    <div className={styles.txnIcon} style={{ background: txn.color }}>
                      {txn.icon}
                    </div>
                    <span className={styles.txnLabel}>{txn.label}</span>
                  </div>
                </td>
                <td className={`${styles.amount} ${txn.amount.startsWith("+") ? styles.pos : styles.neg}`}>
                  {txn.amount}
                </td>
                <td>
                  <span className={`${styles.badge} ${styles[`badge${txn.type}`]}`}>
                    {txn.type}
                  </span>
                </td>
                <td className={styles.date}>{txn.date}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </div>
  );
}