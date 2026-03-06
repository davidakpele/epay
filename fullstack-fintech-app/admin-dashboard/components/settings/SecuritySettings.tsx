"use client";

import { useState, useEffect } from "react";
import { Eye, EyeOff, CheckCircle } from "lucide-react";
import styles from "./Settings.module.css";

interface Props { saveKey: number; resetKey: number; }

export default function SecuritySettings({ saveKey, resetKey }: Props) {
  const [current,  setCurrent]  = useState("");
  const [next,     setNext]     = useState("");
  const [confirm,  setConfirm]  = useState("");
  const [show,     setShow]     = useState({ current: false, next: false, confirm: false });
  const [saved,    setSaved]    = useState(false);
  const [error,    setError]    = useState("");

  const [twoFA,        setTwoFA]        = useState({ email: true,  sms: true  });
  const [loginAlerts,  setLoginAlerts]  = useState({ email: true,  sms: false });
  const [sessionTimeout, setSessionTimeout] = useState("30");

  useEffect(() => { if (saveKey > 0) handleSave(); }, [saveKey]);
  useEffect(() => { if (resetKey > 0) { setCurrent(""); setNext(""); setConfirm(""); setError(""); }}, [resetKey]);

  const handleSave = () => {
    if (next && next !== confirm) { setError("Passwords do not match."); return; }
    if (next && next.length < 8)  { setError("Password must be at least 8 characters."); return; }
    setError("");
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  const toggleShow = (k: keyof typeof show) => setShow(s => ({ ...s, [k]: !s[k] }));

  return (
    <div className={styles.stack}>

      {saved && (
        <div className={styles.toast}><CheckCircle size={16} /> Security settings saved!</div>
      )}

      <div className={styles.twoColGrid}>

        {/* Password */}
        <div className={styles.sectionCard}>
          <h3 className={styles.sectionTitle}>Change Password</h3>

          {error && <div className={styles.errorBanner}>{error}</div>}

          {(["current","next","confirm"] as const).map((field) => {
            const labels = { current: "Current Password", next: "New Password", confirm: "Confirm New Password" };
            const values = { current, next, confirm };
            const setters = { current: setCurrent, next: setNext, confirm: setConfirm };
            return (
              <div className={styles.formGroup} key={field}>
                <label className={styles.label}>
                  {labels[field]} <span className={styles.required}>*</span>
                </label>
                <div className={styles.inputWrap}>
                  <input
                    className={styles.input}
                    type={show[field] ? "text" : "password"}
                    value={values[field]}
                    onChange={e => setters[field](e.target.value)}
                    placeholder="••••••••••"
                    style={{ paddingLeft: "12px", paddingRight: "38px" }}
                  />
                  <button className={styles.eyeBtn} onClick={() => toggleShow(field)}>
                    {show[field] ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
              </div>
            );
          })}

          <button className={styles.saveBtnFull} onClick={handleSave}>Update Password</button>
        </div>

        {/* 2FA + Alerts */}
        <div className={styles.sectionCard}>
          <h3 className={styles.sectionTitle}>Two-Factor Authentication</h3>

          <div className={styles.toggleTable}>
            <div className={styles.toggleTableRow}>
              <span className={styles.toggleTableLabel}>Email 2FA</span>
              <label className={styles.toggleSwitch}>
                <input type="checkbox" checked={twoFA.email} onChange={e => setTwoFA(t => ({ ...t, email: e.target.checked }))} />
                <span className={styles.toggleSlider} />
              </label>
              <span className={`${styles.toggleValue} ${twoFA.email ? styles.toggleOn : styles.toggleOff}`}>
                {twoFA.email ? "Enabled" : "Disabled"}
              </span>
            </div>
            <div className={styles.toggleTableRow}>
              <span className={styles.toggleTableLabel}>SMS 2FA</span>
              <label className={styles.toggleSwitch}>
                <input type="checkbox" checked={twoFA.sms} onChange={e => setTwoFA(t => ({ ...t, sms: e.target.checked }))} />
                <span className={styles.toggleSlider} />
              </label>
              <span className={`${styles.toggleValue} ${twoFA.sms ? styles.toggleOn : styles.toggleOff}`}>
                {twoFA.sms ? "Enabled" : "Disabled"}
              </span>
            </div>
          </div>

          <h3 className={`${styles.sectionTitle} ${styles.mt}`}>Login Alerts</h3>

          <div className={styles.toggleTable}>
            <div className={styles.toggleTableRow}>
              <span className={styles.toggleTableLabel}>Email Alerts</span>
              <label className={styles.toggleSwitch}>
                <input type="checkbox" checked={loginAlerts.email} onChange={e => setLoginAlerts(a => ({ ...a, email: e.target.checked }))} />
                <span className={styles.toggleSlider} />
              </label>
              <span className={`${styles.toggleValue} ${loginAlerts.email ? styles.toggleOn : styles.toggleOff}`}>
                {loginAlerts.email ? "Enabled" : "Disabled"}
              </span>
            </div>
            <div className={styles.toggleTableRow}>
              <span className={styles.toggleTableLabel}>SMS Alerts</span>
              <label className={styles.toggleSwitch}>
                <input type="checkbox" checked={loginAlerts.sms} onChange={e => setLoginAlerts(a => ({ ...a, sms: e.target.checked }))} />
                <span className={styles.toggleSlider} />
              </label>
              <span className={`${styles.toggleValue} ${loginAlerts.sms ? styles.toggleOn : styles.toggleOff}`}>
                {loginAlerts.sms ? "Enabled" : "Disabled"}
              </span>
            </div>
          </div>

          <h3 className={`${styles.sectionTitle} ${styles.mt}`}>Session Timeout</h3>
          <div className={styles.formGroup}>
            <label className={styles.label}>Auto-logout after (minutes)</label>
            <select className={styles.select} value={sessionTimeout} onChange={e => setSessionTimeout(e.target.value)}>
              {["15","30","60","120"].map(v => <option key={v} value={v}>{v} minutes</option>)}
            </select>
          </div>
        </div>

      </div>
    </div>
  );
}