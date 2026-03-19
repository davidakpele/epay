import styles from "./VirtualCardsTab.module.css";
import { CreditCard, Lock, CheckCircle } from "lucide-react";

const CARDS = [
  { id: "VC001", last4: "4521", network: "Visa",       status: "Active",  limit: "₦100,000", spent: "₦42,500"  },
  { id: "VCO02", last4: "8834", network: "Mastercard", status: "Frozen",  limit: "₦50,000",  spent: "₦50,000"  },
  { id: "VC003", last4: "2291", network: "Visa",       status: "Active",  limit: "₦200,000", spent: "₦15,000"  },
];

const statusStyle: Record<string, string> = {
  Active: styles.statusActive,
  Frozen: styles.statusFrozen,
};

export default function VirtualCardsTab() {
  return (
    <div className={styles.wrap}>
      <div className={styles.header}>
        <h3 className={styles.title}>Virtual Cards</h3>
        <button className={styles.createBtn}><CreditCard size={14} /> Create New Card</button>
      </div>

      <div className={styles.cardList}>
        {CARDS.map(c => (
          <div key={c.id} className={styles.cardItem}>
            <div className={styles.cardVisual}>
              <CreditCard size={20} color="rgba(255,255,255,0.8)" />
              <span className={styles.cardNetwork}>{c.network}</span>
            </div>
            <div className={styles.cardDetails}>
              <div className={styles.cardNumber}>•••• •••• •••• {c.last4}</div>
              <div className={styles.cardMeta}>
                <span>Limit: <strong>{c.limit}</strong></span>
                <span>Spent: <strong>{c.spent}</strong></span>
              </div>
            </div>
            <div className={styles.cardActions}>
              <span className={`${styles.cardStatus} ${statusStyle[c.status]}`}>
                {c.status === "Active" ? <CheckCircle size={12} /> : <Lock size={12} />}
                {c.status}
              </span>
              <button className={styles.cardActionBtn}>
                {c.status === "Active" ? "Freeze" : "Unfreeze"}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}