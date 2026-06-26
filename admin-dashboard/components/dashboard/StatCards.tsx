"use client";

import { useEffect, useState } from "react";
import { Users, Banknote, ArrowLeftRight, CreditCard } from "lucide-react";
import styles from "./StatCards.module.css";
import { StatCard } from "@/app/types";

const ICONS = [Users, Banknote, ArrowLeftRight, CreditCard];

const variantClass: Record<StatCard["variant"], string> = {
  green:  styles.green,
  blue:   styles.blue,
  orange: styles.orange,
  yellow: styles.yellow,
};

interface DashboardSummary {
  totalUsers: number;
  totalHistory: number;
  totalVirtualCards: number;
  platformTotalBalance: string;
}

function buildStatCards(data: DashboardSummary): StatCard[] {
  const balance = parseFloat(data.platformTotalBalance || "0");
  const formattedBalance =
    balance >= 1_000_000
      ? `₦${(balance / 1_000_000).toFixed(1)}M`
      : balance >= 1_000
      ? `₦${(balance / 1_000).toFixed(1)}K`
      : `₦${balance.toLocaleString()}`;

  const balanceSub =
    balance > 0
      ? `₦${balance.toLocaleString("en-NG", { minimumFractionDigits: 2 })}`
      : undefined;

  return [
    {
      label:   "Total Users",
      value:   data.totalUsers.toLocaleString(),
      variant: "green",
    },
    {
      label:   "Total Balance",
      value:   formattedBalance,
      sub:     balanceSub,
      variant: "blue",
    },
    {
      label:   "Total Transactions",
      value:   data.totalHistory.toLocaleString(),
      variant: "orange",
    },
    {
      label:   "Virtual Cards",
      value:   data.totalVirtualCards.toLocaleString(),
      variant: "yellow",
    },
  ];
}

function getToken(): string | null {
  return (
    localStorage.getItem("accessToken") ??
    sessionStorage.getItem("accessToken")
  );
}

export default function StatCards() {
  const [cards,   setCards]   = useState<StatCard[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState<string | null>(null);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setError("Not authenticated.");
      setLoading(false);
      return;
    }

    fetch("http://localhost:8109/admin/dashboard/summary", {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type":  "application/json",
      },
    })
      .then(async (res) => {
        if (!res.ok) throw new Error(`Server error: ${res.status}`);
        return res.json() as Promise<DashboardSummary>;
      })
      .then((data) => {
        setCards(buildStatCards(data));
      })
      .catch((err: Error) => {
        setError(err.message ?? "Failed to load summary.");
      })
      .finally(() => setLoading(false));
  }, []);

  // ── Loading skeleton ──────────────────────────────────────────────────
  if (loading) {
    return (
      <div className={styles.grid}>
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className={`${styles.card} ${styles.skeleton}`} />
        ))}
      </div>
    );
  }

  // ── Error state ───────────────────────────────────────────────────────
  if (error || !cards) {
    return (
      <div className={styles.grid}>
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className={`${styles.card} ${styles.errorCard}`}>
            <span className={styles.errorText}>{error ?? "No data"}</span>
          </div>
        ))}
      </div>
    );
  }

  // ── Normal render ─────────────────────────────────────────────────────
  return (
    <div className={styles.grid}>
      {cards.map((card, i) => {
        const Icon = ICONS[i];
        return (
          <div key={card.label} className={`${styles.card} ${variantClass[card.variant]}`}>
            <div className={styles.decorCircle} />
            <div className={styles.iconWrap}><Icon size={20} strokeWidth={2} /></div>
            <div className={styles.info}>
              <div className={styles.label}>{card.label}</div>
              <div className={styles.value}>{card.value}</div>
              {card.sub && <div className={styles.sub}>{card.sub}</div>}
            </div>
          </div>
        );
      })}
    </div>
  );
}