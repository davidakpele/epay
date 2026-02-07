//types/util.ts

import { ReactElement } from "react";
import { JSX } from "react/jsx-runtime";

export interface MobileNavProps {
  activeTab?: string;
}

export interface DepositModalProps {
  isOpen: boolean;
  onClose: () => void;
  theme: 'light' | 'dark';
  onDepositSuccess?: () => void; 
}

export interface WithdrawModalProps {
  isOpen: boolean;
  onClose: () => void;
  theme: 'light' | 'dark';
  onWithdrawReloadSuccess?: () => void; 
}


export interface Bank {
  id: number;
  bankName: string;
  accountName: string;
  accountNumber: string;
  bankCode: string;
  isVerified: boolean;
}

export interface WalletType {
  id: number;
  name: string;
  balance: number;
  currency: string;
  isDefault: boolean;
}

export interface ExchangeCurrency {
  code: string;
  name: string;
  symbol: string;
  flag: string;
}

export interface ExchangeRate {
  from: string;
  to: string;
  rate: number;
  lastUpdated: string;
}

export interface UserData {
  id: string;
  fullName: string;
  email: string;
  username: string;
  phone: string;
  dateOfBirth: string;
  gender: string;
  address: string;
  city: string;
  country: string;
  referralName: string;
  customerId: string;
  status: 'Active' | 'Suspended' | 'Pending';
  kycLevel: number;
}

export interface KYCDocument {
  id: string;
  type: string;
  verifiedOn: string;
  status: 'verified' | 'pending' | 'rejected';
}

export interface LoginHistory {
  id: string;
  device: string;
  browser: string;
  location: string;
  timestamp: string;
}

export interface Transaction {
  id: string;
  type: TransactionType;
  currency: string;
  amount: number;
  fiatAmount: number;
  status: TransactionStatus;
  date: string;
  description: string;
  transactionId?: string;
  sessionId?: string;
  referenceNo?: string;
  terminalId?: string;
  erId?: string;
  accountHolder?: string;
  previousBalance?: number;
  availableBalance?: number;
  icon: React.ReactElement;
  originalData: any;
}

export interface StatusInfo {
  color: string;
  icon: ReactElement;
  text: string;
}

export interface ContactMethod {
  icon: JSX.Element;
  title: string;
  description: string;
  availability: string;
  action: string;
  color: string;
}

export interface Question {
  q: string;
  a: string;
}

export interface FAQCategory {
  category: string;
  icon: JSX.Element;
  questions: Question[];
}

export interface QuickLink {
  icon: JSX.Element;
  label: string;
  href: string;
}

export interface FormData {
  name: string;
  email: string;
  subject: string;
  category: string;
  message: string;
  priority: 'low' | 'medium' | 'high' | 'urgent';
}

export interface ReferralTransaction {
  id: string;
  type: string;
  amount: number;
  commission: number;
  date: string;
}

export interface ReferredUser {
  id: string;
  username: string;
  joinedDate: string;
  totalEarningsFromUser: number;
  transactions: ReferralTransaction[];
}

export interface Service {
  id: string;
  name: string;
  icon: React.ReactNode;
  color: string;
  category: string;
}

export interface UserSettings {
  profile: {
    fullName: string;
    email: string;
    phone: string;
    username: string;
    profileImage: string;
  };
  security: {
    twoFactorEnabled: boolean;
    biometricEnabled: boolean;
    sessionTimeout: number;
  };
  notifications: {
    email: boolean;
    push: boolean;
    sms: boolean;
    transactionAlerts: boolean;
    loginAlerts: boolean;
    marketingEmails: boolean;
  };
  preferences: {
    language: string;
    currency: string;
    theme: 'light' | 'dark' | 'auto';
    timezone: string;
  };
}

export interface AccountTransactionStatement {
  id: string;
  date: string;
  description: string;
  type: string;
  amount: number;
  status: string;
  reference: string;
  currencyType: string;
  availableBalance: number;
  previousBalance: number;
}

export interface BalanceCardProps {
  title: string;
  amount: string;
  subtitle: string;
  icon: React.ReactNode;
  isPrimary?: boolean;
  trend?: string;
}

export type MetaMapErrors = {
  bvn?: string;
  firstName?: string;
  lastName?: string;
  dob?: string;
};

export interface CardTheme {
  name: string;
  imageUrl: string;
  label: string;
};


export type TransactionType = "deposit" | "withdrawal" | "swap" | "transfer" | "credited";
export type TransactionStatus = "completed" | "pending" | "failed";