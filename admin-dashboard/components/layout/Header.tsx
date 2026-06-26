"use client";

import { useState, useRef, useEffect } from "react";
import { Bell, Menu, Shield, ChevronDown, User, LogOut, Settings, Check } from "lucide-react";
import styles from "./Header.module.css";
import { NAV_ITEMS, NOTIFICATIONS } from "@/app/lib/data";
import Link from "next/link";

interface HeaderProps {
  onMenuToggle: () => void;
}

export default function Header({ onMenuToggle }: HeaderProps) {
  const [openNav, setOpenNav]   = useState<number | null>(null);
  const [showNotif, setShowNotif] = useState(false);
  const [showProfile, setShowProfile] = useState(false);
  const [readAll, setReadAll] = useState(false);

  const navRef     = useRef<HTMLElement>(null);
  const notifRef   = useRef<HTMLDivElement>(null);
  const profileRef = useRef<HTMLDivElement>(null);

  // Close everything on outside click
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (navRef.current     && !navRef.current.contains(e.target as Node))     setOpenNav(null);
      if (notifRef.current   && !notifRef.current.contains(e.target as Node))   setShowNotif(false);
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) setShowProfile(false);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const toggleNav     = (i: number) => setOpenNav(openNav === i ? null : i);
  const toggleNotif   = () => { setShowNotif(p => !p); setShowProfile(false); };
  const toggleProfile = () => { setShowProfile(p => !p); setShowNotif(false); };

  return (
    <header className={styles.header}>

      {/* Logo */}
      <div className={styles.logo}>
        <div className={styles.logoIcon}>
          <Shield size={18} color="#166701" strokeWidth={2.5} />
        </div>
        <span className={styles.logoText}>e<span>Pay</span></span>
      </div>

      {/* Nav */}
      <nav className={styles.nav} ref={navRef}>
        {NAV_ITEMS.map((item, i) => (
          <div key={item.label} className={styles.navItem}>
            {item.children ? (
              <>
                <button
                  className={`${styles.navBtn} ${openNav === i ? styles.navBtnOpen : ""}`}
                  onClick={() => toggleNav(i)}
                >
                  {item.label}
                  <ChevronDown size={12} className={`${styles.chevron} ${openNav === i ? styles.chevronOpen : ""}`} />
                </button>
                {openNav === i && (
                  <div className={styles.dropdown}>
                    {item.children.map((child) => (
                      <a key={child.label} href={child.href} className={styles.dropdownItem}>
                        {child.label}
                      </a>
                    ))}
                  </div>
                )}
              </>
            ) : (
              <a href={item.href} className={`${styles.navLink} ${item.active ? styles.navLinkActive : ""}`}>
                {item.label}
              </a>
            )}
          </div>
        ))}
      </nav>

      {/* Right */}
      <div className={styles.right}>

        {/* ── Notification bell ── */}
        <div className={styles.popoverWrap} ref={notifRef}>
          <button className={`${styles.notifBtn} ${showNotif ? styles.notifBtnActive : ""}`} onClick={toggleNotif}>
            <Bell size={16} />
            {!readAll && <span className={styles.notifBadge}>5</span>}
          </button>

          {showNotif && (
            <div className={`${styles.popover} ${styles.popoverNotif}`}>
              <div className={styles.popoverHeader}>
                <span className={styles.popoverTitle}>Notifications</span>
                <button className={styles.markAllBtn} onClick={() => setReadAll(true)}>
                  <Check size={12} /> Mark all read
                </button>
              </div>

              <div className={styles.notifList}>
                {NOTIFICATIONS.map((n, i) => (
                  <div key={i} className={`${styles.notifItem} ${!readAll && i < 3 ? styles.notifUnread : ""}`}>
                    {n.type === "avatar" ? (
                      <div className={styles.notifAvatar}>{n.initials}</div>
                    ) : (
                      <div className={styles.notifIconWrap}><Bell size={13} /></div>
                    )}
                    <div className={styles.notifContent}>
                      <p className={styles.notifText}>{n.text}</p>
                      <span className={styles.notifTime}>{n.time}</span>
                    </div>
                    {!readAll && i < 3 && <span className={styles.unreadDot} />}
                  </div>
                ))}
              </div>

              <div className={styles.popoverFooter}>
                <a href="#" className={styles.viewAllLink}>View all notifications →</a>
              </div>
            </div>
          )}
        </div>

        {/* ── Admin profile ── */}
        <div className={styles.popoverWrap} ref={profileRef}>
          <button className={`${styles.adminProfile} ${showProfile ? styles.adminProfileActive : ""}`} onClick={toggleProfile}>
            <div className={styles.adminAvatar}>A</div>
            <span className={styles.adminName}>Admin</span>
            <ChevronDown size={12} className={`${styles.chevron} ${showProfile ? styles.chevronOpen : ""}`} style={{ color: "rgba(255,255,255,0.7)" }} />
          </button>

          {showProfile && (
            <div className={`${styles.popover} ${styles.popoverProfile}`}>
              {/* User info header */}
              <div className={styles.profileHeader}>
                <div className={styles.profileAvatarLg}>A</div>
                <div>
                  <div className={styles.profileName}>Admin User</div>
                  <div className={styles.profileEmail}>admin@epay.com</div>
                </div>
              </div>

              <div className={styles.profileDivider} />

              <a href="#" className={styles.profileItem}>
                <User size={15} className={styles.profileItemIcon} />
                My Profile
              </a>
              <a href="#" className={styles.profileItem}>
                <Settings size={15} className={styles.profileItemIcon} />
                Settings
              </a>

              <div className={styles.profileDivider} />

              <Link href="/logout" className={`${styles.profileItem} ${styles.profileItemLogout}`}>
                <LogOut size={15} className={styles.profileItemIcon} />
                Logout
              </Link>
            </div>
          )}
        </div>

        <button className={styles.hamburger} onClick={onMenuToggle}>
          <Menu size={22} />
        </button>
      </div>
    </header>
  );
}