"use client";

import React, { useState, useEffect } from "react";
import {
  ArrowUpRight,
  ArrowDownLeft,
  ShoppingBag,
  Landmark,
  X,
  Copy,
  CreditCard,
  RefreshCw,
} from "lucide-react";
import "./History.css";
import { formatAmount } from "@/app/api";

interface Transaction {
  id: string | number;
  type: "credit" | "debit";
  title: string;
  category: string;
  displayAmount: string;
  netAmount: number;
  date: string;
  icon: React.ReactNode;
  ref: string;
  status: string;
  currency: string;
  symbol: string;
  channel?: string;
  accountHolder?: string;
  fee?: number;
  gross?: number;
  previousBalance?: number;
  availableBalance?: number;
  runningBalance?: number;
}

interface HistoryProps {
  theme: "light" | "dark";
  historyData: any[];
}

const History = ({ theme, historyData }: HistoryProps) => {
  const [selectedTx, setSelectedTx] = useState<Transaction | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [displayLimit, setDisplayLimit] = useState(4);

  useEffect(() => {
    if (historyData && Array.isArray(historyData)) {
      setTransactions(transformTransactions(historyData));
    } else {
      setTransactions([]);
    }
  }, [historyData]);

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case "deposit":
        return <ArrowDownLeft size={18} />;
      case "withdrawal":
        return <ArrowUpRight size={18} />;
      case "credited":
        return <CreditCard size={18} />;
      case "swap":
        return <RefreshCw size={18} />;
      default:
        return <Landmark size={18} />;
    }
  };

  const mapTransactionType = (apiType: string, description = "") => {
    const type = String(apiType || "").toLowerCase();
    const desc = String(description || "").toLowerCase();

    if (
      type === "deposit" ||
      type.includes("deposit") ||
      desc.includes("deposit")
    )
      return "deposit";
    if (
      type === "credited" ||
      type === "credit" ||
      type.includes("credit") ||
      desc.includes("received")
    )
      return "credited";
    if (
      type === "withdrawal" ||
      type === "withdraw" ||
      type === "debit" ||
      type === "debited" ||
      type.includes("withdraw") ||
      type.includes("debit") ||
      desc.includes("withdraw") ||
      desc.includes("sent")
    )
      return "withdrawal";
    if (
      type === "swap" ||
      type === "exchange" ||
      type === "conversion" ||
      type.includes("swap") ||
      type.includes("exchange") ||
      desc.includes("swap") ||
      desc.includes("exchange")
    )
      return "swap";
    return "transfer";
  };

  const mapStatus = (status: string) => {
    const s = (status || "").toLowerCase();
    if (["success", "completed", "confirmed", "delivered"].includes(s))
      return "Successful";
    if (["pending", "processing", "initiated", "processed"].includes(s))
      return "Pending";
    if (["failed", "rejected", "cancelled"].includes(s)) return "Failed";
    return "Pending";
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "—";
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return "—";
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  const transformTransactions = (raw: any[]): Transaction[] => {
    return raw.map((tx) => {
      // New nested structure support
      const amountObj = tx.amount || {};
      const balanceObj = tx.balance || {};

      const type = mapTransactionType(
        tx.transactionType || tx.type,
        tx.description,
      );
      const isCredit = ["deposit", "credited", "swap"].includes(type);

      // Prefer new nested amount.net, fallback to legacy flat fields
      const net = Math.abs(
        Number(
          amountObj.net ?? tx.netAmount ?? tx.grossAmount ?? tx.amount ?? 0,
        ),
      );
      const currency = amountObj.currency || tx.currencyType || "NGN";
      const symbol = amountObj.symbol || tx.symbol || currency;

      const sign = isCredit ? "+" : "-";

      return {
        id: tx.transactionId || tx.id,
        type: isCredit ? "credit" : "debit",
        title: tx.description || "Transaction",
        category: type.charAt(0).toUpperCase() + type.slice(1),
        displayAmount: `${sign}${symbol}${formatAmount(net)}`,
        netAmount: net,
        date: formatDate(
          tx.completedAt || tx.createdAt || tx.timestamp || tx.createdOn,
        ),
        icon: getTransactionIcon(type),
        ref:
          tx.reference ||
          tx.transactionId ||
          tx.referenceId ||
          tx.referenceNo ||
          "N/A",
        status: mapStatus(tx.currentStatus || tx.status),
        currency,
        symbol,
        channel: tx.channel || undefined,
        accountHolder: tx.user?.accountHolder || tx.accountHolder || undefined,
        fee: Number(amountObj.fee) || 0,
        gross: Number(amountObj.gross) || net,
        previousBalance:
          Number(balanceObj.previous ?? tx.previousBalance) || undefined,
        availableBalance:
          Number(balanceObj.available ?? tx.availableBalance) || undefined,
        runningBalance:
          Number(balanceObj.running ?? tx.runningBalance) || undefined,
      };
    });
  };

  const handleViewAll = () => {
    setDisplayLimit(transactions.length);
  };

  const themeClass = theme === "dark" ? "color-dark" : "color-light";

  return (
    <div className="history-container">
      <div className="section-header">
        <h3
          className={`history-title ${theme === "dark" ? "color-light" : "color-dark"}`}
        >
          Recent Transactions
        </h3>
        <button className="view-all" onClick={handleViewAll}>
          View All
        </button>
      </div>

      <div className="transaction-list">
        {transactions.length > 0 ? (
          transactions.slice(0, displayLimit).map((tx) => (
            <div
              key={tx.id}
              className="transaction-item"
              onClick={() => setSelectedTx(tx)}
            >
              <div className={`tx-icon-box ${tx.type}`}>{tx.icon}</div>
              <div className={`tx-details ${themeClass}`}>
                <p className="tx-title">{tx.title}</p>
                <p className="tx-meta">
                  {tx.category} • {tx.date}
                </p>
              </div>
              <div className="tx-amount-box">
                <p className={`tx-amount ${tx.type}`}>{tx.displayAmount}</p>
                <span
                  className={`tx-status-dot tx-status-dot--${tx.status.toLowerCase()}`}
                />
              </div>
            </div>
          ))
        ) : (
          <div className="empty-state">
            <p>No transactions found</p>
          </div>
        )}
      </div>

      {/* Transaction detail modal */}
      {selectedTx && (
        <div className="tx-modal-overlay" onClick={() => setSelectedTx(null)}>
          <div
            className="tx-modal-content"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="tx-modal-close"
              onClick={() => setSelectedTx(null)}
            >
              <X size={20} />
            </button>

            <div className="tx-modal-header">
              <div className={`tx-modal-icon-wrapper ${selectedTx.type}`}>
                {selectedTx.icon}
              </div>
              <h4
                className={`${theme === "dark" ? "color-light" : "color-dark"}`}
              >
                Transaction Details
              </h4>
              <p
                className={`tx-status-badge ${selectedTx.status.toLowerCase()}`}
              >
                {selectedTx.status}
              </p>
            </div>

            <div className="tx-modal-body">
              <div className="tx-receipt-amount">
                <span className={selectedTx.type}>
                  {selectedTx.displayAmount}
                </span>
              </div>

              <div className="tx-info-grid">
                <div className="tx-info-row">
                  <span className="label">Type</span>
                  <span
                    className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                  >
                    {selectedTx.type === "credit" ? "Inbound" : "Outbound"}
                  </span>
                </div>
                <div className="tx-info-row">
                  <span className="label">Category</span>
                  <span
                    className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                  >
                    {selectedTx.category}
                  </span>
                </div>
                <div className="tx-info-row">
                  <span className="label">Date</span>
                  <span
                    className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                  >
                    {selectedTx.date}
                  </span>
                </div>
                <div className="tx-info-row">
                  <span className="label">Reference</span>
                  <span
                    className={`value ${theme === "dark" ? "color-light" : "color-dark"} ref-code`}
                  >
                    {selectedTx.ref}
                    <Copy
                      size={14}
                      style={{ marginLeft: "4px", cursor: "pointer" }}
                      onClick={() =>
                        navigator.clipboard.writeText(selectedTx.ref)
                      }
                    />
                  </span>
                </div>

                {/* Channel */}
                {selectedTx.channel && (
                  <div className="tx-info-row">
                    <span className="label">Channel</span>
                    <span
                      className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                    >
                      {selectedTx.channel}
                    </span>
                  </div>
                )}

                {/* Account Holder */}
                {selectedTx.accountHolder && (
                  <div className="tx-info-row">
                    <span className="label">Account Holder</span>
                    <span
                      className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                    >
                      {selectedTx.accountHolder}
                    </span>
                  </div>
                )}

                {/* Gross */}
                {selectedTx.gross !== undefined &&
                  selectedTx.gross !== selectedTx.netAmount && (
                    <div className="tx-info-row">
                      <span className="label">Gross Amount</span>
                      <span
                        className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                      >
                        {selectedTx.symbol}
                        {formatAmount(selectedTx.gross)}
                      </span>
                    </div>
                  )}

                {/* Fee */}
                {selectedTx.fee !== undefined && selectedTx.fee !== 0 && (
                  <div className="tx-info-row">
                    <span className="label">Fee</span>
                    <span
                      className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                    >
                      {selectedTx.symbol}
                      {formatAmount(selectedTx.fee)}
                    </span>
                  </div>
                )}

                {/* Running Balance */}
                {selectedTx.runningBalance !== undefined && (
                  <div className="tx-info-row">
                    <span className="label">Running Balance</span>
                    <span
                      className={`value ${theme === "dark" ? "color-light" : "color-dark"}`}
                    >
                      {selectedTx.symbol}
                      {formatAmount(selectedTx.runningBalance)}
                    </span>
                  </div>
                )}
              </div>

              <button className="tx-download-btn">Download Receipt</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default History;
