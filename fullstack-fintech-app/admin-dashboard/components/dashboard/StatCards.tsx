import { Users, Banknote, ArrowLeftRight, CreditCard } from "lucide-react";
import styles from "./StatCards.module.css";
import { STAT_CARDS } from "@/app/lib/data";
import { StatCard } from "@/app/types";

const ICONS = [Users, Banknote, ArrowLeftRight, CreditCard];

const variantClass: Record<StatCard["variant"], string> = {
  green:  styles.green,
  blue:   styles.blue,
  orange: styles.orange,
  yellow: styles.yellow,
};

export default function StatCards() {
  return (
    <div className={styles.grid}>
      {STAT_CARDS.map((card, i) => {
        const Icon = ICONS[i];
        return (
          <div key={card.label} className={`${styles.card} ${variantClass[card.variant]}`}>
            <div className={styles.decorCircle} />
            <div className={styles.iconWrap}><Icon size={20} strokeWidth={2} /></div>
            <div className={styles.info}>
              <div className={styles.label}>{card.label}</div>
              <div className={styles.value}>{card.value}</div>
              {card.sub && <div className={styles.sub}>{card.sub}</div>}
            </div>
          </div>
        );
      })}
    </div>
  );
}