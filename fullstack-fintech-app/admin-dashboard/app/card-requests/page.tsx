"use client";

import { useState } from "react";
import Header from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles from "./page.module.css";
import { ChevronRight, ChevronLeft, ChevronDown, MoreHorizontal } from "lucide-react";
import Link from "next/link";

/* ─── Types ─── */
type CardStatus = "Pending" | "Approved" | "Rejected";

interface CardRequest {
  id: number;
  name: string;
  email: string;
  avatarColor: string;
  cardType: "Standard Virtual Card" | "Gold Virtual Card";
  requestDate: string;
  status: CardStatus;
}

/* ─── Mock data ─── */
const INITIAL_REQUESTS: CardRequest[] = [
  { id: 1, name: "David Alex",     email: "userid.alex@transaction.com", avatarColor: "#7a9a82", cardType: "Standard Virtual Card", requestDate: "Apr 21, 2024", status: "Pending"  },
  { id: 2, name: "Sarah Clark",    email: "david.algm.com",              avatarColor: "#c4856a", cardType: "Gold Virtual Card",     requestDate: "Apr 21, 2024", status: "Pending"  },
  { id: 3, name: "Michael Wong",   email: "sarid.ae.glgm.com",           avatarColor: "#6a8aaa", cardType: "Standard Virtual Card", requestDate: "Apr 21, 2024", status: "Approved" },
  { id: 4, name: "Emily Mason",    email: "oaeid.ale.ibumitner.com",     avatarColor: "#9a6a7a", cardType: "Gold Virtual Card",     requestDate: "Apr 21, 2024", status: "Rejected" },
  { id: 5, name: "John Doe",       email: "david.ale@gm1.com",           avatarColor: "#8a7a5a", cardType: "Gold Virtual Card",     requestDate: "Apr 23, 2024", status: "Pending"  },
  { id: 6, name: "Lily White",     email: "David.allcmail.com",          avatarColor: "#a06a8a", cardType: "Standard Virtual Card", requestDate: "Apr 24, 2024", status: "Pending"  },
  { id: 7, name: "Robert Johnson", email: "Jann.gem@i.icams.com",        avatarColor: "#6a7aaa", cardType: "Standard Virtual Card", requestDate: "Apr 24, 2024", status: "Pending"  },
];

/* ─── Stat cards ─── */
const STAT_CARDS = [
  {
    label: "Pending Requests",
    key: "Pending" as CardStatus,
    bg: "#7aadd4",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="9 11 12 14 22 4"/>
        <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11"/>
      </svg>
    ),
  },
  {
    label: "Approved Requests",
    key: "Approved" as CardStatus,
    bg: "#4a9e68",
    icon: (
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
        <polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
    ),
  },
  {
    label: "Rejected Requests",
    key: "Rejected" as CardStatus,
    bg: "#c96058",
    icon: (
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10"/>
        <line x1="8" y1="12" x2="16" y2="12"/>
      </svg>
    ),
  },
];

/* ─── Helpers ─── */
function getInitials(name: string) {
  return name.split(" ").map(n => n[0]).join("").slice(0, 2).toUpperCase();
}

function StatusBadge({ status }: { status: CardStatus }) {
  const cls = status === "Approved"
    ? styles.statusApproved
    : status === "Rejected"
    ? styles.statusRejected
    : styles.statusPending;
  return <span className={`${styles.statusBadge} ${cls}`}>{status}</span>;
}

/* ─── Page ─── */
export default function CardRequestsPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [requests, setRequests] = useState<CardRequest[]>(INITIAL_REQUESTS);
  const [statusFilter, setStatusFilter] = useState<"All" | CardStatus>("All");
  const [sortBy, setSortBy] = useState<"Newest" | "Oldest">("Newest");
  const [currentPage, setCurrentPage] = useState(1);

  const TOTAL_RECORDS = 36;
  const ITEMS_PER_PAGE = 7;
  const totalPages = Math.ceil(TOTAL_RECORDS / ITEMS_PER_PAGE);

  const counts = {
    Pending:  requests.filter(r => r.status === "Pending").length,
    Approved: requests.filter(r => r.status === "Approved").length,
    Rejected: requests.filter(r => r.status === "Rejected").length,
  };

  const filtered = requests
    .filter(r => statusFilter === "All" || r.status === statusFilter)
    .sort((a, b) => {
      const da = new Date(a.requestDate).getTime();
      const db = new Date(b.requestDate).getTime();
      return sortBy === "Newest" ? db - da : da - db;
    });

  const handleApprove = (id: number) =>
    setRequests(prev => prev.map(r => r.id === id ? { ...r, status: "Approved" } : r));

  const handleReject = (id: number) =>
    setRequests(prev => prev.map(r => r.id === id ? { ...r, status: "Rejected" } : r));

  const start = (currentPage - 1) * ITEMS_PER_PAGE + 1;
  const end   = Math.min(currentPage * ITEMS_PER_PAGE, TOTAL_RECORDS);

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />

        <main className={styles.main}>

          {/* Breadcrumb */}
          <nav className={styles.breadcrumb}>
            <Link href="/manage-cards" className={styles.bcLink}>Manage Cards</Link>
            <ChevronRight size={13} className={styles.bcSep} />
            <span className={styles.bcCurrent}>Card Requests</span>
          </nav>

          {/* Title */}
          <h1 className={styles.pageTitle}>Card Requests</h1>

          {/* Stat cards */}
          <div className={styles.statGrid}>
            {STAT_CARDS.map(card => (
              <div key={card.key} className={styles.statCard} style={{ background: card.bg }}>
                {/* Left: label top, icon bottom */}
                <div className={styles.statLeft}>
                  <span className={styles.statLabel}>{card.label}</span>
                  <div className={styles.statIcon}>{card.icon}</div>
                </div>
                {/* Right: big number centered */}
                <span className={styles.statValue}>{counts[card.key]}</span>
              </div>
            ))}
          </div>

          {/* Filters */}
          <div className={styles.filtersBar}>
            <div className={styles.filterGroup}>
              <span className={styles.filterLabel}>Filter by Status:</span>
              <div className={styles.filterSelectWrap}>
                <select
                  className={styles.filterSelect}
                  value={statusFilter}
                  onChange={e => setStatusFilter(e.target.value as typeof statusFilter)}
                >
                  <option value="All">All</option>
                  <option value="Pending">Pending</option>
                  <option value="Approved">Approved</option>
                  <option value="Rejected">Rejected</option>
                </select>
                <ChevronDown size={13} className={styles.filterChevron} />
              </div>
            </div>

            <div className={styles.filterGroup}>
              <span className={styles.filterLabel}>Sorted by:</span>
              <div className={styles.filterSelectWrap}>
                <select
                  className={styles.filterSelect}
                  value={sortBy}
                  onChange={e => setSortBy(e.target.value as typeof sortBy)}
                >
                  <option value="Newest">Newest</option>
                  <option value="Oldest">Oldest</option>
                </select>
                <ChevronDown size={13} className={styles.filterChevron} />
              </div>
            </div>
          </div>

          {/* Table */}
          <div className={styles.tableCard}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Card Type</th>
                  <th>Request Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map(req => (
                  <tr key={req.id}>

                    {/* Name + Email */}
                    <td>
                      <div className={styles.userCell}>
                        <div className={styles.avatarWrap}>
                          <div
                            className={styles.avatarFallback}
                            style={{ background: req.avatarColor }}
                          >
                            {getInitials(req.name)}
                          </div>
                          <span className={styles.onlineDot} />
                        </div>
                        <div>
                          <div className={styles.userName}>{req.name}</div>
                          <div className={styles.userEmail}>{req.email}</div>
                        </div>
                      </div>
                    </td>

                    {/* Card type */}
                    <td><span className={styles.cardType}>{req.cardType}</span></td>

                    {/* Date */}
                    <td><span className={styles.dateCell}>{req.requestDate}</span></td>

                    {/* Status */}
                    <td><StatusBadge status={req.status} /></td>

                    {/* Actions */}
                    <td>
                      <div className={styles.actions}>
                        {req.status === "Approved" ? (
                          <button className={`${styles.btn} ${styles.btnPending}`} disabled>
                            Pending
                          </button>
                        ) : (
                          <button
                            className={`${styles.btn} ${styles.btnApprove}`}
                            onClick={() => handleApprove(req.id)}
                          >
                            Approve
                          </button>
                        )}
                        <button
                          className={`${styles.btn} ${styles.btnReject}`}
                          onClick={() => handleReject(req.id)}
                        >
                          Reject
                        </button>
                      </div>
                    </td>

                  </tr>
                ))}
              </tbody>
            </table>

            {/* Pagination */}
            <div className={styles.pagination}>
              <span className={styles.pageInfo}>{start} - {end} of {TOTAL_RECORDS}</span>

              <button
                className={`${styles.pageBtn} ${currentPage === 1 ? styles.pageBtnDisabled : ""}`}
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
              >
                <ChevronLeft size={13} />
              </button>

              {[1, 2, 3].map(p => (
                <button
                  key={p}
                  className={`${styles.pageBtn} ${currentPage === p ? styles.pageBtnActive : ""}`}
                  onClick={() => setCurrentPage(p)}
                >
                  {p}
                </button>
              ))}

              <span className={styles.pageDots}>
                <MoreHorizontal size={14} />
              </span>

              <button
                className={`${styles.pageBtn} ${currentPage === totalPages ? styles.pageBtnDisabled : ""}`}
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
              >
                <ChevronRight size={13} />
              </button>
            </div>
          </div>

        </main>
      </div>
    </div>
  );
}