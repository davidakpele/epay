"use client";

import { useState, useEffect } from "react";
import { CheckCircle } from "lucide-react";
import styles from "./Settings.module.css";

interface Props { saveKey: number; resetKey: number; }

const DEFAULTS = {
  newUser:     { email: true,  sms: false, push: true  },
  kyc:         { email: true,  sms: true,  push: true  },
  transaction: { email: true,  sms: false, push: false },
  suspicious:  { email: true,  sms: true,  push: true  },
  system:      { email: false, sms: false, push: true  },
  report:      { email: true,  sms: false, push: false },
};

type NotifKey = keyof typeof DEFAULTS;
type Channel  = "email" | "sms" | "push";

const LABELS: Record<NotifKey, string> = {
  newUser:     "New User Registration",
  kyc:         "KYC Verification Updates",
  transaction: "Transaction Alerts",
  suspicious:  "Suspicious Activity",
  system:      "System Updates",
  report:      "Weekly Reports",
};

export default function NotificationSettings({ saveKey, resetKey }: Props) {
  const [settings, setSettings] = useState(DEFAULTS);
  const [saved, setSaved] = useState(false);

  useEffect(() => { if (saveKey  > 0) handleSave();             }, [saveKey]);
  useEffect(() => { if (resetKey > 0) setSettings(DEFAULTS);    }, [resetKey]);

  const handleSave = () => { setSaved(true); setTimeout(() => setSaved(false), 2500); };

  const toggle = (key: NotifKey, ch: Channel) => {
    setSettings(s => ({ ...s, [key]: { ...s[key], [ch]: !s[key][ch] } }));
  };

  return (
    <div className={styles.stack}>
      {saved && <div className={styles.toast}><CheckCircle size={16} /> Notification settings saved!</div>}

      <div className={styles.sectionCard}>
        <h3 className={styles.sectionTitle}>Notification Preferences</h3>
        <p className={styles.sectionDesc}>Choose how you receive notifications for each event.</p>

        <div className={styles.notifTable}>
          {/* Header */}
          <div className={`${styles.notifRow} ${styles.notifHeader}`}>
            <span className={styles.notifEventLabel}>Event</span>
            <span className={styles.notifCh}>Email</span>
            <span className={styles.notifCh}>SMS</span>
            <span className={styles.notifCh}>Push</span>
          </div>

          {(Object.keys(LABELS) as NotifKey[]).map(key => (
            <div key={key} className={styles.notifRow}>
              <span className={styles.notifEventLabel}>{LABELS[key]}</span>
              {(["email","sms","push"] as Channel[]).map(ch => (
                <div key={ch} className={styles.notifCh}>
                  <label className={styles.toggleSwitch}>
                    <input
                      type="checkbox"
                      checked={settings[key][ch]}
                      onChange={() => toggle(key, ch)}
                    />
                    <span className={styles.toggleSlider} />
                  </label>
                  <span className={`${styles.toggleValue} ${settings[key][ch] ? styles.toggleOn : styles.toggleOff}`}>
                    {settings[key][ch] ? "On" : "Off"}
                  </span>
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}