"use client";

import { useState } from "react";
import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles from "./page.module.css";
import UsersToolbar from "@/components/users/UsersToolbar";
import UsersTable from "@/components/users/UsersTable";
import AddUserModal from "@/components/users/AddUserModal";

export default function ManageUsersPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />
        <main className={styles.main}>

          {/* Page heading */}
          <div className={styles.pageHead}>
            <div className={styles.pageTitleRow}>
              <div className={styles.pageTitleGroup}>
                <div className={styles.pageTitleIcon}>
                  <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                </div>
                <div>
                  <h1 className={styles.pageTitle}>Manage Users</h1>
                  <p className={styles.pageSubtitle}>View and manage all user accounts on the platform.</p>
                </div>
              </div>
              <button className={styles.addBtn} onClick={() => setShowAddModal(true)}>
                <span className={styles.addBtnPlus}>+</span> Add New User
              </button>
            </div>
          </div>

          {/* Table card */}
          <div className={styles.tableCard}>
            <UsersToolbar />
            <UsersTable />
          </div>

        </main>
      </div>

      {showAddModal && <AddUserModal onClose={() => setShowAddModal(false)} />}
    </div>
  );
}