'use client';

import React, { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import "./Login.css";
import { authService, setAuthToken, updateNotificationContainer } from '@/app/api';
import { Toast } from '@/app/types/auth';
import { LoginFormErrors } from '@/app/types/errors';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const [errors, setErrors] = useState<LoginFormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [toasts, setToasts] = useState<Toast[]>([]);

  const usernameRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);
  const router = useRouter();
  useEffect(() => {
    document.title = "Sign-In Account"
    usernameRef.current?.focus();
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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name as keyof LoginFormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }));
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
      }else if (response.is_profile_complete === false) {
        setAuthToken(payload);
        router.push("/user");
        return;
      }else{
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

      <div className="login-card">
        <div className="form-header-text">
          <h2>Login to your account</h2>
          <p className="signup-link-text">
            Don't have account? <Link href="/auth/register">Register here</Link>
          </p>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label>Username</label>
            <input
              ref={usernameRef}
              autoComplete='off'
              type="text"
              name="username"
              className={`form-control ${errors.username ? 'is-invalid' : ''}`}
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username or email"
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <div className="input-container">
              <input
                ref={passwordRef}
                autoComplete='off'
                type={showPassword ? 'text' : 'password'}
                name="password"
                className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                value={formData.password}
                onChange={handleChange}
                placeholder="•••••"
              />
              <button
                type="button"
                className="password-toggle"
                onClick={() => setShowPassword(!showPassword)}
              >
                <i className={`fa-solid ${showPassword ? 'fa-eye-slash' : 'fa-eye'}`}></i>
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn-login"
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
        </form>

        <div className="forgot-password-container">
          <Link href="/auth/forgot-password" className="forgot-password-link">
            Forgot Password
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;