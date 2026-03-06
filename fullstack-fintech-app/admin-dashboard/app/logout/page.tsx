"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Shield, LogOut } from "lucide-react";
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
      </svg>
    </div>
  );
}

const DURATION = 2000; // 2 seconds

export default function LogoutPage() {
  const router  = useRouter();
  const [progress, setProgress] = useState(0);
  const [done,     setDone]     = useState(false);

  useEffect(() => {
    const start   = performance.now();
    let raf: number;

    const tick = (now: number) => {
      const elapsed = now - start;
      const pct     = Math.min((elapsed / DURATION) * 100, 100);
      setProgress(pct);

      if (pct < 100) {
        raf = requestAnimationFrame(tick);
      } else {
        setDone(true);
        // small pause so user sees 100% before redirect
        setTimeout(() => router.replace("/login"), 300);
      }
    };

    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [router]);

  return (
    <div className={styles.page}>
      <WaveBackground />

      <div className={styles.card}>
        {/* Text */}
        <h1 className={styles.title}>
          {done ? "Goodbye!" : "Signing you out…"}
        </h1>
        <p className={styles.subtitle}>
          {done
            ? "You have been securely signed out."
            : "Please wait while we securely log you out of your account."
          }
        </p>

        {/* Progress bar */}
        <div className={styles.barTrack}>
          <div
            className={styles.barFill}
            style={{ width: `${progress}%` }}
          />
        </div>

        {/* Dots loader (hidden when done) */}
        {!done && (
          <div className={styles.dots}>
            <span className={styles.dot} />
            <span className={styles.dot} />
            <span className={styles.dot} />
          </div>
        )}

        {/* Done checkmark */}
        {done && (
          <div className={styles.checkWrap}>
            <svg className={styles.checkSvg} viewBox="0 0 52 52">
              <circle className={styles.checkCircle} cx="26" cy="26" r="24" fill="none" />
              <path   className={styles.checkMark}   fill="none" d="M14 27 l8 8 l16-16" />
            </svg>
          </div>
        )}

        <p className={styles.redirect}>
          Redirecting to login{!done && <span className={styles.ellipsis} />}
        </p>
      </div>

      

      <p className={styles.pageFooter}>© 2024 ePay. All rights reserved.</p>
    </div>
  );
}