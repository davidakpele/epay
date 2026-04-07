'use client';

import React, { useState, useEffect, useRef } from 'react';
import Head from 'next/head';
import styles from './Default.module.css'; 
import './Default.css'; 
import { Toast } from '@/app/types/auth';
import { LoginFormErrors } from '@/app/types/errors';
import { authService, setAuthToken, updateNotificationContainer } from '@/app/api';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

type LoginState = 'idle' | 'error' | 'loading' | 'success';

export default function KashlyLoginPage() {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [userId, setUserId] = useState('');
  const [rememberUser, setRememberUser] = useState(false);
  const [loginState, setLoginState] = useState<LoginState>('idle');
  const [showRegister, setShowRegister] = useState(false);
  const btnRef = useRef<HTMLButtonElement>(null);
  
  const [formData, setFormData] = useState({
      username: '',
      password: '',
    });

  const [registerData, setRegisterData] = useState({
    firstname: '',
    lastname: '',
    email: '',
    verificationCode: '',
    password: '',
    confirmPassword: '',
  });

  const [isChatOpen, setIsChatOpen] = useState(false);
  const [errors, setErrors] = useState<LoginFormErrors>({});
  const [registerErrors, setRegisterErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isRegisterSubmitting, setIsRegisterSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showRegisterPassword, setShowRegisterPassword] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);

  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const firstnameRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  useEffect(() => {
    document.title = showRegister ? "Create Account" : "Sign-In Account";
    if (showRegister) {
      firstnameRef.current?.focus();
    } else {
      usernameRef.current?.focus();
    }
  }, [showRegister]);

  useEffect(() => {
    document.title = "Sign-In Account"
    usernameRef.current?.focus();
  }, []);

  /* ── Slideshow ── */
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % 3);
    }, 5000);
    return () => clearInterval(interval);
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
    const newErrors: LoginFormErrors = {};

    if (!formData.username.trim()) {
      newErrors.username = 'Username required';
      setErrors(newErrors);
      showToast('Username required');
      usernameRef.current?.focus();
      return false;
    }
    
    if (!formData.password) {
      newErrors.password = 'Password required';
      setErrors(newErrors);
      showToast('Password required');
      passwordRef.current?.focus();
      return false;
    }

    setErrors({});
    return true;
  };

  const validateRegisterForm = () => {
    const newErrors: Record<string, string> = {};

    if (!registerData.firstname.trim()) {
      newErrors.firstname = 'First name required';
      setRegisterErrors(newErrors);
      showToast('First name required');
      firstnameRef.current?.focus();
      return false;
    }
    if (!registerData.lastname.trim()) {
      newErrors.lastname = 'Last name required';
      setRegisterErrors(newErrors);
      showToast('Last name required');
      return false;
    }
    if (!registerData.email.trim()) {
      newErrors.email = 'Email required';
      setRegisterErrors(newErrors);
      showToast('Email required');
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(registerData.email)) {
      newErrors.email = 'Invalid email address';
      setRegisterErrors(newErrors);
      showToast('Invalid email address');
      return false;
    }
    if (!registerData.verificationCode.trim()) {
      newErrors.verificationCode = 'Verification code required';
      setRegisterErrors(newErrors);
      showToast('Verification code required');
      return false;
    }
    if (!registerData.password) {
      newErrors.password = 'Password required';
      setRegisterErrors(newErrors);
      showToast('Password required');
      return false;
    }
    if (registerData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
      setRegisterErrors(newErrors);
      showToast('Password must be at least 6 characters');
      return false;
    }
    if (registerData.password !== registerData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
      setRegisterErrors(newErrors);
      showToast('Passwords do not match');
      return false;
    }

    setRegisterErrors({});
    return true;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name as keyof LoginFormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }));
    }
  };

  const handleRegisterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setRegisterData(prev => ({ ...prev, [name]: value }));
    if (registerErrors[name]) {
      setRegisterErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);

    try {
      const response = await authService.login(formData);
      const payload = {
        token: response.jwt,
        username: response.username,
        userId: response.userId,
        email: response.email,
        referral_link: response.referral_link,
        referral_username: response.referral_username,
        twoFactorAuthEnabled: response.twoFactorAuthEnabled,
        is_verify: response.is_verify,
        fullname: response.fullname,
        country: response.country,
        state: response.state,
        city: response.city,
        dob: response.date_of_birth,
        gender: response.gender,
        telephone: response.telephone || "",
        isCompleteProfile: response.is_profile_complete,
        sessionId: response.sessionId
      };
      if (response.twoFactorAuthEnabled === true) {
        router.push(`/auth/verify?token=${response.jwt}`);
        return; 
      } else if (response.is_profile_complete === false) {
        setAuthToken(payload);
        router.push("/settings/profile");
        return;
      } else {
        setAuthToken(payload);
        showToast('Login successful!', 'success');
        updateNotificationContainer({
          type: "MESSAGES",
          description: "User logged in successfully"
        });

        setFormData({ username: '', password: '' });
        router.push('/dashboard');
      }
      
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

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateRegisterForm()) return;

    setIsRegisterSubmitting(true);
    try {
      // Replace with your actual register API call
      // await authService.register(registerData);
      showToast('Account created successfully!', 'success');
      setRegisterData({ firstname: '', lastname: '', email: '', verificationCode: '', password: '', confirmPassword: '' });
      setShowRegister(false);
    } catch (error: any) {
      showToast(error.toString());
    } finally {
      setIsRegisterSubmitting(false);
    }
  };

  const btnClass = [
    styles.btnLogin,
    loginState === 'error' ? styles.btnLoginError : '',
    loginState === 'success' ? styles.btnLoginSuccess : '',
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <>
    <div className={styles.page}>
      <div className="toastrs">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`toastr toastr--${toast.type} ${toast.exiting ? 'toast-exit' : ''}`}>
            <div className="toast-icon">
              <i className={`fa ${toast.type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`} aria-hidden="true"></i>
            </div>
            <div className="toast-message">{toast.message}</div>
          </div>
        ))}
      </div>
        {/* ── HERO ── */}
        <section className={styles.hero}>

          {/* Slideshow */}
          <div className={styles.heroSlides}>
            <div className={`${styles.heroSlide} ${styles.heroSlide1} ${currentSlide === 0 ? styles.active : ''}`} />
            <div className={`${styles.heroSlide} ${styles.heroSlide2} ${currentSlide === 1 ? styles.active : ''}`} />
            <div className={`${styles.heroSlide} ${styles.heroSlide3} ${currentSlide === 2 ? styles.active : ''}`} />
          </div>

          <div className={styles.heroOverlay} />

          {/* Inner */}
          <div className={styles.heroInner}>

            {/* Copy */}
            <div className={styles.heroCopy}>
              <h1 className={styles.heroHeading}>
                Make every moment<br /><span>count</span>
              </h1>
              <p className={styles.heroSub}>
                You&apos;re going to love banking with us. We created accounts tailored
                to suit your individual needs as a valued customer.
              </p>
            </div>

            {/* ── LOGIN FORM ── */}
            {!showRegister && (
              <form onSubmit={handleSubmit} noValidate>
                <div className={styles.loginCard} id="loginCard">
                  <div className={styles.cardHeader}>
                    <div className={styles.cardHeaderTitle}>Internet Banking</div>
                  </div>

                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z" />
                    </svg>
                    <input
                      ref={usernameRef}
                      autoComplete='off'
                      type="text"
                      name="username"
                      id='username'
                      className={styles.cardInput}
                      value={formData.username}
                      onChange={handleChange}
                      placeholder="Enter your username or email"
                    />
                  </div>

                  <div className={styles.inputGroup}>  
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 17c1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3 1.34 3 3 3zm6-6v-2c0-2.76-2.24-5-5-5s-5 2.24-5 5v2H4v10h16V11h-2zm-8 0h4v2h-4v-2z" />
                    </svg>
                    <input
                      ref={passwordRef}
                      autoComplete='off'
                      type={showPassword ? 'text' : 'password'}
                      name="password"
                      id='password'
                      className={styles.cardInput}
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="•••••"
                    /> 
                  </div>
                  <p className={styles.inputHint}>If Corporate, format is Corp ID.User ID</p>

                  <div className={styles.cardRemember}>
                    <input
                      type="checkbox"
                      id="remember"
                      checked={rememberUser}
                      onChange={(e) => setRememberUser(e.target.checked)}
                    />
                    <label htmlFor="remember">
                      Remember User ID &nbsp;
                      <span style={{ color: '#aaa', fontSize: '0.7rem' }}>ⓘ</span>
                    </label>
                  </div>
                  
                  <button
                    type="submit"
                    ref={btnRef}
                    className={btnClass}
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? (
                      <>
                        <div className="spinner"></div>
                        <span>Logging in...</span>
                      </>
                    ) : (
                      'Login'
                    )}
                  </button>

                  <div className={styles.cardLinks}>
                    <Link href="/auth/forgot-pin" className={styles.cardLink}>Forgot Pin</Link>
                    <span className={styles.cardLinkSep}>|</span>
                    <Link href="/auth/forgot-password" className={styles.cardLink}>Forgot Password</Link>
                    <span className={styles.cardLinkSep}>|</span>
                    <Link href="/auth/forgot-user-id" className={styles.cardLink}>Forgot User ID</Link>
                  </div>

                  <div className={styles.cardRegister}>
                    <button
                      type="button"
                      className={styles.btnRegisterToggle}
                      onClick={() => setShowRegister(true)}
                    >
                      Instant Self-Registration
                    </button>
                  </div>
                </div>
              </form>
            )}

            {/* ── REGISTER FORM ── */}
            {showRegister && (
              <form onSubmit={handleRegisterSubmit} noValidate>
                <div className={`${styles.loginCard} ${styles.registerCard}`} id="registerCard">
                  <div className={styles.cardHeader}>
                    <button
                      type="button"
                      className={styles.cardBackBtn}
                      onClick={() => setShowRegister(false)}
                      aria-label="Back to login"
                    >
                      <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                        <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                      </svg>
                    </button>
                    <div className={styles.cardHeaderTitle}>Create Account</div>
                  </div>

                  {/* Row: First & Last name */}
                  <div className={styles.inputRow}>
                    <div className={styles.inputGroup}>
                      <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z" />
                      </svg>
                      <input
                        ref={firstnameRef}
                        autoComplete='off'
                        type="text"
                        name="firstname"
                        className={`${styles.cardInput} ${registerErrors.firstname ? styles.inputError : ''}`}
                        value={registerData.firstname}
                        onChange={handleRegisterChange}
                        placeholder="First name"
                      />
                    </div>

                    <div className={styles.inputGroup}>
                      <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z" />
                      </svg>
                      <input
                        autoComplete='off'
                        type="text"
                        name="lastname"
                        className={`${styles.cardInput} ${registerErrors.lastname ? styles.inputError : ''}`}
                        value={registerData.lastname}
                        onChange={handleRegisterChange}
                        placeholder="Last name"
                      />
                    </div>
                  </div>

                  {/* Email */}
                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                    </svg>
                    <input
                      autoComplete='off'
                      type="email"
                      name="email"
                      className={`${styles.cardInput} ${registerErrors.email ? styles.inputError : ''}`}
                      value={registerData.email}
                      onChange={handleRegisterChange}
                      placeholder="Email address"
                    />
                  </div>

                  {/* Verification Code */}
                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-1 14l-3-3 1.41-1.41L11 12.17l4.59-4.58L17 9l-6 6z"/>
                    </svg>
                    <input
                      autoComplete='off'
                      type="text"
                      name="verificationCode"
                      className={`${styles.cardInput} ${registerErrors.verificationCode ? styles.inputError : ''}`}
                      value={registerData.verificationCode}
                      onChange={handleRegisterChange}
                      placeholder="Verification code"
                    />
                  </div>

                  {/* Password */}
                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 17c1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3 1.34 3 3 3zm6-6v-2c0-2.76-2.24-5-5-5s-5 2.24-5 5v2H4v10h16V11h-2zm-8 0h4v2h-4v-2z" />
                    </svg>
                    <input
                      autoComplete='off'
                      type={showRegisterPassword ? 'text' : 'password'}
                      name="password"
                      className={`${styles.cardInput} ${registerErrors.password ? styles.inputError : ''}`}
                      value={registerData.password}
                      onChange={handleRegisterChange}
                      placeholder="Password"
                    />
                    <button
                      type="button"
                      className={styles.eyeBtn}
                      onClick={() => setShowRegisterPassword(p => !p)}
                      aria-label="Toggle password"
                    >
                      {showRegisterPassword ? (
                        <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                          <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24M1 1l22 22"/>
                        </svg>
                      ) : (
                        <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                        </svg>
                      )}
                    </button>
                  </div>

                  {/* Confirm Password */}
                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 17c1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3 1.34 3 3 3zm6-6v-2c0-2.76-2.24-5-5-5s-5 2.24-5 5v2H4v10h16V11h-2zm-8 0h4v2h-4v-2z" />
                    </svg>
                    <input
                      autoComplete='off'
                      type={showRegisterPassword ? 'text' : 'password'}
                      name="confirmPassword"
                      className={`${styles.cardInput} ${registerErrors.confirmPassword ? styles.inputError : ''}`}
                      value={registerData.confirmPassword}
                      onChange={handleRegisterChange}
                      placeholder="Confirm password"
                    />
                  </div>

                  <button
                    type="submit"
                    className={btnClass}
                    disabled={isRegisterSubmitting}
                    style={{ marginTop: '6px' }}
                  >
                    {isRegisterSubmitting ? (
                      <>
                        <div className="spinner"></div>
                        <span>Creating account...</span>
                      </>
                    ) : (
                      'Create Account'
                    )}
                  </button>

                  <div className={styles.cardRegister} style={{ marginTop: '8px' }}>
                    <span className={styles.cardLinkSep} style={{ fontSize: '0.75rem', color: '#666' }}>
                      Already have an account?{' '}
                    </span>
                    <button
                      type="button"
                      className={styles.cardLink}
                      style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                      onClick={() => setShowRegister(false)}
                    >
                      Sign in
                    </button>
                  </div>
                </div>
              </form>
            )}
          </div>

          {/* ── BOTTOM BANNER ── */}
          <section className={styles.bottomBanner}>
            <div className={styles.bottomCards}>

              <div className={styles.bottomCard}>
                <div className={styles.bcImg}>
                  <svg viewBox="0 0 24 24">
                    <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" />
                  </svg>
                </div>
                <div className={styles.bcBody}>
                  <div className={`${styles.bcTitle} ${styles.alert}`}>Security Alert!</div>
                  <p className={styles.bcText}>
                    Avoid sharing your card details, internet banking log in and PIN with anyone.
                  </p>
                  <div className={styles.bcLinks}>
                    <a className={styles.bcLink} href="#">Learn More</a>
                    <span className={styles.bcLinkSep}>|</span>
                    <a className={styles.bcLink} href="#">About Us</a>
                  </div>
                </div>
              </div>

              <div className={styles.bottomCard}>
                <div className={styles.bcImg}>
                  <svg viewBox="0 0 24 24">
                    <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z" />
                  </svg>
                </div>
                <div className={styles.bcBody}>
                  <div className={styles.bcTitle}>New User?</div>
                  <p className={styles.bcText}>
                    Our Internet Banking Service gives you unrestricted and secure access to your account.
                  </p>
                  <button
                    type="button"
                    className={styles.btnOpen}
                    onClick={() => setShowRegister(true)}
                  >
                    Open Account
                  </button>
                </div>
              </div>

              <div className={styles.bottomCard}>
                <div className={styles.bcImg}>
                  <svg viewBox="0 0 24 24">
                    <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z" />
                  </svg>
                </div>
                <div className={styles.bcBody}>
                  <div className={styles.bcTitle}>Contact Us</div>
                  <p className={styles.bcText}>
                    Our CFC is a 24/7 one-stop shop to help you with your requests. +234 700 000 0000
                  </p>
                  <div className={styles.bcLinks}>
                    <a className={styles.bcLink} href="#">Live Chat</a>
                    <span className={styles.bcLinkSep}>|</span>
                    <a className={styles.bcLink} href="#">Talk to Leo</a>
                    <span className={styles.bcLinkSep}>|</span>
                    <a className={styles.bcLink} href="#">ATM &amp; Branches</a>
                  </div>
                </div>
              </div>

            </div>
          </section>

          {/* ── FOOTER ── */}
          <footer className={styles.footer}>
            <div className={styles.footerSocial}>
              <a className={styles.socialBtn} href="#" aria-label="Facebook">f</a>
              <a className={styles.socialBtn} href="#" aria-label="Twitter">𝕏</a>
              <a className={styles.socialBtn} href="#" aria-label="Instagram">◎</a>
              <a className={styles.socialBtn} href="#" aria-label="YouTube">▶</a>
              <a className={styles.socialBtn} href="#" aria-label="LinkedIn">in</a>
            </div>
            <div className={styles.footerLinks}>
              <a href="#">Account Opening T&amp;C</a>
              <a href="#">Site Terms &amp; Conditions</a>
              <a href="#">Privacy Policy</a>
            </div>
            <div className={styles.footerCopy}>
              | © 2025. All Rights Reserved. &nbsp;
              <span className={styles.footerBrand}>Kashly</span>
            </div>
          </footer>

        </section> 
      </div>
    </>
  );
}