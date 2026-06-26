"use client";

import { useState, useRef, useEffect } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles  from "./page.module.css";
import {
  MessageSquare, Search, ChevronDown, MoreVertical,
  Ticket, Eye, CheckCircle, XCircle, AlertTriangle,
  ChevronLeft, ChevronRight, X, Hash, Clock, Tag,
  AlertCircle, CheckSquare,
} from "lucide-react";

type Status   = "Pending" | "Open" | "Escalated" | "Resolved" | "Closed";
type Priority = "Low" | "Medium" | "High" | "Critical";

interface Query {
  id:           string;
  name:         string;
  initials:     string;
  color:        string;
  email:        string;
  issue:        string;
  subject:      string;
  status:       Status;
  time:         string;
  actionStatus: Status;
  actionTime:   string;
  ticketId?:    string;
}

interface TicketData {
  id:          string;
  queryId:     string;
  userName:    string;
  userEmail:   string;
  userInitials:string;
  userColor:   string;
  issue:       string;
  subject:     string;
  priority:    Priority;
  category:    string;
  notes:       string;
  status:      Status;
  createdAt:   string;
}

const ALL_QUERIES: Query[] = [
  { id:"Q001", name:"Olivia James",   initials:"OJ", color:"#c8a84b", email:"olivia.james@email.com",   issue:"Unauthorized debit",              subject:"Unauthorized debit on my account",        status:"Pending",   time:"5 mins ago",   actionStatus:"Pending",   actionTime:"5 mins ago"   },
  { id:"Q002", name:"David Alex",     initials:"DA", color:"#166701", email:"david.alex@email.com",     issue:"Account access",                  subject:"Can't log in to my account",              status:"Open",      time:"20 mins ago",  actionStatus:"Open",      actionTime:"20 mins ago"  },
  { id:"Q003", name:"John Carter",    initials:"JC", color:"#3a7abf", email:"john.carter@email.com",    issue:"Transaction error",               subject:"Transaction not reflecting in my balance", status:"Escalated", time:"45 mins ago",  actionStatus:"Escalated", actionTime:"45 mins ago"  },
  { id:"Q004", name:"Sarah Williams", initials:"SW", color:"#5aaa7a", email:"sarah.williams@email.com", issue:"Account verification requested",  subject:"Help with account verification",           status:"Open",      time:"1 hour ago",   actionStatus:"Open",      actionTime:"1 hour ago"   },
  { id:"Q005", name:"Michael Phan",   initials:"MP", color:"#7a5abf", email:"michael.phan@email.com",   issue:"Transaction declined",            subject:"Why was my transaction declined?",         status:"Pending",   time:"5 mins ago",   actionStatus:"Open",      actionTime:"2 hours ago"  },
  { id:"Q006", name:"Michael Phan",   initials:"MP", color:"#7a5abf", email:"michael.phan@email.com",   issue:"Transaction declined",            subject:"Why was my transaction declined?",         status:"Open",      time:"5 mins ago",   actionStatus:"Open",      actionTime:"2 hours ago"  },
  { id:"Q007", name:"Amara Okafor",   initials:"AO", color:"#bf5a5a", email:"amara.okafor@email.com",   issue:"Card blocked unexpectedly",       subject:"My virtual card was blocked without notice",status:"Escalated", time:"3 hours ago",  actionStatus:"Escalated", actionTime:"3 hours ago"  },
  { id:"Q008", name:"Priya Sharma",   initials:"PS", color:"#5a8abf", email:"priya.sharma@email.com",   issue:"Duplicate charge",                subject:"I was charged twice for one transaction",  status:"Pending",   time:"4 hours ago",  actionStatus:"Pending",   actionTime:"4 hours ago"  },
  { id:"Q009", name:"James Obi",      initials:"JO", color:"#aa6601", email:"james.obi@email.com",      issue:"KYC rejected",                    subject:"My KYC was rejected without explanation",  status:"Open",      time:"5 hours ago",  actionStatus:"Open",      actionTime:"5 hours ago"  },
  { id:"Q010", name:"Fatima Hassan",  initials:"FH", color:"#166701", email:"fatima.hassan@email.com",  issue:"Transfer failed",                 subject:"Transfer to GTBank failed, money deducted",status:"Resolved",  time:"6 hours ago",  actionStatus:"Resolved",  actionTime:"6 hours ago"  },
  { id:"Q011", name:"Chen Wei",       initials:"CW", color:"#3a7abf", email:"chen.wei@email.com",       issue:"Account locked",                  subject:"Account locked after wrong PIN attempt",   status:"Open",      time:"7 hours ago",  actionStatus:"Open",      actionTime:"7 hours ago"  },
  { id:"Q012", name:"Ada Eze",        initials:"AE", color:"#c8a84b", email:"ada.eze@email.com",        issue:"Wrong debit amount",              subject:"Debited ₦5000 instead of ₦500",           status:"Escalated", time:"8 hours ago",  actionStatus:"Escalated", actionTime:"8 hours ago"  },
  { id:"Q013", name:"Tom Benson",     initials:"TB", color:"#5aaa7a", email:"tom.benson@email.com",     issue:"App crash on login",              subject:"App keeps crashing when I open it",        status:"Pending",   time:"9 hours ago",  actionStatus:"Pending",   actionTime:"9 hours ago"  },
  { id:"Q014", name:"Nora Adeyemi",   initials:"NA", color:"#bf5a5a", email:"nora.adeyemi@email.com",   issue:"Referral bonus missing",          subject:"Referral bonus not credited after 7 days", status:"Open",      time:"10 hours ago", actionStatus:"Open",      actionTime:"10 hours ago" },
];

const STATUS_OPTS: Status[]   = ["Pending","Open","Escalated","Resolved","Closed"];
const PRIORITY_OPTS: Priority[]= ["Low","Medium","High","Critical"];
const CATEGORIES = ["Unauthorized debit","Account access","Transaction error","KYC","Card issue","Duplicate charge","Account locked","Transfer failed","App issue","Referral","Other"];
const PER_PAGE = 9;

const statusCls: Record<Status, string> = {
  Pending:  "sPending",
  Open:     "sOpen",
  Escalated:"sEscalated",
  Resolved: "sResolved",
  Closed:   "sClosed",
};

const priorityCls: Record<Priority, string> = {
  Low:      "pLow",
  Medium:   "pMedium",
  High:     "pHigh",
  Critical: "pCritical",
};

/* ── Create Ticket Modal ── */
function CreateTicketModal({
  query,
  onClose,
  onSave,
}: {
  query: Query | null;           // null = blank ticket (from Quick Actions)
  onClose: () => void;
  onSave: (t: TicketData) => void;
}) {
  const isLinked = !!query;
  const [form, setForm] = useState({
    userName:  query?.name    ?? "",
    userEmail: query?.email   ?? "",
    issue:     query?.issue   ?? "",
    subject:   query?.subject ?? "",
    priority:  "Medium" as Priority,
    category:  query?.issue   ?? CATEGORIES[0],
    notes:     "",
    status:    "Open" as Status,
  });
  const [success, setSuccess] = useState(false);
  const set = (k: string, v: string) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = () => {
    if (!form.subject.trim()) return;
    const ticket: TicketData = {
      id:           `TKT-${Date.now().toString().slice(-6)}`,
      queryId:      query?.id ?? "",
      userName:     form.userName,
      userEmail:    form.userEmail,
      userInitials: query?.initials ?? form.userName.split(" ").map(n=>n[0]).join("").slice(0,2).toUpperCase(),
      userColor:    query?.color    ?? "#166701",
      issue:        form.issue,
      subject:      form.subject,
      priority:     form.priority,
      category:     form.category,
      notes:        form.notes,
      status:       form.status,
      createdAt:    "Just now",
    };
    onSave(ticket);
    setSuccess(true);
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e=>e.stopPropagation()}>

        <div className={styles.modalHead}>
          <div className={styles.modalHeadLeft}>
            <div className={styles.modalIconWrap}><Ticket size={16}/></div>
            <h3 className={styles.modalTitle}>Create a Ticket</h3>
          </div>
          <button className={styles.modalClose} onClick={onClose}><X size={14}/></button>
        </div>

        {success ? (
          /* ── Success state ── */
          <div className={styles.successState}>
            <div className={styles.successIcon}><CheckSquare size={40}/></div>
            <h3 className={styles.successTitle}>Ticket Created!</h3>
            <p className={styles.successMsg}>
              Ticket has been successfully created{isLinked ? ` for ${query!.name}` : ""}.<br/>
              The support team will be notified immediately.
            </p>
            <button className={styles.doneBtn} onClick={onClose}>Done</button>
          </div>
        ) : (
          <>
            <div className={styles.modalBody}>

              {/* Linked query banner */}
              {isLinked && (
                <div className={styles.linkedBanner}>
                  <AlertCircle size={14}/>
                  <span>Linked to query <strong>{query!.id}</strong> — {query!.name}</span>
                </div>
              )}

              {/* User info — editable if blank ticket */}
              {!isLinked && (
                <div className={styles.formRow}>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>User Name</label>
                    <input className={styles.formInput} placeholder="e.g. John Doe" value={form.userName} onChange={e=>set("userName",e.target.value)}/>
                  </div>
                  <div className={styles.formGroup}>
                    <label className={styles.formLabel}>User Email</label>
                    <input className={styles.formInput} placeholder="user@email.com" value={form.userEmail} onChange={e=>set("userEmail",e.target.value)}/>
                  </div>
                </div>
              )}

              {/* User card (read-only when linked) */}
              {isLinked && (
                <div className={styles.userCard}>
                  <div className={styles.userCardAvatar} style={{background:query!.color}}>{query!.initials}</div>
                  <div>
                    <p className={styles.userCardName}>{query!.name}</p>
                    <p className={styles.userCardEmail}>{query!.email}</p>
                  </div>
                  <span className={`${styles.badge} ${styles[statusCls[query!.status]]}`}>{query!.status}</span>
                </div>
              )}

              {/* Subject */}
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Ticket Subject</label>
                <input
                  className={styles.formInput}
                  placeholder="Describe the problem briefly..."
                  value={form.subject}
                  onChange={e=>set("subject",e.target.value)}
                />
              </div>

              {/* Category + Priority */}
              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>Category</label>
                  <div className={styles.selWrapModal}>
                    <select className={styles.formSelect} value={form.category} onChange={e=>set("category",e.target.value)}>
                      {CATEGORIES.map(c=><option key={c}>{c}</option>)}
                    </select>
                    <ChevronDown size={12} className={styles.selChevModal}/>
                  </div>
                </div>
                <div className={styles.formGroup}>
                  <label className={styles.formLabel}>Priority</label>
                  <div className={styles.priorityChips}>
                    {PRIORITY_OPTS.map(p=>(
                      <button
                        key={p} type="button"
                        className={`${styles.priorityChip} ${form.priority===p?styles[`priorityChipActive_${p}`]:""}`}
                        onClick={()=>set("priority",p)}
                      >{p}</button>
                    ))}
                  </div>
                </div>
              </div>

              {/* Initial status */}
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Initial Status</label>
                <div className={styles.statusChips}>
                  {STATUS_OPTS.map(s=>(
                    <button
                      key={s} type="button"
                      className={`${styles.statusChip} ${form.status===s?styles.statusChipActive:""}`}
                      onClick={()=>set("status",s)}
                    >{s}</button>
                  ))}
                </div>
              </div>

              {/* Admin notes */}
              <div className={styles.formGroup}>
                <label className={styles.formLabel}>Admin Notes <span className={styles.optional}>(optional)</span></label>
                <textarea
                  className={styles.formTextarea}
                  rows={3}
                  placeholder="Add internal notes about this ticket..."
                  value={form.notes}
                  onChange={e=>set("notes",e.target.value)}
                />
              </div>

            </div>

            <div className={styles.modalFoot}>
              <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
              <button className={styles.createTicketBtn} onClick={handleSubmit} disabled={!form.subject.trim()}>
                <Ticket size={14}/> Create Ticket
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

/* ── Ticket Info Modal (view existing ticket) ── */
function TicketViewModal({
  ticket,
  onClose,
  onStatusChange,
}: {
  ticket: TicketData;
  onClose: () => void;
  onStatusChange: (id: string, s: Status) => void;
}) {
  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e=>e.stopPropagation()}>
        <div className={styles.modalHead}>
          <div className={styles.modalHeadLeft}>
            <div className={styles.modalIconWrap}><Ticket size={16}/></div>
            <div>
              <h3 className={styles.modalTitle}>Ticket Details</h3>
              <p className={styles.ticketIdLabel}>{ticket.id}</p>
            </div>
          </div>
          <button className={styles.modalClose} onClick={onClose}><X size={14}/></button>
        </div>

        <div className={styles.modalBody}>

          {/* User row */}
          <div className={styles.userCard}>
            <div className={styles.userCardAvatar} style={{background:ticket.userColor}}>{ticket.userInitials}</div>
            <div>
              <p className={styles.userCardName}>{ticket.userName}</p>
              <p className={styles.userCardEmail}>{ticket.userEmail}</p>
            </div>
          </div>

          {/* Meta pills */}
          <div className={styles.ticketMeta}>
            <span className={styles.metaPill}><Hash size={11}/>{ticket.id}</span>
            <span className={styles.metaPill}><Clock size={11}/>{ticket.createdAt}</span>
            <span className={styles.metaPill}><Tag size={11}/>{ticket.category}</span>
            <span className={`${styles.badge} ${styles[priorityCls[ticket.priority]]}`}>{ticket.priority}</span>
            <span className={`${styles.badge} ${styles[statusCls[ticket.status]]}`}>{ticket.status}</span>
          </div>

          {/* Subject */}
          <div className={styles.detailItem}>
            <span className={styles.detailKey}>Subject</span>
            <span className={styles.detailVal}>{ticket.subject}</span>
          </div>

          {/* Notes */}
          {ticket.notes && (
            <div className={styles.detailItem}>
              <span className={styles.detailKey}>Admin Notes</span>
              <span className={styles.detailVal}>{ticket.notes}</span>
            </div>
          )}

          {/* Update status */}
          <div className={styles.statusActions}>
            <span className={styles.detailKey}>Update Status</span>
            <div className={styles.statusBtns}>
              {STATUS_OPTS.map(s=>(
                <button
                  key={s}
                  className={`${styles.statusBtn} ${ticket.status===s?styles.statusBtnActive:""}`}
                  onClick={()=>onStatusChange(ticket.id,s)}
                >{s}</button>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}

/* ── Details Modal (query view + create ticket button) ── */
function DetailsModal({
  q, onClose, onStatusChange, onCreateTicket,
}: {
  q: Query;
  onClose: ()=>void;
  onStatusChange: (id:string,s:Status)=>void;
  onCreateTicket: (q:Query)=>void;
}) {
  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e=>e.stopPropagation()}>
        <div className={styles.modalHead}>
          <h3 className={styles.modalTitle}>Query Details</h3>
          <button className={styles.modalClose} onClick={onClose}><X size={14}/></button>
        </div>
        <div className={styles.modalBody}>
          <div className={styles.userCard}>
            <div className={styles.userCardAvatar} style={{background:q.color}}>{q.initials}</div>
            <div>
              <p className={styles.userCardName}>{q.name}</p>
              <p className={styles.userCardEmail}>{q.email}</p>
            </div>
            {q.ticketId && (
              <span className={styles.hasTicketTag}><Ticket size={11}/>{q.ticketId}</span>
            )}
          </div>

          <div className={styles.detailGrid}>
            <div className={styles.detailItem}><span className={styles.detailKey}>Issue</span><span className={styles.detailVal}>{q.issue}</span></div>
            <div className={styles.detailItem}><span className={styles.detailKey}>Reported</span><span className={styles.detailVal}>{q.time}</span></div>
            <div className={styles.detailItem} style={{gridColumn:"1/-1"}}><span className={styles.detailKey}>Subject</span><span className={styles.detailVal}>{q.subject}</span></div>
            <div className={styles.detailItem}><span className={styles.detailKey}>Status</span><span className={`${styles.badge} ${styles[statusCls[q.status]]}`}>{q.status}</span></div>
          </div>

          <div className={styles.statusActions}>
            <span className={styles.detailKey}>Update Status</span>
            <div className={styles.statusBtns}>
              {STATUS_OPTS.map(s=>(
                <button key={s} className={`${styles.statusBtn} ${q.actionStatus===s?styles.statusBtnActive:""}`}
                  onClick={()=>onStatusChange(q.id,s)}>{s}</button>
              ))}
            </div>
          </div>

          {/* Create ticket CTA */}
          <div className={styles.createTicketCta}>
            <button className={styles.createTicketCtaBtn} onClick={()=>{ onClose(); onCreateTicket(q); }}>
              <Ticket size={15}/>
              {q.ticketId ? `View Ticket ${q.ticketId}` : "Create a Ticket for this Query"}
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}

/* ── Row action menu ── */
function RowMenu({ onView, onResolve, onEscalate, onClose, onCreateTicket }:{
  onView:()=>void; onResolve:()=>void; onEscalate:()=>void; onClose:()=>void; onCreateTicket:()=>void;
}) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(()=>{
    const h=(e:MouseEvent)=>{ if(ref.current&&!ref.current.contains(e.target as Node)) onClose(); };
    document.addEventListener("mousedown",h);
    return ()=>document.removeEventListener("mousedown",h);
  },[onClose]);
  return (
    <div className={styles.rowMenu} ref={ref}>
      <button className={styles.rowMenuItem} onClick={onView}><Eye size={13}/> View Details</button>
      <button className={styles.rowMenuItem} onClick={onCreateTicket}><Ticket size={13}/> Create Ticket</button>
      <button className={styles.rowMenuItem} onClick={onResolve}><CheckCircle size={13}/> Mark Resolved</button>
      <button className={styles.rowMenuItem} onClick={onEscalate}><AlertTriangle size={13}/> Escalate</button>
      <div className={styles.rowMenuDivider}/>
      <button className={`${styles.rowMenuItem} ${styles.rowMenuDanger}`} onClick={onClose}><XCircle size={13}/> Close Ticket</button>
    </div>
  );
}

/* ══════════════════════════════════════════ */
/*  PAGE                                      */
/* ══════════════════════════════════════════ */
export default function ManageQueriesPage() {
  const [sidebarOpen,  setSidebarOpen]  = useState(false);
  const [queries,      setQueries]      = useState<Query[]>(ALL_QUERIES);
  const [tickets,      setTickets]      = useState<TicketData[]>([]);
  const [search,       setSearch]       = useState("");
  const [statusFilter, setStatusFilter] = useState("All Status");
  const [page,         setPage]         = useState(1);
  const [openMenu,     setOpenMenu]     = useState<string|null>(null);

  // modal states
  const [viewQuery,      setViewQuery]      = useState<Query|null>(null);
  const [createTicketFor,setCreateTicketFor]= useState<Query|"blank"|null>(null);
  const [viewTicket,     setViewTicket]     = useState<TicketData|null>(null);

  const filtered = queries.filter(q => {
    const matchSearch = q.name.toLowerCase().includes(search.toLowerCase()) ||
                        q.email.toLowerCase().includes(search.toLowerCase()) ||
                        q.issue.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter==="All Status" || q.status===statusFilter;
    return matchSearch && matchStatus;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length/PER_PAGE));
  const safePage   = Math.min(page, totalPages);
  const start      = (safePage-1)*PER_PAGE;
  const rows       = filtered.slice(start, start+PER_PAGE);
  const openCount  = queries.filter(q=>["Open","Pending","Escalated"].includes(q.status)).length;

  const goTo = (p:number) => setPage(Math.max(1,Math.min(totalPages,p)));

  const handleStatusChange = (id:string, s:Status) => {
    setQueries(prev=>prev.map(q=>q.id===id?{...q,status:s,actionStatus:s}:q));
    setViewQuery(prev=>prev&&prev.id===id?{...prev,status:s,actionStatus:s}:prev);
  };

  const handleTicketStatusChange = (ticketId:string, s:Status) => {
    setTickets(prev=>prev.map(t=>t.id===ticketId?{...t,status:s}:t));
    setViewTicket(prev=>prev&&prev.id===ticketId?{...prev,status:s}:prev);
    // also sync query status
    const ticket = tickets.find(t=>t.id===ticketId);
    if (ticket?.queryId) handleStatusChange(ticket.queryId, s);
  };

  const handleTicketCreated = (t:TicketData) => {
    setTickets(prev=>[t,...prev]);
    // link ticket to query
    if (t.queryId) {
      setQueries(prev=>prev.map(q=>q.id===t.queryId?{...q,ticketId:t.id,status:"Open",actionStatus:"Open"}:q));
    }
  };

  return (
    <div>
      <Header onMenuToggle={()=>setSidebarOpen(p=>!p)}/>
      <Sidebar isOpen={sidebarOpen} onClose={()=>setSidebarOpen(false)}/>

      {/* Query details modal */}
      {viewQuery && !createTicketFor && (
        <DetailsModal
          q={viewQuery}
          onClose={()=>setViewQuery(null)}
          onStatusChange={handleStatusChange}
          onCreateTicket={(q)=>setCreateTicketFor(q)}
        />
      )}

      {/* Create ticket modal */}
      {createTicketFor && (
        <CreateTicketModal
          query={createTicketFor==="blank"?null:createTicketFor}
          onClose={()=>setCreateTicketFor(null)}
          onSave={(t)=>{ handleTicketCreated(t); }}
        />
      )}

      {/* View existing ticket modal */}
      {viewTicket && (
        <TicketViewModal
          ticket={viewTicket}
          onClose={()=>setViewTicket(null)}
          onStatusChange={handleTicketStatusChange}
        />
      )}

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer}/>

        <main className={styles.main}>

          <div className={styles.titleBlock}>
            <h1 className={styles.pageTitle}>Manage Queries &amp; Complaints</h1>
            <p className={styles.pageSubtitle}>Here, admin can manage and handle all users&apos; queries and complaints.</p>
          </div>

          <div className={styles.contentRow}>

            {/* ── Table card ── */}
            <div className={styles.tableCard}>

              {/* Filters */}
              <div className={styles.filtersRow}>
                <div className={styles.searchWrap}>
                  <Search size={14} className={styles.searchIcon}/>
                  <input className={styles.searchInput} placeholder="Search" value={search}
                    onChange={e=>{setSearch(e.target.value);setPage(1);}}/>
                </div>

                <div className={styles.selGroup}>
                  <div className={styles.selWrap}>
                    <select className={styles.sel} value={statusFilter} onChange={e=>{setStatusFilter(e.target.value);setPage(1);}}>
                      <option>All Status</option>
                      {STATUS_OPTS.map(s=><option key={s}>{s}</option>)}
                    </select>
                    <ChevronDown size={12} className={styles.selChev}/>
                  </div>
                  <div className={styles.selWrap}>
                    <select className={styles.sel}>
                      <option>All</option>
                      {CATEGORIES.map(c=><option key={c}>{c}</option>)}
                    </select>
                    <ChevronDown size={12} className={styles.selChev}/>
                  </div>
                </div>

                <button className={styles.applyBtn}>Apply</button>
              </div>

              {/* Table */}
              <div className={styles.tableWrap}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>User</th>
                      <th>Email</th>
                      <th>Issue</th>
                      <th>Subject</th>
                      <th>Status</th>
                      <th>Ticket</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map(q=>(
                      <tr key={q.id+q.time}>
                        <td>
                          <div className={styles.userCell}>
                            <div className={styles.avatar} style={{background:q.color}}>{q.initials}</div>
                            <span className={styles.userName}>{q.name}</span>
                          </div>
                        </td>
                        <td>
                          <div className={styles.emailCol}>
                            <span className={styles.emailName}>{q.email.split("@")[0].split(".").map((n:string)=>n.charAt(0).toUpperCase()+n.slice(1)).join(" ")}</span>
                            <span className={styles.emailAddr}>{q.email}</span>
                          </div>
                        </td>
                        <td className={styles.issueCell}>{q.issue}</td>
                        <td>
                          <span className={styles.subjectText}>{q.subject}</span>
                        </td>
                        <td>
                          <div className={styles.statusCol}>
                            <span className={`${styles.badge} ${styles[statusCls[q.status]]}`}>{q.status}</span>
                            <span className={styles.timeLabel}>{q.time}</span>
                          </div>
                        </td>
                        <td>
                          {q.ticketId ? (
                            <button
                              className={styles.ticketIdBtn}
                              onClick={()=>{
                                const t = tickets.find(x=>x.id===q.ticketId);
                                if(t) setViewTicket(t);
                              }}
                            >
                              <Ticket size={11}/>{q.ticketId}
                            </button>
                          ) : (
                            <button className={styles.noTicketBtn} onClick={()=>setCreateTicketFor(q)}>
                              + Create
                            </button>
                          )}
                        </td>
                        <td>
                          <div className={styles.actionsCell}>
                            <button className={styles.viewBtn} onClick={()=>setViewQuery(q)}>
                              View Details
                            </button>
                            <div className={styles.menuWrap}>
                              <button
                                className={`${styles.menuBtn} ${openMenu===q.id?styles.menuBtnActive:""}`}
                                onClick={()=>setOpenMenu(openMenu===q.id?null:q.id)}
                              >
                                <MoreVertical size={15}/>
                              </button>
                              {openMenu===q.id&&(
                                <RowMenu
                                  onView={()=>{setViewQuery(q);setOpenMenu(null);}}
                                  onCreateTicket={()=>{setCreateTicketFor(q);setOpenMenu(null);}}
                                  onResolve={()=>{handleStatusChange(q.id,"Resolved");setOpenMenu(null);}}
                                  onEscalate={()=>{handleStatusChange(q.id,"Escalated");setOpenMenu(null);}}
                                  onClose={()=>setOpenMenu(null)}
                                />
                              )}
                            </div>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className={styles.pagination}>
                <span className={styles.showingLabel}>
                  Showing {filtered.length===0?0:start+1} to {Math.min(start+PER_PAGE,filtered.length)} of {filtered.length} entries
                </span>
                <div className={styles.pageControls}>
                  <button className={styles.pageBtn} onClick={()=>goTo(safePage-1)} disabled={safePage===1}>
                    <ChevronLeft size={13}/> Previous
                  </button>
                  {safePage>2&&<button className={styles.pageNumBtn} onClick={()=>goTo(1)}>1</button>}
                  {safePage>3&&<span className={styles.ellipsis}>...</span>}
                  {safePage>1&&<button className={styles.pageNumBtn} onClick={()=>goTo(safePage-1)}>{safePage-1}</button>}
                  <button className={`${styles.pageNumBtn} ${styles.pageNumActive}`}>{safePage}</button>
                  {safePage<totalPages&&<button className={styles.pageNumBtn} onClick={()=>goTo(safePage+1)}>{safePage+1}</button>}
                  {safePage<totalPages-2&&<span className={styles.ellipsis}>...</span>}
                  {safePage<totalPages-1&&<button className={styles.pageNumBtn} onClick={()=>goTo(totalPages)}>{totalPages}</button>}
                  <button className={styles.pageBtn} onClick={()=>goTo(safePage+1)} disabled={safePage===totalPages}>
                    Next <ChevronRight size={13}/>
                  </button>
                </div>
              </div>

            </div>

            {/* ── Right sidebar ── */}
            <aside className={styles.sidebar}>

              <div className={styles.sideCard}>
                <div className={styles.sideCardHead}>
                  <div>
                    <p className={styles.sideCardTitle}>Open Complaints</p>
                    <p className={styles.sideCardSub}>{openCount} active</p>
                  </div>
                  <span className={styles.openCountBadge}>{openCount}</span>
                </div>
              </div>

              {/* Tickets summary */}
              {tickets.length>0&&(
                <div className={styles.sideCard}>
                  <p className={styles.sideCardTitle}>Recent Tickets</p>
                  <div className={styles.ticketsList}>
                    {tickets.slice(0,4).map(t=>(
                      <button key={t.id} className={styles.ticketItem} onClick={()=>setViewTicket(t)}>
                        <div className={styles.ticketItemLeft}>
                          <span className={styles.ticketItemId}>{t.id}</span>
                          <span className={styles.ticketItemSubject}>{t.subject}</span>
                        </div>
                        <span className={`${styles.badge} ${styles[statusCls[t.status]]}`}>{t.status}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}

              <div className={styles.sideCard}>
                <p className={styles.sideCardTitle}>Quick Actions</p>
                <div className={styles.quickActions}>
                  <button className={styles.quickBtn}>
                    <div className={styles.quickIcon}><MessageSquare size={18}/></div>
                    <span>Send a Message</span>
                  </button>
                  <button className={styles.quickBtn} onClick={()=>setCreateTicketFor("blank")}>
                    <div className={styles.quickIcon}><Ticket size={18}/></div>
                    <span>Create a Ticket</span>
                  </button>
                </div>
              </div>

            </aside>
          </div>
        </main>
      </div>
    </div>
  );
}