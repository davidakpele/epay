"use client";

import { useState, useRef, useEffect } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles  from "./page.module.css";
import {
  Search, ChevronDown, MoreHorizontal, CheckCircle,
  XCircle, Eye, ChevronLeft, ChevronRight, X,
  FileText, ScrollText, AlertTriangle, Check,
} from "lucide-react";

type LoanStatus = "Pending" | "Approved" | "Declined" | "Under Review";

interface Loan {
  id:       string;
  name:     string;
  initials: string;
  color:    string;
  email:    string;
  amount:   string;
  purpose:  string;
  status:   LoanStatus;
  time:     string;
}

const ALL_LOANS: Loan[] = [
  { id:"L001", name:"Olivia James",    initials:"OJ", color:"#c8a84b", email:"olivia.james@email.com",    amount:"₦500,000", purpose:"Business Expansion",   status:"Pending",      time:"5 mins ago"    },
  { id:"L002", name:"David Alex",      initials:"DA", color:"#166701", email:"david.alex@email.com",      amount:"₦200,000", purpose:"Medical Bills",         status:"Pending",      time:"15 mins ago"   },
  { id:"L003", name:"John Carter",     initials:"JC", color:"#3a7abf", email:"john.carter@email.com",     amount:"₦750,000", purpose:"Home Renovation",       status:"Pending",      time:"30 mins ago"   },
  { id:"L004", name:"Sarah Williams",  initials:"SW", color:"#5aaa7a", email:"sarah.williams@email.com",  amount:"₦150,000", purpose:"Education Expenses",    status:"Pending",      time:"55 mins ago"   },
  { id:"L005", name:"Michael Phan",    initials:"MP", color:"#7a5abf", email:"michael.phan@email.com",    amount:"₦600,000", purpose:"Pay Off Debt",          status:"Declined",     time:"1 hour ago"    },
  { id:"L006", name:"Linda White",     initials:"LW", color:"#5a8abf", email:"linda.white@email.com",     amount:"₦400,000", purpose:"Vehicle Purchase",      status:"Pending",      time:"2 hours ago"   },
  { id:"L007", name:"Steven Park",     initials:"SP", color:"#5aaa7a", email:"steven.park@email.com",     amount:"₦300,000", purpose:"Small Business Needs",  status:"Pending",      time:"3 hours ago"   },
  { id:"L008", name:"Amara Okafor",    initials:"AO", color:"#bf5a5a", email:"amara.okafor@email.com",    amount:"₦1,000,000",purpose:"Business Expansion",   status:"Approved",     time:"4 hours ago"   },
  { id:"L009", name:"Priya Sharma",    initials:"PS", color:"#aa6601", email:"priya.sharma@email.com",    amount:"₦250,000", purpose:"Medical Bills",         status:"Approved",     time:"5 hours ago"   },
  { id:"L010", name:"James Obi",       initials:"JO", color:"#3a7abf", email:"james.obi@email.com",       amount:"₦450,000", purpose:"Home Renovation",       status:"Under Review", time:"6 hours ago"   },
  { id:"L011", name:"Fatima Hassan",   initials:"FH", color:"#166701", email:"fatima.hassan@email.com",   amount:"₦800,000", purpose:"Business Expansion",    status:"Approved",     time:"7 hours ago"   },
  { id:"L012", name:"Chen Wei",        initials:"CW", color:"#7a5abf", email:"chen.wei@email.com",        amount:"₦120,000", purpose:"Education Expenses",    status:"Declined",     time:"8 hours ago"   },
  { id:"L013", name:"Ada Eze",         initials:"AE", color:"#c8a84b", email:"ada.eze@email.com",         amount:"₦350,000", purpose:"Vehicle Purchase",      status:"Pending",      time:"9 hours ago"   },
  { id:"L014", name:"Tom Benson",      initials:"TB", color:"#5a8abf", email:"tom.benson@email.com",      amount:"₦500,000", purpose:"Pay Off Debt",          status:"Declined",     time:"10 hours ago"  },
  { id:"L015", name:"Nora Adeyemi",    initials:"NA", color:"#bf5a5a", email:"nora.adeyemi@email.com",    amount:"₦900,000", purpose:"Business Expansion",    status:"Approved",     time:"11 hours ago"  },
  { id:"L016", name:"Kwame Asante",    initials:"KA", color:"#5aaa7a", email:"kwame.asante@email.com",    amount:"₦200,000", purpose:"Small Business Needs",  status:"Approved",     time:"12 hours ago"  },
  { id:"L017", name:"Elena Rossi",     initials:"ER", color:"#aa6601", email:"elena.rossi@email.com",     amount:"₦650,000", purpose:"Home Renovation",       status:"Under Review", time:"13 hours ago"  },
  { id:"L018", name:"Ben Okonkwo",     initials:"BO", color:"#3a7abf", email:"ben.okonkwo@email.com",     amount:"₦180,000", purpose:"Medical Bills",         status:"Declined",     time:"14 hours ago"  },
  { id:"L019", name:"Ife Adegoke",     initials:"IA", color:"#166701", email:"ife.adegoke@email.com",     amount:"₦420,000", purpose:"Education Expenses",    status:"Approved",     time:"15 hours ago"  },
  { id:"L020", name:"Zara Moussa",     initials:"ZM", color:"#bf5a5a", email:"zara.moussa@email.com",     amount:"₦275,000", purpose:"Vehicle Purchase",      status:"Pending",      time:"16 hours ago"  },
  { id:"L021", name:"Oscar Lima",      initials:"OL", color:"#7a5abf", email:"oscar.lima@email.com",      amount:"₦1,200,000",purpose:"Business Expansion",   status:"Approved",     time:"17 hours ago"  },
  { id:"L022", name:"Mei Zhang",       initials:"MZ", color:"#c8a84b", email:"mei.zhang@email.com",       amount:"₦330,000", purpose:"Medical Bills",         status:"Pending",      time:"18 hours ago"  },
  { id:"L023", name:"Felix Osei",      initials:"FO", color:"#5aaa7a", email:"felix.osei@email.com",      amount:"₦560,000", purpose:"Small Business Needs",  status:"Declined",     time:"19 hours ago"  },
  { id:"L024", name:"Ingrid Dahl",     initials:"ID", color:"#5a8abf", email:"ingrid.dahl@email.com",     amount:"₦700,000", purpose:"Home Renovation",       status:"Approved",     time:"20 hours ago"  },
  { id:"L025", name:"Carlos Vega",     initials:"CV", color:"#aa6601", email:"carlos.vega@email.com",     amount:"₦490,000", purpose:"Pay Off Debt",          status:"Approved",     time:"21 hours ago"  },
  { id:"L026", name:"Aisha Bello",     initials:"AB", color:"#bf5a5a", email:"aisha.bello@email.com",     amount:"₦210,000", purpose:"Education Expenses",    status:"Pending",      time:"22 hours ago"  },
  { id:"L027", name:"Raj Patel",       initials:"RP", color:"#3a7abf", email:"raj.patel@email.com",       amount:"₦380,000", purpose:"Business Expansion",    status:"Under Review", time:"23 hours ago"  },
  { id:"L028", name:"Sofia Mensah",    initials:"SM", color:"#166701", email:"sofia.mensah@email.com",    amount:"₦850,000", purpose:"Vehicle Purchase",      status:"Approved",     time:"1 day ago"     },
  { id:"L029", name:"Luke Ike",        initials:"LI", color:"#c8a84b", email:"luke.ike@email.com",        amount:"₦145,000", purpose:"Medical Bills",         status:"Declined",     time:"1 day ago"     },
  { id:"L030", name:"Tunde Abiodun",   initials:"TA", color:"#7a5abf", email:"tunde.abiodun@email.com",   amount:"₦620,000", purpose:"Small Business Needs",  status:"Approved",     time:"1 day ago"     },
  { id:"L031", name:"Yemi Babatunde",  initials:"YB", color:"#5a8abf", email:"yemi.babatunde@email.com",  amount:"₦290,000", purpose:"Education Expenses",    status:"Pending",      time:"2 days ago"    },
  { id:"L032", name:"Chioma Eze",      initials:"CE", color:"#5aaa7a", email:"chioma.eze@email.com",      amount:"₦730,000", purpose:"Home Renovation",       status:"Approved",     time:"2 days ago"    },
  { id:"L033", name:"Nnamdi Okafor",   initials:"NO", color:"#aa6601", email:"nnamdi.okafor@email.com",   amount:"₦160,000", purpose:"Pay Off Debt",          status:"Declined",     time:"2 days ago"    },
  { id:"L034", name:"Grace Anyanwu",   initials:"GA", color:"#bf5a5a", email:"grace.anyanwu@email.com",   amount:"₦510,000", purpose:"Business Expansion",    status:"Approved",     time:"3 days ago"    },
  { id:"L035", name:"Emeka Nwosu",     initials:"EN", color:"#166701", email:"emeka.nwosu@email.com",     amount:"₦390,000", purpose:"Vehicle Purchase",      status:"Pending",      time:"3 days ago"    },
  { id:"L036", name:"Adaobi Chukwu",   initials:"AC", color:"#3a7abf", email:"adaobi.chukwu@email.com",   amount:"₦240,000", purpose:"Medical Bills",         status:"Approved",     time:"3 days ago"    },
  { id:"L037", name:"Kelechi Obi",     initials:"KO", color:"#c8a84b", email:"kelechi.obi@email.com",     amount:"₦870,000", purpose:"Business Expansion",    status:"Under Review", time:"4 days ago"    },
  { id:"L038", name:"Simi Afolabi",    initials:"SA", color:"#7a5abf", email:"simi.afolabi@email.com",    amount:"₦195,000", purpose:"Education Expenses",    status:"Declined",     time:"4 days ago"    },
  { id:"L039", name:"Damilola Fash",   initials:"DF", color:"#5aaa7a", email:"damilola.fash@email.com",   amount:"₦680,000", purpose:"Small Business Needs",  status:"Approved",     time:"4 days ago"    },
  { id:"L040", name:"Bisi Oladele",    initials:"BO", color:"#5a8abf", email:"bisi.oladele@email.com",    amount:"₦415,000", purpose:"Home Renovation",       status:"Pending",      time:"5 days ago"    },
  { id:"L041", name:"Funmi Adeyinka",  initials:"FA", color:"#aa6601", email:"funmi.adeyinka@email.com",  amount:"₦960,000", purpose:"Business Expansion",    status:"Approved",     time:"5 days ago"    },
  { id:"L042", name:"Seun Oladipo",    initials:"SO", color:"#bf5a5a", email:"seun.oladipo@email.com",    amount:"₦320,000", purpose:"Pay Off Debt",          status:"Declined",     time:"5 days ago"    },
];

const STATUS_OPTS: LoanStatus[] = ["Pending","Approved","Declined","Under Review"];
const PURPOSES = ["All","Business Expansion","Medical Bills","Home Renovation","Education Expenses","Pay Off Debt","Vehicle Purchase","Small Business Needs"];
const PER_PAGE = 9;

const statusCls: Record<LoanStatus,string> = {
  Pending:      "sPending",
  Approved:     "sApproved",
  Declined:     "sDeclined",
  "Under Review":"sReview",
};

/* ── Confirm modal (approve / decline) ── */
function ConfirmModal({
  loan, action, onClose, onConfirm,
}: {
  loan: Loan; action:"Approve"|"Decline"; onClose:()=>void; onConfirm:()=>void;
}) {
  const isApprove = action==="Approve";
  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.confirmModal} onClick={e=>e.stopPropagation()}>
        <div className={isApprove?styles.confirmIconApprove:styles.confirmIconDecline}>
          {isApprove ? <CheckCircle size={30}/> : <AlertTriangle size={30}/>}
        </div>
        <h3 className={styles.confirmTitle}>{isApprove?"Approve Loan?":"Decline Loan?"}</h3>
        <p className={styles.confirmMsg}>
          {isApprove
            ? <>You are about to approve <strong>{loan.amount}</strong> loan for <strong>{loan.name}</strong>. This action will notify the user.</>
            : <>You are about to decline the loan request from <strong>{loan.name}</strong>. The user will be notified.</>
          }
        </p>
        <div className={styles.confirmFoot}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button className={isApprove?styles.approveConfirmBtn:styles.declineConfirmBtn} onClick={onConfirm}>
            {isApprove ? <><Check size={14}/> Yes, Approve</> : <><XCircle size={14}/> Yes, Decline</>}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Loan detail modal ── */
function LoanDetailModal({
  loan, onClose, onApprove, onDecline,
}: {
  loan:Loan; onClose:()=>void; onApprove:(l:Loan)=>void; onDecline:(l:Loan)=>void;
}) {
  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e=>e.stopPropagation()}>
        <div className={styles.modalHead}>
          <h3 className={styles.modalTitle}>Loan Details</h3>
          <button className={styles.modalClose} onClick={onClose}><X size={14}/></button>
        </div>
        <div className={styles.modalBody}>
          <div className={styles.loanUserCard}>
            <div className={styles.loanAvatar} style={{background:loan.color}}>{loan.initials}</div>
            <div>
              <p className={styles.loanUserName}>{loan.name}</p>
              <p className={styles.loanUserEmail}>{loan.email}</p>
            </div>
            <span className={`${styles.badge} ${styles[statusCls[loan.status]]}`}>{loan.status}</span>
          </div>

          <div className={styles.detailGrid}>
            <div className={styles.detailItem}><span className={styles.detailKey}>Loan ID</span><span className={styles.detailVal}>{loan.id}</span></div>
            <div className={styles.detailItem}><span className={styles.detailKey}>Submitted</span><span className={styles.detailVal}>{loan.time}</span></div>
            <div className={styles.detailItem}><span className={styles.detailKey}>Loan Amount</span><span className={`${styles.detailVal} ${styles.amountHighlight}`}>{loan.amount}</span></div>
            <div className={styles.detailItem}><span className={styles.detailKey}>Loan Purpose</span><span className={styles.detailVal}>{loan.purpose}</span></div>
          </div>

          {loan.status === "Pending" || loan.status === "Under Review" ? (
            <div className={styles.loanActions}>
              <button className={styles.approveBtn} onClick={()=>onApprove(loan)}>
                <CheckCircle size={15}/> Approve Loan
              </button>
              <button className={styles.declineBtn} onClick={()=>onDecline(loan)}>
                <XCircle size={15}/> Decline Loan
              </button>
            </div>
          ) : (
            <div className={`${styles.statusFinalNote} ${loan.status==="Approved"?styles.statusFinalApproved:styles.statusFinalDeclined}`}>
              {loan.status==="Approved" ? <CheckCircle size={15}/> : <XCircle size={15}/>}
              This loan has been <strong>{loan.status.toLowerCase()}</strong>.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

/* ── New Loan Request Modal ── */
function NewLoanModal({ onClose, onSave }: { onClose:()=>void; onSave:(l:Loan)=>void }) {
  const [form, setForm] = useState({ name:"", email:"", amount:"", purpose: PURPOSES[1], notes:"" });
  const set = (k:string,v:string) => setForm(f=>({...f,[k]:v}));

  const handleSave = () => {
    if (!form.name||!form.email||!form.amount) return;
    const initials = form.name.split(" ").map(n=>n[0]).join("").slice(0,2).toUpperCase();
    const colors   = ["#c8a84b","#166701","#3a7abf","#5aaa7a","#7a5abf","#bf5a5a","#5a8abf","#aa6601"];
    onSave({
      id:       `L${String(ALL_LOANS.length+1).padStart(3,"0")}`,
      name:     form.name,
      initials,
      color:    colors[Math.floor(Math.random()*colors.length)],
      email:    form.email,
      amount:   form.amount.startsWith("₦")?form.amount:`₦${form.amount}`,
      purpose:  form.purpose,
      status:   "Pending",
      time:     "Just now",
    });
  };

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e=>e.stopPropagation()}>
        <div className={styles.modalHead}>
          <h3 className={styles.modalTitle}>New Loan Request</h3>
          <button className={styles.modalClose} onClick={onClose}><X size={14}/></button>
        </div>
        <div className={styles.modalBody}>
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Full Name</label>
              <input className={styles.formInput} placeholder="e.g. John Doe" value={form.name} onChange={e=>set("name",e.target.value)}/>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Email Address</label>
              <input className={styles.formInput} placeholder="user@email.com" value={form.email} onChange={e=>set("email",e.target.value)}/>
            </div>
          </div>
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Loan Amount</label>
              <input className={styles.formInput} placeholder="e.g. ₦500,000" value={form.amount} onChange={e=>set("amount",e.target.value)}/>
            </div>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Loan Purpose</label>
              <div className={styles.selWrapModal}>
                <select className={styles.formSelect} value={form.purpose} onChange={e=>set("purpose",e.target.value)}>
                  {PURPOSES.filter(p=>p!=="All").map(p=><option key={p}>{p}</option>)}
                </select>
                <ChevronDown size={12} className={styles.selChevModal}/>
              </div>
            </div>
          </div>
          <div className={styles.formGroup}>
            <label className={styles.formLabel}>Notes <span className={styles.optional}>(optional)</span></label>
            <textarea className={styles.formTextarea} rows={3} placeholder="Additional notes..." value={form.notes} onChange={e=>set("notes",e.target.value)}/>
          </div>
        </div>
        <div className={styles.modalFoot}>
          <button className={styles.cancelBtn} onClick={onClose}>Cancel</button>
          <button className={styles.submitBtn} onClick={handleSave} disabled={!form.name||!form.email||!form.amount}>
            <Check size={14}/> Submit Request
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Row action menu ── */
function RowMenu({ loan, onView, onApprove, onDecline, onClose }:{
  loan:Loan; onView:()=>void; onApprove:()=>void; onDecline:()=>void; onClose:()=>void;
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
      {(loan.status==="Pending"||loan.status==="Under Review")&&<>
        <button className={styles.rowMenuItem} onClick={onApprove}><CheckCircle size={13}/> Approve</button>
        <button className={`${styles.rowMenuItem} ${styles.rowMenuDanger}`} onClick={onDecline}><XCircle size={13}/> Decline</button>
      </>}
    </div>
  );
}

/* ══════ PAGE ══════ */
export default function LoanApprovalsPage() {
  const [sidebarOpen,  setSidebarOpen]  = useState(false);
  const [loans,        setLoans]        = useState<Loan[]>(ALL_LOANS);
  const [search,       setSearch]       = useState("");
  const [statusFilter, setStatusFilter] = useState("All Status");
  const [purposeFilter,setPurposeFilter]= useState("All");
  const [page,         setPage]         = useState(1);
  const [openMenu,     setOpenMenu]     = useState<string|null>(null);

  const [viewLoan,     setViewLoan]     = useState<Loan|null>(null);
  const [confirmLoan,  setConfirmLoan]  = useState<{loan:Loan;action:"Approve"|"Decline"}|null>(null);
  const [newLoanOpen,  setNewLoanOpen]  = useState(false);

  const filtered = loans.filter(l=>{
    const matchSearch  = l.name.toLowerCase().includes(search.toLowerCase())||l.email.toLowerCase().includes(search.toLowerCase())||l.purpose.toLowerCase().includes(search.toLowerCase());
    const matchStatus  = statusFilter==="All Status"||l.status===statusFilter;
    const matchPurpose = purposeFilter==="All"||l.purpose===purposeFilter;
    return matchSearch&&matchStatus&&matchPurpose;
  });

  const totalPages  = Math.max(1,Math.ceil(filtered.length/PER_PAGE));
  const safePage    = Math.min(page,totalPages);
  const start       = (safePage-1)*PER_PAGE;
  const rows        = filtered.slice(start,start+PER_PAGE);

  const pending  = loans.filter(l=>l.status==="Pending").length;
  const approved = loans.filter(l=>l.status==="Approved").length;
  const rejected = loans.filter(l=>l.status==="Declined").length;

  const goTo = (p:number)=>setPage(Math.max(1,Math.min(totalPages,p)));

  const doApprove = (loan:Loan) => {
    setLoans(prev=>prev.map(l=>l.id===loan.id?{...l,status:"Approved"}:l));
    setConfirmLoan(null); setViewLoan(null);
  };
  const doDecline = (loan:Loan) => {
    setLoans(prev=>prev.map(l=>l.id===loan.id?{...l,status:"Declined"}:l));
    setConfirmLoan(null); setViewLoan(null);
  };

  return (
    <div>
      <Header onMenuToggle={()=>setSidebarOpen(p=>!p)}/>
      <Sidebar isOpen={sidebarOpen} onClose={()=>setSidebarOpen(false)}/>

      {viewLoan&&!confirmLoan&&(
        <LoanDetailModal
          loan={viewLoan} onClose={()=>setViewLoan(null)}
          onApprove={l=>setConfirmLoan({loan:l,action:"Approve"})}
          onDecline={l=>setConfirmLoan({loan:l,action:"Decline"})}
        />
      )}

      {confirmLoan&&(
        <ConfirmModal
          loan={confirmLoan.loan} action={confirmLoan.action}
          onClose={()=>setConfirmLoan(null)}
          onConfirm={()=>confirmLoan.action==="Approve"?doApprove(confirmLoan.loan):doDecline(confirmLoan.loan)}
        />
      )}

      {newLoanOpen&&(
        <NewLoanModal
          onClose={()=>setNewLoanOpen(false)}
          onSave={l=>{ setLoans(prev=>[l,...prev]); setNewLoanOpen(false); }}
        />
      )}

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer}/>

        <main className={styles.main}>

          <div className={styles.titleBlock}>
            <h1 className={styles.pageTitle}>Loan Approvals</h1>
            <p className={styles.pageSubtitle}>Here, admin can manage and approve or reject loan requests from users.</p>
          </div>

          <div className={styles.contentRow}>

            {/* ── Table card ── */}
            <div className={styles.tableCard}>
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
                    <select className={styles.sel} value={purposeFilter} onChange={e=>{setPurposeFilter(e.target.value);setPage(1);}}>
                      {PURPOSES.map(p=><option key={p}>{p}</option>)}
                    </select>
                    <ChevronDown size={12} className={styles.selChev}/>
                  </div>
                </div>
                <button className={styles.applyBtn}>Apply</button>
              </div>

              <div className={styles.tableWrap}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th>User</th>
                      <th>Email</th>
                      <th>Loan Amount</th>
                      <th>Loan Purpose</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map(loan=>(
                      <tr key={loan.id}>
                        <td>
                          <div className={styles.userCell}>
                            <div className={styles.avatar} style={{background:loan.color}}>{loan.initials}</div>
                            <span className={styles.userName}>{loan.name}</span>
                          </div>
                        </td>
                        <td>
                          <div className={styles.emailCol}>
                            <span className={styles.emailName}>{loan.email.split("@")[0].split(".").map((n:string)=>n.charAt(0).toUpperCase()+n.slice(1)).join(" ")}</span>
                            <span className={styles.emailAddr}>{loan.email}</span>
                          </div>
                        </td>
                        <td><span className={styles.amountCell}>{loan.amount}</span></td>
                        <td className={styles.purposeCell}>{loan.purpose}</td>
                        <td>
                          <div className={styles.statusCol}>
                            {loan.status==="Declined"&&<AlertTriangle size={12} className={styles.declinedIcon}/>}
                            <span className={`${styles.badge} ${styles[statusCls[loan.status]]}`}>{loan.status}</span>
                            <span className={styles.timeLabel}>{loan.time}</span>
                          </div>
                        </td>
                        <td>
                          <div className={styles.actionsCell}>
                            {(loan.status==="Pending"||loan.status==="Under Review") ? (
                              <button className={styles.approveBtn} onClick={()=>setConfirmLoan({loan,action:"Approve"})}>
                                Approve
                              </button>
                            ) : loan.status==="Declined" ? (
                              <button className={styles.declinedTagBtn} disabled>Declined</button>
                            ) : (
                              <button className={styles.approvedTagBtn} disabled>Approved</button>
                            )}
                            <div className={styles.menuWrap}>
                              <button
                                className={`${styles.menuBtn} ${openMenu===loan.id?styles.menuBtnActive:""}`}
                                onClick={()=>setOpenMenu(openMenu===loan.id?null:loan.id)}
                              >
                                <MoreHorizontal size={15}/>
                              </button>
                              {openMenu===loan.id&&(
                                <RowMenu
                                  loan={loan}
                                  onView={()=>{setViewLoan(loan);setOpenMenu(null);}}
                                  onApprove={()=>{setConfirmLoan({loan,action:"Approve"});setOpenMenu(null);}}
                                  onDecline={()=>{setConfirmLoan({loan,action:"Decline"});setOpenMenu(null);}}
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
                <p className={styles.sideCardTitle}>Loan Requests Summary</p>
                <div className={styles.summaryList}>
                  <div className={styles.summaryItem}>
                    <span className={styles.summaryLabel}>Pending Requests</span>
                    <span className={styles.summaryCount} style={{color:"#c8a84b"}}>{pending}</span>
                  </div>
                  <div className={`${styles.summaryItem} ${styles.summaryApproved}`}>
                    <span className={styles.summaryLabel}>Approved Loans</span>
                    <span className={styles.summaryCount} style={{color:"#166701"}}>{approved}</span>
                  </div>
                  <div className={`${styles.summaryItem} ${styles.summaryRejected}`}>
                    <span className={styles.summaryLabel}>Rejected Loans</span>
                    <span className={styles.summaryCount} style={{color:"#c0392b"}}>{rejected}</span>
                  </div>
                </div>
              </div>

              <div className={styles.sideCard}>
                <p className={styles.sideCardTitle}>Quick Actions</p>
                <div className={styles.quickActions}>
                  <button className={styles.quickBtn} onClick={()=>setNewLoanOpen(true)}>
                    <div className={styles.quickIcon}><FileText size={18}/></div>
                    <span>Create New Loan Request</span>
                  </button>
                  <button className={styles.quickBtn}>
                    <div className={styles.quickIcon}><ScrollText size={18}/></div>
                    <span>View Loan Policies</span>
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