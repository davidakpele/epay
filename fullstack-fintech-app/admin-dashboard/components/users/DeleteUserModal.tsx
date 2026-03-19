import { X, Trash2 } from "lucide-react";
import styles from "./UserModal.module.css";
import { User } from "@/app/types";

interface Props { user: User; onClose: () => void; onConfirm: () => void; }

export default function DeleteUserModal({ user, onClose, onConfirm }: Props) {
  return (
    <div className={styles.overlay} onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`${styles.modal} ${styles.modalSm}`}>

        <div className={styles.header}>
          <h2 className={styles.title}>Delete User</h2>
          <button className={styles.closeBtn} onClick={onClose}><X size={17} /></button>
        </div>

        <div className={styles.body}>
          <div className={styles.confirmIcon} style={{ background: "var(--red-light)" }}>
            <Trash2 size={28} color="var(--red)" />
          </div>
          <p className={styles.confirmText}>
            Are you sure you want to permanently delete <strong>{user.name}</strong>?
            This action <strong>cannot be undone</strong>.
          </p>
        </div>

        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button className={`${styles.submitBtn} ${styles.submitBtnDanger}`} onClick={onConfirm}>
            Yes, Delete User
          </button>
        </div>
      </div>
    </div>
  );
}