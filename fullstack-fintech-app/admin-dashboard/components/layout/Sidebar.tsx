"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard, Users, ScanFace, CreditCard,
  Layers, FileText, ArrowLeftRight, Settings,
  ChevronRight, LogOut,
  BellIcon,
} from "lucide-react";

import styles from "./Sidebar.module.css";
import { SIDEBAR_ITEMS } from "@/app/lib/data";

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const ICONS = [LayoutDashboard, Users, ScanFace, CreditCard, Layers, FileText, BellIcon, Settings];

export default function Sidebar({ isOpen, onClose }: SidebarProps) {
  const pathname = usePathname();
  const [openIndex, setOpenIndex] = useState<number | null>(null);

  const toggle = (i: number) => setOpenIndex(openIndex === i ? null : i);

  return (
    <>
      {isOpen && <div className={styles.overlay} onClick={onClose} />}

      <aside className={`${styles.sidebar} ${isOpen ? styles.open : ""}`}>
        <nav className={styles.nav}>
          {SIDEBAR_ITEMS.map((item, i) => {
            const Icon = ICONS[i] ?? Settings;
            const isExpanded = openIndex === i;
            const isActive = item.href ? pathname === item.href : false;

            if (item.children) {
              // auto-expand if a child route is active
              const childActive = item.children.some(c => pathname === c.href);

              return (
                <div key={item.label} className={styles.group}>
                  <button
                    className={`${styles.navItem} ${(isExpanded || childActive) ? styles.navItemOpen : ""}`}
                    onClick={() => toggle(i)}
                  >
                    <span className={styles.navIcon}><Icon size={16} /></span>
                    <span className={styles.navLabel}>{item.label}</span>
                    <ChevronRight
                      size={13}
                      className={`${styles.navArrow} ${(isExpanded || childActive) ? styles.navArrowOpen : ""}`}
                    />
                  </button>

                  <div className={`${styles.submenu} ${(isExpanded || childActive) ? styles.submenuOpen : ""}`}>
                    {item.children.map((child) => (
                      <Link
                        key={child.label}
                        href={child.href}
                        className={`${styles.subItem} ${pathname === child.href ? styles.subItemActive : ""}`}
                        onClick={onClose}
                      >
                        <span className={styles.subDot} />
                        {child.label}
                      </Link>
                    ))}
                  </div>
                </div>
              );
            }

            return (
              <Link
                key={item.label}
                href={item.href ?? "/"}
                className={`${styles.navItem} ${isActive ? styles.navItemActive : ""}`}
                onClick={onClose}
              >
                <span className={styles.navIcon}><Icon size={16} /></span>
                <span className={styles.navLabel}>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className={styles.footer}>
          <Link href="/logout" className={styles.navItem}>
            <span className={styles.navIcon}><LogOut size={16} /></span>
            <span className={styles.navLabel}>Logout</span>
          </Link>
        </div>
      </aside>
    </>
  );
}