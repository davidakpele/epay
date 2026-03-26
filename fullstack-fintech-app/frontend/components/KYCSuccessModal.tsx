'use client';

import React, { useEffect, useState } from 'react';
import "./KYCSuccessModal.css"

interface KYCSuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  message?: string;
}

const KYCSuccessModal: React.FC<KYCSuccessModalProps> = ({
  isOpen,
  onClose,
  title = 'Success!',
  message = 'Your KYC verification is complete.',
}) => {
  const [visible, setVisible] = useState(false);
  const [animateCheck, setAnimateCheck] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setVisible(true);
      const t = setTimeout(() => setAnimateCheck(true), 150);
      return () => clearTimeout(t);
    } else {
      setAnimateCheck(false);
      const t = setTimeout(() => setVisible(false), 300);
      return () => clearTimeout(t);
    }
  }, [isOpen]);

  if (!visible) return null;

  return (
    <>
      <div className="kyc-overlay" onClick={onClose}>
        <div className="kyc-modal" onClick={(e) => e.stopPropagation()}>
          <button className="kyc-close" onClick={onClose} aria-label="Close">×</button>

          <div className="kyc-icon-wrap">
            {/* Ring pulse */}
            <div className={`kyc-ring ${animateCheck ? 'animate' : ''}`} />

            {/* Confetti dots */}
            <svg className="kyc-confetti" viewBox="0 0 112 112">
              {[
                { cx: 56, cy: 4,  color: '#4ade80', tx: '0, -22px' },
                { cx: 98, cy: 20, color: '#fbbf24', tx: '18px, -14px' },
                { cx: 108, cy: 56, color: '#60a5fa', tx: '22px, 0' },
                { cx: 98, cy: 92, color: '#f472b6', tx: '18px, 14px' },
                { cx: 56, cy: 108, color: '#4ade80', tx: '0, 22px' },
                { cx: 14, cy: 92, color: '#fbbf24', tx: '-18px, 14px' },
                { cx: 4,  cy: 56, color: '#60a5fa', tx: '-22px, 0' },
                { cx: 14, cy: 20, color: '#f472b6', tx: '-18px, -14px' },
              ].map((d, i) => (
                <circle
                  key={i}
                  className={`kyc-dot ${animateCheck ? 'animate' : ''}`}
                  cx={d.cx}
                  cy={d.cy}
                  r="4"
                  fill={d.color}
                  style={{
                    '--translate': `translate(${d.tx})`,
                    animationDelay: animateCheck ? `${0.25 + i * 0.04}s` : '0s',
                  } as React.CSSProperties}
                />
              ))}
            </svg>

            {/* Green circle + checkmark */}
            <div className={`kyc-circle ${animateCheck ? 'animate' : ''}`}>
              <svg className="kyc-check" viewBox="0 0 36 36">
                <path
                  className={`kyc-check-path ${animateCheck ? 'animate' : ''}`}
                  d="M8 18 L15 25 L28 11"
                />
              </svg>
            </div>
          </div>

          <h2 className="kyc-title">{title}</h2>
          <p className="kyc-message">{message}</p>

          <button className="kyc-btn" onClick={onClose}>
            OK
          </button>
        </div>
      </div>
    </>
  );
};

export default KYCSuccessModal;