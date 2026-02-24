'use client';

import React, { useState, useEffect, useRef, Suspense } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import "./Verify.css";
import { authService, setAuthToken, updateNotificationContainer } from '@/app/api';
import { Toast } from '@/app/types/auth';

const VerifyAccountContent = () => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [toasts, setToasts] = useState<Toast[]>([]);
    const [code, setCode] = useState(['', '', '', '']);
    const [isResending, setIsResending] = useState(false);
    const [countdown, setCountdown] = useState(0);
    const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
    const router = useRouter();
    const searchParams = useSearchParams();
    const token = searchParams.get('token');

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

    const handleChange = (index: number, value: string) => {
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
            // if (verificationCode.length === 4) {
            // handleSubmitWithCode(verificationCode);
            // }
        }
    };

    const handleSubmitWithCode = async (verificationCode: string) => {
        if (!token) {
            showToast('Invalid verification session. Please login again.');
            router.push('/auth/login');
            return;
        }

        setIsSubmitting(true);

        try {
            const payload = { otp: verificationCode };
            await authService.verifyOtp(payload)
            .then((response) => {
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
                updateNotificationContainer({
                    type: "welcome",
                    description: "Welcome to our platform!",
                    date: new Date().toISOString()
                });
            
                setAuthToken(payload);
                showToast('Verification successful!', 'success');
                setTimeout(() => {
                    router.push('/dashboard');
                }, 1000);
            }) .catch((error) => {
                showToast(error)
            })
            
            showToast('Verification successful!', 'success');
            setTimeout(() => {
            router.push('/dashboard');
            }, 1000);
        } catch (error: any) {
            const errorMsg = error.toString();
            if (errorMsg.includes('Invalid verification code') || errorMsg.includes('invalid') || errorMsg.includes('Invalid')) {
            showToast('Invalid verification code. Please try again.');
            setCode(['', '', '', '']);
            inputRefs.current[0]?.focus();
            } else if (errorMsg.includes('expired') || errorMsg.includes('Expired')) {
            showToast('Verification code has expired. Please request a new one.');
            } else if (errorMsg.includes('internet connection')) {
            showToast('You are offline. Please check your network.');
            } else if (errorMsg.includes('maintenance') || errorMsg.includes('down')) {
            showToast('Service unavailable. The server might be down.');
            } else {
            showToast(errorMsg || 'Verification failed. Please try again.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
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

    const handlePaste = (e: React.ClipboardEvent) => {
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

    const handleSubmit = async () => {
        const verificationCode = code.join('');
        if (verificationCode.length !== 4) {
            showToast('Please enter the 4-digit verification code');
            return;
        }
        await handleSubmitWithCode(verificationCode);
    };

    const handleResendCode = async () => {
        if (countdown > 0 || !token) return;

        setIsResending(true);
        try {
        await authService.resendOtp(token);
        showToast('New verification code sent!', 'success');
        setCountdown(30); // 30 seconds cooldown
        } catch (error: any) {
        const errorMsg = error.toString();
        if (errorMsg.includes('internet connection')) {
            showToast('You are offline. Please check your network.');
        } else if (errorMsg.includes('maintenance') || errorMsg.includes('down')) {
            showToast('Service unavailable. The server might be down.');
        } else {
            showToast(errorMsg || 'Failed to resend code. Please try again.');
        }
        } finally {
        setIsResending(false);
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
          <h2>Two-Factor Authentication</h2>
          <p className="signup-link-text">
            Enter the 4-digit code from your authenticator app
          </p>
        </div>

        <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }}>
          <div className="verification-container">
            <div className="code-inputs">
              {code.map((digit, index) => (
                <input
                  key={index}
                  ref={(el) => { inputRefs.current[index] = el; }}
                  type="text"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  maxLength={1}
                  value={digit}
                  onChange={(e) => handleChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  onPaste={index === 0 ? handlePaste : undefined}
                  className="code-input"
                  autoComplete="one-time-code"
                  disabled={isSubmitting}
                />
              ))}
            </div>

            <div className="verification-actions">
              <button
                type="button"
                className="btn-resend"
                onClick={handleResendCode}
                disabled={isResending || countdown > 0}
              >
                {isResending ? (
                  <>
                    <div className="spinner spinner-small"></div>
                    <span>Sending...</span>
                  </>
                ) : countdown > 0 ? (
                  `Resend in ${countdown}s`
                ) : (
                  'Resend Code'
                )}
              </button>

              <button
                type="submit"
                className="btn-login"
                disabled={isSubmitting || code.join('').length !== 4}
              >
                {isSubmitting ? (
                  <>
                    <div className="spinner"></div>
                    <span>Verifying...</span>
                  </>
                ) : (
                  'Verify'
                )}
              </button>
            </div>
          </div>
        </form>

        <div className="back-to-login">
          <button
            type="button"
            className="forgot-password-link"
            onClick={() => router.push('/auth/login')}
          >
            ← Back to Login
          </button>
        </div>
      </div>
    </div>
  );
};

// Wrapper component with Suspense boundary
const VerifyAccount = () => {
  return (
    <Suspense fallback={
      <div className="auth-page-wrapper">
        <div className="login-card">
          <div className="form-header-text">
            <h2>Loading...</h2>
          </div>
        </div>
      </div>
    }>
      <VerifyAccountContent />
    </Suspense>
  );
};

export default VerifyAccount;