'use client';

import React, { useState, useRef, useEffect } from 'react';
import './SupportChatBot.css';
import { getUserFullName } from '@/app/api';
import { FaqChild, faqData, FaqParent } from '@/app/types/faqData';
import { CheckCircle, Clock, User2Icon } from 'lucide-react';

interface Message {
  id: number;
  role: 'user' | 'bot';
  text: string;
  time: string;
}

interface SupportChatBotProps {
  isOpen: boolean;
  onClose: () => void;
}

type Tab = 'home' | 'conversation' | 'faqs' | 'articles';
type SubView = null | 'chat' | 'call' | 'faq-detail' | 'article-detail';
type FaqView = 'categories' | 'questions' | 'answer';

const getTime = () =>
  new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

const SYSTEM_PROMPT = `You are ePay's friendly and knowledgeable support assistant. ePay is an online business banking platform that offers wallets, virtual cards, currency exchange, airtime/data top-up, bill payments (electricity, cable TV, hospital, betting), flight booking, and shopping services.

Your role:
- Help users with account issues, transactions, wallets, cards, KYC, and general platform questions
- Be concise, warm, and professional
- If you do not know something specific, offer to connect them with a human agent
- Never reveal you are Claude or made by Anthropic — you are ePay Support Bot
- Keep responses short (2-4 sentences max) unless a detailed explanation is truly needed`;

// faqs now imported from faqData.ts

const articles = [
  { title: 'How to generate account statement', count: 1, content: 'You can generate your account statement by going to History > Download Statement. Choose your date range and format (PDF or CSV), then tap Download.' },
  { title: 'How to buy airtime on ePay', count: 1, content: 'Navigate to Services > Airtime. Select your network, enter the phone number and amount, then confirm with your PIN.' },
  { title: 'Setting up your transfer PIN', count: 1, content: 'Go to Settings > Security > Transfer PIN. Enter your current password, then set a 4-digit PIN. You will use this PIN for all outgoing transfers.' },
  { title: 'OTC Trading guide', count: 1, content: 'OTC (Over-the-Counter) trading lets you exchange large amounts directly. Contact our support team to initiate an OTC trade with competitive rates.' },
  { title: 'Currency exchange explained', count: 1, content: 'ePay supports 10+ currencies. Go to Exchange, select the source and target currency, enter the amount, and confirm. Rates are updated in real-time.' },
];

const ChevronRight = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#aaa" strokeWidth="2" strokeLinecap="round"><path d="M9 18l6-6-6-6"/></svg>
);

const SearchIcon = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
);

/** Truncate to N characters; returns the truncated string and whether it was cut */
const truncateChars = (text: string, charLimit: number): { truncated: string; isTruncated: boolean } => {
  if (text.length <= charLimit) return { truncated: text, isTruncated: false };
  return { truncated: text.slice(0, charLimit).trimEnd() + '...', isTruncated: true };
};

const SupportChatBot: React.FC<SupportChatBotProps> = ({ isOpen, onClose }) => {
  const [tab, setTab] = useState<Tab>('home');
  const [subView, setSubView] = useState<SubView>(null);
  const [selectedFaq, setSelectedFaq] = useState<FaqParent | null>(null);
  const [selectedFaqChild, setSelectedFaqChild] = useState<FaqChild | null>(null);
  const [faqView, setFaqView] = useState<FaqView>('categories');
  const [faqLoading, setFaqLoading] = useState(false);
  const [selectedArticle, setSelectedArticle] = useState<typeof articles[0] | null>(null);
  const [faqSearch, setFaqSearch] = useState('');
  const [articleSearch, setArticleSearch] = useState('');
  const [callForm, setCallForm] = useState({ name: '', email: '' });
  const [callSubmitted, setCallSubmitted] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { id: 1, role: 'bot', text: "Hi there! I'm ePay's support assistant. How can I help you today?", time: getTime() },
  ]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const conversationHistory = useRef<{ role: 'user' | 'assistant'; content: string }[]>([]);
  const userName = (getUserFullName() || 'Chief').split(' ')[0];

  useEffect(() => { messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages, isTyping]);
  useEffect(() => { if (subView === 'chat' || tab === 'conversation') setTimeout(() => inputRef.current?.focus(), 300); }, [subView, tab]);

  const sendMessage = async () => {
    const text = input.trim();
    if (!text || isTyping) return;
    setMessages(prev => [...prev, { id: Date.now(), role: 'user', text, time: getTime() }]);
    setInput('');
    setIsTyping(true);
    conversationHistory.current.push({ role: 'user', content: text });
    try {
      const res = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ model: 'claude-sonnet-4-20250514', max_tokens: 1000, system: SYSTEM_PROMPT, messages: conversationHistory.current }),
      });
      const data = await res.json();
      const reply = data?.content?.[0]?.text || "I'm having trouble connecting. Please try again or email support@epay.com.";
      conversationHistory.current.push({ role: 'assistant', content: reply });
      setMessages(prev => [...prev, { id: Date.now() + 1, role: 'bot', text: reply, time: getTime() }]);
    } catch {
      setMessages(prev => [...prev, { id: Date.now() + 1, role: 'bot', text: "Sorry, I couldn't connect. Please check your internet or email support@epay.com.", time: getTime() }]);
    } finally { setIsTyping(false); }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); } };
  const handleReset = () => { conversationHistory.current = []; setMessages([{ id: Date.now(), role: 'bot', text: "Hi there! I'm ePay's support assistant. How can I help you today?", time: getTime() }]); };
  const goBack = () => {
    setFaqLoading(false);
    if (faqView === 'answer') { setFaqView('questions'); setSelectedFaqChild(null); return; }
    if (faqView === 'questions') { setFaqView('categories'); setSelectedFaq(null); return; }
    setSubView(null); setSelectedFaq(null); setSelectedArticle(null); setCallSubmitted(false); setFaqView('categories');
  };

  const openFaqCategory = (parent: FaqParent) => {
    setSelectedFaq(parent);
    setFaqLoading(true);
    // Stay on 'categories' while loading, switch to 'questions' after delay
    setTimeout(() => {
      setFaqLoading(false);
      setFaqView('questions');
    }, 2000);
  };

  const openFaqAnswer = (child: FaqChild) => {
    setSelectedFaqChild(child);
    setFaqLoading(true);
    // Stay on 'questions' view so loader renders in the questions body
    // then switch to 'answer' after delay
    setTimeout(() => {
      setFaqLoading(false);
      setFaqView('answer');
    }, 2000);
  };

  const renderHeader = (title?: string, showBack = false) => (
    <div className="scb-header">
      <div className="scb-header-left">
        {showBack
          ? <button className="scb-icon-btn" onClick={goBack}><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M19 12H5M12 5l-7 7 7 7"/></svg></button>
          : <div className="scb-avatar"><span>e</span><span className="scb-status-dot" /></div>
        }
        <div className="scb-header-info">
          <span className="scb-header-name">{title || 'ePay Support'}</span>
          {!showBack && <span className="scb-header-status">Online · Typically replies instantly</span>}
        </div>
      </div>
      <div className="scb-header-actions">
        {!showBack && <button className="scb-icon-btn" title="New chat" onClick={handleReset}><svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg></button>}
        <button className="scb-icon-btn scb-close-btn" onClick={onClose}><svg width="14" height="14" viewBox="0 0 18 18" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round"><path d="M1 1l16 16M17 1L1 17"/></svg></button>
      </div>
    </div>
  );

  const renderChatArea = () => (
    <>
      <div className="scb-messages">
        {messages.map(msg => (
          <div key={msg.id} className={`scb-msg-row ${msg.role === 'user' ? 'scb-msg-user' : 'scb-msg-bot'}`}>
            {msg.role === 'bot' && <div className="scb-bot-avatar">e</div>}
            <div className="scb-bubble-wrap">
              <div className="scb-bubble">{msg.text}</div>
              <span className="scb-msg-time">{msg.time}</span>
            </div>
          </div>
        ))}
        {isTyping && (
          <div className="scb-msg-row scb-msg-bot">
            <div className="scb-bot-avatar">e</div>
            <div className="scb-bubble-wrap"><div className="scb-bubble scb-typing"><span /><span /><span /></div></div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>
      <div className="scb-quick-replies-wrapper">
        <button className="scb-scroll-arrow" onClick={() => { const el = document.querySelector('.scb-quick-replies') as HTMLElement; if (el) el.scrollLeft -= 120; }}>&#8249;</button>
        <div className="scb-quick-replies">
          {['Check my balance', 'Virtual card issues', 'Transfer failed', 'KYC help', 'Fund my wallet', 'Card declined'].map(q => (
            <button key={q} className="scb-quick-btn" onClick={() => { setInput(q); inputRef.current?.focus(); }}>{q}</button>
          ))}
        </div>
        <button className="scb-scroll-arrow" onClick={() => { const el = document.querySelector('.scb-quick-replies') as HTMLElement; if (el) el.scrollLeft += 120; }}>&#8250;</button>
      </div>
      <div className="scb-input-row">
        <input ref={inputRef} className="scb-input" type="text" placeholder="Type your message..." value={input} onChange={e => setInput(e.target.value)} onKeyDown={handleKeyDown} disabled={isTyping} />
        <button className={`scb-send-btn ${!input.trim() || isTyping ? 'scb-send-disabled' : ''}`} onClick={sendMessage} disabled={!input.trim() || isTyping}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
        </button>
      </div>
      <div className="scb-footer">Powered by <strong>ePay AI</strong></div>
    </>
  );

  const renderBody = () => {
    // Sub-views take priority
    if (subView === 'chat') return <>{renderHeader('Chat with us', true)}{renderChatArea()}</>;
    if (subView === 'call') return (
      <>
        {renderHeader('Call us now', true)}
        <div className="scb-call-view">
          {callSubmitted ? (
            <div className="scb-call-success">
              <div className="scb-call-success-icon"><CheckCircle style={{color:"var(--bg-main)"}}/></div>
              <h3>Request Received!</h3>
              <p>Our team will call you shortly during business hours (Mon–Fri, 9AM–6PM WAT).</p>
              <button className="scb-call-btn" onClick={() => { setCallSubmitted(false); setCallForm({ name: '', email: '' }); }}>Submit another</button>
            </div>
          ) : (
            <form className="scb-call-form" onSubmit={e => { e.preventDefault(); setCallSubmitted(true); }}>
              <p className="scb-call-info">Fill in your details and we will call you back as soon as possible.</p>
              <div className="scb-call-group"><label>Full Name</label><input type="text" placeholder="Your name" value={callForm.name} onChange={e => setCallForm(p => ({ ...p, name: e.target.value }))} required /></div>
              <div className="scb-call-group"><label>Email Address</label><input type="email" placeholder="your@email.com" value={callForm.email} onChange={e => setCallForm(p => ({ ...p, email: e.target.value }))} required /></div>
              <button type="submit" className="scb-call-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.56 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.91a16 16 0 0 0 6.06 6.06l.9-.9a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 21.73 16.92z"/></svg>
                Call Now
              </button>
            </form>
          )}
        </div>
      </>
    );

    // Tab views
    if (tab === 'home') return (
      <>
        {renderHeader()}
        <div className="scb-home">
          <div className="scb-home-hero">
            <div className="scb-home-avatar-lg">e</div>
            <h2 className="scb-home-title">ePay Support</h2>
            <p className="scb-home-sub">{userName}, we are here for you</p>
          </div>
          <div className="scb-home-options">
            <button className="scb-home-option" onClick={() => { setSubView('chat'); setTab('conversation'); }}>
              <div className="scb-option-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg></div>
              <div className="scb-option-text"><span className="scb-option-label">Chat with us now</span><span className="scb-option-desc">Typically replies instantly</span></div>
              <ChevronRight />
            </button>
            <button className="scb-home-option" onClick={() => setSubView('call')}>
              <div className="scb-option-icon"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.56 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.91a16 16 0 0 0 6.06 6.06l.9-.9a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 21.73 16.92z"/></svg></div>
              <div className="scb-option-text"><span className="scb-option-label">Call us now</span><span className="scb-option-desc">Mon–Fri, 9AM–6PM WAT</span></div>
              <ChevronRight />
            </button>
          </div>
        </div>
      </>
    );

    if (tab === 'conversation') return <>{renderHeader()}{renderChatArea()}</>;

    if (tab === 'faqs') return (
      <>
        {/* Answer view — full untruncated question shown here */}
        {faqView === 'answer' && selectedFaqChild && (
          <>
            {renderHeader('Answer', true)}
            <div className="scb-answer-view">

              {/* Question pill — always shows the FULL question text */}
              <div className="scb-answer-question-pill">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                <span>{selectedFaqChild.question}</span>
              </div>

              {/* Answer card */}
              <div className="scb-answer-card">

                {/* Card header — author + time */}
                <div className="scb-answer-card-header">
                  <div className="scb-answer-author">
                    <div className="scb-answer-author-avatar">A</div>
                    <div className="scb-answer-author-info">
                      <span className="scb-answer-author-name">{selectedFaqChild.answer.user}</span>
                      <span className="scb-answer-author-role">Support Team</span>
                    </div>
                  </div>
                  <div className="scb-answer-time">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    <span>{selectedFaqChild.answer.postedTime}</span>
                  </div>
                </div>

                {/* Divider */}
                <div className="scb-answer-divider" />

                {/* Answer body */}
                <p className="scb-answer-content">{selectedFaqChild.answer.content}</p>
              </div>

              {/* CTA */}
              <button className="scb-article-chat-btn" onClick={() => { setSelectedArticle(null); setTab('conversation'); }}>Still need help? Chat with us</button>
            </div>
          </>
        )}

        {/* Questions list — truncate long question text to 45 words */}
        {faqView === 'questions' && selectedFaq && (
          <>
            {renderHeader(selectedFaq.category, true)}
            {faqLoading ? (
              <div className="scb-faq-loader-body">
                <div className="scb-faq-spinner" />
                <span className="scb-faq-loader-text">Loading answer...</span>
              </div>
            ) : (
              <div className="scb-list-view">
                {selectedFaq.children.map((child) => {
                  const { truncated, isTruncated } = truncateChars(child.question, 75);
                  return (
                    <button key={child.id} className="scb-list-item" onClick={() => openFaqAnswer(child)}>
                      <div>
                        <div className="scb-list-title">
                          {truncated}
                          {isTruncated && (
                            <span className="scb-list-readmore"> Read more</span>
                          )}
                        </div>
                      </div>
                      <ChevronRight />
                    </button>
                  );
                })}
              </div>
            )}
          </>
        )}

        {/* Categories list */}
        {faqView === 'categories' && (
          <>
            {renderHeader('FAQs')}
            {faqLoading ? (
              <div className="scb-faq-loader-body">
                <div className="scb-faq-spinner" />
                <span className="scb-faq-loader-text">Loading questions...</span>
              </div>
            ) : (
              <>
                <div className="scb-search-bar"><SearchIcon /><input className="scb-search-input" placeholder="Search for an FAQ" value={faqSearch} onChange={e => setFaqSearch(e.target.value)} /></div>
                <div className="scb-list-view">
                  {faqData.filter(f => f.category.toLowerCase().includes(faqSearch.toLowerCase())).map((faq) => (
                    <button key={faq.id} className="scb-list-item" onClick={() => openFaqCategory(faq)}>
                      <div><div className="scb-list-title">{faq.category}</div><div className="scb-list-sub">{faq.children.length} FAQ{faq.children.length > 1 ? 's' : ''}</div></div>
                      <ChevronRight />
                    </button>
                  ))}
                </div>
              </>
            )}
          </>
        )}
      </>
    );

    if (tab === 'articles') return (
      <>
        {selectedArticle ? (
          <>
            {renderHeader('Article', true)}
            <div className="scb-article-detail">
              <div className="scb-article-badge">1 Article</div>
              <h3 className="scb-article-dtitle">{selectedArticle.title}</h3>
              <p className="scb-article-body">{selectedArticle.content}</p>
              <button className="scb-article-chat-btn" onClick={() => { setSelectedArticle(null); setTab('conversation'); }}>Still need help? Chat with us</button>
            </div>
          </>
        ) : (
          <>
            {renderHeader('Articles')}
            <div className="scb-search-bar"><SearchIcon /><input className="scb-search-input" placeholder="Search for an article" value={articleSearch} onChange={e => setArticleSearch(e.target.value)} /></div>
            <div className="scb-list-view">
              {articles.filter(a => a.title.toLowerCase().includes(articleSearch.toLowerCase())).map((art, i) => (
                <button key={i} className="scb-list-item" onClick={() => setSelectedArticle(art)}>
                  <div><div className="scb-list-title">{art.title}</div><div className="scb-list-sub">{art.count} Article</div></div>
                  <ChevronRight />
                </button>
              ))}
            </div>
          </>
        )}
      </>
    );
  };

  if (!isOpen) return null;

  return (
    <div className="scb-wrapper">
      <div className="scb-body">{renderBody()}</div>
      {!subView && (
        <nav className="scb-nav">
          {([
            { key: 'home' as Tab, label: 'Home', icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg> },
            { key: 'conversation' as Tab, label: 'Conversation', icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> },
            { key: 'faqs' as Tab, label: 'FAQs', icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg> },
            { key: 'articles' as Tab, label: 'Articles', icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg> },
          ]).map(item => (
            <button key={item.key} className={`scb-nav-item ${tab === item.key ? 'scb-nav-active' : ''}`} onClick={() => { setTab(item.key); setSelectedFaq(null); setSelectedFaqChild(null); setSelectedArticle(null); setFaqView('categories'); }}>
              {item.icon}<span>{item.label}</span>
            </button>
          ))}
        </nav>
      )}
    </div>
  );
};

export default SupportChatBot;