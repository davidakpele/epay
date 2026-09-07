"use client";

import React, { useState, useRef, useEffect, useCallback } from "react";
import "./SupportChatBot.css";
import {
  getUserFullName,
  getUserId,
  getWalletList,
  getWallet,
  depositService,
  supportService,
  walletService,
  setWalletContainer,
} from "@/app/api";
import { CheckCircle } from "lucide-react";

/* ─── Types ──────────────────────────────────────────────────────────────── */

interface Message {
  id: number;
  role: "user" | "bot";
  text: string;
  time: string;
  wallets?: WalletCard[];
  options?: OptionButton[];
}

interface WalletCard {
  currency_code: string;
  balance: number;
  symbol: string;
}

interface OptionButton {
  label: string; // display text
  value: string; // machine value
}

interface SupportChatBotProps {
  isOpen: boolean;
  onClose: () => void;
}

type Tab = "home" | "conversation" | "faqs" | "articles";
type SubView = null | "chat" | "call" | "ticket";
type FaqView = "categories" | "questions" | "answer";

type DepositStep = null | "ask_wallet" | "ask_amount" | "confirm";

interface DepositWizard {
  step: DepositStep;
  currency: string;
  symbol: string;
  amount: number;
}

interface FaqItem {
  id: number;
  category: string;
  question: string;
  answer: string;
  authorName: string;
}
interface FaqCategory {
  category: string;
  items: FaqItem[];
}
interface Article {
  id: number;
  slug: string;
  title: string;
  summary: string;
  content: string;
  category: string;
}

const TICKET_CATEGORIES = [
  { value: "ACCOUNT_ACCESS", label: "Account Access" },
  { value: "TRANSACTION_DISPUTE", label: "Transaction Dispute" },
  { value: "WALLET_ISSUE", label: "Wallet Issue" },
  { value: "KYC_VERIFICATION", label: "KYC Verification" },
  { value: "WITHDRAWAL_PROBLEM", label: "Withdrawal Problem" },
  { value: "DEPOSIT_PROBLEM", label: "Deposit Problem" },
  { value: "FRAUD_REPORT", label: "Fraud Report" },
  { value: "TECHNICAL_ISSUE", label: "Technical Issue" },
  { value: "BILLING", label: "Billing" },
  { value: "GENERAL_INQUIRY", label: "General Inquiry" },
  { value: "OTHER", label: "Other" },
];

/* ─── Helpers ────────────────────────────────────────────────────────────── */

const getTime = () =>
  new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });

const uuidv4 = () =>
  "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === "x" ? r : (r & 0x3) | 0x8).toString(16);
  });

const fmtMoney = (n: number, sym = "") =>
  `${sym}${n.toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

const truncateChars = (text: string, limit: number) => {
  if (text.length <= limit) return { truncated: text, isTruncated: false };
  return {
    truncated: text.slice(0, limit).trimEnd() + "...",
    isTruncated: true,
  };
};

const BOT_DELAY_MS = 1300;

const ChevronRight = () => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#aaa"
    strokeWidth="2"
    strokeLinecap="round"
  >
    <path d="M9 18l6-6-6-6" />
  </svg>
);
const SearchIcon = () => (
  <svg
    width="15"
    height="15"
    viewBox="0 0 24 24"
    fill="none"
    stroke="#9ca3af"
    strokeWidth="2"
    strokeLinecap="round"
  >
    <circle cx="11" cy="11" r="8" />
    <line x1="21" y1="21" x2="16.65" y2="16.65" />
  </svg>
);

const isBalanceIntent = (t: string) =>
  /\b(balance|wallet|how much|my account|available|funds)\b/i.test(t);
const isFundIntent = (t: string) =>
  /\b(fund|deposit|top.?up|recharge|add money|load wallet)\b/i.test(t);

/* ─── Component ──────────────────────────────────────────────────────────── */

const EMPTY_WIZARD: DepositWizard = {
  step: null,
  currency: "",
  symbol: "",
  amount: 0,
};

const SupportChatBot: React.FC<SupportChatBotProps> = ({ isOpen, onClose }) => {
  /* ── UI state ── */
  const [tab, setTab] = useState<Tab>("home");
  const [subView, setSubView] = useState<SubView>(null);
  const [faqView, setFaqView] = useState<FaqView>("categories");
  const [faqLoading, setFaqLoading] = useState(false);
  const [selectedFaqCat, setSelectedFaqCat] = useState<FaqCategory | null>(
    null,
  );
  const [selectedFaqItem, setSelectedFaqItem] = useState<FaqItem | null>(null);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [faqSearch, setFaqSearch] = useState("");
  const [articleSearch, setArticleSearch] = useState("");
  const [callForm, setCallForm] = useState({ name: "", email: "" });
  const [callSubmitted, setCallSubmitted] = useState(false);
  const [userName, setUserName] = useState("there");

  /* ── Remote data ── */
  const [faqCategories, setFaqCategories] = useState<FaqCategory[]>([]);
  const [articles, setArticles] = useState<Article[]>([]);
  const [dataLoading, setDataLoading] = useState(false);

  /* ── Ticket form ── */
  const [ticketForm, setTicketForm] = useState({
    subject: "",
    description: "",
    category: "GENERAL_INQUIRY",
  });
  const [ticketLoading, setTicketLoading] = useState(false);
  const [ticketSubmitted, setTicketSubmitted] = useState(false);
  const [ticketRef, setTicketRef] = useState("");
  const [ticketError, setTicketError] = useState("");

  /* ── Chat ── */
  const [messages, setMessages] = useState<Message[]>([
    {
      id: 1,
      role: "bot",
      text: "Hi there! 👋 I'm ePay's support assistant. How can I help you today?",
      time: getTime(),
    },
  ]);
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const sessionIdRef = useRef<string>(uuidv4());
  const busyRef = useRef(false); // prevents concurrent wizard steps

  /* ── Deposit wizard — stored in a ref so closures always see latest value ── */
  const wizardRef = useRef<DepositWizard>({ ...EMPTY_WIZARD });
  // Mirror in state purely for render (input placeholder)
  const [wizardStep, setWizardStep] = useState<DepositStep>(null);

  const setWizard = (patch: Partial<DepositWizard>) => {
    wizardRef.current = { ...wizardRef.current, ...patch };
    setWizardStep(wizardRef.current.step);
  };
  const resetWizard = () => {
    wizardRef.current = { ...EMPTY_WIZARD };
    setWizardStep(null);
  };

  /* ── Init ── */
  useEffect(() => {
    const name = getUserFullName();
    if (name) setUserName(name.split(" ")[0]);
  }, []);

  useEffect(() => {
    if (!isOpen || dataLoading || faqCategories.length > 0) return;
    setDataLoading(true);
    Promise.allSettled([supportService.getFaqs(), supportService.getArticles()])
      .then(([faqR, artR]) => {
        if (faqR.status === "fulfilled") {
          const r = faqR.value as any;
          if (r?.success && Array.isArray(r.data)) setFaqCategories(r.data);
        }
        if (artR.status === "fulfilled") {
          const r = artR.value as any;
          if (r?.success && Array.isArray(r.data)) setArticles(r.data);
        }
      })
      .finally(() => setDataLoading(false));
  }, [isOpen]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isTyping]);

  useEffect(() => {
    if (subView === "chat" || tab === "conversation")
      setTimeout(() => inputRef.current?.focus(), 300);
  }, [subView, tab]);

  /* ── Core helpers ── */

  const pushBotMsg = (msg: Omit<Message, "id" | "role" | "time">) =>
    setMessages((prev) => [
      ...prev,
      { id: Date.now(), role: "bot", time: getTime(), ...msg },
    ]);

  const pushUserMsg = (text: string) =>
    setMessages((prev) => [
      ...prev,
      { id: Date.now(), role: "user", text, time: getTime() },
    ]);

  /** Shows typing bubble, waits BOT_DELAY_MS, hides it, then appends the bot message. */
  const botSay = useCallback(
    async (
      text: string,
      extras: { wallets?: WalletCard[]; options?: OptionButton[] } = {},
    ) => {
      setIsTyping(true);
      await new Promise((r) => setTimeout(r, BOT_DELAY_MS));
      setIsTyping(false);
      setMessages((prev) => [
        ...prev,
        { id: Date.now(), role: "bot", time: getTime(), text, ...extras },
      ]);
    },
    [],
  );

  /* ── Balance flow ── */
  const handleBalanceCheck = useCallback(async () => {
    const rawWallets = getWalletList();
    if (!rawWallets?.length) {
      await botSay(
        "I couldn't find any wallet data. Please refresh the page or visit your Dashboard.",
      );
      return;
    }
    const cards: WalletCard[] = rawWallets.map((w: any) => ({
      currency_code: w.currency_code,
      balance: parseFloat(String(w.balance ?? 0).replace(/,/g, "")) || 0,
      symbol: w.symbol || (getWallet(w.currency_code) as any)?.symbol || "",
    }));
    await botSay(`Here are your current wallet balances, ${userName}:`, {
      wallets: cards,
    });
  }, [botSay, userName]);

  /* ── Deposit wizard ── */

  const startDepositWizard = useCallback(async () => {
    const rawWallets = getWalletList();
    if (!rawWallets?.length) {
      await botSay(
        "I couldn't find your wallets right now. Please visit the Deposit section on your Dashboard.",
      );
      return;
    }

    const options: OptionButton[] = rawWallets.map((w: any) => {
      const bal = parseFloat(String(w.balance ?? 0).replace(/,/g, "")) || 0;
      const sym = w.symbol || (getWallet(w.currency_code) as any)?.symbol || "";
      return {
        label: `${w.currency_code}  ·  ${fmtMoney(bal, sym)}`,
        value: w.currency_code,
      };
    });

    setWizard({ step: "ask_wallet" });
    await botSay("Sure! Which wallet would you like to fund?", { options });
  }, [botSay]);

  /**
   * Called when the user taps a wallet option button.
   * `currencyCode` = e.g. "EUR", `displayLabel` = e.g. "EUR  ·  €62,000.00"
   */
  const stepWalletSelected = useCallback(
    async (currencyCode: string, displayLabel: string) => {
      const raw = getWalletList()?.find(
        (w: any) => w.currency_code === currencyCode,
      );
      const sym =
        raw?.symbol || (getWallet(currencyCode) as any)?.symbol || currencyCode;

      setWizard({ step: "ask_amount", currency: currencyCode, symbol: sym });
      await botSay(
        `Got it — you've selected your **${currencyCode}** wallet (${sym}).\n\nHow much would you like to deposit? Just type the amount (e.g. 5000).`,
      );
    },
    [botSay],
  );

  /**
   * Called when the user types an amount while in the ask_amount step.
   */
  const stepAmountEntered = useCallback(
    async (raw: string) => {
      const amount = parseFloat(raw.replace(/[^0-9.]/g, ""));

      if (!amount || amount < 100) {
        await botSay("Please enter a valid amount of at least 100.");
        return;
      }

      // Read wizard from ref — always current
      const { currency, symbol } = wizardRef.current;
      setWizard({ step: "confirm", amount });

      const options: OptionButton[] = [
        { label: "✅  Yes, deposit now", value: "confirm" },
        { label: "❌  Cancel", value: "cancel" },
      ];

      await botSay(
        `You'd like to deposit **${fmtMoney(amount, symbol)}** into your **${currency}** wallet.\n\nShall I go ahead?`,
        { options },
      );
    },
    [botSay],
  );

  /**
   * Called when user taps "Yes, deposit now".
   * Reads wizard values from the ref to avoid stale closure.
   */
  const stepConfirm = useCallback(async () => {
    const userId = getUserId();
    if (!userId) {
      resetWizard();
      await botSay(
        "You need to be logged in to make a deposit. Please log in and try again.",
      );
      return;
    }

    // Capture wizard values from ref BEFORE resetting
    const { currency, symbol, amount } = wizardRef.current;
    resetWizard();

    setIsTyping(true);
    await new Promise((r) => setTimeout(r, 600)); // brief delay while "processing"

    try {
      const idempotencyKey = `deposit-${userId}-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;

      const res = (await depositService.create({
        userId: Number(userId),
        amount,
        currency,
        depositSystem: "PAYSTACK",
        callbackUrl:
          typeof window !== "undefined"
            ? `${window.location.origin}/dashboard`
            : "",
        idempotencyKey,
        ipAddress: "",
        deviceId:
          typeof window !== "undefined"
            ? localStorage.getItem("deviceId") || ""
            : "",
        userAgent: typeof navigator !== "undefined" ? navigator.userAgent : "",
        geoLocation: "",
      })) as any;

      // The xhrClient resolves with the raw JSON body (no .data wrapper needed)
      const body = res?.data ?? res;

      setIsTyping(false);

      if (body?.status === "success") {
        // Refresh wallet balances in localStorage silently
        try {
          const walletRes: any = await walletService.getByUserId(userId, "");
          if (walletRes?.wallet_balances)
            setWalletContainer(
              walletRes.wallet_balances,
              walletRes.hasTransferPin,
              walletRes.walletId,
            );
        } catch (_) {}

        pushBotMsg({
          text: `✅ Deposit successful!\n\n**${fmtMoney(amount, symbol)}** has been credited to your **${currency}** wallet. Your balance has been updated.`,
        });
      } else {
        pushBotMsg({
          text: `⚠️ The deposit could not be processed: ${body?.message || "please try again"}. You can also use the Deposit button on your Dashboard.`,
        });
      }
    } catch (err: any) {
      setIsTyping(false);
      pushBotMsg({
        text: "⚠️ Something went wrong while processing your deposit. Please try via the Dashboard Deposit button, or open a support ticket.",
      });
    }
  }, []);

  const stepCancel = useCallback(async () => {
    resetWizard();
    await botSay(
      "No problem — deposit cancelled. Is there anything else I can help you with?",
    );
  }, [botSay]);

  /* ── Option button click — one active at a time ── */
  const handleOptionClick = useCallback(
    async (value: string, label: string) => {
      if (busyRef.current) return;
      busyRef.current = true;

      try {
        const currentStep = wizardRef.current.step;

        // Show the user's choice as a readable message (label, not raw value)
        pushUserMsg(label);

        if (currentStep === "ask_wallet") {
          await stepWalletSelected(value, label);
          return;
        }

        if (currentStep === "confirm") {
          if (value === "confirm") {
            await stepConfirm();
          } else {
            await stepCancel();
          }
          return;
        }
      } finally {
        busyRef.current = false;
      }
    },
    [stepWalletSelected, stepConfirm, stepCancel],
  );

  /* ── Main send ── */
  const sendMessage = useCallback(async () => {
    const text = input.trim();
    if (!text || isTyping || busyRef.current) return;

    busyRef.current = true;
    setInput("");
    pushUserMsg(text);

    try {
      // Deposit wizard intercepts amount input
      if (wizardRef.current.step === "ask_amount") {
        await stepAmountEntered(text);
        return;
      }

      // Intent shortcuts
      if (isBalanceIntent(text)) {
        await handleBalanceCheck();
        return;
      }
      if (isFundIntent(text)) {
        await startDepositWizard();
        return;
      }

      // Knowledge-base bot
      setIsTyping(true);
      try {
        const userId = getUserId();
        const [res] = await Promise.all([
          supportService.chat({
            sessionId: sessionIdRef.current,
            userId: userId ? Number(userId) : null,
            message: text,
          }) as Promise<any>,
          new Promise((r) => setTimeout(r, BOT_DELAY_MS)),
        ]);
        const reply: string =
          res?.data?.reply ||
          "I don't have a specific answer for that yet. Try the FAQs or Articles tabs, or open a support ticket.";
        setIsTyping(false);
        pushBotMsg({ text: reply });
      } catch {
        setIsTyping(false);
        pushBotMsg({
          text: "Something went wrong. Please try again or open a support ticket.",
        });
      }
    } finally {
      busyRef.current = false;
    }
  }, [
    input,
    isTyping,
    stepAmountEntered,
    handleBalanceCheck,
    startDepositWizard,
  ]);

  /* ── Quick reply — auto-sends without going through input ── */
  const handleQuickReply = useCallback(
    async (q: string) => {
      if (isTyping || busyRef.current) return;
      busyRef.current = true;
      pushUserMsg(q);
      try {
        if (isBalanceIntent(q)) {
          await handleBalanceCheck();
          return;
        }
        if (isFundIntent(q)) {
          await startDepositWizard();
          return;
        }

        setIsTyping(true);
        try {
          const userId = getUserId();
          const [res] = await Promise.all([
            supportService.chat({
              sessionId: sessionIdRef.current,
              userId: userId ? Number(userId) : null,
              message: q,
            }) as Promise<any>,
            new Promise((r) => setTimeout(r, BOT_DELAY_MS)),
          ]);
          const reply =
            res?.data?.reply || "I don't have a specific answer for that yet.";
          setIsTyping(false);
          pushBotMsg({ text: reply });
        } catch {
          setIsTyping(false);
          pushBotMsg({ text: "Something went wrong. Please try again." });
        }
      } finally {
        busyRef.current = false;
      }
    },
    [isTyping, handleBalanceCheck, startDepositWizard],
  );

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const handleReset = () => {
    sessionIdRef.current = uuidv4();
    resetWizard();
    busyRef.current = false;
    setMessages([
      {
        id: Date.now(),
        role: "bot",
        text: "Hi there! 👋 I'm ePay's support assistant. How can I help you today?",
        time: getTime(),
      },
    ]);
  };

  /* ── Ticket ── */
  const handleTicketSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const userId = getUserId();
    if (!userId) {
      setTicketError("You must be logged in to submit a ticket.");
      return;
    }
    if (!ticketForm.subject.trim() || !ticketForm.description.trim()) {
      setTicketError("Please fill in all required fields.");
      return;
    }
    setTicketError("");
    setTicketLoading(true);
    try {
      const res = (await supportService.createTicket({
        userId: Number(userId),
        subject: ticketForm.subject.trim(),
        description: ticketForm.description.trim(),
        category: ticketForm.category,
      })) as any;
      if (res?.success) {
        setTicketRef(res.data?.ticketReference || "");
        setTicketSubmitted(true);
      } else
        setTicketError(
          res?.message || "Failed to create ticket. Please try again.",
        );
    } catch {
      setTicketError("Failed to create ticket. Please try again.");
    } finally {
      setTicketLoading(false);
    }
  };

  /* ── Navigation ── */
  const goBack = () => {
    setFaqLoading(false);
    if (faqView === "answer") {
      setFaqView("questions");
      setSelectedFaqItem(null);
      return;
    }
    if (faqView === "questions") {
      setFaqView("categories");
      setSelectedFaqCat(null);
      return;
    }
    setSubView(null);
    setSelectedFaqCat(null);
    setSelectedFaqItem(null);
    setSelectedArticle(null);
    setCallSubmitted(false);
    setTicketSubmitted(false);
    setTicketForm({
      subject: "",
      description: "",
      category: "GENERAL_INQUIRY",
    });
    setTicketError("");
    setFaqView("categories");
  };

  const openFaqCategory = (cat: FaqCategory) => {
    setSelectedFaqCat(cat);
    setFaqLoading(true);
    setTimeout(() => {
      setFaqLoading(false);
      setFaqView("questions");
    }, 1000);
  };
  const openFaqAnswer = (item: FaqItem) => {
    setSelectedFaqItem(item);
    setFaqLoading(true);
    setTimeout(() => {
      setFaqLoading(false);
      setFaqView("answer");
    }, 1000);
  };

  /* ── Render helpers ── */

  const renderHeader = (title?: string, showBack = false) => (
    <div className="scb-header">
      <div className="scb-header-left">
        {showBack ? (
          <button className="scb-icon-btn" onClick={goBack}>
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M19 12H5M12 5l-7 7 7 7" />
            </svg>
          </button>
        ) : (
          <div className="scb-avatar">
            <span>e</span>
            <span className="scb-status-dot" />
          </div>
        )}
        <div className="scb-header-info">
          <span className="scb-header-name">{title || "ePay Support"}</span>
          {!showBack && (
            <span className="scb-header-status">
              Online · Typically replies instantly
            </span>
          )}
        </div>
      </div>
      <div className="scb-header-actions">
        {!showBack && (
          <button
            className="scb-icon-btn"
            title="New chat"
            onClick={handleReset}
          >
            <svg
              width="15"
              height="15"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
            </svg>
          </button>
        )}
        <button className="scb-icon-btn scb-close-btn" onClick={onClose}>
          <svg
            width="14"
            height="14"
            viewBox="0 0 18 18"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.2"
            strokeLinecap="round"
          >
            <path d="M1 1l16 16M17 1L1 17" />
          </svg>
        </button>
      </div>
    </div>
  );

  /** Renders bold (**text**) markers inside bot messages */
  const renderText = (t: string) =>
    t
      .split(/\*\*(.*?)\*\*/g)
      .map((p, i) => (i % 2 === 1 ? <strong key={i}>{p}</strong> : p));

  const renderMessage = (msg: Message) => {
    const isUser = msg.role === "user";
    return (
      <div
        key={msg.id}
        className={`scb-msg-row ${isUser ? "scb-msg-user" : "scb-msg-bot"}`}
      >
        {!isUser && <div className="scb-bot-avatar">e</div>}
        <div className="scb-bubble-wrap">
          <div className="scb-bubble" style={{ whiteSpace: "pre-line" }}>
            {renderText(msg.text)}
          </div>

          {/* Wallet balance cards */}
          {msg.wallets?.length ? (
            <div className="scb-wallet-cards">
              {msg.wallets.map((w) => (
                <div key={w.currency_code} className="scb-wallet-card">
                  <span className="scb-wallet-card-code">
                    {w.currency_code}
                  </span>
                  <span className="scb-wallet-card-bal">
                    {fmtMoney(w.balance, w.symbol)}
                  </span>
                </div>
              ))}
            </div>
          ) : null}

          {/* Option buttons */}
          {msg.options?.length ? (
            <div className="scb-option-btns">
              {msg.options.map((o) => (
                <button
                  key={o.value}
                  className="scb-option-btn"
                  onClick={() => handleOptionClick(o.value, o.label)}
                  disabled={isTyping || busyRef.current}
                >
                  {o.label}
                </button>
              ))}
            </div>
          ) : null}

          <span className="scb-msg-time">{msg.time}</span>
        </div>
      </div>
    );
  };

  const renderChatArea = () => (
    <>
      <div className="scb-messages">
        {messages.map(renderMessage)}
        {isTyping && (
          <div className="scb-msg-row scb-msg-bot">
            <div className="scb-bot-avatar">e</div>
            <div className="scb-bubble-wrap">
              <div className="scb-bubble scb-typing">
                <span />
                <span />
                <span />
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="scb-quick-replies-wrapper">
        <button
          className="scb-scroll-arrow"
          onClick={() => {
            const el = document.querySelector(
              ".scb-quick-replies",
            ) as HTMLElement;
            if (el) el.scrollLeft -= 120;
          }}
        >
          &#8249;
        </button>
        <div className="scb-quick-replies">
          {[
            "Check my balance",
            "Fund my wallet",
            "Virtual card issues",
            "Transfer failed",
            "KYC help",
            "Card declined",
          ].map((q) => (
            <button
              key={q}
              className="scb-quick-btn"
              onClick={() => handleQuickReply(q)}
            >
              {q}
            </button>
          ))}
        </div>
        <button
          className="scb-scroll-arrow"
          onClick={() => {
            const el = document.querySelector(
              ".scb-quick-replies",
            ) as HTMLElement;
            if (el) el.scrollLeft += 120;
          }}
        >
          &#8250;
        </button>
      </div>

      <div className="scb-input-row">
        <input
          ref={inputRef}
          className="scb-input"
          type={wizardStep === "ask_amount" ? "number" : "text"}
          placeholder={
            wizardStep === "ask_amount"
              ? `Enter amount in ${wizardRef.current.currency} (e.g. 5000)`
              : "Type your message..."
          }
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={isTyping || busyRef.current}
          min={wizardStep === "ask_amount" ? 100 : undefined}
          step={wizardStep === "ask_amount" ? "any" : undefined}
        />
        <button
          className={`scb-send-btn ${!input.trim() || isTyping ? "scb-send-disabled" : ""}`}
          onClick={sendMessage}
          disabled={!input.trim() || isTyping || busyRef.current}
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
        </button>
      </div>
      <div className="scb-footer">
        Powered by <strong>ePay AI</strong>
      </div>
    </>
  );

  const renderTicketView = () => (
    <>
      {renderHeader("Open a Ticket", true)}
      <div className="scb-call-view">
        {ticketSubmitted ? (
          <div className="scb-call-success">
            <div className="scb-call-success-icon">
              <CheckCircle style={{ color: "var(--bg-main)" }} />
            </div>
            <h3>Ticket Submitted!</h3>
            {ticketRef && (
              <p
                style={{
                  fontFamily: "monospace",
                  fontWeight: 700,
                  color: "var(--bg-main)",
                }}
              >
                {ticketRef}
              </p>
            )}
            <p>Our support team will get back to you within 1 business day.</p>
            <button
              className="scb-call-btn"
              onClick={() => {
                setTicketSubmitted(false);
                setTicketRef("");
                setTicketForm({
                  subject: "",
                  description: "",
                  category: "GENERAL_INQUIRY",
                });
              }}
            >
              Submit another
            </button>
          </div>
        ) : (
          <form className="scb-call-form" onSubmit={handleTicketSubmit}>
            <p className="scb-call-info">
              Describe your issue and our team will respond within 1 business
              day.
            </p>
            <div className="scb-call-group">
              <label>Category</label>
              <select
                value={ticketForm.category}
                onChange={(e) =>
                  setTicketForm((p) => ({ ...p, category: e.target.value }))
                }
                style={{
                  width: "100%",
                  padding: "8px 10px",
                  borderRadius: 8,
                  border: "1px solid #e2e8f0",
                  fontSize: 13,
                }}
              >
                {TICKET_CATEGORIES.map((c) => (
                  <option key={c.value} value={c.value}>
                    {c.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="scb-call-group">
              <label>
                Subject <span style={{ color: "#e53e3e" }}>*</span>
              </label>
              <input
                type="text"
                placeholder="Brief description"
                value={ticketForm.subject}
                onChange={(e) =>
                  setTicketForm((p) => ({ ...p, subject: e.target.value }))
                }
                maxLength={200}
                required
              />
            </div>
            <div className="scb-call-group">
              <label>
                Description <span style={{ color: "#e53e3e" }}>*</span>
              </label>
              <textarea
                placeholder="Provide as much detail as possible..."
                value={ticketForm.description}
                onChange={(e) =>
                  setTicketForm((p) => ({ ...p, description: e.target.value }))
                }
                rows={4}
                required
                style={{
                  width: "100%",
                  padding: "8px 10px",
                  borderRadius: 8,
                  border: "1px solid #e2e8f0",
                  fontSize: 13,
                  resize: "vertical",
                  fontFamily: "inherit",
                  boxSizing: "border-box",
                }}
              />
            </div>
            {ticketError && (
              <p style={{ color: "#e53e3e", fontSize: 12, marginBottom: 8 }}>
                {ticketError}
              </p>
            )}
            <button
              type="submit"
              className="scb-call-btn"
              disabled={ticketLoading}
            >
              {ticketLoading ? "Submitting…" : "Submit Ticket"}
            </button>
          </form>
        )}
      </div>
    </>
  );

  const renderBody = () => {
    if (subView === "ticket") return renderTicketView();

    if (subView === "chat")
      return (
        <>
          {renderHeader("Chat with us", true)}
          {renderChatArea()}
        </>
      );

    if (subView === "call")
      return (
        <>
          {renderHeader("Call us now", true)}
          <div className="scb-call-view">
            {callSubmitted ? (
              <div className="scb-call-success">
                <div className="scb-call-success-icon">
                  <CheckCircle style={{ color: "var(--bg-main)" }} />
                </div>
                <h3>Request Received!</h3>
                <p>
                  Our team will call you shortly during business hours (Mon–Fri,
                  9AM–6PM WAT).
                </p>
                <button
                  className="scb-call-btn"
                  onClick={() => {
                    setCallSubmitted(false);
                    setCallForm({ name: "", email: "" });
                  }}
                >
                  Submit another
                </button>
              </div>
            ) : (
              <form
                className="scb-call-form"
                onSubmit={(e) => {
                  e.preventDefault();
                  setCallSubmitted(true);
                }}
              >
                <p className="scb-call-info">
                  Fill in your details and we will call you back as soon as
                  possible.
                </p>
                <div className="scb-call-group">
                  <label>Full Name</label>
                  <input
                    type="text"
                    placeholder="Your name"
                    value={callForm.name}
                    onChange={(e) =>
                      setCallForm((p) => ({ ...p, name: e.target.value }))
                    }
                    required
                  />
                </div>
                <div className="scb-call-group">
                  <label>Email Address</label>
                  <input
                    type="email"
                    placeholder="your@email.com"
                    value={callForm.email}
                    onChange={(e) =>
                      setCallForm((p) => ({ ...p, email: e.target.value }))
                    }
                    required
                  />
                </div>
                <button type="submit" className="scb-call-btn">
                  <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.56 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.91a16 16 0 0 0 6.06 6.06l.9-.9a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 21.73 16.92z" />
                  </svg>
                  Call Now
                </button>
              </form>
            )}
          </div>
        </>
      );

    if (tab === "home")
      return (
        <>
          {renderHeader()}
          <div className="scb-home">
            <div className="scb-home-hero">
              <div className="scb-home-avatar-lg">e</div>
              <h2 className="scb-home-title">ePay Support</h2>
              <p className="scb-home-sub">{userName}, we are here for you</p>
            </div>
            <div className="scb-home-options">
              <button
                className="scb-home-option"
                onClick={() => {
                  setSubView("chat");
                  setTab("conversation");
                }}
              >
                <div className="scb-option-icon">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                </div>
                <div className="scb-option-text">
                  <span className="scb-option-label">Chat with us now</span>
                  <span className="scb-option-desc">
                    Typically replies instantly
                  </span>
                </div>
                <ChevronRight />
              </button>
              <button
                className="scb-home-option"
                onClick={() => setSubView("ticket")}
              >
                <div className="scb-option-icon">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                    <line x1="16" y1="13" x2="8" y2="13" />
                    <line x1="16" y1="17" x2="8" y2="17" />
                  </svg>
                </div>
                <div className="scb-option-text">
                  <span className="scb-option-label">
                    Open a support ticket
                  </span>
                  <span className="scb-option-desc">
                    We'll respond within 1 business day
                  </span>
                </div>
                <ChevronRight />
              </button>
              <button
                className="scb-home-option"
                onClick={() => setSubView("call")}
              >
                <div className="scb-option-icon">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.56 1.18h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 8.91a16 16 0 0 0 6.06 6.06l.9-.9a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 21.73 16.92z" />
                  </svg>
                </div>
                <div className="scb-option-text">
                  <span className="scb-option-label">Call us now</span>
                  <span className="scb-option-desc">Mon–Fri, 9AM–6PM WAT</span>
                </div>
                <ChevronRight />
              </button>
            </div>
          </div>
        </>
      );

    if (tab === "conversation")
      return (
        <>
          {renderHeader()}
          {renderChatArea()}
        </>
      );

    if (tab === "faqs")
      return (
        <>
          {faqView === "answer" && selectedFaqItem && (
            <>
              {renderHeader("Answer", true)}
              <div className="scb-answer-view">
                <div className="scb-answer-question-pill">
                  <svg
                    width="14"
                    height="14"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                  >
                    <circle cx="12" cy="12" r="10" />
                    <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
                    <line x1="12" y1="17" x2="12.01" y2="17" />
                  </svg>
                  <span>{selectedFaqItem.question}</span>
                </div>
                <div className="scb-answer-card">
                  <div className="scb-answer-card-header">
                    <div className="scb-answer-author">
                      <div className="scb-answer-author-avatar">A</div>
                      <div className="scb-answer-author-info">
                        <span className="scb-answer-author-name">
                          {selectedFaqItem.authorName || "ePay Support Team"}
                        </span>
                        <span className="scb-answer-author-role">
                          Support Team
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="scb-answer-divider" />
                  <p className="scb-answer-content">{selectedFaqItem.answer}</p>
                </div>
                <button
                  className="scb-article-chat-btn"
                  onClick={() => {
                    setTab("conversation");
                    setSubView(null);
                  }}
                >
                  Still need help? Chat with us
                </button>
              </div>
            </>
          )}

          {faqView === "questions" && selectedFaqCat && (
            <>
              {renderHeader(selectedFaqCat.category, true)}
              {faqLoading ? (
                <div className="scb-faq-loader-body">
                  <div className="scb-faq-spinner" />
                  <span className="scb-faq-loader-text">Loading...</span>
                </div>
              ) : (
                <div className="scb-list-view">
                  {selectedFaqCat.items.map((item) => {
                    const { truncated, isTruncated } = truncateChars(
                      item.question,
                      75,
                    );
                    return (
                      <button
                        key={item.id}
                        className="scb-list-item"
                        onClick={() => openFaqAnswer(item)}
                      >
                        <div>
                          <div className="scb-list-title">
                            {truncated}
                            {isTruncated && (
                              <span className="scb-list-readmore">
                                {" "}
                                Read more
                              </span>
                            )}
                          </div>
                        </div>
                        <ChevronRight />
                      </button>
                    );
                  })}
                </div>
              )}
            </>
          )}

          {faqView === "categories" && (
            <>
              {renderHeader("FAQs")}
              <div className="scb-search-bar">
                <SearchIcon />
                <input
                  className="scb-search-input"
                  placeholder="Search for an FAQ"
                  value={faqSearch}
                  onChange={(e) => setFaqSearch(e.target.value)}
                />
              </div>
              <div className="scb-list-view">
                {faqCategories
                  .filter((c) =>
                    c.category.toLowerCase().includes(faqSearch.toLowerCase()),
                  )
                  .map((cat) => (
                    <button
                      key={cat.category}
                      className="scb-list-item"
                      onClick={() => openFaqCategory(cat)}
                    >
                      <div>
                        <div className="scb-list-title">{cat.category}</div>
                        <div className="scb-list-sub">
                          {cat.items.length} FAQ
                          {cat.items.length !== 1 ? "s" : ""}
                        </div>
                      </div>
                      <ChevronRight />
                    </button>
                  ))}
                {!faqCategories.length && !dataLoading && (
                  <p
                    style={{
                      textAlign: "center",
                      color: "#9ca3af",
                      padding: 24,
                      fontSize: 13,
                    }}
                  >
                    No FAQs available yet.
                  </p>
                )}
              </div>
            </>
          )}
        </>
      );

    if (tab === "articles")
      return (
        <>
          {selectedArticle ? (
            <>
              {renderHeader("Article", true)}
              <div className="scb-article-detail">
                <div className="scb-article-badge">Article</div>
                <h3 className="scb-article-dtitle">{selectedArticle.title}</h3>
                <p className="scb-article-body">{selectedArticle.content}</p>
                <button
                  className="scb-article-chat-btn"
                  onClick={() => {
                    setSelectedArticle(null);
                    setTab("conversation");
                  }}
                >
                  Still need help? Chat with us
                </button>
              </div>
            </>
          ) : (
            <>
              {renderHeader("Articles")}
              <div className="scb-search-bar">
                <SearchIcon />
                <input
                  className="scb-search-input"
                  placeholder="Search for an article"
                  value={articleSearch}
                  onChange={(e) => setArticleSearch(e.target.value)}
                />
              </div>
              <div className="scb-list-view">
                {articles
                  .filter((a) =>
                    a.title.toLowerCase().includes(articleSearch.toLowerCase()),
                  )
                  .map((art) => (
                    <button
                      key={art.id}
                      className="scb-list-item"
                      onClick={() => setSelectedArticle(art)}
                    >
                      <div>
                        <div className="scb-list-title">{art.title}</div>
                        <div className="scb-list-sub">{art.category}</div>
                      </div>
                      <ChevronRight />
                    </button>
                  ))}
                {!articles.length && !dataLoading && (
                  <p
                    style={{
                      textAlign: "center",
                      color: "#9ca3af",
                      padding: 24,
                      fontSize: 13,
                    }}
                  >
                    No articles available yet.
                  </p>
                )}
              </div>
            </>
          )}
        </>
      );
  };

  if (!isOpen) return null;

  return (
    <div className="scb-wrapper">
      <div className="scb-body">{renderBody()}</div>
      {!subView && (
        <nav className="scb-nav">
          {[
            {
              key: "home" as Tab,
              label: "Home",
              icon: (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                  <polyline points="9 22 9 12 15 12 15 22" />
                </svg>
              ),
            },
            {
              key: "conversation" as Tab,
              label: "Conversation",
              icon: (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                </svg>
              ),
            },
            {
              key: "faqs" as Tab,
              label: "FAQs",
              icon: (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="12" cy="12" r="10" />
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
                  <line x1="12" y1="17" x2="12.01" y2="17" />
                </svg>
              ),
            },
            {
              key: "articles" as Tab,
              label: "Articles",
              icon: (
                <svg
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="16" y1="13" x2="8" y2="13" />
                  <line x1="16" y1="17" x2="8" y2="17" />
                  <polyline points="10 9 9 9 8 9" />
                </svg>
              ),
            },
          ].map((item) => (
            <button
              key={item.key}
              className={`scb-nav-item ${tab === item.key ? "scb-nav-active" : ""}`}
              onClick={() => {
                setTab(item.key);
                setSelectedFaqCat(null);
                setSelectedFaqItem(null);
                setSelectedArticle(null);
                setFaqView("categories");
              }}
            >
              {item.icon}
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
      )}
    </div>
  );
};

export default SupportChatBot;
