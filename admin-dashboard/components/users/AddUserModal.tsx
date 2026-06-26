"use client";

import { useEffect, useRef } from "react";
import { X, User, Mail, Phone, Lock } from "lucide-react";
import styles from "./AddUserModal.module.css";

interface AddUserModalProps {
  onClose: () => void;
}

export default function AddUserModal({ onClose }: AddUserModalProps) {
  const overlayRef = useRef<HTMLDivElement>(null);

  // Close on Escape
  useEffect(() => {
    const handler = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", handler);
    return () => document.removeEventListener("keydown", handler);
  }, [onClose]);

  return (
    <div
      className={styles.overlay}
      ref={overlayRef}
      onClick={(e) => { if (e.target === overlayRef.current) onClose(); }}
    >
      <div className={styles.modal}>
        {/* Header */}
        <div className={styles.modalHeader}>
          <div>
            <h2 className={styles.modalTitle}>Add New User</h2>
            <p className={styles.modalSubtitle}>Fill in the details to create a new user account.</p>
          </div>
          <button className={styles.closeBtn} onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {/* Body */}
        <div className={styles.modalBody}>
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label}>Full Name</label>
              <div className={styles.inputWrap}>
                <User size={15} className={styles.inputIcon} />
                <input type="text" placeholder="e.g. David Alex" className={styles.input} />
              </div>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Email Address</label>
              <div className={styles.inputWrap}>
                <Mail size={15} className={styles.inputIcon} />
                <input type="email" placeholder="user@email.com" className={styles.input} />
              </div>
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label}>Phone Number</label>
              <div className={styles.inputWrap}>
                <Phone size={15} className={styles.inputIcon} />
                <input type="tel" placeholder="+234 000 000 0000" className={styles.input} />
              </div>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Password</label>
              <div className={styles.inputWrap}>
                <Lock size={15} className={styles.inputIcon} />
                <input type="password" placeholder="Set initial password" className={styles.input} />
              </div>
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label}>KYC Status</label>
              <select className={styles.select}>
                <option value="">Select KYC status</option>
                <option>Verified</option>
                <option>Pending</option>
              </select>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Account Status</label>
              <select className={styles.select}>
                <option value="">Select status</option>
                <option>Active</option>
                <option>Inactive</option>
              </select>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className={styles.modalFooter}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button className={styles.submitBtn}>Create User</button>
        </div>
      </div>
    </div>
  );
}