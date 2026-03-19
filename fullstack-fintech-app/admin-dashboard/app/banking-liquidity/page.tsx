"use client";

import { useState } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles from "./page.module.css";
import { ChevronRight } from "lucide-react";
import Link from "next/link";
import { STAT_LIQUIDITY_CARDS } from "../lib/data";
import LiquidityChart from "@/components/components/liquidity/LiquidityChart";
import BankingPoolsTable from "@/components/components/liquidity/BankingPoolsTable";
import PoolsSidebar from "@/components/components/liquidity/PoolsSidebar";

export default function BankingLiquidityPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />

        <main className={styles.main}>

          {/* Breadcrumb */}
          <nav className={styles.breadcrumb}>
            <Link href="/" className={styles.bcLink}>Manage Liquidity</Link>
            <ChevronRight size={13} className={styles.bcSep} />
            <span className={styles.bcCurrent}>Banking Liquidity</span>
          </nav>

          {/* Page title */}
          <h1 className={styles.pageTitle}>Banking Liquidity</h1>

          {/* Stat cards */}
          <div className={styles.statGrid}>
            {STAT_LIQUIDITY_CARDS.map(card => (
              <div
                key={card.label}
                className={styles.statCard}
                style={{ background: card.bg }}
              >
                <span className={styles.statLabel} style={{ color: card.subCol }}>
                  {card.label}
                </span>
                <span className={styles.statValue} style={{ color: card.textCol }}>
                  {card.value}
                </span>
              </div>
            ))}
          </div>

          {/* Body: chart + table LEFT | sidebar RIGHT */}
          <div className={styles.body}>

            {/* Left column */}
            <div className={styles.left}>
              <LiquidityChart />
              <BankingPoolsTable />
            </div>

            {/* Right sidebar */}
            <aside className={styles.right}>
              <PoolsSidebar />
            </aside>

          </div>

        </main>
      </div>
    </div>
  );
}