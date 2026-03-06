"use client";

import { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  MoreHorizontal, Eye, Pencil, ShieldOff,
  Trash2, ChevronLeft, ChevronRight, Wallet,
} from "lucide-react";
import styles from "./UsersTable.module.css";
import { ActionType, KycStatus, User, UserStatus } from "@/app/types";
import ViewUserModal    from "./ViewUserModal";
import DeleteUserModal  from "./DeleteUserModal";
import EditUserModal    from "./EditUserModal";
import SuspendUserModal from "./SuspendUserModal";
import { TOTAL_PAGES, USERS, USERS_PER_PAGE } from "@/app/lib/users";

const kycClass: Record<KycStatus, string> = {
  Verified: styles.kycVerified,
  Pending:  styles.kycPending,
  Rejected: styles.kycRejected,
};

const statusClass: Record<UserStatus, string> = {
  Active:    styles.statusActive,
  Suspended: styles.statusSuspended,
  Inactive:  styles.statusInactive,
};

/* ── Row action dropdown ── */
interface ActionMenuProps {
  onView: () => void;
  onViewWallet: () => void;
  onEdit: () => void;
  onSuspend: () => void;
  onDelete: () => void;
  onClose: () => void;
}

function ActionMenu({ onView, onViewWallet, onEdit, onSuspend, onDelete, onClose }: ActionMenuProps) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onClose();
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [onClose]);

  return (
    <div className={styles.actionMenu} ref={ref}>
      <button className={styles.actionItem} onClick={onView}>
        <Eye size={14} className={styles.actionIcon} /> View
      </button>

      {/* ── New: View Wallet ── */}
      <button className={`${styles.actionItem} ${styles.actionWallet}`} onClick={onViewWallet}>
        <Wallet size={14} className={styles.actionIcon} /> View Wallet
      </button>

      <button className={styles.actionItem} onClick={onEdit}>
        <Pencil size={14} className={styles.actionIcon} /> Edit
      </button>
      <button className={styles.actionItem} onClick={onSuspend}>
        <ShieldOff size={14} className={styles.actionIcon} /> Suspend
      </button>
      <div className={styles.actionDivider} />
      <button className={`${styles.actionItem} ${styles.actionDelete}`} onClick={onDelete}>
        <Trash2 size={14} className={styles.actionIcon} /> Delete
      </button>
    </div>
  );
}

/* ── Main table ── */
export default function UsersTable() {
  const router = useRouter();

  const [page, setPage]         = useState(1);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [openMenu, setOpenMenu] = useState<string | null>(null);
  const [jumpPage, setJumpPage] = useState("1");

  const [activeUser,   setActiveUser]   = useState<User | null>(null);
  const [activeAction, setActiveAction] = useState<ActionType>(null);
  const [userList, setUserList]         = useState<User[]>(USERS);

  const start = (page - 1) * USERS_PER_PAGE;
  const rows  = userList.slice(start, start + USERS_PER_PAGE);

  const openAction = (user: User, action: ActionType) => {
    setActiveUser(user);
    setActiveAction(action);
    setOpenMenu(null);
  };

  const closeModal = () => {
    setActiveUser(null);
    setActiveAction(null);
  };

  const handleEdit = (updated: User) => {
    setUserList(prev => prev.map(u => u.id === updated.id ? updated : u));
    closeModal();
  };

  const handleSuspend = (userId: string) => {
    setUserList(prev => prev.map(u =>
      u.id === userId ? { ...u, status: u.status === "Suspended" ? "Active" : "Suspended" } : u
    ));
    closeModal();
  };

  const handleDelete = (userId: string) => {
    setUserList(prev => prev.filter(u => u.id !== userId));
    closeModal();
  };

  const toggleAll = () => {
    setSelected(selected.size === rows.length ? new Set() : new Set(rows.map(u => u.id)));
  };

  const toggleOne = (id: string) => {
    const next = new Set(selected);
    next.has(id) ? next.delete(id) : next.add(id);
    setSelected(next);
  };

  const goToPage = (p: number) => {
    const clamped = Math.max(1, Math.min(TOTAL_PAGES, p));
    setPage(clamped);
    setJumpPage(String(clamped));
    setSelected(new Set());
  };

  return (
    <>
      <div>
        {/* Count + jump row */}
        <div className={styles.metaRow}>
          <span className={styles.countLabel}>
            Users: <strong>{rows.length} of {userList.length}</strong>
          </span>
          <div className={styles.jumpRow}>
            <span className={styles.jumpLabel}>Page {page} of {TOTAL_PAGES}</span>
            <input
              type="number"
              min={1}
              max={TOTAL_PAGES}
              value={jumpPage}
              onChange={e => setJumpPage(e.target.value)}
              onKeyDown={e => e.key === "Enter" && goToPage(Number(jumpPage))}
              className={styles.jumpInput}
            />
            <span className={styles.jumpLabel}>of {TOTAL_PAGES}</span>
          </div>
        </div>

        {/* Table */}
        <div className={styles.tableWrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th className={styles.checkTh}>
                  <input
                    type="checkbox"
                    checked={selected.size === rows.length && rows.length > 0}
                    onChange={toggleAll}
                    className={styles.checkbox}
                  />
                </th>
                <th>User</th>
                <th>Email</th>
                <th>Balance</th>
                <th>KYC</th>
                <th>Status</th>
                <th>Date Registered</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((user) => (
                <tr key={user.id} className={selected.has(user.id) ? styles.rowSelected : ""}>
                  <td className={styles.checkTd}>
                    <input
                      type="checkbox"
                      checked={selected.has(user.id)}
                      onChange={() => toggleOne(user.id)}
                      className={styles.checkbox}
                    />
                  </td>

                  <td>
                    <div className={styles.userCell}>
                      <div className={styles.avatar}>{user.avatar}</div>
                      <span className={styles.userName}>{user.name}</span>
                    </div>
                  </td>

                  <td className={styles.emailCell}>{user.email}</td>
                  <td className={styles.balanceCell}>{user.balance}</td>

                  <td>
                    <span className={`${styles.badge} ${kycClass[user.kyc]}`}>
                      KYC {user.kyc}
                    </span>
                  </td>

                  <td>
                    <span className={`${styles.badge} ${statusClass[user.status]}`}>
                      {user.status}
                    </span>
                  </td>

                  <td className={styles.dateCell}>{user.dateRegistered}</td>

                  <td className={styles.actionTd}>
                    <div className={styles.actionWrap}>
                      <button
                        className={`${styles.menuBtn} ${openMenu === user.id ? styles.menuBtnActive : ""}`}
                        onClick={() => setOpenMenu(openMenu === user.id ? null : user.id)}
                      >
                        <MoreHorizontal size={16} />
                      </button>
                      {openMenu === user.id && (
                        <ActionMenu
                          onClose={()      => setOpenMenu(null)}
                          onView={()       => openAction(user, "view")}
                          onViewWallet={()  => router.push(`/manage-users/${user.id}/wallet`)}
                          onEdit={()       => openAction(user, "edit")}
                          onSuspend={()    => openAction(user, "suspend")}
                          onDelete={()     => openAction(user, "delete")}
                        />
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className={styles.pagination}>
          <span className={styles.showingLabel}>
            Showing {start + 1} to {Math.min(start + USERS_PER_PAGE, userList.length)} of {userList.length} entries
          </span>

          <div className={styles.pageControls}>
            <button className={styles.pageBtn} onClick={() => goToPage(page - 1)} disabled={page === 1}>
              <ChevronLeft size={14} /> Prev
            </button>

            {Array.from({ length: Math.min(5, TOTAL_PAGES) }, (_, i) => {
              let p: number;
              if (TOTAL_PAGES <= 5)             p = i + 1;
              else if (page <= 3)               p = i + 1;
              else if (page >= TOTAL_PAGES - 2) p = TOTAL_PAGES - 4 + i;
              else                              p = page - 2 + i;
              return (
                <button
                  key={p}
                  className={`${styles.pageNumBtn} ${page === p ? styles.pageNumActive : ""}`}
                  onClick={() => goToPage(p)}
                >
                  {p}
                </button>
              );
            })}

            {TOTAL_PAGES > 5 && page < TOTAL_PAGES - 2 && <span className={styles.pageEllipsis}>…</span>}
            {TOTAL_PAGES > 5 && page < TOTAL_PAGES - 2 && (
              <button className={styles.pageNumBtn} onClick={() => goToPage(TOTAL_PAGES)}>{TOTAL_PAGES}</button>
            )}

            <span className={styles.pageOf}>of {TOTAL_PAGES}</span>

            <button className={styles.pageBtn} onClick={() => goToPage(page + 1)} disabled={page === TOTAL_PAGES}>
              Next <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>

      {/* Modals */}
      {activeAction === "view"    && activeUser && <ViewUserModal    user={activeUser} onClose={closeModal} />}
      {activeAction === "edit"    && activeUser && <EditUserModal    user={activeUser} onClose={closeModal} onSave={handleEdit} />}
      {activeAction === "suspend" && activeUser && <SuspendUserModal user={activeUser} onClose={closeModal} onConfirm={() => handleSuspend(activeUser.id)} />}
      {activeAction === "delete"  && activeUser && <DeleteUserModal  user={activeUser} onClose={closeModal} onConfirm={() => handleDelete(activeUser.id)} />}
    </>
  );
}