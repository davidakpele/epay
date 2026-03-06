import { Transaction, Notification, StatCard, NavItem } from "../types";

export const TRANSACTIONS: Transaction[] = [
  { id: "TXN123456", user: "David Alex",     initials: "DA", amount: "+₦50,000",  isPositive: true,  type: "Deposit",      status: "Successful" },
  { id: "TXN123455", user: "Sarah Williams", initials: "SW", amount: "-₦7,500",   isPositive: false, type: "Withdrawal",   status: "Failed"     },
  { id: "TXN123454", user: "John Doe",       initials: "JD", amount: "-₦6,000",   isPositive: false, type: "Bit Payment",  status: "Successful" },
  { id: "TXN123453", user: "Emma Johnson",   initials: "EJ", amount: "+₦90,000",  isPositive: true,  type: "Deposit",      status: "Successful" },
  { id: "TXN123452", user: "Michael Brown",  initials: "MB", amount: "-₦7,000",   isPositive: false, type: "Virtual Card", status: "Successful" },
  { id: "TXN123451", user: "Linda Green",    initials: "LG", amount: "+₦20,000",  isPositive: true,  type: "Deposit",      status: "Successful" },
];

export const NOTIFICATIONS: Notification[] = [
  { type: "icon",   text: "3 new KYC verifications are pending", time: "2 hours ago"  },
  { type: "avatar", initials: "SW", text: "Sarah Williams withdrew $7,500",      time: "3 hours ago"  },
  { type: "avatar", initials: "DA", text: "David Alex just signed up",           time: "6 hours ago"  },
  { type: "avatar", initials: "EJ", text: "Emma Johnson created a virtual card", time: "10 hours ago" },
  { type: "avatar", initials: "JD", text: "John Doe deposited ₦5,000",          time: "1 day ago"    },
  { type: "avatar", initials: "JD", text: "John Doe deposited ₦5,000",          time: "1 day ago"    },
];

export const STAT_CARDS: StatCard[] = [
  { label: "Total Users",        value: "18,940",  variant: "green"  },
  { label: "Total Balance",      value: "₦84.5M",  sub: "₦84,560,200",    variant: "blue"   },
  { label: "Total Transactions", value: "607,125", sub: "₦1,238,888,900", variant: "orange" },
  { label: "Virtual Cards",      value: "5,218",   variant: "yellow" },
];

export const NAV_ITEMS: NavItem[] = [
  { label: "Dashboard", href: "#", active: true },
  { label: "Users",     href: "#" },
  {
    label: "Transactions",
    children: [
      { label: "All Transactions", href: "#" },
      { label: "Deposits",         href: "#" },
      { label: "Withdrawals",      href: "#" },
      { label: "Transfers",        href: "#" },
    ],
  },
  {
    label: "Virtual Cards",
    children: [
      { label: "All Cards",    href: "#" },
      { label: "Active Cards", href: "#" },
      { label: "Frozen Cards", href: "#" },
    ],
  },
  {
    label: "Paybills",
    children: [
      { label: "Airtime",   href: "#" },
      { label: "Data",      href: "#" },
      { label: "Utilities", href: "#" },
      { label: "Cable TV",  href: "#" },
    ],
  },
  {
    label: "Exchange",
    children: [
      { label: "Buy Crypto",  href: "#" },
      { label: "Sell Crypto", href: "#" },
      { label: "Swap",        href: "#" },
      { label: "Rates",       href: "#" },
    ],
  },
  {
    label: "Account",
    children: [
      { label: "Profile",   href: "#" },
      { label: "Security",  href: "#" },
      { label: "KYC",       href: "#" },
      { label: "API Keys",  href: "#" },
    ],
  },
];

export const SIDEBAR_ITEMS: NavItem[] = [
  { label: "Dashboard",    href: "/",              active: true },
  { label: "Manage Users", href: "/manage-users"               },
  { label: "Manage KYC",  href: "/manage-kyc"                 },
  {
    label: "Transactions",
    children: [
      { label: "All Transactions", href: "/transactions" },
      { label: "Deposits",         href: "/transactions/deposits" },
      { label: "Withdrawals",      href: "/transactions/withdrawals" },
      { label: "Transfers",        href: "/transactions/transfers" },
    ],
  },
  {
    label: "Virtual Cards",
    children: [
      { label: "All Cards",    href: "/virtual-cards" },
      { label: "Active Cards", href: "/virtual-cards/active" },
      { label: "Frozen Cards", href: "/virtual-cards/frozen" },
    ],
  },
  {
    label: "Paybills",
    children: [
      { label: "Airtime",   href: "/paybills/airtime" },
      { label: "Data",      href: "/paybills/data" },
      { label: "Utilities", href: "/paybills/utilities" },
      { label: "Cable TV",  href: "/paybills/cable" },
    ],
  },
  {
    label: "Exchange",
    children: [
      { label: "Buy Crypto",  href: "/exchange/buy" },
      { label: "Sell Crypto", href: "/exchange/sell" },
      { label: "Swap",        href: "/exchange/swap" },
    ],
  },
  { label: "Settings", href: "/settings" },
];

export const CHART_MONTHS = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan","Feb","Mar"];
export const CHART_TRANSACTIONS = [18000,30000,59875,40000,55000,70000,90000,110000,130000,120000,150000,180000,140000,160000,200000];
export const CHART_REVENUE = [5000,12000,29000,18000,24000,35000,48000,62000,75000,68000,90000,110000,85000,100000,134000];