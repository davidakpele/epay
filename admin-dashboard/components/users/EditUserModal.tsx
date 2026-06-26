"use client";

import { useState } from "react";
import { X, User as UserIcon, Mail, Phone, Lock } from "lucide-react";
import styles from "./UserModal.module.css";
import { User } from "@/app/types";

interface Props { user: User; onClose: () => void; onSave: (u: User) => void; }

export default function EditUserModal({ user, onClose, onSave }: Props) {
  const [form, setForm] = useState({
    name:    user.name,
    email:   user.email,
    phone:   user.phone ?? "",
    balance: user.balance,
    kyc:     user.kyc,
    status:  user.status,
  });

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  const handleSave = () => {
    onSave({ ...user, ...form });
  };

  return (
    <div className={styles.overlay} onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className={styles.modal}>

        <div className={styles.header}>
          <div>
            <h2 className={styles.title}>Edit User</h2>
            <p className={styles.subtitle}>Update details for <strong>{user.name}</strong></p>
          </div>
          <button className={styles.closeBtn} onClick={onClose}><X size={17} /></button>
        </div>

        <div className={styles.body}>
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label}>Full Name</label>
              <div className={styles.inputWrap}>
                <UserIcon size={14} className={styles.inputIcon} />
                <input className={styles.input} value={form.name} onChange={e => set("name", e.target.value)} />
              </div>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Email</label>
              <div className={styles.inputWrap}>
                <Mail size={14} className={styles.inputIcon} />
                <input className={styles.input} type="email" value={form.email} onChange={e => set("email", e.target.value)} />
              </div>
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label}>Phone</label>
              <div className={styles.inputWrap}>
                <Phone size={14} className={styles.inputIcon} />
                <input className={styles.input} value={form.phone} onChange={e => set("phone", e.target.value)} />
              </div>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Balance</label>
              <div className={styles.inputWrap}>
                <Lock size={14} className={styles.inputIcon} />
                <input className={styles.input} value={form.balance} onChange={e => set("balance", e.target.value)} />
              </div>
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label}>KYC Status</label>
              <select className={styles.select} value={form.kyc} onChange={e => set("kyc", e.target.value)}>
                <option>Verified</option>
                <option>Pending</option>
                <option>Rejected</option>
              </select>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.label}>Account Status</label>
              <select className={styles.select} value={form.status} onChange={e => set("status", e.target.value)}>
                <option>Active</option>
                <option>Suspended</option>
                <option>Inactive</option>
              </select>
            </div>
          </div>
        </div>

        <div className={styles.footer}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button className={styles.submitBtn} onClick={handleSave}>Save Changes</button>
        </div>
      </div>
    </div>
  );
}