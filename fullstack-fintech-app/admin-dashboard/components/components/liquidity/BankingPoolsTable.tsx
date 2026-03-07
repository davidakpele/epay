"use client";

import { useState } from "react";
import styles from "./BankingPoolsTable.module.css";

interface Pool {
  id:       string;
  bank:     string;
  logo:     string;
  logoBg:   string;
  acct:     string;
  currency: "₦" | "$";
  symbol:   "N" | "S";
  symBg:    string;
  balance:  string;
}

const POOLS: Pool[] = [
  { id: "P001", bank: "Zenith Bank",          logo: "Z",  logoBg: "#d22b2b", acct: "·········875", currency: "₦", symbol: "N", symBg: "#166701", balance: "₦3,250,000"  },
  { id: "P002", bank: "Access Bank",          logo: "A",  logoBg: "#0050a0", acct: "·········732", currency: "₦", symbol: "N", symBg: "#166701", balance: "₦1,820,000"  },
  { id: "P003", bank: "GTBank",               logo: "GT", logoBg: "#f07000", acct: "·········175", currency: "$", symbol: "S", symBg: "#3a5a8a", balance: "$16,500.75"   },
  { id: "P004", bank: "United Bank for Africa",logo: "U", logoBg: "#cc1a1a", acct: "·········987", currency: "₦", symbol: "N", symBg: "#166701", balance: "₦520,000"     },
  { id: "P005", bank: "First Bank",           logo: "FB", logoBg: "#0044a8", acct: "·········654", currency: "$", symbol: "S", symBg: "#3a5a8a", balance: "$7,200.50"    },
];

type ModalState = { open: boolean; pool: Pool | null; action: "deposit" | "withdraw" | null };

export default function BankingPoolsTable() {
  const [modal, setModal] = useState<ModalState>({ open: false, pool: null, action: null });
  const [amount, setAmount] = useState("");

  const openModal = (pool: Pool, action: "deposit" | "withdraw") => {
    setModal({ open: true, pool, action });
    setAmount("");
  };

  const closeModal = () => setModal({ open: false, pool: null, action: null });

  return (
    <>
      <div className={styles.card}>
        <div className={styles.header}>
          <h3 className={styles.title}>Banking Pools</h3>
        </div>

        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>TXN ID</th>
                <th>BANK</th>
                <th>ACCOUNT NUMBER</th>
                <th>CURRENCY</th>
                <th>BALANCE</th>
                <th>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {POOLS.map(pool => (
                <tr key={pool.id}>
                  <td className={styles.idCell}>{pool.id}</td>

                  {/* Bank with logo */}
                  <td>
                    <div className={styles.bankCell}>
                      <div
                        className={styles.bankLogo}
                        style={{ background: pool.logoBg }}
                      >
                        {pool.logo}
                      </div>
                      <span className={styles.bankName}>{pool.bank}</span>
                    </div>
                  </td>

                  {/* Account number with currency symbol badge */}
                  <td>
                    <div className={styles.acctCell}>
                      <div className={styles.symBadge} style={{ background: pool.symBg }}>
                        {pool.symbol}
                      </div>
                      <span className={styles.acctNum}>{pool.acct}</span>
                    </div>
                  </td>

                  {/* Currency */}
                  <td>
                    <span className={styles.currency}>{pool.currency}</span>
                  </td>

                  {/* Balance */}
                  <td className={styles.balance}>{pool.balance}</td>

                  {/* Manage button */}
                  <td>
                    <button
                      className={styles.manageBtn}
                      onClick={() => openModal(pool, "deposit")}
                    >
                      Manage
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>

            {/* Total row */}
            <tfoot>
              <tr className={styles.totalRow}>
                <td colSpan={4} className={styles.totalLabel}>Total Balance</td>
                <td colSpan={2} className={styles.totalValues}>
                  <span className={styles.totalNgn}>₦5,590,000</span>
                  <span className={styles.totalUsd}>$23,700.25</span>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      {/* Manage Modal */}
      {modal.open && modal.pool && (
        <div className={styles.overlay} onClick={closeModal}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <div className={styles.modalHead}>
              <div className={styles.modalBank}>
                <div className={styles.bankLogo} style={{ background: modal.pool.logoBg }}>
                  {modal.pool.logo}
                </div>
                <div>
                  <div className={styles.modalBankName}>{modal.pool.bank}</div>
                  <div className={styles.modalAcct}>{modal.pool.acct}</div>
                </div>
              </div>
              <button className={styles.modalClose} onClick={closeModal}>✕</button>
            </div>

            <div className={styles.modalBalance}>
              <span className={styles.modalBalLabel}>Current Balance</span>
              <span className={styles.modalBalValue}>{modal.pool.balance}</span>
            </div>

            <div className={styles.modalActions}>
              <button
                className={`${styles.modalTab} ${modal.action === "deposit" ? styles.modalTabActive : ""}`}
                onClick={() => setModal(m => ({ ...m, action: "deposit" }))}
              >
                Deposit
              </button>
              <button
                className={`${styles.modalTab} ${modal.action === "withdraw" ? styles.modalTabActiveRed : ""}`}
                onClick={() => setModal(m => ({ ...m, action: "withdraw" }))}
              >
                Withdraw
              </button>
            </div>

            <div className={styles.modalField}>
              <label className={styles.modalLabel}>Amount ({modal.pool.currency})</label>
              <input
                className={styles.modalInput}
                type="number"
                placeholder="0.00"
                value={amount}
                onChange={e => setAmount(e.target.value)}
              />
            </div>

            <button
              className={`${styles.modalSubmit} ${modal.action === "withdraw" ? styles.modalSubmitRed : ""}`}
            >
              {modal.action === "deposit" ? "Confirm Deposit" : "Confirm Withdrawal"}
            </button>
          </div>
        </div>
      )}
    </>
  );
}