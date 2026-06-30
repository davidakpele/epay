'use client';

import React, { useState } from 'react';

interface DateFilterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApply: (startDate: string, endDate: string) => void;
  onClear: () => void;
  initialStartDate?: string;
  initialEndDate?: string;
}

const DAYS = ['S', 'M', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
const MONTHS = [
  'January','February','March','April','May','June',
  'July','August','September','October','November','December'
];

function getDaysInMonth(year: number, month: number) {
  return new Date(year, month + 1, 0).getDate();
}

function getFirstDayOfMonth(year: number, month: number) {
  return new Date(year, month, 1).getDay();
}

function toDateStr(year: number, month: number, day: number): string {
  return `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
}

function parseDateStr(str: string): { year: number; month: number; day: number } | null {
  if (!str) return null;
  const [y, m, d] = str.split('-').map(Number);
  return { year: y, month: m - 1, day: d };
}

const DateFilterModal: React.FC<DateFilterModalProps> = ({
  isOpen,
  onClose,
  onApply,
  onClear,
  initialStartDate = '',
  initialEndDate = '',
}) => {
  const today = new Date();
  const [viewYear, setViewYear] = useState(today.getFullYear());
  const [viewMonth, setViewMonth] = useState(today.getMonth());
  const [startDate, setStartDate] = useState(initialStartDate);
  const [endDate, setEndDate] = useState(initialEndDate);
  const [selecting, setSelecting] = useState<'start' | 'end'>('start');

  if (!isOpen) return null;

  const daysInMonth = getDaysInMonth(viewYear, viewMonth);
  const firstDay = getFirstDayOfMonth(viewYear, viewMonth);

  const handleQuickSelect = (days: number) => {
    const end = new Date();
    const start = new Date();
    if (days === 0) {
      const s = toDateStr(end.getFullYear(), end.getMonth(), end.getDate());
      setStartDate(s);
      setEndDate(s);
    } else {
      start.setDate(start.getDate() - days);
      setStartDate(toDateStr(start.getFullYear(), start.getMonth(), start.getDate()));
      setEndDate(toDateStr(end.getFullYear(), end.getMonth(), end.getDate()));
    }
    setSelecting('start');
  };

  const handleDayClick = (day: number) => {
    const dateStr = toDateStr(viewYear, viewMonth, day);
    if (selecting === 'start') {
      setStartDate(dateStr);
      setEndDate('');
      setSelecting('end');
    } else {
      if (startDate && dateStr < startDate) {
        setEndDate(startDate);
        setStartDate(dateStr);
      } else {
        setEndDate(dateStr);
      }
      setSelecting('start');
    }
  };

  const isInRange = (day: number): boolean => {
    if (!startDate || !endDate) return false;
    const d = toDateStr(viewYear, viewMonth, day);
    return d > startDate && d < endDate;
  };

  const isStart = (day: number) => toDateStr(viewYear, viewMonth, day) === startDate;
  const isEnd = (day: number) => toDateStr(viewYear, viewMonth, day) === endDate;
  const isToday = (day: number) =>
    viewYear === today.getFullYear() &&
    viewMonth === today.getMonth() &&
    day === today.getDate();

  const prevMonth = () => {
    if (viewMonth === 0) { setViewMonth(11); setViewYear(y => y - 1); }
    else setViewMonth(m => m - 1);
  };

  const nextMonth = () => {
    if (viewMonth === 11) { setViewMonth(0); setViewYear(y => y + 1); }
    else setViewMonth(m => m + 1);
  };

  const formatDisplay = (dateStr: string) => {
    if (!dateStr) return '·';
    const p = parseDateStr(dateStr);
    if (!p) return '·';
    return `${MONTHS[p.month].slice(0, 3)} ${p.day}`;
  };

  const handleClear = () => {
    setStartDate('');
    setEndDate('');
    setSelecting('start');
    onClear();
  };

  const handleApply = () => {
    onApply(startDate, endDate);
    onClose();
  };

  const cells = Array.from({ length: firstDay + daysInMonth }, (_, i) =>
    i < firstDay ? null : i - firstDay + 1
  );

  return (
    <>
      <style>{`
        .dfm-overlay {
          position: fixed;
          inset: 0;
          background: rgba(0,0,0,0.6);
          z-index: 9998;
          animation: dfmFadeIn 0.2s ease;
        }
        @keyframes dfmFadeIn {
          from { opacity: 0; }
          to   { opacity: 1; }
        }
        .dfm-modal {
          position: fixed;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 450px;
          height:auto;
          max-width: 95vw;
          background: #0f172a;
          border-radius: 16px;
          padding: 28px 32px 28px 32px;
          z-index: 9999;
          animation: dfmSlideIn 0.28s cubic-bezier(0.22, 1, 0.36, 1);
          font-family: 'DM Sans', system-ui, sans-serif;
        }
        @keyframes dfmSlideIn {
          from { opacity: 0; transform: translate(-50%, -48%) scale(0.96); }
          to   { opacity: 1; transform: translate(-50%, -50%) scale(1); }
        }

        /* Header */
        .dfm-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 0px;
        }
        .dfm-header-left {
          display: flex;
          align-items: center;
          gap: 12px;
        }
        .dfm-filter-icon {
          width: 22px; height: 22px;
          color: #fff;
        }
        .dfm-title {
          color: #fff;
          font-size: 20px;
          font-weight: 700;
          letter-spacing: -0.3px;
          margin: 0;
        }
        .dfm-close-btn {
          background: #1e293b;
          border: none;
          border-radius: 8px;
          width: 36px; height: 36px;
          display: flex; align-items: center; justify-content: center;
          cursor: pointer;
          color: #94a3b8;
          transition: background 0.15s, color 0.15s;
        }
        .dfm-close-btn:hover { background: #334155; color: #fff; }

        /* Quick Select */
        .dfm-quick-label {
          font-size: 12px;
          font-weight: 700;
          color: #64748b;
          letter-spacing: 1px;
          text-transform: uppercase;
          margin-bottom: 0px;
        }
        .dfm-quick-btns {
          display: flex;
          gap: 10px;
          margin-bottom: 12px;
        }
        .dfm-quick-btn {
          flex: 1;
          padding: 11px 0;
          background: #47257a;
          border: none;
          border-radius: 8px;
          color: #fff;
          font-size: 14px;
          font-weight: 600;
          cursor: pointer;
          transition: background 0.15s, transform 0.1s;
        }
        .dfm-quick-btn:hover { background: #166534; transform: translateY(-1px); }
        .dfm-quick-btn:active { transform: translateY(0); }

        /* Custom Range */
        .dfm-range-label {
          font-size: 12px;
          font-weight: 700;
          color: #64748b;
          letter-spacing: 1px;
          text-transform: uppercase;
          
        }
        .dfm-range-inputs {
          display: grid;
          grid-template-columns: 1fr auto 1fr;
          gap: 10px;
          align-items: center;
          margin-bottom: 20px;
        }
        .dfm-input-wrap {
          position: relative;
        }
        .dfm-cal-icon {
          position: absolute;
          left: 12px;
          top: 50%;
          transform: translateY(-50%);
          width: 16px; 
          height: 16px;
          color: #2b0f56;
          pointer-events: none;
        }
        .dfm-date-input {
            width: 100%;
            background: #2b0f56;
            border: 1.5px solid #e2e8f0;
            border-radius: 8px;
            padding: 11px 12px 11px 38px;
            color: #94a3b8;
            font-size: 14px;
            outline: none;
            transition: border-color 0.2s;
            box-sizing: border-box;
        }
        .dfm-date-input:focus {
          border-color: #2b0f56;
          color: #fff;
        }
        .dfm-date-sep {
          color: #2b0f56;
          font-weight: 700;
          font-size: 16px;
          text-align: center;
        }

        /* Range display pill */
        .dfm-range-pill {
          display: flex;
          align-items: center;
          gap: 6px;
          justify-content: flex-end;
          margin-bottom: 6px;
          font-size: 13px;
          color: #94a3b8;
        }
        .dfm-range-pill span.active { color: #fff; font-weight: 600; }
        .dfm-range-pill .arrow {
          background: #1e293b;
          border-radius: 4px;
          padding: 2px 6px;
          color: #2b0f56;
          font-size: 12px;
        }

        /* Calendar */
        .dfm-calendar {
            background: #8abaff2e;
            border-radius: 12px;
            padding: 16px;
            margin-bottom: 20px;
        }
        .dfm-cal-nav {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 4px;
        }
        .dfm-cal-nav-btn {
          background: #0f172a;
          border: none;
          border-radius: 6px;
          width: 30px; height: 30px;
          display: flex; align-items: center; justify-content: center;
          cursor: pointer;
          color: #94a3b8;
          transition: background 0.15s, color 0.15s;
        }
        .dfm-cal-nav-btn:hover { background: #334155; color: #fff; }
        .dfm-cal-month {
          color: #fff;
          font-weight: 700;
          font-size: 15px;
        }
        .dfm-days-header {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          margin-bottom: 0px;
        }
        .dfm-day-label {
          text-align: center;
          font-size: 12px;
          font-weight: 600;
          color: #64748b;
          padding: 4px 0;
        }
        .dfm-days-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 2px;
        }
        .dfm-day-cell {
          aspect-ratio: 1;
          display: flex; align-items: center; justify-content: center;
          font-size: 13px;
          font-weight: 500;
          color: #cbd5e1;
          border-radius: 6px;
          cursor: pointer;
          position: relative;
          transition: background 0.15s, color 0.15s;
          user-select: none;
        }
        .dfm-day-cell:hover:not(.dfm-day-empty) {
          background: #334155;
          color: #fff;
        }
        .dfm-day-cell.dfm-in-range {
          background: rgba(74, 222, 128, 0.15);
          border-radius: 0;
          color: #fff;
        }
        .dfm-day-cell.dfm-is-start,
        .dfm-day-cell.dfm-is-end {
          background: #16a34a !important;
          color: #fff !important;
          border-radius: 6px !important;
          font-weight: 700;
        }
        .dfm-day-cell.dfm-is-start.dfm-in-range-adj {
          border-radius: 6px 0 0 6px !important;
        }
        .dfm-day-cell.dfm-is-end.dfm-in-range-adj {
          border-radius: 0 6px 6px 0 !important;
        }
        .dfm-day-cell.dfm-today:not(.dfm-is-start):not(.dfm-is-end)::after {
          content: '';
          position: absolute;
          bottom: 4px;
          left: 50%;
          transform: translateX(-50%);
          width: 4px; height: 4px;
          background: #2b0f56;
          border-radius: 50%;
        }
        .dfm-day-cell.dfm-empty { cursor: default; }

        /* Footer Actions */
        .dfm-footer {
          display: flex;
          gap: 12px;
        }
        .dfm-clear-btn {
            height: 40px;
            flex: 1;
            padding: 4px;
            background: #fff;
            border: 1.5px solid #334155;
            border-radius: 10px;
            color: #2b0f56;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: border-color 0.2s, color 0.2s, background 0.2s;
        }
    
        .dfm-apply-btn {
            flex: 1;
            height: 40px;
            padding: 4px;
            background: #6836c9b0;
            border: none;
            border-radius: 10px;
            color: #fff;
            font-size: 15px;
            font-weight: 400;
            cursor: pointer;
            transition: background 0.15s, transform 0.1s;
            box-shadow: 0 4px 14px rgba(22, 163, 74, 0.3);
        }
        .dfm-apply-btn:hover { background: #2b0f56; transform: translateY(-1px); }
        .dfm-apply-btn:active { transform: translateY(0); }

        @media (max-width: 640px) {
          .dfm-modal { padding: 20px 16px; }
          .dfm-range-inputs { grid-template-columns: 1fr auto 1fr; }
          .dfm-quick-btns { gap: 6px; }
          .dfm-quick-btn { font-size: 12px; padding: 10px 0; }
        }
      `}</style>

      <div className="dfm-overlay" onClick={onClose} />
      <div className="dfm-modal" onClick={e => e.stopPropagation()}>
        {/* Header */}
        <div className="dfm-header">
          <div className="dfm-header-left">
            <svg className="dfm-filter-icon" viewBox="0 0 24 24" fill="none">
              <path d="M3 6h18M7 12h10M11 18h2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <h2 className="dfm-title">Filter by Date</h2>
          </div>
          <button className="dfm-close-btn" onClick={onClose}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        {/* Quick Select */}
        <div className="dfm-quick-label">QUICK SELECT</div>
        <div className="dfm-quick-btns">
          <button className="dfm-quick-btn" onClick={() => handleQuickSelect(0)}>Today</button>
          <button className="dfm-quick-btn" onClick={() => handleQuickSelect(7)}>Last 7 Days</button>
          <button className="dfm-quick-btn" onClick={() => handleQuickSelect(30)}>Last 30 Days</button>
        </div>

        {/* Custom Range */}
        <div className="dfm-range-label">CUSTOM RANGE</div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '8px', alignItems: 'center', gap: '6px' }}>
          <span style={{ fontSize: '13px', color: '#64748b' }}>From</span>
          <span style={{ fontSize: '13px', color: startDate ? '#fff' : '#64748b', fontWeight: startDate ? 600 : 400 }}>
            {formatDisplay(startDate)}
          </span>
          <span style={{ fontSize: '13px', color: '#64748b' }}>· To</span>
          <span style={{ fontSize: '13px', color: endDate ? '#fff' : '#64748b', fontWeight: endDate ? 600 : 400 }}>
            {formatDisplay(endDate)}
          </span>
          <div style={{
            background: '#1e293b', borderRadius: '4px', padding: '2px 8px',
            color: '#2b0f56', fontSize: '12px', marginLeft: '4px', cursor: 'pointer'
          }}>›</div>
        </div>

        <div className="dfm-range-inputs">
          <div className="dfm-input-wrap">
            <svg className="dfm-cal-icon" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="4" width="18" height="18" rx="3" stroke="currentColor" strokeWidth="2"/>
              <path d="M8 2v4M16 2v4M3 10h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <input
              type="date"
              className="dfm-date-input"
              value={startDate}
              onChange={e => { setStartDate(e.target.value); setSelecting('end'); }}
              max={endDate || new Date().toISOString().split('T')[0]}
            />
          </div>
          <span className="dfm-date-sep">–</span>
          <div className="dfm-input-wrap">
            <svg className="dfm-cal-icon" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="4" width="18" height="18" rx="3" stroke="currentColor" strokeWidth="2"/>
              <path d="M8 2v4M16 2v4M3 10h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <input
              type="date"
              className="dfm-date-input"
              value={endDate}
              onChange={e => { setEndDate(e.target.value); setSelecting('start'); }}
              min={startDate}
              max={new Date().toISOString().split('T')[0]}
            />
          </div>
        </div>

        {/* Calendar */}
        <div className="dfm-calendar">
          <div className="dfm-cal-nav">
            <button className="dfm-cal-nav-btn" onClick={prevMonth}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M15 18l-6-6 6-6" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
            <span className="dfm-cal-month">{MONTHS[viewMonth]} {viewYear}</span>
            <button className="dfm-cal-nav-btn" onClick={nextMonth}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M9 18l6-6-6-6" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
          </div>

          <div className="dfm-days-header">
            {DAYS.map(d => <div key={d} className="dfm-day-label">{d}</div>)}
          </div>

          <div className="dfm-days-grid">
            {cells.map((day, i) => {
              if (!day) return <div key={`e-${i}`} className="dfm-day-cell dfm-empty" />;
              const start = isStart(day);
              const end = isEnd(day);
              const inRange = isInRange(day);
              const todayCell = isToday(day);

              return (
                <div
                  key={day}
                  className={[
                    'dfm-day-cell',
                    start ? 'dfm-is-start' : '',
                    end ? 'dfm-is-end' : '',
                    inRange ? 'dfm-in-range' : '',
                    todayCell ? 'dfm-today' : '',
                  ].filter(Boolean).join(' ')}
                  onClick={() => handleDayClick(day)}
                >
                  {day}
                </div>
              );
            })}
          </div>
        </div>

        {/* Footer */}
        <div className="dfm-footer">
          <button className="dfm-clear-btn" onClick={handleClear}>Clear</button>
          <button className="dfm-apply-btn" onClick={handleApply}>Apply</button>
        </div>
      </div>
    </>
  );
};

export default DateFilterModal;