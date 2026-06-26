import { makeAuthenticatedRequest } from '../utils';
import { API_URLS } from '../config';

export interface InvestmentPlan {
  duration: string;
  label: string;
  annualRate: number;
  durationDays: number;
}

export interface InvestmentCalculation {
  principal: number;
  duration: string;
  durationDays: number;
  annualRate: number;
  expectedProfit: number;
  totalPayout: number;
  currencyCode: string;
  maturityDate: string;
}

export interface Investment {
  id: number;
  userId: number;
  walletId: number;
  currencyCode: string;
  principal: number;
  returnRate: number;
  expectedProfit: number;
  totalPayout: number;
  duration: string;
  durationDays: number;
  startDate: string;
  maturityDate: string;
  status: 'ACTIVE' | 'MATURED' | 'PAID_OUT' | 'FAILED';
  paidOutAt?: string;
  referenceId?: string;
  createdOn: string;
  updatedOn: string;
}

export const investmentService = {
  /** Fetch all available investment plans and their rates. */
  getPlans: () =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.PLANS, 'GET'),

  /** Preview profit/payout for a given principal and duration before investing. */
  calculate: (data: { principal: number; duration: string; currencyCode: string }) =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.CALCULATE, 'POST', data),

  /** Create a new investment — deducts principal from wallet immediately. */
  create: (data: {
    userId: number | string;
    walletId: number | string;
    currencyCode: string;
    principal: number;
    duration: string;
  }) => makeAuthenticatedRequest(API_URLS.INVESTMENT.CREATE, 'POST', data),

  /** Retrieve all investments for a user. */
  getByUserId: (userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.BY_USER(userId), 'GET'),

  /** Retrieve a single investment by ID. */
  getById: (id: number | string, userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.BY_ID(id, userId), 'GET'),
};

export default investmentService;
