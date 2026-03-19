import { Bell } from "lucide-react";
import styles from "./Notifications.module.css";
import { NOTIFICATIONS } from "@/app/lib/data";

export default function Notifications() {
  return (
    <div className="card">
      <div className="card-hd">
        <span className="card-title">Latest Notifications</span>
      </div>
      <div className="card-body">
        {NOTIFICATIONS.map((n, i) => (
          <div key={i} className={`${styles.item} ${i < NOTIFICATIONS.length - 1 ? styles.bordered : ""}`}>
            {n.type === "avatar" ? (
              <div className={styles.avatar}>{n.initials}</div>
            ) : (
              <div className={styles.iconWrap}>
                <Bell size={14} />
              </div>
            )}
            <div>
              <div className={styles.text}>{n.text}</div>
              <div className={styles.time}>{n.time}</div>
            </div>
          </div>
        ))}
        <a href="#" className={styles.viewMore}>View More →</a>
      </div>
    </div>
  );
}