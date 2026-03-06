export interface Transaction {
  id: string;
  user: string;
  initials: string;
  amount: string;
  isPositive: boolean;
  type: TransactionType;
  status: TransactionStatus;
}

export interface Notification {
  type: "icon" | "avatar";
  initials?: string;
  text: string;
  time: string;
}

export interface StatCard {
  label: string;
  value: string;
  sub?: string;
  variant: "green" | "blue" | "orange" | "yellow";
}

export interface NavItem {
  label: string;
  href?: string;
  active?: boolean;
  children?: { label: string; href: string }[];
}

export type TransactionType = "Deposit" | "Withdrawal" | "Bit Payment" | "Virtual Card";
export type TransactionStatus = "Successful" | "Failed";

export interface Notification {
  type: "icon" | "avatar";
  initials?: string;
  text: string;
  time: string;
}

export interface StatCard {
  label: string;
  value: string;
  sub?: string;
  variant: "green" | "blue" | "orange" | "yellow";
}

export interface NavItem {
  label: string;
  href?: string;
  hasArrow?: boolean;
  active?: boolean;
}

export type KycStatus = "Verified" | "Pending" | "Rejected";
export type UserStatus = "Active" | "Suspended" | "Inactive";

export interface User {
  id: string;
  name: string;
  email: string;
  balance: string;
  kyc: KycStatus;
  status: UserStatus;
  dateRegistered: string;
  avatar: string;
  phone?: string;
}

export type ActionType = "view" | "edit" | "suspend" | "delete" | null;