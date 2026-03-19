"use client";

import { useState } from "react";
import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";

import styles from "./page.module.css";
import { Settings as SettingsIcon, Save, RotateCcw } from "lucide-react";
import ProfileSettings from "@/components/settings/ProfileSettings";
import SecuritySettings from "@/components/settings/SecuritySettings";
import NotificationSettings from "@/components/settings/NotificationSettings";
import ApiSettings from "@/components/settings/ApiSettings";

const TABS = ["Profile Settings", "Security Settings", "Notification Settings", "API Settings"] as const;
type Tab = typeof TABS[number];

export default function SettingsPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [activeTab, setActiveTab]     = useState<Tab>("Profile Settings");
  const [saveKey, setSaveKey]         = useState(0);
  const [resetKey, setResetKey]       = useState(0);

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />
        <main className={styles.main}>

          {/* Page heading */}
          <div className={styles.pageHead}>
            <div className={styles.pageTitleGroup}>
              <div className={styles.pageTitleIcon}>
                <SettingsIcon size={22} />
              </div>
              <div>
                <h1 className={styles.pageTitle}>Settings</h1>
                <p className={styles.pageSubtitle}>Manage the platform settings and preferences.</p>
              </div>
            </div>
            <div className={styles.headActions}>
              <button className={styles.saveBtn} onClick={() => setSaveKey(k => k + 1)}>
                <Save size={15} /> Save Changes
              </button>
              <button className={styles.resetBtn} onClick={() => setResetKey(k => k + 1)}>
                <RotateCcw size={14} /> Reset
              </button>
            </div>
          </div>

          {/* Tab bar */}
          <div className={styles.tabBar}>
            {TABS.map(tab => (
              <button
                key={tab}
                className={`${styles.tab} ${activeTab === tab ? styles.tabActive : ""}`}
                onClick={() => setActiveTab(tab)}
              >
                {tab}
              </button>
            ))}
          </div>

          {/* Tab content */}
          <div className={styles.tabContent}>
            {activeTab === "Profile Settings"      && <ProfileSettings      saveKey={saveKey} resetKey={resetKey} />}
            {activeTab === "Security Settings"     && <SecuritySettings     saveKey={saveKey} resetKey={resetKey} />}
            {activeTab === "Notification Settings" && <NotificationSettings saveKey={saveKey} resetKey={resetKey} />}
            {activeTab === "API Settings"          && <ApiSettings          saveKey={saveKey} resetKey={resetKey} />}
          </div>

        </main>
      </div>
    </div>
  );
}