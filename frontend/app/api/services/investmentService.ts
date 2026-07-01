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
  getPlans: () =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.PLANS, 'GET'),

  calculate: (data: { principal: number; duration: string; currencyCode: string }) =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.CALCULATE, 'POST', data),

  create: (data: {
    userId: number | string;
    walletId: number | string;
    currencyCode: string;
    principal: number;
    duration: string;
  }) => makeAuthenticatedRequest(API_URLS.INVESTMENT.CREATE, 'POST', data),

  getByUserId: (userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.BY_USER(userId), 'GET'),

  getById: (id: number | string, userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.INVESTMENT.BY_ID(id, userId), 'GET'),
};

export default investmentService;
