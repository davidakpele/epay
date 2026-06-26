"use client";

import { Search } from "lucide-react";
import styles from "./TransactionsTable.module.css";
import { TransactionStatus, TransactionType } from "@/app/types";
import { TRANSACTIONS } from "@/app/lib/data";

const typeClass: Record<TransactionType, string> = {
  "Deposit":      styles.typeDep,
  "Withdrawal":   styles.typeWit,
  "Bit Payment":  styles.typeBit,
  "Virtual Card": styles.typeVir,
};

const statusClass: Record<TransactionStatus, string> = {
  "Successful": styles.statusOk,
  "Failed":     styles.statusFail,
};

export default function TransactionsTable() {
  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">Recent Transactions</span>
        <div className={styles.controls}>
          <button className={styles.searchBtn}><Search size={14} /></button>
          <select className={styles.select}>
            <option>All</option>
            <option>Deposit</option>
            <option>Withdrawal</option>
          </select>
          <select className={styles.select}>
            <option>Month</option>
            <option>Week</option>
            <option>Day</option>
          </select>
        </div>
      </div>
      <div className={styles.tableWrap}>
        <table className={styles.table}>
          <thead>
            <tr>
              {["TXN ID", "User", "Amount", "Type", "Status"].map((h) => (
                <th key={h}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {TRANSACTIONS.map((t) => (
              <tr key={t.id}>
                <td className={styles.txnId}>{t.id}</td>
                <td>
                  <div className={styles.userCell}>
                    <div className={styles.userAvatar}>{t.initials}</div>
                    {t.user}
                  </div>
                </td>
                <td className={t.isPositive ? styles.amtPos : styles.amtNeg}>{t.amount}</td>
                <td><span className={`${styles.typeBadge} ${typeClass[t.type]}`}>{t.type}</span></td>
                <td>
                  <span className={`${styles.statusBadge} ${statusClass[t.status]}`}>
                    {t.status === "Successful" ? "+ Successful" : "Failed"}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}