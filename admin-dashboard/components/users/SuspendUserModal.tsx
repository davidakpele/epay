import { X, ShieldOff, ShieldCheck } from "lucide-react";
import styles from "./UserModal.module.css";
import { User } from "@/app/types";

interface Props { user: User; onClose: () => void; onConfirm: () => void; }

export default function SuspendUserModal({ user, onClose, onConfirm }: Props) {
  const isSuspended = user.status === "Suspended";

  return (
    <div className={styles.overlay} onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`${styles.modal} ${styles.modalSm}`}>

        <div className={styles.header}>
          <h2 className={styles.title}>{isSuspended ? "Unsuspend" : "Suspend"} User</h2>
          <button className={styles.closeBtn} onClick={onClose}><X size={17} /></button>
        </div>

        <div className={styles.body}>
          <div className={styles.confirmIcon} style={{ background: isSuspended ? "#e6f7ee" : "#fff3cd" }}>
            {isSuspended
              ? <ShieldCheck size={28} color="#2d9e5f" />
              : <ShieldOff   size={28} color="#b07d00" />
            }
          </div>
          <p className={styles.confirmText}>
            {isSuspended
              ? <>Are you sure you want to <strong>unsuspend</strong> <strong>{user.name}</strong>? They will regain full access.</>
              : <>Are you sure you want to <strong>suspend</strong> <strong>{user.name}</strong>? They will lose access to their account.</>
            }
          </p>
        </div>

        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button
            className={styles.submitBtn}
            style={{ background: isSuspended ? "var(--success)" : "#b07d00" }}
            onClick={onConfirm}
          >
            {isSuspended ? "Yes, Unsuspend" : "Yes, Suspend"}
          </button>
        </div>
      </div>
    </div>
  );
}