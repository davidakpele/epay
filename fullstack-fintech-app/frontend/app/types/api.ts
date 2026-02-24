import { StatementItem } from "./errors";

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface ApiError {
  error: string;
  message: string;
}

export interface Currency {
  name: string;
  code: string;
  symbol: string;
}

export interface StatementItemPayload {
  id: string;
  date: string;
  description: string;
  type: string;
  currencyType: string;
  amount: number;
  balance: number;
  reference: string;
}

export interface SendStatementPayload {
  email: string;
  statements: StatementItem[];
}

export interface ForwardStatementPayload {
  email: string;
  statements: StatementItemPayload[];
}