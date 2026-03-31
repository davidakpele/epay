'use client';

import React, { useState, useRef, useEffect } from 'react';
import './SupportChatBot.css';

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

const getTime = () =>
  new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

const SYSTEM_PROMPT = `You are ePay's friendly and knowledgeable support assistant. ePay is an online business banking platform that offers wallets, virtual cards, currency exchange, airtime/data top-up, bill payments (electricity, cable TV, hospital, betting), flight booking, and shopping services.

Your role:
- Help users with account issues, transactions, wallets, cards, KYC, and general platform questions
- Be concise, warm, and professional
- If you don't know something specific, offer to connect them with a human agent
- Never reveal you are Claude or made by Anthropic — you are ePay Support Bot
- Keep responses short (2-4 sentences max) unless a detailed explanation is truly needed`;

const SupportChatBot: React.FC<SupportChatBotProps> = ({ isOpen, onClose }) => {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      role: 'bot',
      text: "Hi there! 👋 I'm ePay's support assistant. How can I help you today?",
      time: getTime(),
    },
  ]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const conversationHistory = useRef<{ role: 'user' | 'assistant'; content: string }[]>([]);

  useEffect(() => {
    if (isOpen && !isMinimized) {
      setTimeout(() => inputRef.current?.focus(), 300);
    }
  }, [isOpen, isMinimized]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isTyping]);

  const sendMessage = async () => {
    const text = input.trim();
    if (!text || isTyping) return;

    const userMsg: Message = { id: Date.now(), role: 'user', text, time: getTime() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsTyping(true);

    conversationHistory.current.push({ role: 'user', content: text });

    try {
      const response = await fetch('https://api.anthropic.com/v1/messages', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: 'claude-sonnet-4-20250514',
          max_tokens: 1000,
          system: SYSTEM_PROMPT,
          messages: conversationHistory.current,
        }),
      });

      const data = await response.json();
      const replyText =
        data?.content?.[0]?.text ||
        "I'm having trouble connecting right now. Please try again or contact us via email.";

      conversationHistory.current.push({ role: 'assistant', content: replyText });

      const botMsg: Message = { id: Date.now() + 1, role: 'bot', text: replyText, time: getTime() };
      setMessages(prev => [...prev, botMsg]);
    } catch {
      const errMsg: Message = {
        id: Date.now() + 1,
        role: 'bot',
        text: "Sorry, I couldn't connect. Please check your internet or reach us at support@epay.com.",
        time: getTime(),
      };
      setMessages(prev => [...prev, errMsg]);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleReset = () => {
    conversationHistory.current = [];
    setMessages([
      {
        id: Date.now(),
        role: 'bot',
        text: "Hi there! 👋 I'm ePay's support assistant. How can I help you today?",
        time: getTime(),
      },
    ]);
  };

  if (!isOpen) return null;

  return (
    <div className={`scb-wrapper ${isMinimized ? 'scb-minimized' : ''}`}>
      {/* Header */}
      <div className="scb-header">
        <div className="scb-header-left">
          <div className="scb-avatar">
            <span>e</span>
            <span className="scb-status-dot" />
          </div>
          <div className="scb-header-info">
            <span className="scb-header-name">ePay Support</span>
            <span className="scb-header-status">Online · Typically replies instantly</span>
          </div>
        </div>
        <div className="scb-header-actions">
          <button className="scb-icon-btn" title="New chat" onClick={handleReset}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
            </svg>
          </button>
          <button className="scb-icon-btn" title={isMinimized ? 'Expand' : 'Minimize'} onClick={() => setIsMinimized(p => !p)}>
            {isMinimized ? (
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/>
                <line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/>
              </svg>
            ) : (
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="4 14 10 14 10 20"/><polyline points="20 10 14 10 14 4"/>
                <line x1="10" y1="14" x2="3" y2="21"/><line x1="21" y1="3" x2="14" y2="10"/>
              </svg>
            )}
          </button>
          <button className="scb-icon-btn scb-close-btn" title="Close" onClick={onClose}>
            <svg width="15" height="15" viewBox="0 0 18 18" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round">
              <path d="M1 1l16 16M17 1L1 17"/>
            </svg>
          </button>
        </div>
      </div>

      {/* Body */}
      {!isMinimized && (
        <>
          <div className="scb-messages">
            {messages.map(msg => (
              <div key={msg.id} className={`scb-msg-row ${msg.role === 'user' ? 'scb-msg-user' : 'scb-msg-bot'}`}>
                {msg.role === 'bot' && (
                  <div className="scb-bot-avatar">e</div>
                )}
                <div className="scb-bubble-wrap">
                  <div className="scb-bubble">{msg.text}</div>
                  <span className="scb-msg-time">{msg.time}</span>
                </div>
              </div>
            ))}

            {isTyping && (
              <div className="scb-msg-row scb-msg-bot">
                <div className="scb-bot-avatar">e</div>
                <div className="scb-bubble-wrap">
                  <div className="scb-bubble scb-typing">
                    <span /><span /><span />
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Quick replies */}
          <div className="scb-quick-replies-wrapper">
            <button className="scb-scroll-arrow scb-scroll-left" onClick={() => {
              const el = document.querySelector('.scb-quick-replies') as HTMLElement;
              if (el) el.scrollLeft -= 120;
            }}>&#8249;</button>
            <div className="scb-quick-replies">
              {['Check my balance', 'Virtual card issues', 'Transfer failed', 'KYC help', 'Fund my wallet', 'Card declined'].map(q => (
                <button key={q} className="scb-quick-btn" onClick={() => { setInput(q); inputRef.current?.focus(); }}>
                  {q}
                </button>
              ))}
            </div>
            <button className="scb-scroll-arrow scb-scroll-right" onClick={() => {
              const el = document.querySelector('.scb-quick-replies') as HTMLElement;
              if (el) el.scrollLeft += 120;
            }}>&#8250;</button>
          </div>

          {/* Input */}
          <div className="scb-input-row">
            <input
              ref={inputRef}
              className="scb-input"
              type="text"
              placeholder="Type your message..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isTyping}
            />
            <button
              className={`scb-send-btn ${!input.trim() || isTyping ? 'scb-send-disabled' : ''}`}
              onClick={sendMessage}
              disabled={!input.trim() || isTyping}
              title="Send"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="22" y1="2" x2="11" y2="13"/>
                <polygon points="22 2 15 22 11 13 2 9 22 2"/>
              </svg>
            </button>
          </div>

          <div className="scb-footer">Powered by <strong>ePay AI</strong></div>
        </>
      )}
    </div>
  );
};

export default SupportChatBot;