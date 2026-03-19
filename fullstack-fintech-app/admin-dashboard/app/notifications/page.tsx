"use client";

import { useState } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles  from "./page.module.css";
import { Bell, Plus, Download, Send, Archive, Pencil, ChevronLeft, ChevronRight } from "lucide-react";

type Status   = "Sent" | "Expired" | "Draft";
type Audience = "All Users" | "Registered Users" | "Mobile Users" | "Unverified Users";

interface Notification {
  id:        string;
  title:     string;
  message:   string;
  audience:  Audience;
  status:    Status;
  created:   string;
  expiry:    string | null;
}

const ALL_NOTIFICATIONS: Notification[] = [
  { id: "NTF789654", title: "System Maintenance Notice",      message: "The platform will undergo scheduled maintenance on April 25th, 2024, from 2 AM.", audience: "All Users",          status: "Sent",    created: "Apr 22, 2024 10:10 AM", expiry: "Apr 25, 2024" },
  { id: "NTF789653", title: "New Feature: Virtual Cards",     message: "Introducing your new Virtual card service!",                                        audience: "Registered Users",  status: "Sent",    created: "Apr 20, 2024 09:20 AM", expiry: "Jun 01, 2024" },
  { id: "NTF789652", title: "Important Security Update",      message: "The changed security measures implementing to enable 2FA.",                          audience: "All Users",          status: "Sent",    created: "Apr 15, 2024 02:45 PM", expiry: "May 01, 2024" },
  { id: "NTF789651", title: "Special Promotion Free Data",    message: "Get a free RGB data for detime top up on April 13th, 2024, 2-1",                    audience: "Mobile Users",       status: "Sent",    created: "Apr 19, 2024 11:30 AM", expiry: "Apr 30, 2024" },
  { id: "NTF789650", title: "KYC Reminder",                   message: "Users to complete KYC, for full access.",                                           audience: "Unverified Users",   status: "Expired", created: "Apr 01, 2024 06:19 AM", expiry: "Apr 15, 2024" },
  { id: "NTF789649", title: "System Upgrade: Notification",   message: "System upgrade to fixing improved performance on April 25th, 2024. & ...",          audience: "All Users",          status: "Draft",   created: "Apr 01, 2024 04:25 PM", expiry: null           },
  { id: "NTF789648", title: "System Upgrade: Notification",   message: "System upgrades for improved performance on April 25th, 2024.",                     audience: "All Users",          status: "Draft",   created: "Apr 01, 2024 03:25 PM", expiry: null           },
  { id: "NTF789647", title: "New Login Alert",                message: "A new login was detected on your account. Please verify if this was you.",          audience: "All Users",          status: "Sent",    created: "Mar 28, 2024 08:00 AM", expiry: "Apr 10, 2024" },
  { id: "NTF789646", title: "Password Reset Notice",          message: "Your password was recently changed. Contact support if you didn't initiate this.",  audience: "Registered Users",  status: "Sent",    created: "Mar 25, 2024 01:15 PM", expiry: "Apr 01, 2024" },
  { id: "NTF789645", title: "Scheduled Downtime Alert",       message: "Brief downtime expected on March 30th from 3–5 AM for system updates.",            audience: "All Users",          status: "Expired", created: "Mar 22, 2024 10:00 AM", expiry: "Mar 30, 2024" },
  { id: "NTF789644", title: "Cashback Promotion",             message: "Earn 5% cashback on all transactions this weekend only.",                           audience: "Mobile Users",       status: "Expired", created: "Mar 20, 2024 09:00 AM", expiry: "Mar 24, 2024" },
  { id: "NTF789643", title: "Account Verification Reminder",  message: "Complete your account verification to unlock all platform features.",               audience: "Unverified Users",   status: "Draft",   created: "Mar 18, 2024 07:30 AM", expiry: null           },
  { id: "NTF789642", title: "New Bill Payment Feature",       message: "You can now pay electricity and water bills directly from your ePay wallet.",       audience: "Registered Users",  status: "Sent",    created: "Mar 15, 2024 02:00 PM", expiry: "Apr 15, 2024" },
  { id: "NTF789641", title: "Referral Bonus Announcement",    message: "Invite a friend and earn ₦500 when they complete their first transaction.",         audience: "All Users",          status: "Sent",    created: "Mar 10, 2024 11:00 AM", expiry: "Apr 30, 2024" },
  { id: "NTF789640", title: "Mobile App Update Available",    message: "Update your ePay app to version 3.2 for improved security and new features.",       audience: "Mobile Users",       status: "Draft",   created: "Mar 05, 2024 08:45 AM", expiry: null           },
];

const PER_PAGE_OPTIONS = [6, 10, 15, 25];

const statusCls: Record<Status, string> = {
  Sent:    "statusSent",
  Expired: "statusExpired",
  Draft:   "statusDraft",
};

/* ── New Notification Modal ── */
function NewNotificationModal({ onClose }: { onClose: () => void }) {
  const [form, setForm] = useState({ title: "", message: "", audience: "All Users", status: "Draft" });
  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        <div className={styles.modalHead}>
          <h3 className={styles.modalTitle}>New Notification</h3>
          <button className={styles.modalClose} onClick={onClose}>✕</button>
        </div>

        <div className={styles.modalBody}>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>Title</label>
            <input className={styles.formInput} placeholder="Notification title..." value={form.title} onChange={e => set("title", e.target.value)} />
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>Message</label>
            <textarea className={styles.formTextarea} rows={4} placeholder="Notification message..." value={form.message} onChange={e => set("message", e.target.value)} />
          </div>
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Target Audience</label>
              <select className={styles.formSelect} value={form.audience} onChange={e => set("audience", e.target.value)}>
                <option>All Users</option>
                <option>Registered Users</option>
                <option>Mobile Users</option>
                <option>Unverified Users</option>
              </select>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Status</label>
              <select className={styles.formSelect} value={form.status} onChange={e => set("status", e.target.value)}>
                <option>Draft</option>
                <option>Sent</option>
              </select>
            </div>
          </div>
        </div>

        <div className={styles.modalFoot}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button className={styles.submitBtn}><Send size={14} /> Send Notification</button>
        </div>
      </div>
    </div>
  );
}

export default function NotificationsPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [showModal,   setShowModal]   = useState(false);

  const [statusFilter,   setStatusFilter]   = useState("All Status");
  const [audienceFilter, setAudienceFilter] = useState("All Audience");
  const [dateFilter,     setDateFilter]     = useState("All Date");

  const [page,    setPage]    = useState(1);
  const [perPage, setPerPage] = useState(6);
  const [jumpVal, setJumpVal] = useState("1");

  /* filtering */
  const filtered = ALL_NOTIFICATIONS.filter(n => {
    const matchStatus   = statusFilter   === "All Status"   || n.status   === statusFilter;
    const matchAudience = audienceFilter === "All Audience" || n.audience === audienceFilter;
    return matchStatus && matchAudience;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / perPage));
  const safePage   = Math.min(page, totalPages);
  const start      = (safePage - 1) * perPage;
  const rows       = filtered.slice(start, start + perPage);

  const goTo = (p: number) => {
    const c = Math.max(1, Math.min(totalPages, p));
    setPage(c);
    setJumpVal(String(c));
  };

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {showModal && <NewNotificationModal onClose={() => setShowModal(false)} />}

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />

        <main className={styles.main}>

          {/* ── Page header ── */}
          <div className={styles.pageHeader}>
            <div className={styles.titleBlock}>
              <div className={styles.titleRow}>
                <Bell size={22} className={styles.titleIcon} />
                <h1 className={styles.pageTitle}>Notifications / Announcements</h1>
              </div>
              <p className={styles.pageSubtitle}>Platform messaging for updates and alerts.</p>
            </div>
            <button className={styles.newBtn} onClick={() => setShowModal(true)}>
              <Plus size={16} /> New Notification
            </button>
          </div>

          {/* ── Filters row ── */}
          <div className={styles.filtersRow}>
            <div className={styles.filterSelects}>

              <div className={styles.selWrap}>
                <select className={styles.sel} value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(1); }}>
                  <option>All Status</option>
                  <option>Sent</option>
                  <option>Expired</option>
                  <option>Draft</option>
                </select>
                <ChevronRight size={12} className={styles.selChev} />
              </div>

              <div className={styles.selWrap}>
                <select className={styles.sel} value={audienceFilter} onChange={e => { setAudienceFilter(e.target.value); setPage(1); }}>
                  <option>All Audience</option>
                  <option>All Users</option>
                  <option>Registered Users</option>
                  <option>Mobile Users</option>
                  <option>Unverified Users</option>
                </select>
                <ChevronRight size={12} className={styles.selChev} />
              </div>

              <div className={styles.selWrap}>
                <select className={styles.sel} value={dateFilter} onChange={e => setDateFilter(e.target.value)}>
                  <option>All Date</option>
                  <option>April 2024</option>
                  <option>March 2024</option>
                </select>
                <ChevronRight size={12} className={styles.selChev} />
              </div>

            </div>

            <button className={styles.exportBtn}>
              <Download size={15} /> Export
            </button>
          </div>

          {/* ── Table card ── */}
          <div className={styles.tableCard}>

            {/* Table meta row */}
            <div className={styles.tableMeta}>
              <span className={styles.tableMetaLabel}>Platform messaging</span>
              <div className={styles.jumpRow}>
                <span className={styles.jumpLabel}>Page {safePage} of {totalPages}</span>
                <input
                  type="number"
                  className={styles.jumpInput}
                  value={jumpVal}
                  min={1}
                  max={totalPages}
                  onChange={e => setJumpVal(e.target.value)}
                  onKeyDown={e => e.key === "Enter" && goTo(Number(jumpVal))}
                />
                <span className={styles.jumpLabel}>of</span>
                <button className={styles.jumpArrow} onClick={() => goTo(safePage + 1)} disabled={safePage === totalPages}>
                  <ChevronRight size={14} />
                </button>
              </div>
            </div>

            {/* Table */}
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Notification ID</th>
                    <th>Title</th>
                    <th>Message</th>
                    <th>
                      <div className={styles.thWithSort}>
                        Target Audience
                        <ChevronRight size={12} className={styles.sortIcon} />
                      </div>
                    </th>
                    <th>Status</th>
                    <th>
                      <div className={styles.thWithSort}>
                        Created Date
                        <ChevronRight size={12} className={styles.sortIcon} />
                      </div>
                    </th>
                    <th>Expiry Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map(n => (
                    <tr key={n.id + n.created}>
                      <td className={styles.idCell}>{n.id}</td>
                      <td className={styles.titleCell}>{n.title}</td>
                      <td className={styles.msgCell}>{n.message}</td>
                      <td className={styles.audienceCell}>{n.audience}</td>
                      <td>
                        <span className={`${styles.statusBadge} ${styles[statusCls[n.status]]}`}>
                          {n.status}
                        </span>
                      </td>
                      <td className={styles.dateCell}>{n.created}</td>
                      <td className={styles.dateCell}>{n.expiry ?? "—"}</td>
                      <td>
                        <div className={styles.actions}>
                          <button className={styles.actionIconBtn} title="Resend">
                            {n.status === "Expired" || n.status === "Draft"
                              ? <Archive size={14} />
                              : <Send size={14} />
                            }
                          </button>
                          <button className={styles.editActionBtn}>
                            <Pencil size={13} /> Edit
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination footer */}
            <div className={styles.pagination}>
              <span className={styles.showingLabel}>
                Showing {filtered.length === 0 ? 0 : start + 1} to {Math.min(start + perPage, filtered.length)} of {filtered.length} entries
              </span>

              <div className={styles.pageControls}>
                <button className={styles.pageBtn} onClick={() => goTo(safePage - 1)} disabled={safePage === 1}>
                  <ChevronLeft size={13} /> Prev
                </button>

                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  let p: number;
                  if (totalPages <= 5)           p = i + 1;
                  else if (safePage <= 3)        p = i + 1;
                  else if (safePage >= totalPages - 2) p = totalPages - 4 + i;
                  else                           p = safePage - 2 + i;
                  return (
                    <button
                      key={p}
                      className={`${styles.pageNumBtn} ${safePage === p ? styles.pageNumActive : ""}`}
                      onClick={() => goTo(p)}
                    >
                      {p}
                    </button>
                  );
                })}

                {totalPages > 5 && safePage < totalPages - 2 && (
                  <>
                    <span className={styles.ellipsis}>…</span>
                    <button className={styles.pageNumBtn} onClick={() => goTo(totalPages)}>{totalPages}</button>
                  </>
                )}

                <span className={styles.pageOf}>of {totalPages}</span>

                <div className={styles.perPageWrap}>
                  <select className={styles.perPageSel} value={perPage} onChange={e => { setPerPage(Number(e.target.value)); setPage(1); }}>
                    {PER_PAGE_OPTIONS.map(o => <option key={o} value={o}>{o}</option>)}
                  </select>
                </div>

                <button className={styles.pageBtn} onClick={() => goTo(safePage + 1)} disabled={safePage === totalPages}>
                  Next <ChevronRight size={13} />
                </button>
              </div>
            </div>

          </div>
        </main>
      </div>
    </div>
  );
}