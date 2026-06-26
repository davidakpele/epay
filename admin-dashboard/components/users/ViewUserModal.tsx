import { X, Mail, Phone, CreditCard, ShieldCheck, Calendar, CircleUser } from "lucide-react";
import styles from "./UserModal.module.css";
import { User } from "@/app/types";

interface Props { user: User; onClose: () => void; }

const kycColor: Record<string, string> = {
  Verified: "#2d9e5f", Pending: "#b07d00", Rejected: "#e63946",
};
const statusColor: Record<string, string> = {
  Active: "#2d9e5f", Suspended: "#b07d00", Inactive: "#e63946",
};

export default function ViewUserModal({ user, onClose }: Props) {
  return (
    <div className={styles.overlay} onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className={styles.modal}>

        <div className={styles.header}>
          <h2 className={styles.title}>User Details</h2>
          <button className={styles.closeBtn} onClick={onClose}><X size={17} /></button>
        </div>

        {/* Avatar + name hero */}
        <div className={styles.hero}>
          <div className={styles.heroAvatar}>{user.avatar}</div>
          <div>
            <div className={styles.heroName}>{user.name}</div>
            <div className={styles.heroId}>ID: {user.id}</div>
          </div>
        </div>

        <div className={styles.divider} />

        <div className={styles.grid}>
          <div className={styles.field}>
            <span className={styles.fieldIcon}><Mail size={14} /></span>
            <div>
              <div className={styles.fieldLabel}>Email</div>
              <div className={styles.fieldValue}>{user.email}</div>
            </div>
          </div>

          <div className={styles.field}>
            <span className={styles.fieldIcon}><Phone size={14} /></span>
            <div>
              <div className={styles.fieldLabel}>Phone</div>
              <div className={styles.fieldValue}>{user.phone ?? "—"}</div>
            </div>
          </div>

          <div className={styles.field}>
            <span className={styles.fieldIcon}><CreditCard size={14} /></span>
            <div>
              <div className={styles.fieldLabel}>Balance</div>
              <div className={styles.fieldValue}>{user.balance}</div>
            </div>
          </div>

          <div className={styles.field}>
            <span className={styles.fieldIcon}><Calendar size={14} /></span>
            <div>
              <div className={styles.fieldLabel}>Date Registered</div>
              <div className={styles.fieldValue}>{user.dateRegistered}</div>
            </div>
          </div>

          <div className={styles.field}>
            <span className={styles.fieldIcon}><ShieldCheck size={14} /></span>
            <div>
              <div className={styles.fieldLabel}>KYC Status</div>
              <div className={styles.fieldValue}>
                <span className={styles.pill} style={{ background: kycColor[user.kyc] + "22", color: kycColor[user.kyc] }}>
                  KYC {user.kyc}
                </span>
              </div>
            </div>
          </div>

          <div className={styles.field}>
            <span className={styles.fieldIcon}><CircleUser size={14} /></span>
            <div>
              <div className={styles.fieldLabel}>Account Status</div>
              <div className={styles.fieldValue}>
                <span className={styles.pill} style={{ background: statusColor[user.status] + "22", color: statusColor[user.status] }}>
                  {user.status}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}