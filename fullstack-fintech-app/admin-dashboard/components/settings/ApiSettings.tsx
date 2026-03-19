"use client";

import { useState, useEffect } from "react";
import { Copy, RefreshCw, Eye, EyeOff, CheckCircle, Plus, Trash2 } from "lucide-react";
import styles from "./Settings.module.css";

interface Props { saveKey: number; resetKey: number; }

const MOCK_KEY   = "epay_live_sk_9f3a2b1c8d7e6f5a4b3c2d1e0f";
const MOCK_PUB   = "epay_pub_pk_1a2b3c4d5e6f7a8b9c0d1e2f3a";
const MOCK_WEBHOOK = "https://yoursite.com/webhooks/epay";

export default function ApiSettings({ saveKey, resetKey }: Props) {
  const [showKey,   setShowKey]   = useState(false);
  const [webhook,   setWebhook]   = useState(MOCK_WEBHOOK);
  const [copied,    setCopied]    = useState<string | null>(null);
  const [saved,     setSaved]     = useState(false);
  const [ipList,    setIpList]    = useState(["192.168.1.1", "10.0.0.2"]);
  const [newIp,     setNewIp]     = useState("");

  useEffect(() => { if (saveKey  > 0) handleSave();          }, [saveKey]);
  useEffect(() => { if (resetKey > 0) setWebhook(MOCK_WEBHOOK); }, [resetKey]);

  const handleSave = () => { setSaved(true); setTimeout(() => setSaved(false), 2500); };

  const copy = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopied(label);
    setTimeout(() => setCopied(null), 1800);
  };

  const addIp = () => {
    if (newIp.trim() && !ipList.includes(newIp.trim())) {
      setIpList(l => [...l, newIp.trim()]);
      setNewIp("");
    }
  };

  return (
    <div className={styles.stack}>
      {saved && <div className={styles.toast}><CheckCircle size={16} /> API settings saved!</div>}

      <div className={styles.twoColGrid}>

        {/* Keys */}
        <div className={styles.sectionCard}>
          <h3 className={styles.sectionTitle}>API Keys</h3>
          <p className={styles.sectionDesc}>Use these keys to authenticate API requests.</p>

          {/* Live secret key */}
          <div className={styles.formGroup}>
            <label className={styles.label}>Live Secret Key</label>
            <div className={styles.keyRow}>
              <div className={styles.keyBox}>
                <span className={styles.keyText}>
                  {showKey ? MOCK_KEY : "•".repeat(32)}
                </span>
              </div>
              <button className={styles.iconBtn} onClick={() => setShowKey(s => !s)}>
                {showKey ? <EyeOff size={15} /> : <Eye size={15} />}
              </button>
              <button className={styles.iconBtn} onClick={() => copy(MOCK_KEY, "secret")}>
                {copied === "secret" ? <CheckCircle size={15} color="var(--success)" /> : <Copy size={15} />}
              </button>
              <button className={styles.iconBtnWarn}>
                <RefreshCw size={15} />
              </button>
            </div>
          </div>

          {/* Public key */}
          <div className={styles.formGroup}>
            <label className={styles.label}>Public Key</label>
            <div className={styles.keyRow}>
              <div className={styles.keyBox}>
                <span className={styles.keyText}>{MOCK_PUB}</span>
              </div>
              <button className={styles.iconBtn} onClick={() => copy(MOCK_PUB, "pub")}>
                {copied === "pub" ? <CheckCircle size={15} color="var(--success)" /> : <Copy size={15} />}
              </button>
            </div>
          </div>

          {/* Webhook */}
          <div className={styles.formGroup}>
            <label className={styles.label}>Webhook URL</label>
            <div className={styles.inputWrap}>
              <input
                className={styles.input}
                value={webhook}
                onChange={e => setWebhook(e.target.value)}
                style={{ paddingLeft: "12px" }}
                placeholder="https://yoursite.com/webhook"
              />
            </div>
          </div>

          <button className={styles.saveBtnFull} onClick={handleSave}>Save API Settings</button>
        </div>

        {/* IP Whitelist */}
        <div className={styles.sectionCard}>
          <h3 className={styles.sectionTitle}>IP Whitelist</h3>
          <p className={styles.sectionDesc}>Only whitelisted IPs can make API calls.</p>

          <div className={styles.ipList}>
            {ipList.map(ip => (
              <div key={ip} className={styles.ipRow}>
                <span className={styles.ipText}>{ip}</span>
                <button
                  className={styles.iconBtnDanger}
                  onClick={() => setIpList(l => l.filter(x => x !== ip))}
                >
                  <Trash2 size={14} />
                </button>
              </div>
            ))}
          </div>

          <div className={styles.addIpRow}>
            <input
              className={styles.input}
              value={newIp}
              onChange={e => setNewIp(e.target.value)}
              onKeyDown={e => e.key === "Enter" && addIp()}
              placeholder="e.g. 203.0.113.0"
              style={{ paddingLeft: "12px" }}
            />
            <button className={styles.addIpBtn} onClick={addIp}>
              <Plus size={15} /> Add
            </button>
          </div>

          <div className={`${styles.sectionCard} ${styles.infoBox}`}>
            <p className={styles.infoText}>
              <strong>Note:</strong> Leave empty to allow all IPs. Changes take effect within 5 minutes.
            </p>
          </div>
        </div>

      </div>
    </div>
  );
}