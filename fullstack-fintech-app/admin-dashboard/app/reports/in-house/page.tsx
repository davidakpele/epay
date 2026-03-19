"use client";

import { useState, useEffect, useRef } from "react";
import Header  from "@/components/layout/Header";
import Sidebar from "@/components/layout/Sidebar";
import styles  from "./page.module.css";
import {
  BarChart2, ArrowDownToLine, ArrowUpFromLine,
  ArrowRightLeft, CircleDollarSign, AlertCircle,
  Calendar, ChevronDown, ChevronLeft, ChevronRight,
} from "lucide-react";

type Period = "Last 7 Days" | "Last 30 Days" | "Last 90 Days";
const PERIODS: Period[] = ["Last 7 Days", "Last 30 Days", "Last 90 Days"];

const STATS = [
  { label: "Total Deposits",      value: "₦3,220,000", color: "#166701", Icon: ArrowDownToLine,  iconBg: "#e8f5e8" },
  { label: "Total Withdrawals",   value: "₦2,180,000", color: "#c8a84b", Icon: ArrowUpFromLine,  iconBg: "#fff8e8" },
  { label: "Total Transfers",     value: "₦725,000",   color: "#3a7abf", Icon: ArrowRightLeft,   iconBg: "#e8f0ff" },
  { label: "Platform Revenue",    value: "₦125,000",   color: "#166701", Icon: CircleDollarSign, iconBg: "#e8f5e8" },
  { label: "Failed Transactions", value: "82",         color: "#c8a84b", Icon: AlertCircle,      iconBg: "#fff8e8" },
];

const FINANCIAL: Record<Period, { labels:string[]; dep:number[]; wit:number[]; tra:number[] }> = {
  "Last 7 Days":  { labels:["Sat","Sun","Mon","Tue","Wed","Thu","Fri","Sat","Sun"], dep:[200,240,260,310,340,315,290,345,460], wit:[130,150,165,200,230,255,275,295,325], tra:[120,140,160,185,215,235,250,265,285] },
  "Last 30 Days": { labels:["W1","W2","W3","W4","W5","W6","W7","W8","W9"],         dep:[300,355,385,425,465,445,485,515,555], wit:[160,190,215,245,265,285,305,325,355], tra:[100,130,155,185,205,225,245,265,295] },
  "Last 90 Days": { labels:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep"], dep:[500,625,705,785,855,905,955,1005,1105], wit:[300,355,405,455,505,545,585,625,685], tra:[150,205,245,285,325,365,405,445,495] },
};

const CHART2: Record<Period, { labels:string[]; dep:number[]; wit:number[]; tra:number[] }> = {
  "Last 7 Days":  { labels:["Sun","Sun","Mon","Tue","Wed","Thu","Fri","Sat","Sun"], dep:[160,200,240,285,325,345,365,385,405], wit:[100,130,160,195,225,255,275,305,335], tra:[80,110,140,175,205,235,255,285,315] },
  "Last 30 Days": { labels:["W1","W2","W3","W4","W5","W6","W7","W8","W9"],         dep:[200,255,305,355,405,435,465,495,525], wit:[130,160,195,225,255,285,305,335,365], tra:[90,120,155,185,215,245,265,295,325] },
  "Last 90 Days": { labels:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep"], dep:[400,505,585,665,745,805,865,925,1005], wit:[250,305,365,425,485,535,575,615,665], tra:[120,175,215,265,305,345,385,425,475] },
};

const FAILED: Record<Period, { labels:string[]; vals:number[] }> = {
  "Last 7 Days":  { labels:["Sun","Mon","Tue","Wed","Thu","Fri","Sat"], vals:[10,15,16,18,23,25,10] },
  "Last 30 Days": { labels:["W1","W2","W3","W4"],                      vals:[42,58,71,65]          },
  "Last 90 Days": { labels:["Jan","Feb","Mar"],                         vals:[180,210,195]          },
};

type ChartInst = { destroy:()=>void };
type ChartCtor = new (...a:unknown[])=>ChartInst;

function useChartJs(cb:()=>void, deps:unknown[]) {
  const [ready, setReady] = useState(false);
  useEffect(()=>{
    const w = window as unknown as Record<string,unknown>;
    if (w.Chart) { setReady(true); return; }
    const existing = document.querySelector('script[data-cjs]');
    if (existing) { existing.addEventListener("load",()=>setReady(true)); return; }
    const s = document.createElement("script");
    s.src = "https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js";
    s.setAttribute("data-cjs","1");
    s.onload = ()=>setReady(true);
    document.head.appendChild(s);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  },[]);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(()=>{ if(ready) cb(); },[ready,...deps]);
}

function LineChartCanvas({ id, src, period }:{ id:string; src:typeof FINANCIAL; period:Period }) {
  const ref  = useRef<HTMLCanvasElement>(null);
  const inst = useRef<ChartInst|null>(null);
  useChartJs(()=>{
    if (!ref.current) return;
    const C = (window as unknown as {Chart:ChartCtor}).Chart;
    inst.current?.destroy();
    const d = src[period];
    inst.current = new C(ref.current, {
      type:"line",
      data:{
        labels:d.labels,
        datasets:[
          { label:"Total Deposits",    data:d.dep, borderColor:"#4db825", backgroundColor:"rgba(77,184,37,0.07)",  borderWidth:2.5, pointBackgroundColor:"#fff", pointBorderColor:"#4db825", pointRadius:4, tension:0.38, fill:true  },
          { label:"Total Withdrawals", data:d.wit, borderColor:"#c8a84b", backgroundColor:"transparent",           borderWidth:2,   pointBackgroundColor:"#fff", pointBorderColor:"#c8a84b", pointRadius:4, tension:0.38, fill:false },
          { label:"Total Transfers",   data:d.tra, borderColor:"#3a7abf", backgroundColor:"transparent",           borderWidth:2,   pointBackgroundColor:"#fff", pointBorderColor:"#3a7abf", pointRadius:4, tension:0.38, fill:false },
        ],
      },
      options:{
        responsive:true, maintainAspectRatio:false,
        interaction:{mode:"index",intersect:false},
        plugins:{ legend:{display:false}, tooltip:{backgroundColor:"#fff",titleColor:"#2a3a2a",bodyColor:"#5a6a5a",borderColor:"#dde8dd",borderWidth:1,padding:10} },
        scales:{
          x:{grid:{color:"rgba(0,0,0,0.035)"},ticks:{font:{size:10},color:"#9aaa9a"}},
          y:{grid:{color:"rgba(0,0,0,0.045)"},ticks:{font:{size:10},color:"#9aaa9a",callback:(v:unknown)=>`N${v}k`}},
        },
      },
    });
  },[period,id]);
  return <canvas ref={ref} />;
}

function BarChartCanvas({ period }:{ period:Period }) {
  const ref  = useRef<HTMLCanvasElement>(null);
  const inst = useRef<ChartInst|null>(null);
  useChartJs(()=>{
    if (!ref.current) return;
    const C = (window as unknown as {Chart:ChartCtor}).Chart;
    inst.current?.destroy();
    const d = FAILED[period];
    inst.current = new C(ref.current, {
      type:"bar",
      data:{ labels:d.labels, datasets:[{ label:"Failed", data:d.vals, backgroundColor:"#c8a84b", borderRadius:5, borderSkipped:false }] },
      options:{
        responsive:true, maintainAspectRatio:false,
        plugins:{ legend:{display:false}, tooltip:{backgroundColor:"#fff",titleColor:"#2a3a2a",bodyColor:"#5a6a5a",borderColor:"#dde8dd",borderWidth:1,padding:10} },
        scales:{
          x:{grid:{display:false},ticks:{font:{size:10},color:"#9aaa9a"}},
          y:{grid:{color:"rgba(0,0,0,0.045)"},ticks:{font:{size:10},color:"#9aaa9a"},beginAtZero:true},
        },
      },
    });
  },[period]);
  return <canvas ref={ref} />;
}

function DonutCanvas() {
  const ref  = useRef<HTMLCanvasElement>(null);
  const inst = useRef<ChartInst|null>(null);
  useChartJs(()=>{
    if (!ref.current) return;
    const C = (window as unknown as {Chart:ChartCtor}).Chart;
    inst.current?.destroy();
    inst.current = new C(ref.current, {
      type:"doughnut",
      data:{
        labels:["Total Deposits","Total Withdrawals","Total Transfers"],
        datasets:[{ data:[52.8,35.7,11.5], backgroundColor:["#4db825","#c8a84b","#3a7abf"], borderWidth:2, borderColor:"#fff", hoverOffset:6 }],
      },
      options:{
        responsive:true, maintainAspectRatio:false,
        cutout:"58%",
        plugins:{ legend:{display:false}, tooltip:{backgroundColor:"#fff",titleColor:"#2a3a2a",bodyColor:"#5a6a5a",borderColor:"#dde8dd",borderWidth:1,padding:10,callbacks:{label:(c:{parsed:number})=>`${c.parsed}%`}} },
      },
    });
  },[]);
  return <canvas ref={ref} />;
}

function PeriodPicker({ value, onChange }:{ value:Period; onChange:(p:Period)=>void }) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  useEffect(()=>{
    const h=(e:MouseEvent)=>{ if(ref.current&&!ref.current.contains(e.target as Node)) setOpen(false); };
    document.addEventListener("mousedown",h);
    return ()=>document.removeEventListener("mousedown",h);
  },[]);
  return (
    <div className={styles.picker} ref={ref}>
      <button className={styles.pickerBtn} onClick={()=>setOpen(p=>!p)}>
        <Calendar size={12} /> {value} <ChevronDown size={11} />
      </button>
      {open&&(
        <div className={styles.pickerDrop}>
          {PERIODS.map(p=>(
            <button key={p} className={`${styles.pickerOpt} ${value===p?styles.pickerOptActive:""}`}
              onClick={()=>{onChange(p);setOpen(false);}}>{p}</button>
          ))}
        </div>
      )}
    </div>
  );
}

export default function ReportsPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [period,      setPeriod]      = useState<Period>("Last 7 Days");
  const [page,        setPage]        = useState(1);
  const [perPage,     setPerPage]     = useState(10);

  const total      = 14;
  const totalPages = Math.max(1, Math.ceil(total/perPage));
  const goTo = (p:number) => setPage(Math.max(1,Math.min(totalPages,p)));

  return (
    <div>
      <Header onMenuToggle={()=>setSidebarOpen(p=>!p)} />
      <Sidebar isOpen={sidebarOpen} onClose={()=>setSidebarOpen(false)} />

      <div className={styles.layout}>
        <div className={styles.sidebarSpacer} />

        <main className={styles.main}>

          {/* Title */}
          <div className={styles.titleRow}>
            <BarChart2 size={26} className={styles.titleIcon} />
            <div>
              <h1 className={styles.pageTitle}>Reports</h1>
              <p className={styles.pageSubtitle}>Financial reporting.</p>
            </div>
          </div>

          {/* Stat cards */}
          <div className={styles.statGrid}>
            {STATS.map(({ label, value, color, Icon, iconBg })=>(
              <div key={label} className={styles.statCard}>
                <div className={styles.statIcon} style={{ background:iconBg }}>
                  <Icon size={20} color={color} />
                </div>
                <p className={styles.statLabel}>{label}</p>
                <p className={styles.statValue} style={{ color }}>{value}</p>
              </div>
            ))}
          </div>

          {/* Charts */}
          <div className={styles.chartsGrid}>

            {/* LEFT */}
            <div className={styles.chartsLeft}>
              <div className={styles.card}>
                <div className={styles.cardHead}>
                  <h3 className={styles.cardTitle}>Financial Report</h3>
                  <PeriodPicker value={period} onChange={setPeriod} />
                </div>
                <div className={styles.lineWrap}>
                  <LineChartCanvas id="c1" src={FINANCIAL} period={period} />
                </div>
                <div className={styles.legend}>
                  {[{l:"Total Deposits",c:"#4db825"},{l:"Total Withdrawals",c:"#c8a84b"},{l:"Total Transfers",c:"#3a7abf"}].map(x=>(
                    <span key={x.l} className={styles.legendItem}>
                      <span className={styles.legendDot} style={{background:x.c}} />{x.l}
                    </span>
                  ))}
                </div>
              </div>

              <div className={styles.card} style={{marginTop:14}}>
                <div className={styles.lineWrap}>
                  <LineChartCanvas id="c2" src={CHART2} period={period} />
                </div>
              </div>
            </div>

            {/* RIGHT */}
            <div className={styles.chartsRight}>
              <div className={styles.card}>
                <h3 className={styles.cardTitle}>Transaction Breakdown</h3>
                <div className={styles.donutWrap}>
                  <DonutCanvas />
                </div>
                <div className={styles.donutLegend}>
                  {[
                    {l:"Total Deposits",    pct:"52.8%",c:"#4db825"},
                    {l:"Total Withdrawals", pct:"35.7%",c:"#c8a84b"},
                    {l:"Total Transfers",   pct:"11.5%",c:"#3a7abf"},
                  ].map(x=>(
                    <div key={x.l} className={styles.donutRow}>
                      <span className={styles.legendDot} style={{background:x.c}} />
                      <span className={styles.donutLabel}>{x.l}</span>
                      <span className={styles.donutPct} style={{color:x.c}}>{x.pct}</span>
                    </div>
                  ))}
                  <div className={styles.donutSubs}>
                    <span style={{color:"#4db825"}}>2800K</span>
                    <span style={{color:"#c8a84b"}}>4&apos;45,600</span>
                    <span style={{color:"#3a7abf"}}>1140X</span>
                  </div>
                </div>
              </div>

              <div className={styles.card} style={{marginTop:14}}>
                <h3 className={styles.cardTitle}>Failed Transactions</h3>
                <div className={styles.barWrap}>
                  <BarChartCanvas period={period} />
                </div>
              </div>
            </div>

          </div>

          {/* Pagination */}
          <div className={styles.pagination}>
            <span className={styles.showingLabel}>
              Showing 1 to {Math.min(perPage,total)} of {total} entries
            </span>
            <div className={styles.pageControls}>
              <button className={styles.pageBtn} onClick={()=>goTo(page-1)} disabled={page===1}>
                <ChevronLeft size={13} /> Prev
              </button>
              {Array.from({length:Math.min(5,totalPages)},(_,i)=>{
                const p=i+1;
                return <button key={p} className={`${styles.pageNumBtn} ${page===p?styles.pageNumActive:""}`} onClick={()=>goTo(p)}>{p}</button>;
              })}
              <span className={styles.pageOf}>of {totalPages}</span>
              <select className={styles.perPageSel} value={perPage} onChange={e=>{setPerPage(Number(e.target.value));setPage(1);}}>
                {[10,25,50].map(o=><option key={o} value={o}>{o}</option>)}
              </select>
              <button className={styles.pageBtn} onClick={()=>goTo(page+1)} disabled={page===totalPages}>
                Next <ChevronRight size={13} />
              </button>
            </div>
          </div>

        </main>
      </div>
    </div>
  );
}