'use client';

import React, { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import "./Register.css";
import { countries } from '@/components/countries';
import { authService } from '@/app/api';
import { Country, Toast } from '@/app/types/auth';

const Register = () => {
  const [regMode, setRegMode] = useState<'email' | 'phone'>('email');
  const [isSuccess, setIsSuccess] = useState(false);
  const [phoneChannel, setPhoneChannel] = useState<'SMS' | 'WHATSAPP'>('SMS');
  const [codeSent, setCodeSent] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    verificationCode: '',
  });

  const [selectedCountry, setSelectedCountry] = useState<Country | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [isScrolling, setIsScrolling] = useState(false);
  const scrollTimer = useRef<NodeJS.Timeout | null>(null);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isRequestingCode, setIsRequestingCode] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);

  const nameRef = useRef<HTMLInputElement>(null);
  const usernameRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const phoneRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const confirmPasswordRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    document.title = "Sign-Up Account"
    nameRef.current?.focus();
  }, []);

  const handleSwitchMode = (mode: 'email' | 'phone') => {
    setRegMode(mode);
    setFormData(prev => ({
      ...prev,
      email: '',
      phone: '',
      verificationCode: ''
    }));
    setSelectedCountry(null);
    setCodeSent(false);
    setPhoneChannel('SMS');
  };

  const handleScroll = () => {
    setIsScrolling(true);
    if (scrollTimer.current) clearTimeout(scrollTimer.current);
    scrollTimer.current = setTimeout(() => {
      setIsScrolling(false);
    }, 1000);
  };

  const showToast = (msg: string, type: 'warning' | 'success' = 'warning') => {
    setToasts((prev) => {
      if (prev.length >= 5) return prev;
      const id = Date.now();
      const newToast: Toast = { id, message: msg, type, exiting: false };
      setTimeout(() => {
        setToasts((curr) => curr.map((t) => (t.id === id ? { ...t, exiting: true } : t)));
        setTimeout(() => {
          setToasts((curr) => curr.filter((t) => t.id !== id));
        }, 300);
      }, 3000);
      return [...prev, newToast];
    });
  };

  const validateForm = () => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    
    if (!formData.name.trim()) { 
        showToast('Name is required'); 
        nameRef.current?.focus();
        return false; 
    }
    if (!formData.username.trim()) { 
        showToast('Username is required'); 
        usernameRef.current?.focus();
        return false; 
    }
    
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
        if (!selectedCountry) { 
            showToast('Please select a country'); 
            return false; 
        }
        if (!formData.phone.trim()) { 
            showToast('Phone number is required'); 
            phoneRef.current?.focus();
            return false; 
        }
    }

    const { password } = formData;
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

    if (password !== formData.confirmPassword) { 
        showToast('Passwords do not match'); 
        confirmPasswordRef.current?.focus();
        return false; 
    }

    if (!formData.verificationCode.trim()) {
        showToast('Verification code is required');
        return false;
    }

    return true;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleRequestCode = async () => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (regMode === 'email') {
      if (!formData.email.trim()) {
        showToast('Please enter your email address first');
        emailRef.current?.focus();
        return;
      }
      if (!emailRegex.test(formData.email.trim())) {
        showToast('Invalid email address');
        emailRef.current?.focus();
        return;
      }
    } else {
      if (!selectedCountry) {
        showToast('Please select a country first');
        return;
      }
      if (!formData.phone.trim()) {
        showToast('Please enter your phone number first');
        phoneRef.current?.focus();
        return;
      }
    }

    const identifier = regMode === 'email' ? formData.email : `${selectedCountry?.code}${formData.phone}`;
    const method = regMode === 'email' ? 'EMAIL' : phoneChannel;

    setIsRequestingCode(true);
    try {
        const response = await authService.sendVerifyCode(identifier, method);
        if (response.status !== 201) {
          setCodeSent(false);
            throw new Error('Failed to send verification code. Please try again.');
        }else {
          setCodeSent(true);
          showToast(
            regMode === 'email' 
              ? 'Verification code sent to your email!' 
              : `Verification code sent via ${phoneChannel}!`, 
            'success'
          );
        }
      } catch (error: any) {
        showToast(error.toString());
    } finally {
        setIsRequestingCode(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);

    const payload = {
        ...formData,
        phone: regMode === 'phone' ? `${selectedCountry?.code}${formData.phone}` : '',
        regMode,
        verificationMethod: regMode === 'email' ? 'EMAIL' : phoneChannel
    };

    try {
      await authService.register(payload);
      setIsSuccess(true);
    } catch (error: any) {
      const errorMsg = error.toString();
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

  const checkStrength = (req: boolean) => (req ? 'valid' : 'invalid');

  const filteredCountries = countries.filter(c => 
    c.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <>
      <div className="auth-page-wrapper">
        <div className="toastrs">
          {toasts.map((toast) => (
            <div key={toast.id} className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}>
              <div className="toast-icon">
                <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`}></i>
              </div>
              <div className="toast-message">{toast.message}</div>
            </div>
          ))}
        </div>

        {isSuccess ? (
          <div className="success-card">
            <div className="success-icon-wrapper">
              <i className="fa-solid fa-envelope-circle-check"></i>
            </div>
            <h2>Account Created!</h2>
            <p>
              {regMode === 'email' ? (
                <>A confirmation email has been sent to <strong>{formData.email}</strong>. 
                Please check your inbox (and spam folder) to verify your account.</>
              ) : (
                <>Your account has been created successfully! You can now sign in with your phone number.</>
              )}
            </p>
            <Link href="/auth/login" className="btn-submit btn-success-nav">
              Go to Sign In
            </Link>
          </div>
        ) : (
          <div className="register-card">
            <div className="form-header-text"><h2>Create an account</h2></div>

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

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Name</label>
                <input ref={nameRef} type="text" name="name" className="form-control" value={formData.name} onChange={handleChange} placeholder="Name" />
              </div>
              <div className="form-group">
                <label>Username</label>
                <input ref={usernameRef} type="text" name="username" className="form-control" value={formData.username} onChange={handleChange} placeholder="Username" />
              </div>

              {regMode === 'email' ? (
                <>
                  <div className="form-group">
                    <label>Email</label>
                    <input ref={emailRef} type="email" name="email" className="form-control" value={formData.email} onChange={handleChange} placeholder="Email" />
                  </div>

                  <div className="form-group">
                    <label>Verification code</label>
                    <div className="verification-wrapper">
                      <input type="text" name="verificationCode" value={formData.verificationCode} className="form-control" onChange={handleChange} placeholder="Enter code" />
                      <button type="button" className="btn-request-code" onClick={handleRequestCode} disabled={isRequestingCode}>
                        {isRequestingCode ? 'Sending...' : codeSent ? 'Resend Code' : 'Send Code'}
                      </button>
                    </div>
                    {codeSent && <small className="helper-text">Code sent! Check your email inbox.</small>}
                  </div>
                </>
              ) : (
                <>
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
                </>
              )}

              <div className="form-group">
                <label>Password</label>
                <div className="input-container">
                  <input ref={passwordRef} type={showPassword ? 'text' : 'password'} name="password" className="form-control" value={formData.password} onChange={handleChange} placeholder="Enter password" />
                  <button type="button" className="password-toggle" onClick={() => setShowPassword(!showPassword)}>
                    <i className={`fa-solid ${showPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                  </button>
                </div>
                <div className="password-constraints">
                  <span className={checkStrength(formData.password.length >= 8)}><i className="fa fa-info-circle"></i> At least 8 characters</span>
                  <span className={checkStrength(/[a-z]/.test(formData.password))}><i className="fa fa-info-circle"></i> Lowercase letter (a-z)</span>
                  <span className={checkStrength(/[A-Z]/.test(formData.password))}><i className="fa fa-info-circle"></i> Uppercase letter (A-Z)</span>
                  <span className={checkStrength(/[0-9]/.test(formData.password))}><i className="fa fa-info-circle"></i> Number (0-9)</span>
                  <span className={checkStrength(/[^A-Za-z0-9]/.test(formData.password))}><i className="fa fa-info-circle"></i> Special character (#,*)</span>
                </div>
              </div>

              <div className="form-group">
                <label>Confirm Password</label>
                <div className="input-container">
                  <input ref={confirmPasswordRef} type={showConfirmPassword ? 'text' : 'password'} name="confirmPassword" value={formData.confirmPassword} className="form-control" onChange={handleChange} />
                  <button type="button" className="password-toggle" onClick={() => setShowConfirmPassword(!showConfirmPassword)}>
                    <i className={`fa-solid ${showConfirmPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
                  </button>
                </div>
              </div>
              {regMode != 'email' &&(
                <>
                  <div className="form-group">
                    <label>Receive OTP Via</label>
                    <div className="otp-options">
                      <div className="otp-option" onClick={() => setPhoneChannel('WHATSAPP')}>
                        <div className="otp-option-left">
                          <i className="fab fa-whatsapp otp-icon"></i>
                          <span className="otp-label">WhatsApp (Instant)</span>
                        </div>
                        <div className={`toggle-switch ${phoneChannel === 'WHATSAPP' ? 'active' : ''}`}>
                          <div className="toggle-slider"></div>
                        </div>
                      </div>
                      <div className="otp-option" onClick={() => setPhoneChannel('SMS')}>
                        <div className="otp-option-left">
                          <i className="fa fa-comment-dots otp-icon"></i>
                          <span className="otp-label">SMS (0 - 5 MIN)</span>
                        </div>
                        <div className={`toggle-switch ${phoneChannel === 'SMS' ? 'active' : ''}`}>
                          <div className="toggle-slider"></div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Verification code</label>
                    <div className="verification-wrapper">
                      <input type="text" name="verificationCode" value={formData.verificationCode} className="form-control" onChange={handleChange} placeholder="Enter code" />
                      <button type="button" className="btn-request-code" onClick={handleRequestCode} disabled={isRequestingCode}>
                        {isRequestingCode ? 'Sending...' : codeSent ? 'Resend Code' : 'Send Code'}
                      </button>
                    </div>
                    {codeSent && <small className="helper-text">Code sent via {phoneChannel}! Check your messages.</small>}
                  </div>
                </>
              )}

              <button type="submit" className="btn-submit" disabled={codeSent ? isSubmitting : true}>
                {isSubmitting ? <div className="spinner"></div> : 'Sign Up'}
              </button>
            </form>

            <div className="footer-link">Already have an account? <Link href="/auth/login">Sign in</Link></div>
          </div>
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
    </>
  );
};

export default Register;