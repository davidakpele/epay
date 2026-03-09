"use client";

import { useState, useRef, useEffect } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles  from "./page.module.css";
import {
  Search, Download, ChevronDown, ChevronLeft, ChevronRight,
  X, Code2, Shield, User, Settings, DollarSign, FileText,
  LogIn, UserX, Key, RefreshCw, AlertTriangle, Lock,
} from "lucide-react";

type ActionType =
  | "BALANCE_ADJUSTMENT"   | "ACCOUNT_SUSPENSION"  | "FEE_CONFIG_CHANGE"
  | "DAILY_RECONCILIATION" | "USER_LOGIN"          | "USER_DEACTIVATED"
  | "TRANSACTION_EDITED"   | "PASSWORD_CHANGED"    | "REPORT_GENERATED"
  | "KYC_STATUS_UPDATED"   | "USER_CREATED"        | "PERMISSION_CHANGED"
  | "WITHDRAWAL_APPROVED"  | "LOAN_APPROVED"       | "SYSTEM_CONFIG";

interface LogEntry {
  id:          string;
  timestamp:   string;       // e.g. "Dec 26, 2025"
  time:        string;       // e.g. "05:42:10 AM"
  actorName:   string;
  actorInitials:string;
  actorId:     string;
  actorColor:  string;
  actionType:  ActionType;
  description: string;       // may include **bold** markup
  reason?:     string;
  ip:          string;
  location:    string;
  details:     Record<string, unknown>;
  severity:    "info" | "warning" | "critical";
}

const ACTION_ICONS: Record<ActionType, React.ComponentType<{size?:number;className?:string}>> = {
  BALANCE_ADJUSTMENT:   DollarSign,
  ACCOUNT_SUSPENSION:   Lock,
  FEE_CONFIG_CHANGE:    Settings,
  DAILY_RECONCILIATION: RefreshCw,
  USER_LOGIN:           LogIn,
  USER_DEACTIVATED:     UserX,
  TRANSACTION_EDITED:   FileText,
  PASSWORD_CHANGED:     Key,
  REPORT_GENERATED:     FileText,
  KYC_STATUS_UPDATED:   Shield,
  USER_CREATED:         User,
  PERMISSION_CHANGED:   Settings,
  WITHDRAWAL_APPROVED:  DollarSign,
  LOAN_APPROVED:        DollarSign,
  SYSTEM_CONFIG:        Settings,
};

const ACTION_COLORS: Record<ActionType, {bg:string;color:string}> = {
  BALANCE_ADJUSTMENT:   {bg:"#fff8e8",color:"#c8a84b"},
  ACCOUNT_SUSPENSION:   {bg:"#fff0f0",color:"#c0392b"},
  FEE_CONFIG_CHANGE:    {bg:"#eef0ff",color:"#3a5abf"},
  DAILY_RECONCILIATION: {bg:"#e8f5e8",color:"#166701"},
  USER_LOGIN:           {bg:"#e8f5e8",color:"#166701"},
  USER_DEACTIVATED:     {bg:"#fff0f0",color:"#c0392b"},
  TRANSACTION_EDITED:   {bg:"#eef0ff",color:"#3a5abf"},
  PASSWORD_CHANGED:     {bg:"#fff8e8",color:"#c8a84b"},
  REPORT_GENERATED:     {bg:"#e8f5e8",color:"#166701"},
  KYC_STATUS_UPDATED:   {bg:"#e8f0ff",color:"#3a5abf"},
  USER_CREATED:         {bg:"#e8f5e8",color:"#166701"},
  PERMISSION_CHANGED:   {bg:"#fff8e8",color:"#c8a84b"},
  WITHDRAWAL_APPROVED:  {bg:"#e8f5e8",color:"#166701"},
  LOAN_APPROVED:        {bg:"#e8f5e8",color:"#166701"},
  SYSTEM_CONFIG:        {bg:"#eef0ff",color:"#3a5abf"},
};

const SEVERITY_CLS: Record<string,string> = {
  info:     "sevInfo",
  warning:  "sevWarning",
  critical: "sevCritical",
};

const LOGS: LogEntry[] = [
  {
    id:"AL001", timestamp:"Dec 26, 2025", time:"05:42:10 AM",
    actorName:"John_Super", actorInitials:"JS", actorId:"#ADM-001", actorColor:"#7a5abf",
    actionType:"BALANCE_ADJUSTMENT",
    description:'Manually increased **USD Wallet** for user **#USR-9920** by **$1,500.00**.',
    reason:"Resolution for stuck wire transfer.",
    ip:"192.168.1.44", location:"Lagos, NG",
    details:{"prev_bal":100.00,"new_bal":1600.00,"ticket_ref":"FIN-442"},
    severity:"warning",
  },
  {
    id:"AL002", timestamp:"Dec 26, 2025", time:"05:30:45 AM",
    actorName:"System_Auto", actorInitials:"SA", actorId:"#SYS-001", actorColor:"#bf5a5a",
    actionType:"ACCOUNT_SUSPENSION",
    description:'Account **#USR-8812** suspended due to multiple failed 2FA attempts.',
    ip:"Internal Worker", location:"",
    details:{"trigger":"BRUTE_FORCE","attempts":5,"geo_lock":"RU"},
    severity:"critical",
  },
  {
    id:"AL003", timestamp:"Dec 26, 2025", time:"04:15:22 AM",
    actorName:"Mark_Ops", actorInitials:"MM", actorId:"#ADM-007", actorColor:"#3a7abf",
    actionType:"FEE_CONFIG_CHANGE",
    description:'Updated **Card Issuance Fee** from **$2.00** to **$2.50**.',
    ip:"102.89.34.11", location:"London, UK",
    details:{"field":"virtual_card_fee","old":2.00,"new":2.50},
    severity:"warning",
  },
  {
    id:"AL004", timestamp:"Dec 26, 2025", time:"03:00:01 AM",
    actorName:"System", actorInitials:"S", actorId:"#SYS-000", actorColor:"#5a6a5a",
    actionType:"DAILY_RECONCILIATION",
    description:"Automated sync completed for all 4 fiat float accounts.",
    ip:"Cron-Job", location:"",
    details:{"status":"success","synced_accounts":["USD","NGN","EUR","AUD"]},
    severity:"info",
  },
  {
    id:"AL005", timestamp:"Apr 24, 2024", time:"3:20 PM",
    actorName:"David Alex", actorInitials:"DA", actorId:"#ADM-002", actorColor:"#166701",
    actionType:"USER_LOGIN",
    description:"Admin logged into the system.",
    ip:"192.168.1.10", location:"Abuja, NG",
    details:{"session_id":"SES-8812","device":"Chrome/Mac"},
    severity:"info",
  },
  {
    id:"AL006", timestamp:"Apr 24, 2024", time:"2:45 PM",
    actorName:"David Alex", actorInitials:"DA", actorId:"#ADM-002", actorColor:"#166701",
    actionType:"USER_DEACTIVATED",
    description:"Deactivated user **John Doe** (#USR-4421).",
    ip:"192.168.1.10", location:"Abuja, NG",
    details:{"user_id":"USR-4421","reason":"Policy violation"},
    severity:"warning",
  },
  {
    id:"AL007", timestamp:"Apr 24, 2024", time:"2:10 PM",
    actorName:"David Alex", actorInitials:"DA", actorId:"#ADM-002", actorColor:"#166701",
    actionType:"TRANSACTION_EDITED",
    description:"Edited transaction **TXN-8817**. Amount corrected from **₦4,800** to **₦5,000**.",
    ip:"192.168.1.10", location:"Abuja, NG",
    details:{"txn_id":"TXN-8817","old_amount":4800,"new_amount":5000},
    severity:"warning",
  },
  {
    id:"AL008", timestamp:"Apr 24, 2024", time:"1:50 PM",
    actorName:"David Alex", actorInitials:"DA", actorId:"#ADM-002", actorColor:"#166701",
    actionType:"PASSWORD_CHANGED",
    description:"Admin password changed successfully.",
    ip:"192.168.1.10", location:"Abuja, NG",
    details:{"method":"manual_reset","2fa_required":true},
    severity:"info",
  },
  {
    id:"AL009", timestamp:"Apr 24, 2024", time:"1:45 PM",
    actorName:"David Alex", actorInitials:"DA", actorId:"#ADM-002", actorColor:"#166701",
    actionType:"REPORT_GENERATED",
    description:"Published monthly **Financial Report** for March 2024.",
    ip:"192.168.1.15", location:"Abuja, NG",
    details:{"report_id":"RPT-0324","format":"PDF","size_kb":1240},
    severity:"info",
  },
  {
    id:"AL010", timestamp:"Apr 24, 2024", time:"1:20 PM",
    actorName:"Sarah Collins", actorInitials:"SC", actorId:"#ADM-005", actorColor:"#5aaa7a",
    actionType:"BALANCE_ADJUSTMENT",
    description:"Total balance adjusted by **₦3,600** for user **#USR-2210**.",
    ip:"192.168.1.10", location:"Lagos, NG",
    details:{"prev_bal":12000,"new_bal":15600,"ref":"OPS-331"},
    severity:"warning",
  },
  {
    id:"AL011", timestamp:"Apr 24, 2024", time:"1:20 PM",
    actorName:"Sarah Collins", actorInitials:"SC", actorId:"#ADM-005", actorColor:"#5aaa7a",
    actionType:"BALANCE_ADJUSTMENT",
    description:"Total **₦5,500** adjusted for **₦5,000** settlement.",
    ip:"192.168.1.15", location:"Lagos, NG",
    details:{"prev_bal":5000,"new_bal":10500,"settlement_ref":"STL-889"},
    severity:"warning",
  },
  {
    id:"AL012", timestamp:"Apr 24, 2024", time:"1:15 PM",
    actorName:"Sarah Collins", actorInitials:"SC", actorId:"#ADM-005", actorColor:"#5aaa7a",
    actionType:"KYC_STATUS_UPDATED",
    description:"KYC status updated to **Verified** for user **#USR-3398**.",
    ip:"192.168.1.15", location:"Lagos, NG",
    details:{"prev_status":"Pending","new_status":"Verified","doc_ref":"KYC-3398"},
    severity:"info",
  },
  {
    id:"AL013", timestamp:"Apr 24, 2024", time:"1:15 PM",
    actorName:"Sarah Collins", actorInitials:"SC", actorId:"#ADM-005", actorColor:"#5aaa7a",
    actionType:"KYC_STATUS_UPDATED",
    description:"Initiated KYC re-verification for user **$M68** — additional docs required.",
    ip:"192.168.1.15", location:"Lagos, NG",
    details:{"user_ref":"$M68","trigger":"doc_expired","expiry":"2024-03-01"},
    severity:"warning",
  },
  {
    id:"AL014", timestamp:"Apr 23, 2024", time:"11:30 AM",
    actorName:"System_Auto", actorInitials:"SA", actorId:"#SYS-001", actorColor:"#bf5a5a",
    actionType:"ACCOUNT_SUSPENSION",
    description:"Account **#USR-7741** auto-suspended after 3 failed PIN attempts.",
    ip:"Internal Worker", location:"",
    details:{"trigger":"PIN_FAIL","attempts":3,"user_id":"USR-7741"},
    severity:"critical",
  },
  {
    id:"AL015", timestamp:"Apr 23, 2024", time:"10:05 AM",
    actorName:"Mark_Ops", actorInitials:"MM", actorId:"#ADM-007", actorColor:"#3a7abf",
    actionType:"PERMISSION_CHANGED",
    description:"Admin role updated for **#ADM-009** — promoted to **Super Admin**.",
    ip:"102.89.34.11", location:"London, UK",
    details:{"from":"admin","to":"super_admin","target":"ADM-009"},
    severity:"critical",
  },
  {
    id:"AL016", timestamp:"Apr 23, 2024", time:"9:15 AM",
    actorName:"David Alex", actorInitials:"DA", actorId:"#ADM-002", actorColor:"#166701",
    actionType:"LOAN_APPROVED",
    description:"Loan **#LN-2240** approved for user **#USR-1182** — **₦750,000**.",
    ip:"192.168.1.10", location:"Abuja, NG",
    details:{"loan_id":"LN-2240","amount":750000,"duration":"12 months"},
    severity:"info",
  },
  {
    id:"AL017", timestamp:"Apr 22, 2024", time:"4:50 PM",
    actorName:"System", actorInitials:"S", actorId:"#SYS-000", actorColor:"#5a6a5a",
    actionType:"DAILY_RECONCILIATION",
    description:"End-of-day reconciliation completed. **₦42,800,000** balanced across accounts.",
    ip:"Cron-Job", location:"",
    details:{"status":"success","total":42800000,"discrepancies":0},
    severity:"info",
  },
  {
    id:"AL018", timestamp:"Apr 22, 2024", time:"2:30 PM",
    actorName:"Sarah Collins", actorInitials:"SC", actorId:"#ADM-005", actorColor:"#5aaa7a",
    actionType:"WITHDRAWAL_APPROVED",
    description:"Manual withdrawal approved for user **#USR-5540** — **₦200,000** to GTBank.",
    ip:"192.168.1.10", location:"Lagos, NG",
    details:{"bank":"GTBank","acct":"0123456789","amount":200000},
    severity:"info",
  },
  {
    id:"AL019", timestamp:"Apr 22, 2024", time:"11:00 AM",
    actorName:"Mark_Ops", actorInitials:"MM", actorId:"#ADM-007", actorColor:"#3a7abf",
    actionType:"SYSTEM_CONFIG",
    description:"Updated **NGN/USD exchange rate** from **1550** to **1580**.",
    ip:"102.89.34.11", location:"London, UK",
    details:{"param":"ngn_usd_rate","old":1550,"new":1580},
    severity:"warning",
  },
  {
    id:"AL020", timestamp:"Apr 21, 2024", time:"8:00 AM",
    actorName:"System_Auto", actorInitials:"SA", actorId:"#SYS-001", actorColor:"#bf5a5a",
    actionType:"FEE_CONFIG_CHANGE",
    description:"Transfer fee automatically updated to **1.5%** per system policy.",
    ip:"Internal Worker", location:"",
    details:{"fee_type":"transfer_pct","old":1.2,"new":1.5,"policy":"POL-004"},
    severity:"warning",
  },
];

const ACTION_TYPES = ["All","BALANCE_ADJUSTMENT","ACCOUNT_SUSPENSION","FEE_CONFIG_CHANGE","DAILY_RECONCILIATION","USER_LOGIN","USER_DEACTIVATED","TRANSACTION_EDITED","PASSWORD_CHANGED","REPORT_GENERATED","KYC_STATUS_UPDATED","USER_CREATED","PERMISSION_CHANGED","WITHDRAWAL_APPROVED","LOAN_APPROVED","SYSTEM_CONFIG"];
const ADMINS       = ["All","David Alex","Sarah Collins","Mark_Ops","John_Super","System","System_Auto"];
const DATE_RANGES  = ["Last 7 Days","Last 30 Days","Last 90 Days","This Year","All Time"];
const PER_PAGE_OPTS= [10,25,50,100];

/* ── Render description with **bold** markup ── */
function RichText({ text }: { text: string }) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g);
  return (
    <>
      {parts.map((p, i) =>
        p.startsWith("**") && p.endsWith("**")
          ? <strong key={i}>{p.slice(2,-2)}</strong>
          : <span key={i}>{p}</span>
      )}
    </>
  );
}

/* ── Details popover ── */
function DetailsPopover({ data, onClose }: { data: Record<string,unknown>; onClose:()=>void }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(()=>{
    const h=(e:MouseEvent)=>{ if(ref.current&&!ref.current.contains(e.target as Node)) onClose(); };
    document.addEventListener("mousedown",h);
    return ()=>document.removeEventListener("mousedown",h);
  },[onClose]);
  return (
    <div className={styles.detailsPop} ref={ref}>
      <div className={styles.detailsPopHead}>
        <Code2 size={12}/> View Data
        <button className={styles.detailsPopClose} onClick={onClose}><X size={12}/></button>
      </div>
      <pre className={styles.detailsJson}>{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}

/* ══════════════════ PAGE ══════════════════ */
export default function AuditLogsPage() {
  const [sidebarOpen,  setSidebarOpen]  = useState(false);
  const [search,       setSearch]       = useState("");
  const [actionFilter, setActionFilter] = useState("All");
  const [adminFilter,  setAdminFilter]  = useState("All");
  const [dateRange,    setDateRange]    = useState("Last 30 Days");
  const [page,         setPage]         = useState(1);
  const [perPage,      setPerPage]      = useState(25);
  const [openDetails,  setOpenDetails]  = useState<string|null>(null);

  const filtered = LOGS.filter(l=>{
    const matchSearch = search==="" ||
      l.actorName.toLowerCase().includes(search.toLowerCase()) ||
      l.actionType.toLowerCase().includes(search.toLowerCase()) ||
      l.description.toLowerCase().includes(search.toLowerCase()) ||
      l.ip.toLowerCase().includes(search.toLowerCase());
    const matchAction = actionFilter==="All" || l.actionType===actionFilter;
    const matchAdmin  = adminFilter==="All"  || l.actorName.includes(adminFilter);
    return matchSearch && matchAction && matchAdmin;
  });

  const totalPages = Math.max(1,Math.ceil(filtered.length/perPage));
  const safePage   = Math.min(page,totalPages);
  const start      = (safePage-1)*perPage;
  const rows       = filtered.slice(start,start+perPage);

  const goTo = (p:number)=>{ setPage(Math.max(1,Math.min(totalPages,p))); };

  return (
    <div>
      <Header onMenuToggle={()=>setSidebarOpen(p=>!p)}/>
      <Sidebar isOpen={sidebarOpen} onClose={()=>setSidebarOpen(false)}/>

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer}/>

        <main className={styles.main}>

          {/* Title */}
          <div className={styles.titleBlock}>
            <h1 className={styles.pageTitle}>Audit Logs</h1>
            <p className={styles.pageSubtitle}>System Audit Logs. Immutable history of all administrative and system-level actions.</p>
          </div>

          {/* Filters row */}
          <div className={styles.filtersRow}>
            <div className={styles.filterGroup}>

              <div className={styles.filterItem}>
                <label className={styles.filterLabel}>Action Type</label>
                <div className={styles.selWrap}>
                  <select className={styles.sel} value={actionFilter} onChange={e=>{setActionFilter(e.target.value);setPage(1);}}>
                    {ACTION_TYPES.map(a=><option key={a}>{a}</option>)}
                  </select>
                  <ChevronDown size={12} className={styles.selChev}/>
                </div>
              </div>

              <div className={styles.filterItem}>
                <label className={styles.filterLabel}>Administrator</label>
                <div className={styles.selWrap}>
                  <select className={styles.sel} value={adminFilter} onChange={e=>{setAdminFilter(e.target.value);setPage(1);}}>
                    {ADMINS.map(a=><option key={a}>{a}</option>)}
                  </select>
                  <ChevronDown size={12} className={styles.selChev}/>
                </div>
              </div>

              <div className={styles.filterItem}>
                <label className={styles.filterLabel}>Date Range</label>
                <div className={styles.selWrap}>
                  <select className={styles.sel} value={dateRange} onChange={e=>setDateRange(e.target.value)}>
                    {DATE_RANGES.map(d=><option key={d}>{d}</option>)}
                  </select>
                  <ChevronDown size={12} className={styles.selChev}/>
                </div>
              </div>

              <div className={styles.searchWrap}>
                <Search size={14} className={styles.searchIcon}/>
                <input
                  className={styles.searchInput}
                  placeholder="Search logs..."
                  value={search}
                  onChange={e=>{setSearch(e.target.value);setPage(1);}}
                />
              </div>

            </div>

            <button className={styles.exportBtn}>
              <Download size={14}/> Export CSV
            </button>
          </div>

          {/* Table */}
          <div className={styles.tableCard}>
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th className={styles.thNum}>S/N</th>
                    <th>Timestamp</th>
                    <th>Actor</th>
                    <th>Action &amp; Description</th>
                    <th>IP Address</th>
                    <th>Severity</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((log, idx)=>{
                    const Icon   = ACTION_ICONS[log.actionType];
                    const aColor = ACTION_COLORS[log.actionType];
                    return (
                      <tr key={log.id} className={log.severity==="critical"?styles.rowCritical:log.severity==="warning"?styles.rowWarning:""}>

                        {/* S/N */}
                        <td className={styles.tdNum}>{start+idx+1}.</td>

                        {/* Timestamp */}
                        <td className={styles.tdTimestamp}>
                          <span className={styles.tsDate}>{log.timestamp}</span>
                          <span className={styles.tsTime}>{log.time}</span>
                        </td>

                        {/* Actor */}
                        <td className={styles.tdActor}>
                          <div className={styles.actorRow}>
                            <div className={styles.actorInitials} style={{background:log.actorColor}}>{log.actorInitials}</div>
                            <div>
                              <span className={styles.actorName}>{log.actorName}</span>
                              <span className={styles.actorId}>{log.actorId}</span>
                            </div>
                          </div>
                        </td>

                        {/* Action + Description */}
                        <td className={styles.tdAction}>
                          <div className={styles.actionTypeRow}>
                            <span className={styles.actionIcon} style={{background:aColor.bg,color:aColor.color}}>
                              <Icon size={12}/>
                            </span>
                            <span className={styles.actionTypeLabel} style={{color:aColor.color}}>
                              {log.actionType}
                            </span>
                          </div>
                          <p className={styles.actionDesc}>
                            <RichText text={log.description}/>
                          </p>
                          {log.reason&&(
                            <p className={styles.actionReason}>Reason: {log.reason}</p>
                          )}
                        </td>

                        {/* IP Address */}
                        <td className={styles.tdIp}>
                          <span className={styles.ipAddr}>{log.ip}</span>
                          {log.location&&<span className={styles.ipLocation}>{log.location}</span>}
                        </td>

                        {/* Severity */}
                        <td>
                          <span className={`${styles.sevBadge} ${styles[SEVERITY_CLS[log.severity]]}`}>
                            {log.severity==="critical"&&<AlertTriangle size={10}/>}
                            {log.severity.charAt(0).toUpperCase()+log.severity.slice(1)}
                          </span>
                        </td>

                        {/* Details */}
                        <td className={styles.tdDetails}>
                          <div className={styles.detailsWrap}>
                            <button
                              className={styles.viewDataBtn}
                              onClick={()=>setOpenDetails(openDetails===log.id?null:log.id)}
                            >
                              <Code2 size={12}/> View Data
                            </button>
                            {openDetails===log.id&&(
                              <DetailsPopover data={log.details} onClose={()=>setOpenDetails(null)}/>
                            )}
                          </div>
                        </td>

                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className={styles.pagination}>
              <span className={styles.showingLabel}>
                Showing {filtered.length===0?0:start+1} to {Math.min(start+perPage,filtered.length)} of {filtered.length} entries
              </span>
              <div className={styles.pageControls}>
                <button className={styles.pageBtn} onClick={()=>goTo(safePage-1)} disabled={safePage===1}>
                  <ChevronLeft size={13}/> Prev
                </button>

                {Array.from({length:Math.min(5,totalPages)},(_,i)=>{
                  let p: number;
                  if(totalPages<=5)               p=i+1;
                  else if(safePage<=3)            p=i+1;
                  else if(safePage>=totalPages-2) p=totalPages-4+i;
                  else                            p=safePage-2+i;
                  return (
                    <button key={p} className={`${styles.pageNumBtn} ${safePage===p?styles.pageNumActive:""}`} onClick={()=>goTo(p)}>{p}</button>
                  );
                })}

                {totalPages>5&&safePage<totalPages-2&&<span className={styles.ellipsis}>...</span>}
                {totalPages>5&&safePage<totalPages-1&&(
                  <button className={styles.pageNumBtn} onClick={()=>goTo(totalPages)}>{totalPages}</button>
                )}

                <button className={styles.pageBtn} onClick={()=>goTo(safePage+1)} disabled={safePage===totalPages}>
                  Next <ChevronRight size={13}/>
                </button>

                <div className={styles.perPageWrap}>
                  <select className={styles.perPageSel} value={perPage} onChange={e=>{setPerPage(Number(e.target.value));setPage(1);}}>
                    {PER_PAGE_OPTS.map(o=><option key={o} value={o}>{o} / page</option>)}
                  </select>
                  <ChevronDown size={11} className={styles.perPageChev}/>
                </div>
              </div>
            </div>
          </div>

        </main>
      </div>
    </div>
  );
}