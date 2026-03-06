"use client";

import { useState } from "react";
import { User, Mail, Phone, CheckCircle } from "lucide-react";
import styles from "./AccountSettingsTab.module.css";

interface Props {
  user: { name: string; email: string; avatar: string; kyc: string; status: string; };
}

export default function AccountSettingsTab({ user }: Props) {
  const [form, setForm] = useState({ name: user.name, email: user.email, phone: "+234 801 234 5678" });
  const [saved, setSaved] = useState(false);
  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  const handleSave = () => { setSaved(true); setTimeout(() => setSaved(false), 2500); };

  return (
    <div className={styles.wrap}>
      {saved && <div className={styles.toast}><CheckCircle size={15} /> Changes saved successfully!</div>}

      <div className={styles.section}>
        <h3 className={styles.sectionTitle}>Account Information</h3>
        <div className={styles.formGrid}>
          <div className={styles.formGroup}>
            <label className={styles.label}>Full Name</label>
            <div className={styles.inputWrap}>
              <User size={14} className={styles.inputIcon} />
              <input className={styles.input} value={form.name} onChange={e => set("name", e.target.value)} />
            </div>
          </div>
          <div className={styles.formGroup}>
            <label className={styles.label}>Email Address</label>
            <div className={styles.inputWrap}>
              <Mail size={14} className={styles.inputIcon} />
              <input className={styles.input} type="email" value={form.email} onChange={e => set("email", e.target.value)} />
            </div>
          </div>
          <div className={styles.formGroup}>
            <label className={styles.label}>Phone Number</label>
            <div className={styles.inputWrap}>
              <Phone size={14} className={styles.inputIcon} />
              <input className={styles.input} value={form.phone} onChange={e => set("phone", e.target.value)} />
            </div>
          </div>
          <div className={styles.formGroup}>
            <label className={styles.label}>KYC Status</label>
            <select className={styles.select}>
              <option>Verified</option><option>Pending</option><option>Rejected</option>
            </select>
          </div>
          <div className={styles.formGroup}>
            <label className={styles.label}>Account Status</label>
            <select className={styles.select}>
              <option>Active</option><option>Suspended</option><option>Inactive</option>
            </select>
          </div>
        </div>
        <button className={styles.saveBtn} onClick={handleSave}>Save Changes</button>
      </div>
    </div>
  );
}