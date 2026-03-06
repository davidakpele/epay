"use client";

import { useRef, useState, useEffect } from "react";
import { User, Mail, Phone, Upload, CheckCircle } from "lucide-react";
import styles from "./Settings.module.css";

interface Props { saveKey: number; resetKey: number; }

const DEFAULTS = { name: "David Admin", email: "admin@email.com", phone: "+234 801 234 5678" };

export default function ProfileSettings({ saveKey, resetKey }: Props) {
  const [form, setForm]       = useState(DEFAULTS);
  const [saved, setSaved]     = useState(false);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);
  const [accountActive, setAccountActive]     = useState(true);
  const [maintenance, setMaintenance]         = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);

  // Save trigger from parent
  useEffect(() => { if (saveKey > 0) handleSave(); }, [saveKey]);
  // Reset trigger from parent
  useEffect(() => { if (resetKey > 0) { setForm(DEFAULTS); setAccountActive(true); setMaintenance(false); }}, [resetKey]);

  const handleSave = () => {
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  const handlePhoto = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) setPhotoUrl(URL.createObjectURL(file));
  };

  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  return (
    <div className={styles.stack}>

      {saved && (
        <div className={styles.toast}>
          <CheckCircle size={16} /> Profile saved successfully!
        </div>
      )}

      {/* Admin Profile card */}
      <div className={styles.sectionCard}>
        <h3 className={styles.sectionTitle}>Admin Profile</h3>

        <div className={styles.profileGrid}>
          {/* Left — avatar */}
          <div className={styles.avatarCol}>
            <p className={styles.colLabel}>Profile Settings</p>
            <div className={styles.avatarWrap}>
              {photoUrl
                ? <img src={photoUrl} alt="avatar" className={styles.avatarImg} />
                : <div className={styles.avatarPlaceholder}>DA</div>
              }
            </div>
            <input ref={fileRef} type="file" accept="image/*" hidden onChange={handlePhoto} />
            <button className={styles.uploadBtn} onClick={() => fileRef.current?.click()}>
              <Upload size={14} /> Upload New Photo
            </button>
          </div>

          {/* Right — fields */}
          <div className={styles.fieldsCol}>
            <div className={styles.formGroup}>
              <label className={styles.label}>Full Name</label>
              <div className={styles.inputWrap}>
                <User size={14} className={styles.inputIcon} />
                <input
                  className={styles.input}
                  value={form.name}
                  onChange={e => set("name", e.target.value)}
                />
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Email Address</label>
              <div className={styles.inputWrap}>
                <Mail size={14} className={styles.inputIcon} />
                <input
                  className={styles.input}
                  type="email"
                  value={form.email}
                  onChange={e => set("email", e.target.value)}
                />
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Phone Number</label>
              <div className={styles.inputWrap}>
                <span className={styles.flagIcon}>🇳🇬</span>
                <input
                  className={`${styles.input} ${styles.inputFlag}`}
                  type="tel"
                  value={form.phone}
                  onChange={e => set("phone", e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Account Settings card */}
      <div className={styles.sectionCard}>
        <div className={styles.cardHeadRow}>
          <h3 className={styles.sectionTitle}>Account Settings</h3>
          <div className={styles.cardHeadActions}>
            <button className={styles.saveBtnSm} onClick={handleSave}>Save Changes</button>
            <button className={styles.resetBtnSm} onClick={() => { setAccountActive(true); setMaintenance(false); }}>Reset</button>
          </div>
        </div>

        <div className={styles.togglesRow}>
          <div className={styles.toggleGroup}>
            <span className={styles.toggleLabel}>Account Status</span>
            <label className={styles.toggleSwitch}>
              <input type="checkbox" checked={accountActive} onChange={e => setAccountActive(e.target.checked)} />
              <span className={styles.toggleSlider} />
            </label>
            <span className={`${styles.toggleValue} ${accountActive ? styles.toggleOn : styles.toggleOff}`}>
              {accountActive ? "Active" : "Inactive"}
            </span>
          </div>

          <div className={styles.toggleGroup}>
            <span className={styles.toggleLabel}>Maintenance Mode</span>
            <label className={styles.toggleSwitch}>
              <input type="checkbox" checked={maintenance} onChange={e => setMaintenance(e.target.checked)} />
              <span className={styles.toggleSlider} />
            </label>
            <span className={`${styles.toggleValue} ${maintenance ? styles.toggleOn : styles.toggleOff}`}>
              {maintenance ? "Active" : "Inactive"}
            </span>
          </div>
        </div>
      </div>

    </div>
  );
}