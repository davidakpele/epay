'use client';

import React, { useState, useRef } from 'react';
import Link from 'next/link';
import "./ForgotPassword.css";
import { authService } from '@/app/api';
import { Country, Toast } from '@/app/types/auth';
import { ResetPasswordFormErrors } from '@/app/types/errors';
import { countries } from '@/components/countries';
import SupportChatBot from '@/components/SupportChatBot';

const ForgotPassword = () => {
    const [formData, setFormData] = useState({
        email: '',
        phone: '',
    });
    const [isChatOpen, setIsChatOpen] = useState(false);
    const [regMode, setRegMode] = useState<'email' | 'phone'>('email');
    const [errors, setErrors] = useState<ResetPasswordFormErrors>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [isScrolling, setIsScrolling] = useState(false);
    const scrollTimer = useRef<NodeJS.Timeout | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [toasts, setToasts] = useState<Toast[]>([]);
    const [selectedCountry, setSelectedCountry] = useState<Country | null>(null);
    const emailRef = useRef<HTMLInputElement>(null);
    const phoneRef = useRef<HTMLInputElement>(null);

    const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
        setToasts((prev: Toast[]) => {
            if (prev.length >= 5) return prev;
            const id = Date.now();
            const newToast: Toast = { id, message: msg, type, exiting: false };
            setTimeout(() => {
                setToasts((currentToasts: Toast[]) =>
                    currentToasts.map((t: Toast) => (t.id === id ? { ...t, exiting: true } : t))
                );
                setTimeout(() => {
                    setToasts((currentToasts: Toast[]) => currentToasts.filter((t: Toast) => t.id !== id));
                }, 300);
            }, 3000);
            return [...prev, newToast];
        });
    };

    const validateForm = () => {
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (regMode === 'email') {
            if (!formData.email.trim()) {
                showToast('Email Address is required');
                emailRef.current?.focus();
                return false;
            } else if (!emailRegex.test(formData.email.trim())) {
                showToast('Invalid email address.');
                emailRef.current?.focus();
                return false;
            }
        } else {
            if (!formData.phone.trim()) {
                showToast('Phone number is required');
                phoneRef.current?.focus();
                return false;
            }
        }
        setErrors({});
        return true;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev: typeof formData) => ({ ...prev, [name]: value }));
        if (errors[name as keyof ResetPasswordFormErrors]) {
            setErrors((prev: ResetPasswordFormErrors) => ({ ...prev, [name]: undefined }));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsSubmitting(true);
        try {
            const payload =
                regMode === 'email'
                    ? { identifier: formData.email.trim(), method: 'EMAIL' }
                    : { identifier: `${selectedCountry?.code}${formData.phone.trim()}`, method: 'PHONE' };

            await authService.forgotPassword(payload);
            showToast('Password reset code sent successfully!', 'success');
        } catch (error: unknown) {
            const errorMsg = (error as Error).toString();
            if (errorMsg.includes('internet connection')) {
                showToast('You are offline. Please check your network.');
            } else if (errorMsg.includes('maintenance') || errorMsg.includes('down')) {
                showToast('Service unavailable. The server might be down.');
            } else {
                showToast(errorMsg);
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleSwitchMode = (mode: 'email' | 'phone') => {
        setRegMode(mode);
        setFormData({ email: '', phone: '' });
        setSelectedCountry(null);
        setIsSubmitting(false);
    };

    const handleScroll = () => {
        setIsScrolling(true);
        if (scrollTimer.current) clearTimeout(scrollTimer.current);
        scrollTimer.current = setTimeout(() => {
            setIsScrolling(false);
        }, 1000);
    };

    const filteredCountries = countries.filter((c: Country) =>
        c.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="auth-page-wrapper">
            <div className="toastrs">
                {toasts.map((toast: Toast) => (
                    <div
                        key={toast.id}
                        className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}
                    >
                        <div className="toast-icon">
                            <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`} aria-hidden="true"></i>
                        </div>
                        <div className="toast-message">{toast.message}</div>
                    </div>
                ))}
            </div>

            <div className="register-card">
                <div className="settings-avatar-wrapper">
                    <Link href="/dashboard">
                        <img
                            src={'../assets/images/logo.png'}
                            alt="User profile"
                            style={{
                                width: 'auto',
                                height: '37px',
                                maxWidth: '108px',
                                objectFit: 'contain',
                                display: 'block',
                                cursor: 'pointer',
                            }}
                            onError={(e) => {
                                e.currentTarget.src = '/assets/images/logo.jpg';
                            }}
                        />
                    </Link>
                </div>

                <div className="step1">
                    <div className="form-header-text">
                        <h2>Reset Your Password</h2>
                        <p className='subtitle-header'>
                            Don&apos;t worry, just select the mode you registered with and we&apos;ll send a reset code right away.
                        </p>
                    </div>
                    <div className="toggle-container">
                        <button
                            type="button"
                            className={regMode === 'email' ? 'active' : ''}
                            onClick={() => handleSwitchMode('email')}
                        >
                            Email
                        </button>
                        <button
                            type="button"
                            className={regMode === 'phone' ? 'active' : ''}
                            onClick={() => handleSwitchMode('phone')}
                        >
                            Phone
                        </button>
                    </div>
                    <form onSubmit={handleSubmit} noValidate>
                        {regMode === 'email' ? (
                            <div className="form-group">
                                <label>Email</label>
                                <input
                                    ref={emailRef}
                                    type="email"
                                    name="email"
                                    className="form-control"
                                    value={formData.email}
                                    onChange={handleChange}
                                    placeholder="Email"
                                />
                            </div>
                        ) : (
                            <div className="form-group">
                                <label>Phone number</label>
                                <div className="phone-input-group">
                                    <div className="country-dropdown" onClick={() => setIsModalOpen(true)}>
                                        <span>
                                            {selectedCountry
                                                ? `${selectedCountry.abbr3} (${selectedCountry.code})`
                                                : 'Country'}
                                        </span>
                                        <i className="fa fa-chevron-down"></i>
                                    </div>
                                    <input
                                        ref={phoneRef}
                                        type="tel"
                                        name="phone"
                                        className="form-control"
                                        value={formData.phone}
                                        onChange={handleChange}
                                        placeholder="Phone"
                                    />
                                </div>
                            </div>
                        )}
                        <button type="submit" className="btn-submit" disabled={isSubmitting}>
                            {isSubmitting ? <div className="spinner"></div> : 'Send password reset code'}
                            {isSubmitting ? 'Processing...' : ''}
                        </button>
                        <div className="forgot-password-container">
                            <Link href="/auth/login" className="forgot-password-link">
                                Back to Login
                            </Link>
                        </div>
                    </form>
                </div>

                {isModalOpen && (
                    <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
                        <div className="modal-content" onClick={(e: React.MouseEvent) => e.stopPropagation()}>
                            <div className="modal-header"><h3>Select Country</h3></div>
                            <div className="search-container">
                                <i className="fa fa-search"></i>
                                <input
                                    type="text"
                                    placeholder="Search"
                                    value={searchTerm}
                                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchTerm(e.target.value)}
                                />
                            </div>
                            <div
                                className={`country-list ${isScrolling ? 'is-scrolling' : ''}`}
                                onScroll={handleScroll}
                            >
                                {filteredCountries.map((c: Country) => (
                                    <div
                                        key={c.name}
                                        className="country-item"
                                        onClick={() => { setSelectedCountry(c); setIsModalOpen(false); setSearchTerm(''); }}
                                    >
                                        <span>{c.name} ({c.code})</span>
                                        <div className={`radio-outer ${selectedCountry?.name === c.name ? 'checked' : ''}`}>
                                            <div className="radio-inner"></div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}

                {!isChatOpen && (
                    <button
                        className="chat-fab"
                        onClick={() => setIsChatOpen(true)}
                        aria-label="Open support chat"
                    >
                        <i className="fa-solid fa-comment-dots"></i>
                    </button>
                )}

                <SupportChatBot isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
            </div>
        </div>
    );
};

export default ForgotPassword;
