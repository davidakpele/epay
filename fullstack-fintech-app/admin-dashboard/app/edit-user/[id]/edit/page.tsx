"use client";

import { useState } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles from "./Page.module.css";
import {
  UserCircle, Shield, Lock, CreditCard,
  ShieldAlert, CheckCircle, ChevronRight,
  Phone, Mail, AtSign, Pencil,
  Smartphone, Laptop, Wallet,
  ArrowDownToLine, ArrowUpFromLine,
  ArrowRightLeft, Hash, Globe,
  StickyNote, AlertCircle,
} from "lucide-react";

const USER = {
  id:         "USR123456",
  name:       "David Alex",
  internalId: "USRT79000",
  username:   "david_alex",
  email:      "david.alex@email.com",
  phone:      "+234 801 234 5678",
  kyc:        "Verified",
  balance:    "₦150,000",
};

const IDENTITY = [
  { label: "Wallet ID: WAL123456", value: "WAL123456" },
  { label: "Wallet Balance:",       value: "₦150,000"  },
  { label: "Available Balance:",    value: "₦150,000"  },
  { label: "Locked Balance:",       value: "₦0"        },
  { label: "Currency:",             value: "NGN"       },
  { label: "Wallet Status:",        value: "Active"    },
];

const WALLET_BALANCE = [
  { Icon: Hash,           label: "Wallet ID:",      value: "₦150,000", iconCls: "wi_card"   },
  { Icon: Wallet,         label: "Wallet Balance:", value: "₦150,000", iconCls: "wi_wallet" },
  { Icon: Lock,           label: "Locked Balance:", value: "₦0",       iconCls: "wi_lock"   },
  { Icon: Globe,          label: "Currency:",       value: "NGN",      iconCls: "wi_globe"  },
];

const TXN_OVERVIEW = [
  { Icon: ArrowDownToLine, label: "Total Deposits",       value: "₦520,000", link: true,  iconCls: "txi_deposit"  },
  { Icon: ArrowUpFromLine, label: "Total Withdrawals",    value: "₦370,000", link: true,  iconCls: "txi_withdraw" },
  { Icon: ArrowRightLeft,  label: "Total Transfers Sent", value: "₦130,000", link: false, iconCls: "txi_transfer" },
  { Icon: Hash,            label: "Total Transactions:",  value: "72",       link: false, iconCls: "txi_txn"      },
];

const KYC_TXN = [
  { id: "TXN12345", type: "Bill Payment", amount: "₦5,000",   status: "Failed"     },
  { id: "TXN12344", type: "Deposit",      amount: "+₦75,000", status: "Successful" },
  { id: "TXN12343", type: "Virtual Card", amount: "-₦7,000",  status: "Successful" },
  { id: "TXN12342", type: "Exchange",     amount: "+₦40,000", status: "Successful" },
  { id: "TXN12341", type: "Ayo Ade",      amount: "+₦40,000", status: "Successful" },
];

const ACCOUNT_STATUS = [
  { label: "Account Status",            value: "Active",           link: false },
  { label: "KYC Status",                value: "Verified",         link: false },
  { label: "Account Tier / Level",      value: "Level 2 (Silver)", link: true  },
  { label: "Email Verification",        value: "Verified",         link: false },
  { label: "Phone Verification",        value: "Verified",         link: true  },
  { label: "Two-Factor Authentication", value: "Enabled",          link: true  },
];

const RECENT_TXN = [
  { label: "TXN ID",                    value: "₦520,000", link: true  },
  { label: "Total Withdrawals:",        value: "₦370,000", link: false },
  { label: "Total Transfers Sent:",     value: "₦130,000", link: false },
  { label: "Total Transfers Received:", value: "₦95,000",  link: false },
  { label: "Total Transactions:",       value: "72",       link: true  },
];

const DEVICES = [
  {
    IconComp: Smartphone,
    name:     "1 iPhone 13",
    location: "Lagos, Nigeria — Chrome",
    browser:  "Chrome · Apr 23, 2024 · 16:00",
  },
  {
    IconComp: Laptop,
    name:     "MacBook Pro · Lagos, Nigeria",
    location: "Lagos, Nigeria — Firefox",
    browser:  "Firefox · Apr 22, 2024 · 9:27 AM",
  },
];

const LIMITS = [
  { label: "Daily Transfer Limit",      value: "₦560,000"   },
  { label: "Daily Withdrawal Limit",    value: "₦200,000"   },
  { label: "Monthly Transaction Limit", value: "₦5,000,000" },
  { label: "Account Restrictions",      value: "None"       },
];

const ADMIN_NOTES = [
  { date: "Apr 21, 2024", note: "Verified, new address, documents,... — Admin" },
  { date: "Apr 15, 2024", note: "User reported issue with virtual card — Admin" },
];

/* ── Shared card header with edit pencil ── */
function CardHeader({ title }: { title: string }) {
  return (
    <div className={styles.cardHeader}>
      <h3 className={styles.cardTitle}>{title}</h3>
      <button className={styles.editBtn} title={`Edit ${title}`}>
        <Pencil size={13} />
      </button>
    </div>
  );
}

export default function UserProfilePage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [adminTab,    setAdminTab]    = useState<"support" | "complaints">("support");

  return (
    <div>
      <Header onMenuToggle={() => setSidebarOpen(p => !p)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />

        <main className={styles.main}>

          {/* ── Page header ── */}
          <div className={styles.pageHeader}>
            <div className={styles.pageTitleRow}>
              <UserCircle size={22} className={styles.pageTitleIcon} />
              <h1 className={styles.pageTitle}>User Profile</h1>
            </div>
            <div className={styles.headerActions}>
              <button className={`${styles.actionBtn} ${styles.actionBtnRed}`}>
                <ShieldAlert size={14} /> Suspend Account
              </button>
              <button className={`${styles.actionBtn} ${styles.actionBtnOrange}`}>
                <Lock size={14} /> Lock Account
              </button>
              <button className={`${styles.actionBtn} ${styles.actionBtnBlue}`}>
                <CreditCard size={14} /> Freeze Wallet
              </button>
            </div>
          </div>

          {/* ── Hero card ── */}
          <div className={styles.heroCard}>
            <div className={styles.heroLeft}>
              <div className={styles.avatarWrap}>
                <div className={styles.avatar}>DA</div>
                <span className={styles.avatarOnline} />
              </div>
              <div className={styles.heroInfo}>
                <span className={styles.heroUserId}>User ID: {USER.id}</span>
                <h2 className={styles.heroName}>{USER.name}</h2>
                <span className={styles.heroSubId}>ID: {USER.internalId}</span>
                <div className={styles.heroMeta}>
                  <span className={styles.heroMetaItem}><UserCircle size={13} />{USER.name}</span>
                  <span className={styles.heroMetaItem}><AtSign size={13} />{USER.username}</span>
                  <span className={styles.heroMetaItem}><Mail size={13} />{USER.email}</span>
                  <span className={styles.heroMetaItem}><Phone size={13} />{USER.phone}</span>
                </div>
              </div>
            </div>
            <div className={styles.heroRight}>
              <div className={styles.kycBadge}>
                <Shield size={14} />
                KYC {USER.kyc}
              </div>
              <div className={styles.accountStatusRow}>
                <span className={styles.accountStatusLabel}>Account status:</span>
                <span className={styles.accountStatusValue}>{USER.balance}</span>
              </div>
            </div>
          </div>

          {/* ── Body grid ── */}
          <div className={styles.grid}>

            {/* ════ LEFT COLUMN ════ */}
            <div className={styles.col}>

              {/* Identity Information */}
              <div className={styles.card}>
                <CardHeader title="Identity Information" />
                <div className={styles.infoList}>
                  {IDENTITY.map(row => (
                    <div key={row.label} className={styles.infoRow}>
                      <span className={styles.infoLabel}>{row.label}</span>
                      <span className={styles.infoValue}>{row.value}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Wallet / Account Balance */}
              <div className={styles.card}>
                <CardHeader title="Wallet / Account Balance" />
                <div className={styles.walletList}>
                  {WALLET_BALANCE.map(({ Icon, label, value, iconCls }) => (
                    <div key={label} className={styles.walletRow}>
                      <div className={styles.walletIconCell}>
                        <div className={`${styles.walletIcon} ${styles[iconCls]}`}>
                          <Icon size={14} />
                        </div>
                        <span className={styles.walletLabel}>{label}</span>
                      </div>
                      <span className={styles.walletValue}>{value}</span>
                    </div>
                  ))}
                </div>
                <div className={styles.walletActions}>
                  <button className={styles.walletActionBtn}>+ Add Bank Account</button>
                  <button className={styles.walletActionBtn}>+ Add Card / Account</button>
                </div>
              </div>

              {/* Transaction Overview */}
              <div className={styles.card}>
                <CardHeader title="Transaction Overview" />
                <div className={styles.txnOverList}>
                  {TXN_OVERVIEW.map(({ Icon, label, value, link, iconCls }) => (
                    <div key={label} className={styles.txnOverRow}>
                      <div className={styles.txnOverLeft}>
                        <div className={`${styles.txnOverIcon} ${styles[iconCls]}`}>
                          <Icon size={13} />
                        </div>
                        <span className={styles.txnOverLabel}>{label}</span>
                      </div>
                      <div className={styles.txnOverRight}>
                        <span className={styles.txnOverValue}>{value}</span>
                        {link && <ChevronRight size={14} className={styles.txnChev} />}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* KYC / Identity Verification */}
              <div className={styles.card}>
                <CardHeader title="KYC / Identity Verification" />
                <div className={styles.kycTableWrap}>
                  <table className={styles.kycTable}>
                    <thead>
                      <tr>
                        <th>TXN ID</th>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {KYC_TXN.map((txn, i) => (
                        <tr key={i}>
                          <td className={styles.kycId}>{txn.id}</td>
                          <td className={styles.kycType}>{txn.type}</td>
                          <td className={`${styles.kycAmt} ${
                            txn.amount.startsWith("+") ? styles.kycPos
                            : txn.amount.startsWith("-") ? styles.kycNeg : ""
                          }`}>
                            {txn.amount}
                          </td>
                          <td>
                            <span className={`${styles.kycBadge} ${
                              txn.status === "Failed" ? styles.kycFailed : styles.kycSuccess
                            }`}>
                              {txn.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <button className={styles.viewMoreBtn}>View More &nbsp;›</button>
              </div>

            </div>

            {/* ════ RIGHT COLUMN ════ */}
            <div className={styles.col}>

              {/* Account Status */}
              <div className={styles.card}>
                <CardHeader title="Account Status" />
                <div className={styles.statusList}>
                  {ACCOUNT_STATUS.map(row => (
                    <div key={row.label} className={styles.statusRow}>
                      <div className={styles.statusLeft}>
                        <CheckCircle size={15} className={styles.statusCheck} />
                        <span className={styles.statusLabel}>{row.label}</span>
                      </div>
                      <div className={styles.statusRight}>
                        <span className={styles.statusValue}>{row.value}</span>
                        {row.link && <ChevronRight size={14} className={styles.txnChev} />}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Recent Transactions */}
              <div className={styles.card}>
                <CardHeader title="Recent Transactions" />
                <div className={styles.infoList}>
                  {RECENT_TXN.map(row => (
                    <div key={row.label} className={styles.infoRow}>
                      <span className={styles.infoLabel}>{row.label}</span>
                      <div className={styles.recentRight}>
                        <span className={styles.infoValueBold}>{row.value}</span>
                        {row.link && <ChevronRight size={14} className={styles.txnChev} />}
                      </div>
                    </div>
                  ))}
                </div>
                <button className={styles.viewMoreBtn}>View More &nbsp;›</button>
              </div>

              {/* Devices & Security */}
              <div className={styles.card}>
                <CardHeader title="Devices & Security" />
                <div className={styles.deviceList}>
                  {DEVICES.map((d, i) => (
                    <div key={i} className={styles.deviceRow}>
                      <div className={styles.deviceIconWrap}>
                        <d.IconComp size={18} className={styles.deviceIcon} />
                      </div>
                      <div className={styles.deviceInfo}>
                        <span className={styles.deviceName}>{d.name}</span>
                        <span className={styles.deviceLocation}>{d.location}</span>
                        <span className={styles.deviceBrowser}>{d.browser}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Limits & Restrictions */}
              <div className={styles.card}>
                <CardHeader title="Limits & Restrictions" />
                <div className={styles.infoList}>
                  {LIMITS.map(row => (
                    <div key={row.label} className={styles.infoRow}>
                      <span className={styles.infoLabel}>{row.label}</span>
                      <span className={styles.infoValueBold}>{row.value}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Admin Notes */}
              <div className={styles.card}>
                <div className={styles.cardHeader}>
                  <div className={styles.adminTitleRow}>
                    <StickyNote size={15} className={styles.adminTitleIcon} />
                    <h3 className={styles.cardTitle}>Admin Notes</h3>
                  </div>
                  <button className={styles.editBtn} title="Edit Admin Notes">
                    <Pencil size={13} />
                  </button>
                </div>
                <div className={styles.adminNoteTabs}>
                  <button
                    className={`${styles.adminTab} ${adminTab === "support" ? styles.adminTabActive : ""}`}
                    onClick={() => setAdminTab("support")}
                  >
                    Support Stokes
                  </button>
                  <button
                    className={`${styles.adminTab} ${adminTab === "complaints" ? styles.adminTabActive : ""}`}
                    onClick={() => setAdminTab("complaints")}
                  >
                    Previous Complaints
                  </button>
                </div>
                <div className={styles.notesList}>
                  {adminTab === "support" ? ADMIN_NOTES.map((n, i) => (
                    <div key={i} className={styles.noteRow}>
                      <AlertCircle size={13} className={styles.noteIcon} />
                      <div className={styles.noteContent}>
                        <span className={styles.noteDate}>{n.date}</span>
                        <span className={styles.noteText}>{n.note}</span>
                      </div>
                    </div>
                  )) : (
                    <p className={styles.emptyNotes}>No previous complaints on record.</p>
                  )}
                </div>
              </div>

            </div>
          </div>
        </main>
      </div>
    </div>
  );
}