"use client";

import { useState } from "react";
import styles from "../txns/PoolsSidebar.module.css";

const RECENT_TXN = [
  { id: "TXN1234520", type: "Deposit",  amount: "₦300,000",  amtCol: "#166701" },
  { id: "TXN1234589", type: "Withdraw", amount: "₦200,000",  amtCol: "#e05555" },
  { id: "TXN1234588", type: "$5,000",   amount: "$5,000",    amtCol: "#3a5a8a" },
  { id: "TXN1234587", type: "Withdraw", amount: "₦50,000",   amtCol: "#e05555" },
  { id: "TXN1234586", type: "Deposit",  amount: "$5,200",    amtCol: "#3a5a8a" },
];

type TxnFilter = "Reneday" | "Weekly" | "Monthly";

export default function PoolsSidebar() {
  const [txnFilter, setTxnFilter] = useState<TxnFilter>("Reneday");

  return (
    <div className={styles.wrap}>

      {/* ── Banking Pools summary ── */}
      <div className={styles.card}>
        <h3 className={styles.cardTitle}>Banking Pools</h3>

        <div className={styles.poolRows}>
          <div className={styles.poolRow}>
            <span className={styles.poolLabel}>Total Balance</span>
            <span className={styles.poolValueGreen}>₦5,590,000</span>
          </div>
          <div className={styles.poolRow}>
            <span className={styles.poolLabel}>Net Liquidity</span>
            <span className={styles.poolValueBlue}>$23,700.25</span>
          </div>
        </div>
      </div>

      {/* ── Net Liquidity highlight ── */}
      <div className={styles.netCard}>
        <span className={styles.netLabel}>Net Liquidity</span>
        <span className={styles.netValue}>$253,700.25</span>
      </div>

      {/* ── Recent Transactions ── */}
      <div className={styles.card}>
        <div className={styles.txnHeader}>
          <h3 className={styles.cardTitle}>Recent Transactions</h3>
          <div className={styles.txnFilterWrap}>
            {(["Reneday","Weekly","Monthly"] as TxnFilter[]).map(f => (
              <button
                key={f}
                className={`${styles.txnFilterBtn} ${txnFilter === f ? styles.txnFilterActive : ""}`}
                onClick={() => setTxnFilter(f)}
              >
                {f}
              </button>
            ))}
          </div>
        </div>

        {/* Mini table */}
        <div className={styles.miniTableWrap}>
          <table className={styles.miniTable}>
            <thead>
              <tr>
                <th>TXN ID</th>
                <th>TYPE</th>
                <th>AMOUNT</th>
              </tr>
            </thead>
            <tbody>
              {RECENT_TXN.map(txn => (
                <tr key={txn.id}>
                  <td className={styles.miniId}>{txn.id}</td>
                  <td className={styles.miniType}>{txn.type}</td>
                  <td
                    className={styles.miniAmt}
                    style={{ color: txn.amtCol }}
                  >
                    {txn.amount}
                  </td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr className={styles.miniFooter}>
                <td className={styles.miniFootLabel}>Total Balance</td>
                <td>
                  <span className={styles.miniFootNgn}>₦5,500,000</span>
                </td>
                <td>
                  <span className={styles.miniFootUsd}>$23,700.25</span>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

    </div>
  );
}