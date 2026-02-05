'use client';

import React, { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import "./ForgotPassword.css";
import { authService } from '@/app/api';
import { Country, Toast } from '@/app/types/auth';
import { ResetPasswordFormErrors } from '@/app/types/errors';
import { countries } from '@/components/countries';

const ForgotPassword = () => {
    const [formData, setFormData] = useState({
        email: '',
        phone: '',
    });
    const [nextStepformData, setNextStepformData] = useState({
        password: '',
        confirmPassword: '',
        verificationCode: '',
    });
    const [regMode, setRegMode] = useState<'email' | 'phone'>('email');
    const [errors, setErrors] = useState<ResetPasswordFormErrors>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [isComplete, setIsComplete] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [isScrolling, setIsScrolling] = useState(false);
    const scrollTimer = useRef<NodeJS.Timeout | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isNextStep, setIsNextStep] = useState(false);
    const [toasts, setToasts] = useState<Toast[]>([]);
    const [selectedCountry, setSelectedCountry] = useState<Country | null>(null);
    const emailRef = useRef<HTMLInputElement>(null);
    const phoneRef = useRef<HTMLInputElement>(null);
    const passwordRef = useRef<HTMLInputElement>(null);
    const confirmPasswordRef = useRef<HTMLInputElement>(null);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);
    const [pin, setPin] = useState(['', '', '', '']);
    const router = useRouter();
    useEffect(() => {
        emailRef.current?.focus();
    }, []);

    const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
        setToasts((prev) => {
        if (prev.length >= 5) return prev;

        const id = Date.now();
        const newToast: Toast = { id, message: msg, type, exiting: false };

        setTimeout(() => {
            setToasts((currentToasts) =>
            currentToasts.map((t) => (t.id === id ? { ...t, exiting: true } : t))
            );

            setTimeout(() => {
            setToasts((currentToasts) => currentToasts.filter((t) => t.id !== id));
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
        }else{
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
        setFormData(prev => ({ ...prev, [name]: value }));
        if (errors[name as keyof ResetPasswordFormErrors]) {
        setErrors(prev => ({ ...prev, [name]: undefined }));
        }
    };

    const handleNextStepFormDataChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setNextStepformData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsSubmitting(true);
        setTimeout(() => {
            setIsNextStep(true);
            setIsSubmitting(false);
        }, 3000);
    };

    const handleSwitchMode = (mode: 'email' | 'phone') => {
        setRegMode(mode);
        setFormData(prev => ({
        ...prev,
        email: '',
        phone: '',
        }));
        setSelectedCountry(null);
        setIsSubmitting(false)
    };

    const handleScroll = () => {
        setIsScrolling(true);
        if (scrollTimer.current) clearTimeout(scrollTimer.current);
        scrollTimer.current = setTimeout(() => {
        setIsScrolling(false);
        }, 1000);
    };

    const handlePinChange = (index: number, value: string) => {
        if (!/^\d?$/.test(value)) return;

        const newPin = [...pin];
        newPin[index] = value;
        setPin(newPin);

        if (value && index < 3) {
            const nextInput = document.getElementById(`pin-input-${index + 1}`);
            if (nextInput) nextInput.focus();
        }
    };

    const handlePinKeyDown = (index: number, e: React.KeyboardEvent) => {
        if (e.key === 'Backspace' && !pin[index] && index > 0) {
            const prevInput = document.getElementById(`pin-input-${index - 1}`);
            if (prevInput) prevInput.focus();
        }
    };

    const validateNewPasswordForm = () => {
        const { password } = nextStepformData;
        if (!password) {
            showToast('Password is required'); 
            passwordRef.current?.focus();
            return false; 
        }
        if (password.length < 8) { 
            showToast('Password must be at least 8 characters'); 
            passwordRef.current?.focus();
            return false; 
        }
        if (!/[a-z]/.test(password)) {
            showToast('Password must contain a lowercase letter');
            passwordRef.current?.focus();
            return false;
        }
        if (!/[A-Z]/.test(password)) {
            showToast('Password must contain an uppercase letter');
            passwordRef.current?.focus();
            return false;
        }
        if (!/[0-9]/.test(password)) {
            showToast('Password must contain a number');
            passwordRef.current?.focus();
            return false;
        }
        if (!/[^A-Za-z0-9]/.test(password)) {
            showToast('Password must contain a special character');
            passwordRef.current?.focus();
            return false;
        }

        if (password !== nextStepformData.confirmPassword) { 
            showToast('Passwords do not match'); 
            confirmPasswordRef.current?.focus();
            return false; 
        }

        const enteredPin = pin.join('');
        if (enteredPin.length !== 4) {
            showToast('Please enter a 4-digit PIN');
            return false; 
        }
        return true;
    }

    const handleCompleteForm = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateNewPasswordForm()) return;

        setIsComplete(true);
        setTimeout(() => {
            showToast('Password has been success Updated.!', 'success');
        }, 3000);
         
        setTimeout(() => {
            setIsComplete(false);
            router.push('/auth/login');
        }, 4000);
    };

    const checkStrength = (req: boolean) => (req ? 'valid' : 'invalid');

    const filteredCountries = countries.filter(c => 
    c.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

  return (
    <div className="auth-page-wrapper">
      <div className="toastrs">
        {toasts.map((toast) => (
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
        {!isNextStep && (
            <>
            <div className="step1">
                <div className="form-header-text">
                <h2>Reset Your Password</h2>
                <p className='subtitle-header'>Don`t worry chief, Just select the mode you register with and we will send a code to you to reset your password in just a minute.</p>
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
                        <input ref={emailRef} type="email" name="email" className="form-control" value={formData.email} onChange={handleChange} placeholder="Email" />
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
                        <input ref={phoneRef} type="tel" name="phone" className="form-control" value={formData.phone} onChange={handleChange} placeholder="Phone" />
                        </div>
                    </div>
                    )}
                    <button type="submit" className="btn-submit" disabled={isSubmitting}>
                        {isSubmitting ? <div className="spinner"></div>: 'Send password reset code'}
                        {isSubmitting ?'Processing...':''}
                    </button>
                     <div className="forgot-password-container">
                        <Link href="/auth/login" className="forgot-password-link">
                            Login
                        </Link>
                    </div>
                </form>
            </div>
            </>
        )}

        {isNextStep && (
            <>
            <div className="form-header-text">
                <h2>Reset Your Password</h2>
                <p className='subtitle-header'>
                    Reset password OTP has been sent to your email or phone. <br/> To reset password, create new password and input your OTP to complete the process.
                </p>
                </div>
                <form onSubmit={handleCompleteForm} noValidate>
                    <div className="form-group">
                        <label>Password</label>
                        <div className="input-container">
                            <input ref={passwordRef} type={showPassword ? 'text' : 'password'} name="password" className="form-control" value={nextStepformData.password} onChange={handleNextStepFormDataChange} placeholder="Enter password" />
                            <button type="button" className="password-toggle" onClick={() => setShowPassword(!showPassword)}>
                            <i className={`fa-solid ${showPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                            </button>
                        </div>
                        <div className="password-constraints">
                            <span className={checkStrength(nextStepformData.password.length >= 8)}><i className="fa fa-info-circle"></i> At least 8 characters</span>
                            <span className={checkStrength(/[a-z]/.test(nextStepformData.password))}><i className="fa fa-info-circle"></i> Lowercase letter (a-z)</span>
                            <span className={checkStrength(/[A-Z]/.test(nextStepformData.password))}><i className="fa fa-info-circle"></i> Uppercase letter (A-Z)</span>
                            <span className={checkStrength(/[0-9]/.test(nextStepformData.password))}><i className="fa fa-info-circle"></i> Number (0-9)</span>
                            <span className={checkStrength(/[^A-Za-z0-9]/.test(nextStepformData.password))}><i className="fa fa-info-circle"></i> Special character (#,*)</span>
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Confirm Password</label>
                        <div className="input-container">
                            <input ref={confirmPasswordRef} type={showConfirmPassword ? 'text' : 'password'} name="confirmPassword" value={nextStepformData.confirmPassword} className="form-control" onChange={handleNextStepFormDataChange} />
                            <button type="button" className="password-toggle" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
                            <i className={`fa-solid ${showConfirmPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                            </button>
                        </div>
                    </div>
                    <div className="form-group">
                         <label className='otp-label'>OTP</label>
                        <div className="pin-inputs">
                            {pin.map((digit, index) => (
                                <input
                                key={index}
                                id={`pin-input-${index}`}
                                type="password"
                                className="pin-input"
                                maxLength={1}
                                value={digit}
                                onChange={(e) => handlePinChange(index, e.target.value)}
                                onKeyDown={(e) => handlePinKeyDown(index, e)}
                                disabled={isProcessing}
                                autoFocus={index === 0}
                                />
                            ))}
                        </div>
                    </div>
                    <button type="submit" className="btn-submit" disabled={isComplete}>
                        {isComplete ? <div className="spinner"></div>: 'Reset Password'}
                        {isComplete ?'Loading...':''}
                    </button>
                    
                </form>
            </>
        )}
         {isModalOpen && (
          <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header"><h3>Select Country</h3></div>
              <div className="search-container">
                <i className="fa fa-search"></i>
                <input type="text" placeholder="Search" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
              </div>
              <div 
                className={`country-list ${isScrolling ? 'is-scrolling' : ''}`} 
                onScroll={handleScroll}
              >
                {filteredCountries.map((c) => (
                  <div key={c.name} className="country-item" onClick={() => { setSelectedCountry(c); setIsModalOpen(false); setSearchTerm('') }}>
                    <span>{c.name} ({c.code})</span>
                    <div className={`radio-outer ${selectedCountry?.name === c.name ? 'checked' : ''}`}><div className="radio-inner"></div></div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;