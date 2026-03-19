import { TrendingUp, Clock } from "lucide-react";
import styles from "./UserActivity.module.css";

const stats = [
  { num: "128",   label: "New Users",    badge: "+12.2%", badgeType: "green", icon: <TrendingUp size={11} /> },
  { num: "12",    label: "KYC Pending",  badge: "Pending", badgeType: "yellow", icon: <Clock size={11} />    },
  { num: "5,987", label: "Active Users", badge: "+4%",    badgeType: "green", icon: <TrendingUp size={11} /> },
];

export default function UserActivity() {
  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">User Activity</span>
      </div>
      <div className="card-body">
        {stats.map((s, i) => (
          <div key={s.label} className={`${styles.row} ${i < stats.length - 1 ? styles.bordered : ""}`}>
            <div>
              <div className={`${styles.num} ${s.badgeType === "yellow" ? styles.numYellow : ""}`}>{s.num}</div>
              <div className={styles.label}>{s.label}</div>
            </div>
            <span className={`${styles.badge} ${s.badgeType === "green" ? styles.badgeGreen : styles.badgeYellow}`}>
              {s.icon}{s.badge}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}