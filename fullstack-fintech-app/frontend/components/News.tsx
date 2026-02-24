'use client';

import React, { useState } from 'react';
import { Zap, Gift, ShieldCheck, X } from 'lucide-react';
import './News.css';

interface NewsItem {
  id: number;
  icon: React.ReactNode;
  title: string;
  desc: string;
  longDesc: string;
  date: string;
}

interface NewsProps {
  theme: 'light' | 'dark';
}

const News = ({ theme }: NewsProps) => {
  const [selectedNews, setSelectedNews] = useState<NewsItem | null>(null);

  const updates: NewsItem[] = [
    { 
      id: 1, 
      icon: <Gift color="#ff7a5c" />, 
      title: 'Refer & Earn', 
      desc: 'Get ₦1,000 for every friend you invite to e-pay.',
      longDesc: 'Start earning today by sharing your unique referral code with friends and family. Once they sign up and complete their first transaction of at least ₦2,000, both of you will receive a ₦1,000 bonus credited instantly to your wallets.',
      date: 'Active Now'
    },
    { 
      id: 2, 
      icon: <Zap color="#5eb7cd" />, 
      title: 'Virtual Cards', 
      desc: 'Our new USD virtual cards are now active. Try it now!',
      longDesc: 'You can now create USD Virtual Cards for your international subscriptions like Netflix, Spotify, and Amazon. These cards come with competitive exchange rates and zero maintenance fees for the first 3 months.',
      date: 'Updated 2 days ago'
    },
    { 
      id: 3, 
      icon: <ShieldCheck color="#5ecdbf" />, 
      title: 'Security Update', 
      desc: '2FA is now mandatory for withdrawals over ₦100k.',
      longDesc: 'To ensure the safety of your funds, we have implemented a mandatory Two-Factor Authentication (2FA) for all withdrawals exceeding ₦100,000. Please ensure your email and phone number are verified in your profile settings.',
      date: 'Security Alert'
    },
  ];

  const closeModal = () => setSelectedNews(null);
  const themeClass = theme === "dark" ? "color-dark" : "color-light";

  return (
    <div className="news-container">
      <div className="section-header">
        <h3 className={`${theme === "dark" ? "color-light" : "color-dark"}`}>Latest Updates</h3>
      </div>
      
      <div className="news-grid">
        {updates.map((news) => (
          <div 
            key={news.id} 
            className="news-card" 
            onClick={() => setSelectedNews(news)}
          >
            <div className="news-icon">{news.icon}</div>
            <div className={`news-info ${themeClass}`}>
              <h4>{news.title}</h4>
              <p>{news.desc}</p>
            </div>
          </div>
        ))}
      </div>

      {selectedNews && (
        <div className="news-modal-overlay" onClick={closeModal}>
          <div className="news-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="news-modal-close" onClick={closeModal}>
              <X size={20} />
            </button>
            
            <div className="news-modal-header">
              <div className="news-modal-icon-wrapper">
                {selectedNews.icon}
              </div>
              <div className="news-modal-title-area">
                <span className="news-tag">Announcement</span>
                <h4 className={`${theme === "dark" ? "color-light" : "color-dark"}`}>
                  {selectedNews.title}
                </h4>
              </div>
            </div>

            <div className="news-modal-body">
              <p className="news-date">{selectedNews.date}</p>
              <p className="news-description">{selectedNews.longDesc}</p>
              
              <div className="news-modal-footer">
                <button className="news-action-btn" onClick={closeModal}>
                  Got it
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default News;