"use client";

import React, { useState } from "react";

interface DateFilterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApply: (startDate: string, endDate: string) => void;
  onClear: () => void;
  initialStartDate?: string;
  initialEndDate?: string;
}

const DAYS = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];
const MONTHS = [
  "January",
  "February",
  "March",
  "April",
  "May",
  "June",
  "July",
  "August",
  "September",
  "October",
  "November",
  "December",
];

function getDaysInMonth(year: number, month: number) {
  return new Date(year, month + 1, 0).getDate();
}

function getFirstDayOfMonth(year: number, month: number) {
  return new Date(year, month, 1).getDay();
}

function toDateStr(year: number, month: number, day: number): string {
  return `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

function fmtShort(dateStr: string): string {
  if (!dateStr) return "";
  const [y, m, d] = dateStr.split("-").map(Number);
  return `${MONTHS[m - 1].slice(0, 3)} ${d}, ${y}`;
}

const DateFilterModal: React.FC<DateFilterModalProps> = ({
  isOpen,
  onClose,
  onApply,
  onClear,
  initialStartDate = "",
  initialEndDate = "",
}) => {
  const today = new Date();
  const [viewYear, setViewYear] = useState(today.getFullYear());
  const [viewMonth, setViewMonth] = useState(today.getMonth());
  const [startDate, setStartDate] = useState(initialStartDate);
  const [endDate, setEndDate] = useState(initialEndDate);
  const [selecting, setSelecting] = useState<"start" | "end">("start");

  if (!isOpen) return null;

  /* ── helpers ── */
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
      setStartDate(
        toDateStr(start.getFullYear(), start.getMonth(), start.getDate()),
      );
      setEndDate(toDateStr(end.getFullYear(), end.getMonth(), end.getDate()));
    }
    setSelecting("start");
  };

  const handleDayClick = (day: number) => {
    const ds = toDateStr(viewYear, viewMonth, day);
    if (selecting === "start") {
      setStartDate(ds);
      setEndDate("");
      setSelecting("end");
    } else {
      if (startDate && ds < startDate) {
        setEndDate(startDate);
        setStartDate(ds);
      } else {
        setEndDate(ds);
      }
      setSelecting("start");
    }
  };

  const isStart = (day: number) =>
    toDateStr(viewYear, viewMonth, day) === startDate;
  const isEnd = (day: number) =>
    toDateStr(viewYear, viewMonth, day) === endDate;
  const isInRange = (day: number) => {
    if (!startDate || !endDate) return false;
    const d = toDateStr(viewYear, viewMonth, day);
    return d > startDate && d < endDate;
  };
  const isToday = (day: number) =>
    viewYear === today.getFullYear() &&
    viewMonth === today.getMonth() &&
    day === today.getDate();

  const prevMonth = () => {
    if (viewMonth === 0) {
      setViewMonth(11);
      setViewYear((y) => y - 1);
    } else setViewMonth((m) => m - 1);
  };
  const nextMonth = () => {
    if (viewMonth === 11) {
      setViewMonth(0);
      setViewYear((y) => y + 1);
    } else setViewMonth((m) => m + 1);
  };

  const handleClear = () => {
    setStartDate("");
    setEndDate("");
    setSelecting("start");
    onClear();
  };

  const handleApply = () => {
    onApply(startDate, endDate);
    onClose();
  };

  // pad leading empty cells
  const cells = Array.from({ length: firstDay + daysInMonth }, (_, i) =>
    i < firstDay ? null : i - firstDay + 1,
  );

  const hasSelection = startDate || endDate;

  return (
    <>
      <style>{`
        /* ── overlay ── */
        .dfm-overlay {
          position: fixed;
          inset: 0;
          background: rgba(15, 23, 42, 0.45);
          z-index: 9998;
          backdrop-filter: blur(2px);
          animation: dfmFadeIn 0.18s ease;
        }
        @keyframes dfmFadeIn { from { opacity: 0; } to { opacity: 1; } }

        /* ── modal shell ── */
        .dfm-modal {
          position: fixed;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 400px;
          max-width: calc(100vw - 32px);
          background: #ffffff;
          border-radius: 18px;
          box-shadow: 0 20px 60px rgba(0,0,0,0.15), 0 4px 16px rgba(0,0,0,0.08);
          z-index: 9999;
          overflow: hidden;
          animation: dfmSlideIn 0.22s cubic-bezier(0.22, 1, 0.36, 1);
          font-family: 'Outfit', 'DM Sans', system-ui, sans-serif;
        }
        @keyframes dfmSlideIn {
          from { opacity: 0; transform: translate(-50%, -46%) scale(0.95); }
          to   { opacity: 1; transform: translate(-50%, -50%) scale(1); }
        }

        /* ── header ── */
        .dfm-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 16px 18px 12px;
          border-bottom: 1px solid #f1f5f9;
        }
        .dfm-header-left {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        .dfm-title {
          font-size: 15px;
          font-weight: 700;
          color: #0f172a;
          margin: 0;
        }
        .dfm-close-btn {
          width: 28px;
          height: 28px;
          border-radius: 8px;
          background: #f1f5f9;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #64748b;
          transition: background 0.15s, color 0.15s;
          flex-shrink: 0;
        }
        .dfm-close-btn:hover { background: #e2e8f0; color: #0f172a; }

        /* ── body ── */
        .dfm-body { padding: 14px 18px; }

        /* ── quick chips ── */
        .dfm-quick-label {
          font-size: 10px;
          font-weight: 700;
          letter-spacing: 0.08em;
          text-transform: uppercase;
          color: #94a3b8;
          margin-bottom: 8px;
        }
        .dfm-quick-chips {
          display: flex;
          gap: 6px;
          margin-bottom: 16px;
          flex-wrap: wrap;
        }
        .dfm-chip {
          padding: 5px 12px;
          border-radius: 20px;
          border: 1.5px solid #e2e8f0;
          background: #fff;
          font-size: 12px;
          font-weight: 600;
          color: #475569;
          cursor: pointer;
          transition: border-color 0.15s, background 0.15s, color 0.15s;
          white-space: nowrap;
        }
        .dfm-chip:hover {
          border-color: var(--bg-main, #16a34a);
          color: var(--bg-main, #16a34a);
          background: #f0fdf4;
        }
        .dfm-chip.active {
          border-color: var(--bg-main, #16a34a);
          background: var(--bg-main, #16a34a);
          color: #fff;
        }

        /* ── selected range pill ── */
        .dfm-range-display {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 8px 12px;
          background: #f8fafc;
          border: 1px solid #e2e8f0;
          border-radius: 10px;
          margin-bottom: 14px;
          min-height: 36px;
        }
        .dfm-range-date {
          flex: 1;
          text-align: center;
          font-size: 12px;
          font-weight: 600;
          color: #0f172a;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        .dfm-range-placeholder {
          flex: 1;
          text-align: center;
          font-size: 12px;
          color: #cbd5e1;
        }
        .dfm-range-arrow {
          color: #94a3b8;
          font-size: 14px;
          flex-shrink: 0;
        }
        .dfm-range-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: var(--bg-main, #16a34a);
          flex-shrink: 0;
          animation: dfmPulse 1.5s infinite;
        }
        @keyframes dfmPulse {
          0%, 100% { opacity: 1; transform: scale(1); }
          50%       { opacity: 0.5; transform: scale(0.8); }
        }
        .dfm-selecting-hint {
          font-size: 10.5px;
          color: #94a3b8;
          text-align: center;
          margin-bottom: 10px;
          font-style: italic;
        }

        /* ── calendar ── */
        .dfm-cal {
          margin-bottom: 14px;
        }
        .dfm-cal-nav {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 10px;
        }
        .dfm-cal-nav-btn {
          width: 28px;
          height: 28px;
          border-radius: 8px;
          background: #f1f5f9;
          border: none;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #475569;
          transition: background 0.15s, color 0.15s;
        }
        .dfm-cal-nav-btn:hover { background: #e2e8f0; color: #0f172a; }
        .dfm-cal-month {
          font-size: 14px;
          font-weight: 700;
          color: #0f172a;
        }

        /* day-of-week labels */
        .dfm-days-header {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          margin-bottom: 4px;
        }
        .dfm-day-label {
          text-align: center;
          font-size: 11px;
          font-weight: 600;
          color: #94a3b8;
          padding: 2px 0;
        }

        /* day cells */
        .dfm-days-grid {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          gap: 1px;
        }
        .dfm-day-cell {
          aspect-ratio: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12.5px;
          font-weight: 500;
          color: #334155;
          border-radius: 7px;
          cursor: pointer;
          position: relative;
          transition: background 0.12s, color 0.12s;
          user-select: none;
        }
        .dfm-day-cell:hover:not(.dfm-day-empty) {
          background: #f1f5f9;
          color: #0f172a;
        }
        .dfm-day-cell.dfm-in-range {
          background: #dcfce7;
          border-radius: 0;
          color: #15803d;
          font-weight: 600;
        }
        .dfm-day-cell.dfm-is-start,
        .dfm-day-cell.dfm-is-end {
          background: var(--bg-main, #16a34a) !important;
          color: #fff !important;
          font-weight: 700;
          border-radius: 7px !important;
          z-index: 1;
        }
        .dfm-day-cell.dfm-is-start.dfm-has-end {
          border-radius: 7px 0 0 7px !important;
        }
        .dfm-day-cell.dfm-is-end.dfm-has-start {
          border-radius: 0 7px 7px 0 !important;
        }
        .dfm-day-cell.dfm-today:not(.dfm-is-start):not(.dfm-is-end)::after {
          content: '';
          position: absolute;
          bottom: 3px;
          left: 50%;
          transform: translateX(-50%);
          width: 4px;
          height: 4px;
          background: var(--bg-main, #16a34a);
          border-radius: 50%;
        }
        .dfm-day-cell.dfm-day-empty { cursor: default; pointer-events: none; }

        /* ── footer ── */
        .dfm-footer {
          display: flex;
          gap: 8px;
          padding: 12px 18px 16px;
          border-top: 1px solid #f1f5f9;
        }
        .dfm-clear-btn {
          flex: 1;
          height: 38px;
          background: #fff;
          border: 1.5px solid #e2e8f0;
          border-radius: 10px;
          color: #64748b;
          font-size: 13px;
          font-weight: 600;
          cursor: pointer;
          transition: border-color 0.15s, color 0.15s;
        }
        .dfm-clear-btn:hover { border-color: #ef4444; color: #ef4444; }
        .dfm-apply-btn {
          flex: 2;
          height: 38px;
          background: var(--bg-main, #16a34a);
          border: none;
          border-radius: 10px;
          color: #fff;
          font-size: 13px;
          font-weight: 700;
          cursor: pointer;
          transition: opacity 0.15s;
        }
        .dfm-apply-btn:hover { opacity: 0.88; }
        .dfm-apply-btn:disabled {
          opacity: 0.4;
          cursor: not-allowed;
        }

        /* ── mobile ── */
        @media (max-width: 400px) {
          .dfm-modal   { width: calc(100vw - 24px); border-radius: 14px; }
          .dfm-day-cell { font-size: 11.5px; }
          .dfm-body    { padding: 12px 14px; }
          .dfm-footer  { padding: 10px 14px 14px; }
          .dfm-header  { padding: 14px 14px 10px; }
        }
      `}</style>

      <div className="dfm-overlay" onClick={onClose} />

      <div className="dfm-modal" onClick={(e) => e.stopPropagation()}>
        {/* ── Header ── */}
        <div className="dfm-header">
          <div className="dfm-header-left">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              style={{ color: "var(--bg-main, #16a34a)" }}
            >
              <path
                d="M3 6h18M7 12h10M11 18h2"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
              />
            </svg>
            <h2 className="dfm-title">Filter by Date</h2>
          </div>
          <button
            className="dfm-close-btn"
            onClick={onClose}
            aria-label="Close"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path
                d="M18 6L6 18M6 6l12 12"
                stroke="currentColor"
                strokeWidth="2.5"
                strokeLinecap="round"
              />
            </svg>
          </button>
        </div>

        <div className="dfm-body">
          {/* ── Quick chips ── */}
          <div className="dfm-quick-label">Quick Select</div>
          <div className="dfm-quick-chips">
            {[
              { label: "Today", days: 0 },
              { label: "Last 7 days", days: 7 },
              { label: "Last 30 days", days: 30 },
              { label: "This month", days: -1 },
            ].map(({ label, days }) => (
              <button
                key={label}
                className="dfm-chip"
                onClick={() => {
                  if (days === -1) {
                    // current month
                    const now = new Date();
                    const start = toDateStr(
                      now.getFullYear(),
                      now.getMonth(),
                      1,
                    );
                    const end = toDateStr(
                      now.getFullYear(),
                      now.getMonth(),
                      now.getDate(),
                    );
                    setStartDate(start);
                    setEndDate(end);
                    setSelecting("start");
                  } else {
                    handleQuickSelect(days);
                  }
                }}
              >
                {label}
              </button>
            ))}
          </div>

          {/* ── Selected range display ── */}
          <div className="dfm-range-display">
            {startDate ? (
              <span className="dfm-range-date">{fmtShort(startDate)}</span>
            ) : (
              <span className="dfm-range-placeholder">Start date</span>
            )}
            <span className="dfm-range-arrow">→</span>
            {endDate ? (
              <span className="dfm-range-date">{fmtShort(endDate)}</span>
            ) : (
              <span className="dfm-range-placeholder">End date</span>
            )}
            {!endDate && startDate && <span className="dfm-range-dot" />}
          </div>

          {/* Instruction hint */}
          <div className="dfm-selecting-hint">
            {selecting === "start"
              ? "Click a day to set start date"
              : "Now click to set end date"}
          </div>

          {/* ── Calendar ── */}
          <div className="dfm-cal">
            <div className="dfm-cal-nav">
              <button
                className="dfm-cal-nav-btn"
                onClick={prevMonth}
                aria-label="Previous month"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M15 18l-6-6 6-6"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </button>
              <span className="dfm-cal-month">
                {MONTHS[viewMonth]} {viewYear}
              </span>
              <button
                className="dfm-cal-nav-btn"
                onClick={nextMonth}
                aria-label="Next month"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M9 18l6-6-6-6"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
              </button>
            </div>

            <div className="dfm-days-header">
              {DAYS.map((d) => (
                <div key={d} className="dfm-day-label">
                  {d}
                </div>
              ))}
            </div>

            <div className="dfm-days-grid">
              {cells.map((day, i) => {
                if (!day)
                  return (
                    <div
                      key={`e-${i}`}
                      className="dfm-day-cell dfm-day-empty"
                    />
                  );
                const start = isStart(day);
                const end = isEnd(day);
                const inRange = isInRange(day);
                const todayC = isToday(day);

                return (
                  <div
                    key={day}
                    className={[
                      "dfm-day-cell",
                      start ? "dfm-is-start" : "",
                      end ? "dfm-is-end" : "",
                      start && endDate ? "dfm-has-end" : "",
                      end && startDate ? "dfm-has-start" : "",
                      inRange ? "dfm-in-range" : "",
                      todayC ? "dfm-today" : "",
                    ]
                      .filter(Boolean)
                      .join(" ")}
                    onClick={() => handleDayClick(day)}
                  >
                    {day}
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* ── Footer ── */}
        <div className="dfm-footer">
          <button className="dfm-clear-btn" onClick={handleClear}>
            Clear
          </button>
          <button
            className="dfm-apply-btn"
            onClick={handleApply}
            disabled={!startDate}
          >
            Apply
            {startDate && endDate
              ? ` (${fmtShort(startDate)} – ${fmtShort(endDate)})`
              : ""}
          </button>
        </div>
      </div>
    </>
  );
};

export default DateFilterModal;
