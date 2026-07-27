'use client';

import React, { useState, useEffect, useRef } from 'react';
import Head from 'next/head';
import styles from './Default.module.css'; 
import './Default.css'; 
import { Country, Toast } from '@/app/types/auth';
import { LoginFormErrors } from '@/app/types/errors';
import { authService, setAuthToken, updateNotificationContainer } from '@/app/api';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { countries } from '@/components/countries';

type LoginState = 'idle' | 'error' | 'loading' | 'success';

// ── Shape of a structured API error response, e.g.:
// { errorCode: "VALID_11001", errorId: "...", fieldErrors: { password: "..." },
//   message: "Validation failed", path: "/auth/login", status: 400, success: false, timestamp: "..." }
// or the simpler shape: { status: 400, success: false, message: "Invalid user credentials." }
interface ApiErrorResponse {
  errorCode?: string;
  errorId?: string;
  fieldErrors?: Record<string, string>;
  message: string;
  path?: string;
  status: number;
  success: false;
  timestamp?: string;
}

/**
 * Normalizes whatever `authService` throws into an ApiErrorResponse, or
 * returns null if it doesn't look like one of our backend's error shapes
 * (e.g. a network failure, where `error` is a plain Error/TypeError).
 *
 * Handles a few common cases so this keeps working regardless of exactly
 * how authService surfaces errors:
 *   1. authService throws the parsed JSON body directly.
 *   2. authService throws an Error whose `.message` IS the JSON string.
 *   3. axios-style: authService throws an error with `.response.data`.
 *
 * NOTE: if authService currently does something like
 * `throw new Error(data.message)`, fieldErrors/errorCode are being
 * discarded before they ever reach this component — that needs to throw
 * the full parsed body (or attach it as `error.data`) instead.
 */
function parseApiError(error: any): ApiErrorResponse | null {
  if (error && typeof error === 'object' && error.success === false && typeof error.message === 'string') {
    return error as ApiErrorResponse;
  }
  if (error?.response?.data && typeof error.response.data === 'object') {
    return error.response.data as ApiErrorResponse;
  }
  if (error?.data && typeof error.data === 'object' && error.data.success === false) {
    return error.data as ApiErrorResponse;
  }
  if (typeof error?.message === 'string') {
    try {
      const parsed = JSON.parse(error.message);
      if (parsed && typeof parsed === 'object' && 'success' in parsed) {
        return parsed as ApiErrorResponse;
      }
    } catch {
      // error.message wasn't JSON — fall through
    }
  }
  return null;
}

export default function Default() {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [rememberUser, setRememberUser] = useState(false);
  const [loginState, setLoginState] = useState<LoginState>('idle');
  const [showRegister, setShowRegister] = useState(false);
  const [showForgetUsernameForm, setshowForgetUsernameForm] = useState(false);
  const [showResetPasswordForm, setShowResetPasswordForm] = useState(false);
  const [showOTPForm, setShowOTPForm] = useState(false);
  const btnRef = useRef<HTMLButtonElement>(null);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [errors, setErrors] = useState<LoginFormErrors>({});
  const [registerErrors, setRegisterErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isRegisterSubmitting, setIsRegisterSubmitting] = useState(false);
  const [selectedCountry, setSelectedCountry] = useState<Country | null>(null);
  const [showRegisterPassword, setShowRegisterPassword] = useState(false);
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [isScrolling, setIsScrolling] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isRequestingCode, setIsRequestingCode] = useState(false);
  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const firstnameRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const forgotPinEmailRef = useRef<HTMLInputElement>(null);
  const resetEmailRef = useRef<HTMLInputElement>(null);
  const resetPhoneRef = useRef<HTMLInputElement>(null);
  const scrollTimer = useRef<NodeJS.Timeout | null>(null);
  const router = useRouter();
  const [regMode, setRegMode] = useState<'email' | 'phone'>('email');
  const [codeSent, setCodeSent] = useState(false);
   const [code, setCode] = useState(['', '', '', '']);
  const [isResending, setIsResending] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const [formData, setFormData] = useState({
    firstname: '',
    lastname: '',
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    verificationCode: '',
  });

  const [registerData, setRegisterData] = useState({
    firstname: '', lastname: '', username: '',  phone: '',
    email: '', verificationCode: '', password: '', confirmPassword: '',
  });

  // ── Dedicated state for Forgot Pin form ──
  const [forgotPinData, setForgotPinData] = useState({ email: '' });
  const [forgotPinErrors, setForgotPinErrors] = useState<Record<string, string>>({});
  const [isForgotPinSubmitting, setIsForgotPinSubmitting] = useState(false);

  // ── Dedicated state for Reset Password form ──
  const [resetPasswordData, setResetPasswordData] = useState({ email: '', phone: '' });
  const [resetPasswordErrors, setResetPasswordErrors] = useState<Record<string, string>>({});
  const [isResetPasswordSubmitting, setIsResetPasswordSubmitting] = useState(false);

  // ── Dedicated state for Confirm Reset Password form (step 2) ──
  const [showConfirmResetForm, setShowConfirmResetForm] = useState(false);
  const [confirmResetData, setConfirmResetData] = useState({ password: '', confirmPassword: '' });
  const [confirmResetPin, setConfirmResetPin] = useState(['', '', '', '']);
  const [showConfirmResetPassword, setShowConfirmResetPassword] = useState(false);
  const [showConfirmResetConfirmPassword, setShowConfirmResetConfirmPassword] = useState(false);
  const [isConfirmResetSubmitting, setIsConfirmResetSubmitting] = useState(false);
  const confirmResetPasswordRef = React.useRef<HTMLInputElement>(null);
  const confirmResetConfirmPasswordRef = React.useRef<HTMLInputElement>(null);

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

  useEffect(() => {
    if (inputRefs.current[0]) {
    inputRefs.current[0].focus();
    }
  }, []);
  
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (countdown > 0) {
    timer = setInterval(() => {
        setCountdown((prev) => prev - 1);
    }, 1000);
    }
    return () => {
      if (timer) clearInterval(timer);
    };
  }, [countdown]);


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

  /**
   * Shared error handler for every auth form. Give it:
   *  - the caught error
   *  - (optional) a field-error setter, so validation errors like
   *    { fieldErrors: { password: "..." } } populate the right form field
   *  - (optional) a map of field name -> input ref, so the offending
   *    field gets focused
   *  - (optional) a fallback message for the generic case (e.g. wrong
   *    username/password), since the backend's own `message` is often
   *    fine to show as-is but you may want something friendlier
   */
  const handleAuthError = (
    error: any,
    setFieldErrors?: React.Dispatch<React.SetStateAction<Record<string, string>>>,
    fieldRefs?: Record<string, React.RefObject<HTMLInputElement | null>>,
    fallbackMessage?: string
  ) => {
    const apiError = parseApiError(error);
console.log('RAW ERROR:', error);
    if (apiError) {
      if (apiError.fieldErrors && Object.keys(apiError.fieldErrors).length > 0) {
        const fieldErrors = apiError.fieldErrors;
        setFieldErrors?.((prev) => ({ ...prev, ...fieldErrors }));

        const firstField = Object.keys(fieldErrors)[0];
        showToast(fieldErrors[firstField]);
        fieldRefs?.[firstField]?.current?.focus();
      } else {
        // e.g. { message: "Invalid user credentials." } — no field to blame,
        // so just surface the backend's message (or errorCode as fallback)
        showToast(apiError.message || fallbackMessage || 'Something went wrong. Please try again.');
      }
      return;
    }

    // Not a recognized API error shape — likely a network/runtime error
    const errorMsg = error?.toString?.() ?? fallbackMessage ?? 'An unexpected error occurred';
    if (errorMsg.includes('internet connection') || errorMsg.includes('Failed to fetch') || errorMsg.includes('NetworkError')) {
      showToast('You are offline. Please check your network.');
    } else if (errorMsg.includes('maintenance') || errorMsg.includes('down')) {
      showToast('Service unavailable. The server might be down.');
    } else {
      showToast(fallbackMessage || errorMsg);
    }
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
    if (!registerData.username.trim()) {
      newErrors.username = 'Username required';
      setRegisterErrors(newErrors);
      showToast('Username required');
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

  const validateForgotPinForm = () => {
    const newErrors: Record<string, string> = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!forgotPinData.email.trim()) {
      newErrors.email = 'Email is required';
      setForgotPinErrors(newErrors);
      showToast('Email is required');
      forgotPinEmailRef.current?.focus();
      return false;
    }
    if (!emailRegex.test(forgotPinData.email.trim())) {
      newErrors.email = 'Invalid email address';
      setForgotPinErrors(newErrors);
      showToast('Invalid email address');
      forgotPinEmailRef.current?.focus();
      return false;
    }

    setForgotPinErrors({});
    return true;
  };

  const validateResetPasswordForm = () => {
    const newErrors: Record<string, string> = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (regMode === 'email') {
      if (!resetPasswordData.email.trim()) {
        newErrors.email = 'Email is required';
        setResetPasswordErrors(newErrors);
        showToast('Email is required');
        resetEmailRef.current?.focus();
        return false;
      }
      if (!emailRegex.test(resetPasswordData.email.trim())) {
        newErrors.email = 'Invalid email address';
        setResetPasswordErrors(newErrors);
        showToast('Invalid email address');
        resetEmailRef.current?.focus();
        return false;
      }
    } else {
      // phone mode
      if (!selectedCountry) {
        newErrors.phone = 'Please select a country code';
        setResetPasswordErrors(newErrors);
        showToast('Please select a country code');
        return false;
      }
      if (!resetPasswordData.phone.trim()) {
        newErrors.phone = 'Phone number is required';
        setResetPasswordErrors(newErrors);
        showToast('Phone number is required');
        resetPhoneRef.current?.focus();
        return false;
      }
      const digitsOnly = resetPasswordData.phone.replace(/\D/g, '');
      if (digitsOnly.length < 7 || digitsOnly.length > 15) {
        newErrors.phone = 'Enter a valid phone number';
        setResetPasswordErrors(newErrors);
        showToast('Enter a valid phone number');
        resetPhoneRef.current?.focus();
        return false;
      }
    }

    setResetPasswordErrors({});
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

  const handleForgotPinChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForgotPinData(prev => ({ ...prev, [name]: value }));
    if (forgotPinErrors[name]) {
      setForgotPinErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleResetPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setResetPasswordData(prev => ({ ...prev, [name]: value }));
    if (resetPasswordErrors[name]) {
      setResetPasswordErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleLoginSubmit = async (e: React.FormEvent) => {
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
        setShowOTPForm(true);
        router.replace(`?token=${response.jwt}`, { scroll: false });
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
        router.push('/dashboard');
      }
      
    } catch (error: any) {
      handleAuthError(
        error,
        setErrors as React.Dispatch<React.SetStateAction<Record<string, string>>>,
        { username: usernameRef, password: passwordRef },
        'Invalid username or password.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateRegisterForm()) return;

    setIsRegisterSubmitting(true);
      try {
        const payload = {
          firstname: registerData.firstname,
          lastname: registerData.lastname,
          username: registerData.username,
          email: registerData.email,
          phone: '',    
          password: registerData.password,
          confirmPassword: registerData.confirmPassword,
          verificationCode: registerData.verificationCode,
          regMode: 'email',
          verificationMethod: 'EMAIL',
        };

      await authService.register(payload);
      showToast('Account created successfully!', 'success');
      setRegisterData({
        firstname: '', lastname: '', username: '',
        email: '', phone: '', 
        verificationCode: '', password: '', confirmPassword: '',
      });
      setCodeSent(false);
      setShowRegister(false);
    } catch (error: any) {
      handleAuthError(
        error,
        setRegisterErrors,
        { username: usernameRef, email: emailRef, firstname: firstnameRef },
        'Could not create account. Please check your details and try again.'
      );
    } finally {
      setIsRegisterSubmitting(false);
    }
  };

  const handleForgotPinSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForgotPinForm()) return;

    setIsForgotPinSubmitting(true);
    try {
      await authService.forgotUsername(forgotPinData.email);
      showToast('Your username has been sent to your email!', 'success');
      setForgotPinData({ email: '' });
      setForgotPinErrors({});
      showForm('login');
    } catch (error: any) {
      handleAuthError(
        error,
        setForgotPinErrors,
        { email: forgotPinEmailRef },
        'Could not send username. Please check the email and try again.'
      );
    } finally {
      setIsForgotPinSubmitting(false);
    }
  };

  const handleResetPasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateResetPasswordForm()) return;

    setIsResetPasswordSubmitting(true);
    try {
      const payload =
        regMode === 'email'
          ? { identifier: resetPasswordData.email, method: 'EMAIL' }
          : { identifier: `${selectedCountry?.code}${resetPasswordData.phone}`, method: 'PHONE' };

      await authService.forgotPassword(payload);
      showToast('Password reset code sent to your email/phone!', 'success');
      setResetPasswordErrors({});
      // Move to step 2: confirm reset form
      setShowConfirmResetForm(true);
    } catch (error: any) {
      handleAuthError(
        error,
        setResetPasswordErrors,
        { email: resetEmailRef, phone: resetPhoneRef },
        'Could not send reset code. Please check your details and try again.'
      );
    } finally {
      setIsResetPasswordSubmitting(false);
    }
  };

  const validateConfirmResetForm = () => {
    const { password, confirmPassword } = confirmResetData;

    if (!password) {
      showToast('Password is required');
      confirmResetPasswordRef.current?.focus();
      return false;
    }
    if (password.length < 8) {
      showToast('Password must be at least 8 characters');
      confirmResetPasswordRef.current?.focus();
      return false;
    }
    if (!/[a-z]/.test(password)) {
      showToast('Password must contain a lowercase letter');
      confirmResetPasswordRef.current?.focus();
      return false;
    }
    if (!/[A-Z]/.test(password)) {
      showToast('Password must contain an uppercase letter');
      confirmResetPasswordRef.current?.focus();
      return false;
    }
    if (!/[0-9]/.test(password)) {
      showToast('Password must contain a number');
      confirmResetPasswordRef.current?.focus();
      return false;
    }
    if (!/[^A-Za-z0-9]/.test(password)) {
      showToast('Password must contain a special character');
      confirmResetPasswordRef.current?.focus();
      return false;
    }
    if (password !== confirmPassword) {
      showToast('Passwords do not match');
      confirmResetConfirmPasswordRef.current?.focus();
      return false;
    }
    const otp = confirmResetPin.join('');
    if (otp.length !== 4) {
      showToast('Please enter the 4-digit OTP');
      return false;
    }
    return true;
  };

  const handleConfirmResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateConfirmResetForm()) return;

    setIsConfirmResetSubmitting(true);
    try {
      const identifier =
        regMode === 'email'
          ? resetPasswordData.email
          : `${selectedCountry?.code}${resetPasswordData.phone}`;

      await authService.resetPassword({
        identifier,
        otp: confirmResetPin.join(''),
        newPassword: confirmResetData.password,
      });

      showToast('Password reset successfully!', 'success');
      // Reset all state and go back to login
      setTimeout(() => {
        setConfirmResetData({ password: '', confirmPassword: '' });
        setConfirmResetPin(['', '', '', '']);
        setResetPasswordData({ email: '', phone: '' });
        setShowConfirmResetForm(false);
        showForm('login');
      }, 1500);
    } catch (error: any) {
      handleAuthError(
        error,
        undefined,
        { password: confirmResetPasswordRef, confirmPassword: confirmResetConfirmPasswordRef },
        'Could not reset password. The code may be invalid or expired.'
      );
    } finally {
      setIsConfirmResetSubmitting(false);
    }
  };

  const handleConfirmResetPinChange = (index: number, value: string) => {
    if (!/^\d?$/.test(value)) return;
    const newPin = [...confirmResetPin];
    newPin[index] = value;
    setConfirmResetPin(newPin);
    if (value && index < 3) {
      const next = document.getElementById(`confirm-reset-pin-${index + 1}`);
      if (next) (next as HTMLInputElement).focus();
    }
  };

  const handleConfirmResetPinKeyDown = (index: number, e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && !confirmResetPin[index] && index > 0) {
      const prev = document.getElementById(`confirm-reset-pin-${index - 1}`);
      if (prev) (prev as HTMLInputElement).focus();
    }
  };

  const checkResetStrength = (req: boolean) => (req ? 'valid' : 'invalid');

  const handleRequestCode = async () => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!registerData.email.trim()) {
      showToast('Please enter your email address first');
      emailRef.current?.focus();
      return;
    }
    if (!emailRegex.test(registerData.email.trim())) {
      showToast('Invalid email address');
      emailRef.current?.focus();
      return;
    }

    setIsRequestingCode(true);
    try {
      const response = await authService.sendVerifyCode(registerData.email, 'EMAIL');
      if (response.status !== 201) {
        setCodeSent(false);
        throw new Error('Failed to send verification code. Please try again.');
      } else {
        setCodeSent(true);
        showToast('Verification code sent to your email!', 'success');
      }
    } catch (error: any) {
      handleAuthError(error, undefined, { email: emailRef }, 'Could not send verification code. Please try again.');
    } finally {
      setIsRequestingCode(false);
    }
  };

  const handleSwitchMode = (mode: 'email' | 'phone') => {
    setRegMode(mode);
    setResetPasswordData({ email: '', phone: '' });
    setResetPasswordErrors({});
    setSelectedCountry(null);
    setIsResetPasswordSubmitting(false);
  };

  const showForm = (form: 'login' | 'register' | 'forgotPin' | 'resetPassword') => {
    setShowRegister(form === 'register');
    setshowForgetUsernameForm(form === 'forgotPin');
    setShowResetPasswordForm(form === 'resetPassword');
    if (form !== 'resetPassword') {
      setShowConfirmResetForm(false);
    }
  };

  const handleScroll = () => {
      setIsScrolling(true);
      if (scrollTimer.current) clearTimeout(scrollTimer.current);
      scrollTimer.current = setTimeout(() => {
      setIsScrolling(false);
      }, 1000);
  };

  const filteredCountries = countries.filter(c => 
    c.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleOTPChange = (index: number, value: string) => {
    if (value.length > 1) return;
    if (value && !/^\d$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);

    if (value && index < 3) {
        inputRefs.current[index + 1]?.focus();
    }
    if (value && index === 3) {
        const verificationCode = newCode.join('');
    }
  };

  const handleOTPSubmitWithCode = async (verificationCode: string) => {
    setIsSubmitting(true);

    try {
        const payload = { otp: verificationCode };
        const response = await authService.verifyOtp(payload);

        const authPayload = {
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
        updateNotificationContainer({
            type: "welcome",
            description: "Welcome to our platform!",
            date: new Date().toISOString()
        });

        setAuthToken(authPayload);
        showToast('Verification successful!', 'success');
        setTimeout(() => {
            router.push('/dashboard');
        }, 1000);
    } catch (error: any) {
        const apiError = parseApiError(error);
        const code = apiError?.errorCode ?? '';
        const message = apiError?.message ?? '';

        if (/invalid/i.test(code) || /invalid/i.test(message)) {
            showToast('Invalid verification code. Please try again.');
            setCode(['', '', '', '']);
            inputRefs.current[0]?.focus();
        } else if (/expired/i.test(code) || /expired/i.test(message)) {
            showToast('Verification code has expired. Please request a new one.');
            setCode(['', '', '', '']);
            inputRefs.current[0]?.focus();
        } else {
            handleAuthError(error, undefined, undefined, 'Verification failed. Please try again.');
        }
    } finally {
        setIsSubmitting(false);
    }
  };
  
  const handleOTPKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
    if (!code[index] && index > 0) {
        const newCode = [...code];
        newCode[index - 1] = '';
        setCode(newCode);
        inputRefs.current[index - 1]?.focus();
    }
    } else if (e.key === 'ArrowLeft' && index > 0) {
    inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < 3) {
    inputRefs.current[index + 1]?.focus();
    }
  };
  
  const handleOTPPaste = (e: React.ClipboardEvent) => {
      e.preventDefault();
      const pastedData = e.clipboardData.getData('text').trim();
      
      // Check if pasted data is a 4-digit number
      if (/^\d{4}$/.test(pastedData)) {
      const digits = pastedData.split('');
      const newCode = [...code];
      
      digits.forEach((digit, index) => {
          if (index < 4) {
          newCode[index] = digit;
          }
      });
      
      setCode(newCode);
      
      // Focus last input
      setTimeout(() => {
          const lastFilledIndex = newCode.findIndex(digit => digit === '');
          const focusIndex = lastFilledIndex === -1 ? 3 : Math.min(lastFilledIndex, 3);
          inputRefs.current[focusIndex]?.focus();
      }, 0);
      } else {
      showToast('Please paste a valid 4-digit code');
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
            {!showRegister && !showForgetUsernameForm && !showOTPForm && !showResetPasswordForm && (
              <form onSubmit={handleLoginSubmit} noValidate>
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
                      className={`${styles.cardInput} ${errors.username ? styles.inputError : ''}`}
                      value={formData.username}
                      onChange={handleChange}
                      placeholder="Enter your username or email"
                    />
                  </div>
                  {errors.username && <p style={{ color: '#e53e3e', fontSize: '0.75rem', margin: '4px 0 0' }}>{errors.username}</p>}

                  <div className={styles.inputGroup}>  
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 17c1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3 1.34 3 3 3zm6-6v-2c0-2.76-2.24-5-5-5s-5 2.24-5 5v2H4v10h16V11h-2zm-8 0h4v2h-4v-2z" />
                    </svg>
                    <input
                      ref={passwordRef}
                      autoComplete='off'
                      type={showLoginPassword ? 'text' : 'password'}
                      name="password"
                      id='password'
                      className={`${styles.cardInput} ${errors.password ? styles.inputError : ''}`}
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="•••••"
                    /> 
                     <button
                      type="button"
                      className={styles.eyeBtn}
                      onClick={() => setShowLoginPassword(p => !p)}
                      aria-label="Toggle password"
                    >
                      <i className={`fa-solid ${showLoginPassword ? 'fa-eye-slash' : 'fa-eye'}`} style={{fontSize:"12px"}}></i>
                    </button>
                  </div>
                  {errors.password && <p style={{ color: '#e53e3e', fontSize: '0.75rem', margin: '4px 0 0' }}>{errors.password}</p>}
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
                    onClick={handleLoginSubmit}
                    disabled={isSubmitting}>
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
                    <Link href="#" className={styles.cardLink} onClick={() => showForm('forgotPin')}>Forgot Pin</Link>
                    <span className={styles.cardLinkSep}>|</span>
                    <Link href="#" className={styles.cardLink} onClick={() => showForm('resetPassword')}>Forgot Password</Link>
              
                  </div>

                  <div className={styles.cardRegister}>
                    <button
                      type="button"
                      className={styles.btnRegisterToggle}
                      onClick={() => showForm('register')}>
                      Instant Self-Registration
                    </button>
                  </div>
                </div>
              </form>
            )}

            {/* ── REGISTER FORM ── */}
            {showRegister && (
              <form onSubmit={handleRegisterSubmit} noValidate>
                <div className={`${styles.RegisterCard} ${styles.registerCard}`} id="registerCard">
                  <div className={styles.cardHeader}>
                    <button
                      type="button"
                      className={styles.cardBackBtn}
                      onClick={() => showForm('login')}
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
                 <div className={styles.inputGroup}>
                    <input ref={usernameRef} type="text" name="username" className={`${styles.cardInput} ${registerErrors.username ? styles.inputError : ''}`} value={registerData.username} onChange={handleRegisterChange} placeholder="Username" />
                  </div>
                  {/* Email */}
                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                    </svg>
                    <input
                      ref={emailRef}
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
                    <div className={styles.verificationWrapper}>
                      <input type="text" name="verificationCode" value={registerData.verificationCode} className={`${styles.formControl} ${registerErrors.verificationCode ? styles.inputError : ''}`} onChange={handleRegisterChange} placeholder="Enter code" />
                      <button type="button" className={styles.btnRequestCode} onClick={handleRequestCode} disabled={isRequestingCode}>
                        {isRequestingCode ? 'Sending...' : codeSent ? 'Resend Code' : 'Send Code'}
                      </button>
                    </div>
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
                      <i className={`fa-solid ${showRegisterPassword ? 'fa-eye-slash' : 'fa-eye'}`} style={{fontSize:"12px"}}></i>
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
                  style={{ marginTop: '6px' }}>
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

            {/* ── FORGOT PIN (Retrieve Username) Form ── */}
            {!showRegister && showForgetUsernameForm && !showOTPForm && !showResetPasswordForm && (
              <form onSubmit={handleForgotPinSubmit} noValidate>
                <div className={styles.ForgetUsernameCard}>
                  <div className={styles.cardHeader}>
                    <button
                      type="button"
                      className={styles.cardBackBtn}
                      onClick={() => showForm('login')}
                      aria-label="Back to login"
                    >
                      <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                        <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                      </svg>
                    </button>
                    <h2 className={styles.cardHeaderTitle}>Retrieve Username</h2>
                  </div>

                  <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                    </svg>
                    <input
                      ref={forgotPinEmailRef}
                      autoComplete='off'
                      type="email"
                      name="email"
                      className={`${styles.cardInput} ${forgotPinErrors.email ? styles.inputError : ''}`}
                      value={forgotPinData.email}
                      onChange={handleForgotPinChange}
                      placeholder="Enter your registered email"
                    />
                  </div>

                  <button
                    type="submit"
                    className={btnClass}
                    style={{ marginTop: '6px' }}
                    disabled={isForgotPinSubmitting}
                  >
                    {isForgotPinSubmitting ? (
                      <>
                        <div className="spinner"></div>
                        <span>Submitting...</span>
                      </>
                    ) : (
                      'Submit'
                    )}
                  </button>
                </div>
              </form>
            )}

            {/* ── CONFIRM RESET PASSWORD Form (Step 2) ── */}
            {!showRegister && !showForgetUsernameForm && !showOTPForm && showResetPasswordForm && showConfirmResetForm && (
              <div className={styles.ResetPasswordCard}>
                <div className={styles.cardHeader}>
                  <button
                    type="button"
                    className={styles.cardBackBtn}
                    onClick={() => setShowConfirmResetForm(false)}
                    aria-label="Back to send code">
                    <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                      <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                    </svg>
                  </button>
                  <h2 className={styles.cardHeaderTitle}>Create New Password</h2>
                </div>

                <p className={styles.ResetPasswordSubtitleHeader} style={{ marginBottom: '14px', marginTop: '4px' }}>
                  OTP sent to your email/phone. Enter your new password and the OTP to complete reset.
                </p>

                <form onSubmit={handleConfirmResetSubmit} noValidate>
                  {/* Password */}
                  <div className={styles.inputGroup} style={{ position: 'relative' }}>
                    <input
                      ref={confirmResetPasswordRef}
                      type={showConfirmResetPassword ? 'text' : 'password'}
                      name="password"
                      className={styles.cardInput}
                      value={confirmResetData.password}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setConfirmResetData((prev: typeof confirmResetData) => ({ ...prev, password: e.target.value }))}
                      placeholder="New password"
                      style={{ paddingLeft: '12px' }}
                    />
                    <button
                      type="button"
                      className={styles.eyeBtn}
                      onClick={() => setShowConfirmResetPassword((p: boolean) => !p)}
                      aria-label="Toggle password visibility"
                    >
                      <i className={`fa-solid ${showConfirmResetPassword ? 'fa-eye-slash' : 'fa-eye'}`} style={{ fontSize: '12px' }}></i>
                    </button>
                  </div>

                  {/* Password strength hints */}
                  <div className={styles.passwordConstraints}>
                    <span className={checkResetStrength(confirmResetData.password.length >= 8)}><i className="fa fa-info-circle"></i> At least 8 characters</span>
                    <span className={checkResetStrength(/[a-z]/.test(confirmResetData.password))}><i className="fa fa-info-circle"></i> Lowercase (a-z)</span>
                    <span className={checkResetStrength(/[A-Z]/.test(confirmResetData.password))}><i className="fa fa-info-circle"></i> Uppercase (A-Z)</span>
                    <span className={checkResetStrength(/[0-9]/.test(confirmResetData.password))}><i className="fa fa-info-circle"></i> Number (0-9)</span>
                    <span className={checkResetStrength(/[^A-Za-z0-9]/.test(confirmResetData.password))}><i className="fa fa-info-circle"></i> Special character</span>
                  </div>

                  {/* Confirm Password */}
                  <div className={styles.inputGroup} style={{ position: 'relative', marginTop: '10px' }}>
                    <input
                      ref={confirmResetConfirmPasswordRef}
                      type={showConfirmResetConfirmPassword ? 'text' : 'password'}
                      name="confirmPassword"
                      className={styles.cardInput}
                      value={confirmResetData.confirmPassword}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => setConfirmResetData((prev: typeof confirmResetData) => ({ ...prev, confirmPassword: e.target.value }))}
                      placeholder="Confirm new password"
                      style={{ paddingLeft: '12px' }}
                    />
                    <button
                      type="button"
                      className={styles.eyeBtn}
                      onClick={() => setShowConfirmResetConfirmPassword((p: boolean) => !p)}
                      aria-label="Toggle confirm password visibility"
                    >
                      <i className={`fa-solid ${showConfirmResetConfirmPassword ? 'fa-eye-slash' : 'fa-eye'}`} style={{ fontSize: '12px' }}></i>
                    </button>
                  </div>

                  {/* OTP */}
                  <div style={{ marginTop: '12px' }}>
                    <p style={{ fontSize: '11px', color: '#555', marginBottom: '6px', textAlign: 'center', fontFamily: 'Karla, sans-serif' }}>Enter OTP</p>
                    <div className={styles.confirmResetPinInputs}>
                      {confirmResetPin.map((digit: string, index: number) => (
                        <input
                          key={index}
                          id={`confirm-reset-pin-${index}`}
                          type="password"
                          className={styles.confirmResetPinInput}
                          maxLength={1}
                          value={digit}
                          onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleConfirmResetPinChange(index, e.target.value)}
                          onKeyDown={(e: React.KeyboardEvent<HTMLInputElement>) => handleConfirmResetPinKeyDown(index, e)}
                          autoFocus={index === 0}
                          disabled={isConfirmResetSubmitting}
                        />
                      ))}
                    </div>
                  </div>

                  <button
                    type="submit"
                    className={styles.btnSubmit}
                    disabled={isConfirmResetSubmitting}
                    style={{ marginTop: '14px' }}
                  >
                    {isConfirmResetSubmitting ? (
                      <>
                        <div className={styles.spinner}></div>
                        <span>Resetting...</span>
                      </>
                    ) : (
                      'Reset Password'
                    )}
                  </button>
                </form>
              </div>
            )}

            {/* ── RESET PASSWORD Form ── */}
            {!showRegister && !showForgetUsernameForm && !showOTPForm && showResetPasswordForm && !showConfirmResetForm && (
              <div className={styles.ResetPasswordCard}>
                <div className={styles.cardHeader}>
                  <button
                    type="button"
                    className={styles.cardBackBtn}
                    onClick={() => showForm('login')}
                    aria-label="Back to login">
                    <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                      <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                    </svg>
                  </button>
                  <h2 className={styles.cardHeaderTitle}>Reset Your Password</h2>
                </div>
              
                <div className={styles.toggleContainer}>
                  <button 
                    type="button"
                    className={regMode === 'email' ? styles.active : ''} 
                    onClick={() => handleSwitchMode('email')}>
                    Email
                  </button>
                  <button 
                    type="button"
                    className={regMode === 'phone' ? styles.active : ''} 
                    onClick={() => handleSwitchMode('phone')}>
                    Phone
                  </button>
                </div>

                <form onSubmit={handleResetPasswordSubmit} noValidate>
                  {regMode === 'email' ? (
                   
                   <div className={styles.inputGroup}>
                    <svg className={styles.inputIcon} viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                    </svg>
                     <input
                        ref={resetEmailRef}
                        autoComplete='off'
                        type="email"
                        name="email"
                        className={`${styles.cardInput} ${resetPasswordErrors.email ? styles.inputError : ''}`}
                        value={resetPasswordData.email}
                        onChange={handleResetPasswordChange}
                        placeholder="Email"
                      />
                  </div>
                  ) : (
                    <div className={styles.formGroup}>
                      <div className={styles.phoneInputGroup}>
                        <div
                          className={`${styles.countryDropdown} ${resetPasswordErrors.phone ? styles.inputError : ''}`}
                          onClick={() => setIsModalOpen(true)}
                        >
                          <span>
                            {selectedCountry 
                              ? `${selectedCountry.abbr3} (${selectedCountry.code})` 
                              : 'Country'}
                          </span>
                          <i className="fa fa-chevron-down"></i>
                        </div>
                        <input
                          ref={resetPhoneRef}
                          autoComplete='off'
                          type="tel"
                          name="phone"
                          className={`${styles.formControl} ${resetPasswordErrors.phone ? styles.inputError : ''}`}
                          value={resetPasswordData.phone}
                          onChange={handleResetPasswordChange}
                          placeholder="Phone number"
                        />
                      </div>
                    </div>
                  )}

                  <button
                    type="submit"
                    className={styles.btnSubmit}
                    disabled={isResetPasswordSubmitting}
                  >
                    {isResetPasswordSubmitting ? (
                      <>
                        <div className={styles.spinner}></div>
                        <span>Processing...</span>
                      </>
                    ) : (
                      'Send password reset code'
                    )}
                  </button>
                </form>
              </div>
            )}

            {!showRegister && !showForgetUsernameForm && !showResetPasswordForm && showOTPForm && (
              <div className={styles.OTPCard}>
                <div className={styles.cardHeader}>
                  <button
                    type="button"
                    className={styles.cardBackBtn}
                    onClick={() => showForm('login')}
                    aria-label="Back to login">
                    <svg viewBox="0 0 24 24" fill="currentColor" width="14" height="14">
                      <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
                    </svg>
                  </button>
                  <h2 className={styles.cardHeaderTitle}>Two-Factor Authentication</h2>
                </div>

                <div className={styles.otpInfo}>
                  <p>Enter the 4-digit code sent to your email/phone to complete login.</p>
                </div>
                
                <form onSubmit={(e) => { e.preventDefault(); handleOTPSubmitWithCode(code.join('')); }} className={styles.otpForm}>
                  <div className={styles.codeInputs} onPaste={handleOTPPaste}>
                    {code.map((digit, index) => (
                      <input
                        key={index}
                        ref={(el) => { inputRefs.current[index] = el; }}
                        type="text"
                        inputMode="numeric"
                        pattern="[0-9]*"
                        maxLength={1}
                        value={digit}
                        onChange={(e) => handleOTPChange(index, e.target.value)}
                        onKeyDown={(e) => handleOTPKeyDown(index, e)}
                        onPaste={index === 0 ? handleOTPPaste : undefined}
                        className={styles.codeInput}
                        autoComplete="one-time-code"
                        disabled={isSubmitting}
                      />
                    ))}
                  </div>
                  <div className={styles.verificationActions}>
                    <button type="submit" className={styles.btnResend} disabled={isSubmitting}>
                    {isResending ? (
                        <>
                          <div className={styles.spinner + ' ' + styles.spinnerSmall}></div>
                          <span>Sending...</span>
                        </>
                      ) : countdown > 0 ? (
                        `Resend in ${countdown}s`
                      ) : (
                        'Resend Code'
                      )}
                    </button>

                    <button type="submit" className={styles.btnOTP} disabled={isSubmitting || code.join('').length !== 4}>
                      {isSubmitting ? (
                        <>
                          <div className={styles.spinner}></div>
                          <span>Verifying...</span>
                        </>
                      ) : (
                        'Verify'
                      )}
                    </button>
                  </div>
                </form>

                 <div className={styles.backToLogin}>
                  <button type="button" className={styles.forgotPasswordLink}  onClick={() => showForm('login')}>
                    ← Back to Login
                  </button>
                </div>
              </div>
            )}

            {/* ── COUNTRY MODAL (shared) ── */}
            {isModalOpen && (
              <div className={styles.modalOverlay} onClick={() => setIsModalOpen(false)}>
                <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                  <div className={styles.modalHeader}><h3>Select Country</h3></div>
                  <div className={styles.searchContainer}>
                    <i className="fa fa-search"></i>
                    <input type="text" placeholder="Search" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                  </div>
                  <div 
                    className={`${styles.countryList} ${isScrolling ? styles.isScrolling : ''}`}
                    onScroll={handleScroll}
                  >
                    {filteredCountries.map((c) => (
                      <div key={c.name} className={styles.countryItem} onClick={() => { setSelectedCountry(c); setIsModalOpen(false); setSearchTerm('') }}>
                        <span>{c.name} ({c.code})</span>
                        <div className={`${styles.radioOuter} ${selectedCountry?.name === c.name ? styles.checked : ''}`}>
                          <div className={styles.radioInner}></div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
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
                   onClick={() => showForm('register')}>
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