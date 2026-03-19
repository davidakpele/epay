"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Mail, Lock, Eye, EyeOff, Shield } from "lucide-react";
import styles from "./page.module.css";

function WaveBackground() {
  return (
    <div className={styles.bg} aria-hidden>
      <svg
        className={styles.waveSvg}
        viewBox="0 0 1440 900"
        preserveAspectRatio="xMidYMid slice"
        xmlns="http://www.w3.org/2000/svg"
      >
        <defs>
          <linearGradient id="bgGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%"   stopColor="#143d08" />
            <stop offset="100%" stopColor="#2d7a14" />
          </linearGradient>
          <linearGradient id="brightGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%"   stopColor="#4db825" stopOpacity="0.55" />
            <stop offset="100%" stopColor="#7ed44a" stopOpacity="0.2"  />
          </linearGradient>
          <linearGradient id="midGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%"   stopColor="#1f5c0a" stopOpacity="0.9" />
            <stop offset="100%" stopColor="#3a9e1a" stopOpacity="0.6" />
          </linearGradient>
        </defs>
        <rect width="1440" height="900" fill="url(#bgGrad)" />
        <path d="M0,0 L1440,0 L1440,520 Q1200,300 900,380 Q600,460 300,340 Q150,280 0,320 Z" fill="#0e3406" opacity="0.75" />
        <path d="M0,380 Q200,280 400,350 Q650,430 900,310 Q1100,220 1440,300 L1440,900 L0,900 Z" fill="#1a5209" opacity="0.9" />
        <path d="M0,430 Q180,340 380,400 Q600,470 850,360 Q1080,270 1440,360 L1440,900 L0,900 Z" fill="#1e600c" opacity="0.85" />
        <path d="M0,490 Q160,400 340,455 Q540,520 780,420 Q1020,330 1440,430 L1440,900 L0,900 Z" fill="url(#brightGrad)" opacity="0.7" />
        <path d="M0,540 Q200,460 400,510 Q620,570 860,475 Q1080,395 1440,490 L1440,900 L0,900 Z" fill="url(#midGrad)" opacity="0.6" />
        <path d="M0,600 Q220,530 440,575 Q660,625 880,540 Q1100,460 1440,545 L1440,900 L0,900 Z" fill="#3d9918" opacity="0.45" />
        <path d="M0,670 Q260,610 500,650 Q720,695 950,615 Q1150,545 1440,620 L1440,900 L0,900 Z" fill="#4db825" opacity="0.28" />
        <rect y="860" width="1440" height="40" fill="#2d7a14" opacity="0.5" />
      </svg>
    </div>
  );
}

export default function LoginPage() {
  const router = useRouter();
  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [showPass, setShowPass] = useState(false);
  const [remember, setRemember] = useState(false);
  const [loading,  setLoading]  = useState(false);
  const [errors,   setErrors]   = useState<{ email?: string; password?: string }>({});

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const newErrors: { email?: string; password?: string } = {};
    if (!email)    newErrors.email    = "Please enter your email address.";
    if (!password) newErrors.password = "Please enter your password.";
    if (newErrors.email || newErrors.password) { setErrors(newErrors); return; }

    setErrors({});
    setLoading(true);
    await new Promise(r => setTimeout(r, 1400));

    if (email && password) {
      router.push("/");
    } else {
      setErrors({
        email:    "Invalid email or password.",
        password: "Invalid email or password.",
      });
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>

      <WaveBackground />

      {/* Logo */}
      <div className={styles.logoWrap}>
        <div className={styles.logoIcon}>
          <Shield size={22} color="#166701" strokeWidth={2.5} />
        </div>
        <span className={styles.logoText}>e<span>Pay</span></span>
      </div>

      {/* Card */}
      <div className={styles.card}>
        <h1 className={styles.title}>Admin Login</h1>
        <p className={styles.subtitle}>Sign in to your admin account</p>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>

          {/* Email */}
          <div>
            <div className={styles.fieldWrap}>
              <span className={styles.fieldIcon}><Mail size={17} /></span>
              <input
                className={styles.input}
                style={errors.email ? { borderColor: "#c0392b", boxShadow: "0 0 0 3px rgba(192,57,43,0.1)" } : undefined}
                type="email"
                placeholder="Email Address"
                value={email}
                onChange={e => { setEmail(e.target.value); if (errors.email) setErrors(prev => ({ ...prev, email: undefined })); }}
                autoComplete="email"
              />
            </div>
            {errors.email && (
              <p style={{ color: "#c0392b", fontSize: "0.76rem", fontWeight: 500, marginTop: 5, display: "flex", alignItems: "center", gap: 4 }}>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {errors.email}
              </p>
            )}
          </div>

          {/* Password */}
          <div>
            <div className={styles.fieldWrap}>
              <span className={styles.fieldIcon}><Lock size={17} /></span>
              <input
                className={styles.input}
                style={errors.password ? { borderColor: "#c0392b", boxShadow: "0 0 0 3px rgba(192,57,43,0.1)" } : undefined}
                type={showPass ? "text" : "password"}
                placeholder="Password"
                value={password}
                onChange={e => { setPassword(e.target.value); if (errors.password) setErrors(prev => ({ ...prev, password: undefined })); }}
                autoComplete="current-password"
              />
              <button
                type="button"
                className={styles.eyeBtn}
                onClick={() => setShowPass(p => !p)}
                tabIndex={-1}
              >
                {showPass ? <EyeOff size={17} /> : <Eye size={17} />}
              </button>
            </div>
            {errors.password && (
              <p style={{ color: "#c0392b", fontSize: "0.76rem", fontWeight: 500, marginTop: 5, display: "flex", alignItems: "center", gap: 4 }}>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ flexShrink: 0 }}><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {errors.password}
              </p>
            )}
          </div>

          <div className={styles.row}>
            <label className={styles.rememberLabel}>
              <input
                type="checkbox"
                checked={remember}
                onChange={e => setRemember(e.target.checked)}
                className={styles.checkbox}
              />
              Remember me
            </label>
            <Link href="/forgot-password" className={styles.forgotLink}>
              Forgot password?
            </Link>
          </div>

          <button
            type="submit"
            className={`${styles.submitBtn} ${loading ? styles.submitBtnLoading : ""}`}
            disabled={loading}
          >
            {loading ? <span className={styles.spinner} /> : "Sign In"}
          </button>

        </form>

        <p className={styles.support}>
          Trouble signing in?{" "}
          <a href="mailto:support@epay.com" className={styles.supportLink}>
            Contact support
          </a>
        </p>

        <div className={styles.cardDivider} />
        <p className={styles.cardFooter}>© 2024 ePay. All rights reserved</p>
      </div>

      <p className={styles.pageFooter}>© 2024 ePay. All rights reserved.</p>
    </div>
  );
}