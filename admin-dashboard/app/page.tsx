"use client";

import { useState } from "react";

import styles from "./page.module.css";
import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import StatCards from "@/components/dashboard/StatCards";
import AnalyticsChart from "@/components/charts/AnalyticsChart";
import UserStatsChart from "@/components/charts/UserStatsChart";
import UserActivity from "@/components/dashboard/UserActivity";
import TransactionsTable from "@/components/dashboard/TransactionsTable";
import Notifications from "@/components/dashboard/Notifications";

export default function DashboardPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen((p) => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />
        <main className={styles.main}>
          <h1 className={styles.pageTitle}>Welcome, Admin!</h1>
          <p className={styles.pageSubtitle}>Here is an overview of the platform&apos;s activity.</p>

          <StatCards />

          <div className={styles.mainGrid}>
            <div className={styles.mgAnalytics}>
              <AnalyticsChart />
            </div>
            <div className={styles.mgUserStats}>
              <UserStatsChart />
            </div>
            <div className={styles.mgUserActivity}>
              <UserActivity />
            </div>
            <div className={styles.mgTxns}>
              <TransactionsTable />
            </div>
            <div className={styles.mgNotifs}>
              <Notifications />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}