"use client";

import { useState } from "react";
import { Search, ChevronDown, Download } from "lucide-react";
import styles from "./UsersToolbar.module.css";

export default function UsersToolbar() {
  const [search, setSearch] = useState("");

  return (
    <div className={styles.toolbar}>
      {/* Search */}
      <div className={styles.searchWrap}>
        <Search size={15} className={styles.searchIcon} />
        <input
          type="text"
          placeholder="Search"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className={styles.searchInput}
        />
      </div>

      <div className={styles.filters}>
        {/* Status filter */}
        <div className={styles.selectWrap}>
          <select className={styles.select}>
            <option>All Status</option>
            <option>Active</option>
            <option>Suspended</option>
            <option>Inactive</option>
          </select>
          <ChevronDown size={13} className={styles.selectChevron} />
        </div>

        {/* Date filter */}
        <div className={styles.selectWrap}>
          <select className={styles.select}>
            <option>All Date</option>
            <option>Today</option>
            <option>This Week</option>
            <option>This Month</option>
          </select>
          <ChevronDown size={13} className={styles.selectChevron} />
        </div>

        {/* KYC filter */}
        <div className={styles.selectWrap}>
          <select className={styles.select}>
            <option>All KYC</option>
            <option>Verified</option>
            <option>Pending</option>
            <option>Rejected</option>
          </select>
          <ChevronDown size={13} className={styles.selectChevron} />
        </div>

        {/* Export */}
        <button className={styles.exportBtn}>
          <Download size={14} />
          Export
        </button>
      </div>
    </div>
  );
}