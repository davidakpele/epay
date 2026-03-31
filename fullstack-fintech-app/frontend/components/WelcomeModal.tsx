'use client';

import React, { useEffect, useState } from 'react';
import './WelcomeModal.css';
import { hasSeenWelcome, markWelcomeAsSeen } from '@/app/api';

interface WelcomeModalProps {
  userName?: string;
  imageSrc?: string;
}

const WelcomeModal: React.FC<WelcomeModalProps> = ({
  userName = 'David',
  imageSrc = '/assets/images/welcome-img.png',
}) => {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    if (!hasSeenWelcome()) {
      setVisible(true);
    }
  }, []);

  const handleClose = () => {
    markWelcomeAsSeen();
    setVisible(false);
  };

  if (!visible) return null;

  return (
    <div className="wm-overlay" role="dialog" aria-modal="true" aria-label="Welcome modal">
      <div className="wm-backdrop" onClick={handleClose} />
      <div className="wm-card">
        <button className="wm-close" onClick={handleClose} aria-label="Close">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M1 1l16 16M17 1L1 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
        </button>
        <div className="wm-body">
          <div className="wm-text">
            <h2 className="wm-title">
              Welcome to ePay, <span className="wm-name">{userName}!</span>
            </h2>
            <p className="wm-line">Thank you for choosing ePay!</p>
            <p className="wm-line">We're thrilled to have you on board.</p>
            <p className="wm-line">
              Manage your finances easily and securely with our platform.{' '}
              <span className="wm-highlight">Happy transacting!</span>
            </p>
            <button className="wm-cta" onClick={handleClose}>
              Get Started
            </button>
          </div>
          <div className="wm-illustration">
            <img
              src={imageSrc}
              alt="ePay welcome illustration"
              className="wm-image"
              draggable={false}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default WelcomeModal;