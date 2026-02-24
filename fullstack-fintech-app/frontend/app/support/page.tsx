'use client';

import { useState, FormEvent, ChangeEvent, useEffect } from 'react';
import {
  Headphones, Clock, CheckCircle,
  Send, Search, ChevronDown, ChevronUp
} from 'lucide-react';
import './Support.css';
import Sidebar from '@/components/Sidebar';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import MobileNav from '@/components/MobileNav';
import {FormData} from '../types/utils';
import { contactMethods, faqs, quickLinks } from '../lib/SupportData';
import DepositModal from '@/components/DepositModal';
import LoadingScreen from '@/components/loader/Loadingscreen';

const Support = () => {
    const [searchQuery, setSearchQuery] = useState<string>('');
    const [isDepositOpen, setIsDepositOpen] = useState(false);
    const [theme, setTheme] = useState<'light' | 'dark'>('light');
    const [expandedFaq, setExpandedFaq] = useState<string | null>(null);
    const [isPageLoading, setIsPageLoading] = useState<boolean>(true);
    const [formData, setFormData] = useState<FormData>({
        name: '',
        email: '',
        subject: '',
        category: '',
        message: '',
        priority: 'medium'
    });
    const [submitted, setSubmitted] = useState<boolean>(false);

    // Event handlers with proper typing
    const toggleFaq = (categoryIndex: number, questionIndex: number): void => {
        const key = `${categoryIndex}-${questionIndex}`;
        setExpandedFaq(expandedFaq === key ? null : key);
    };

    useEffect(() => {
        const loadingTimer = setTimeout(() => {
          setIsPageLoading(false);
        }, 2000);
    
        return () => clearTimeout(loadingTimer);
    }, []);

    const handleInputChange = (
        e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>
    ): void => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const toggleTheme = () => {
        const newTheme = theme === 'light' ? 'dark' : 'light';
        setTheme(newTheme);
        localStorage.setItem('theme', newTheme);
        document.documentElement.classList.toggle('dark', newTheme === 'dark');
        document.body.classList.toggle('dark-theme', newTheme === 'dark');
    };

    const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
        e.preventDefault();
        
        // In a real Next.js app, you would call your API route here
        // Example:
        // const response = await fetch('/api/support/ticket', {
        //   method: 'POST',
        //   headers: { 'Content-Type': 'application/json' },
        //   body: JSON.stringify(formData)
        // });
        // const data = await response.json();
        
        setSubmitted(true);
        
        setTimeout(() => {
        setSubmitted(false);
        setFormData({
            name: '',
            email: '',
            subject: '',
            category: '',
            message: '',
            priority: 'medium'
        });
        }, 3000);
    };

    if (isPageLoading) {
        return <LoadingScreen />;
    }
  return (
    <div className={`dashboard-container`}>
      <Sidebar />
      <main className={`main-content ${isDepositOpen ? 'dashboard-blur' : ''}`}>
        <Header theme={theme} toggleTheme={toggleTheme} />
        <div className="scrollable-content">
            <div className={`support-page`}>
            {/* Hero Section */}
            <div className="support-hero">
                <div className="hero-content">
                <div className="hero-icon-wrapper">
                    <Headphones size={48} />
                </div>
                <h1 className="hero-title">How can we help you?</h1>
                <p className="hero-subtitle">
                    Get instant answers or connect with our support team
                </p>
                
                {/* Search Bar */}
                <div className="search-wrapper">
                    <Search className="support-search-icon" size={20} />
                    <input
                    type="text"
                    placeholder="Search for help articles, FAQs, guides..."
                    className="support-search-input"
                    value={searchQuery}
                    onChange={(e: ChangeEvent<HTMLInputElement>) => setSearchQuery(e.target.value)}
                    />
                </div>
                </div>
            </div>

            <div className={`support-content`}>
                {/* Contact Methods */}
                <section className="support-section">
                <h2 className="section-title-f">Contact Our Support Team</h2>
                <div className="contact-grid">
                    {contactMethods.map((method, idx) => (
                    <div 
                        key={idx} 
                        className="contact-card"
                        style={{ '--accent-color': method.color } as React.CSSProperties}
                    >
                        <div className="contact-icon" style={{ backgroundColor: method.color }}>
                        {method.icon}
                        </div>
                        <h3 className="contact-title">{method.title}</h3>
                        <p className="contact-description">{method.description}</p>
                        <div className="contact-availability">
                        <Clock size={14} />
                        <span>{method.availability}</span>
                        </div>
                        <button className="contact-action">{method.action}</button>
                    </div>
                    ))}
                </div>
                </section>

                {/* Quick Links */}
                <section className="support-section">
                <h2 className="section-title">Quick Links</h2>
                <div className="quick-links-grid">
                    {quickLinks.map((link, idx) => (
                    <a key={idx} href={link.href} className="quick-link-card">
                        <div className="quick-link-icon">{link.icon}</div>
                        <span>{link.label}</span>
                    </a>
                    ))}
                </div>
                </section>

                {/* FAQs */}
                <section className="support-section">
                <h2 className="section-title">Frequently Asked Questions</h2>
                <div className="faq-container">
                    {faqs.map((category, catIdx) => (
                    <div key={catIdx} className="faq-category">
                        <div className="faq-category-header">
                        <div className="faq-category-icon">{category.icon}</div>
                        <h3 className="faq-category-title">{category.category}</h3>
                        </div>
                        <div className="faq-questions">
                        {category.questions.map((item, qIdx) => {
                            const isExpanded = expandedFaq === `${catIdx}-${qIdx}`;
                            return (
                            <div key={qIdx} className="faq-item">
                                <button
                                type="button"
                                className="faq-question"
                                onClick={() => toggleFaq(catIdx, qIdx)}
                                >
                                <span>{item.q}</span>
                                {isExpanded ? (
                                    <ChevronUp size={20} />
                                ) : (
                                    <ChevronDown size={20} />
                                )}
                                </button>
                                {isExpanded && (
                                <div className="faq-answer">
                                    <p>{item.a}</p>
                                </div>
                                )}
                            </div>
                            );
                        })}
                        </div>
                    </div>
                    ))}
                </div>
                </section>

                {/* Support Ticket Form */}
                <section className="support-section">
                <h2 className="section-title">Submit a Support Ticket</h2>
                <div className="ticket-form-wrapper">
                    {submitted ? (
                    <div className="success-message">
                        <CheckCircle size={48} />
                        <h3>Ticket Submitted Successfully!</h3>
                        <p>We&apos;ve received your support request. Our team will respond within 24 hours.</p>
                        <p className="ticket-number">Ticket #EP-{Math.floor(Math.random() * 100000)}</p>
                    </div>
                    ) : (
                    <form className="ticket-form" onSubmit={handleSubmit}>
                        <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="name">Full Name</label>
                            <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            placeholder="Enter your full name"
                            required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="email">Email Address</label>
                            <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            placeholder="your.email@example.com"
                            required
                            />
                        </div>
                        </div>

                        <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="category">Category</label>
                            <select
                            id="category"
                            name="category"
                            value={formData.category}
                            onChange={handleInputChange}
                            required
                            >
                            <option value="">Select a category</option>
                            <option value="account">Account Issues</option>
                            <option value="payment">Payment Problems</option>
                            <option value="security">Security Concerns</option>
                            <option value="technical">Technical Support</option>
                            <option value="billing">Billing Questions</option>
                            <option value="other">Other</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="priority">Priority</label>
                            <select
                            id="priority"
                            name="priority"
                            value={formData.priority}
                            onChange={handleInputChange}
                            required
                            >
                            <option value="low">Low</option>
                            <option value="medium">Medium</option>
                            <option value="high">High</option>
                            <option value="urgent">Urgent</option>
                            </select>
                        </div>
                        </div>

                        <div className="form-group">
                        <label htmlFor="subject">Subject</label>
                        <input
                            type="text"
                            id="subject"
                            name="subject"
                            value={formData.subject}
                            onChange={handleInputChange}
                            placeholder="Brief description of your issue"
                            required
                        />
                        </div>

                        <div className="form-group">
                        <label htmlFor="message">Message</label>
                        <textarea
                            id="message"
                            name="message"
                            value={formData.message}
                            onChange={handleInputChange}
                            placeholder="Please provide detailed information about your issue..."
                            rows={6}
                            required
                        />
                        </div>

                        <button type="submit" className="submit-button">
                        <Send size={20} />
                        Submit Ticket
                        </button>
                    </form>
                    )}
                </div>
                </section>

                {/* Support Hours */}
                <section className="support-section">
                <div className="support-hours">
                    <div className="hours-content">
                    <Clock size={32} />
                    <div>
                        <h3>Support Hours</h3>
                        <p>Our team is available to help you:</p>
                        <ul>
                        <li><strong>Live Chat:</strong> 24/7</li>
                        <li><strong>Phone Support:</strong> Monday - Friday, 9:00 AM - 6:00 PM (WAT)</li>
                        <li><strong>Email Support:</strong> Response within 24 hours</li>
                        </ul>
                    </div>
                    </div>
                </div>
                </section>
            </div>
            </div>
            <Footer theme={theme} />
        </div>
      </main>
      <MobileNav activeTab="none" onPlusClick={() => setIsDepositOpen(true)} />
        <DepositModal 
        isOpen={isDepositOpen} 
        onClose={() => setIsDepositOpen(false)} 
        theme={theme} 
        />
    </div>
  );
};

export default Support;