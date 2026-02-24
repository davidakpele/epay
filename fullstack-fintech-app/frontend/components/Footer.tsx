'use client';

import React from 'react';
import Link from 'next/link';
import { Facebook, Twitter, Instagram, Linkedin, Send } from 'lucide-react';
import './Footer.css';

interface FooterProps {
  theme: 'light' | 'dark';
}

const Footer = ({ theme }: FooterProps) => {
  const themeClass = theme === "dark" ? "color-light" : "color-light";

  return (
    <footer className={`main-footer ${theme}`}>
      <div className="footer-content">
        <div className="footer-section brand-info">
          <div className="footer-logo">
            <div className="logo-icon-small">ePay</div>
          </div>
          <strong className={`footer-desc ${theme === "dark" ? "color-light" : "color-light"}`}>
            The smartest way to manage your finances, pay bills, and send money across borders with zero stress.
          </strong>
          <div className="social-links">
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}><Facebook size={18} /></Link>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}><Twitter size={18} /></Link>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}><Instagram size={18} /></Link>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}><Linkedin size={18} /></Link>
          </div>
        </div>

        <div className="footer-links-grid">
          <div className="footer-section">
            <h4 className={`${theme === "dark" ? "color-light" : "color-light"}`}>Company</h4>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}>About Us</Link>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}>Contact</Link>
          </div>

          <div className="footer-section">
            <h4 className={`${theme === "dark" ? "color-light" : "color-light"}`}>Legal</h4>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}>Privacy Policy</Link>
            <Link href="#" className={`${theme === "dark" ? "color-light" : "color-light"}`}>Terms of Service</Link>
          </div>

          <div className="footer-section newsletter">
            <h4 className={`${theme === "dark" ? "color-light" : "color-light"}`}>Newsletter</h4>
            <p className={`${theme === "dark" ? "color-light" : "color-light"}`}>Get the latest updates on new features.</p>
            <div className="newsletter-input-box">
              <input type="email" placeholder="Your email" />
              <button className="subscribe-btn"><Send size={16} /></button>
            </div>
          </div>
        </div>
      </div>

      <div className="footer-bottom">
        <p className={`${theme === "dark" ? "color-light" : "color-light"}`}>&copy; 2025 e-pay Financial Services. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;