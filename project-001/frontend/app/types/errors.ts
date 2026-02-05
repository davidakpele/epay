export interface LoginFormErrors {
  username?: string;
  password?: string;
}

export interface RegisterFormErrors {
    firstname?: string;
    lastname?: string;
    email?: string;
    password?: string;
}

export interface ResetPasswordFormErrors {
  email?: string;
  phone?: string;
}

export interface ForwardAccountStatement {
  email?: string;
  general?: string;
}

// Statement item that matches backend requirements
export interface StatementItem {
  id: string;
  date: string;
  description: string;
  type: string;
  currencyType: string;
  amount: number;
  balance: number;
  reference: string;
}

// Request payload for sending statement email
export interface SendStatementRequest {
  email: string;
  statements: StatementItem[];
}

// Generic API response structure
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}

// Response data when email is sent successfully
export interface EmailSentData {
  email: string;
  statementCount: number;
  sentAt: string;
}

// Form validation errors
export interface ValidationErrors {
  email?: string;
  statements?: string;
  [key: string]: string | undefined;
}