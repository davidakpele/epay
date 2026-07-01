import { makeAuthenticatedRequest } from '../utils';
import { API_URLS } from '../config';

export interface TargetSavings {
  id: number;
  userId: number;
  walletId: number;
  currencyCode: string;
  goalName: string;
  description?: string;
  targetAmount: number;
  savedAmount: number;
  targetDate?: string;
  status: 'ACTIVE' | 'COMPLETED' | 'WITHDRAWN' | 'CANCELLED';
  goalIcon?: string;
  completedAt?: string;
  withdrawnAt?: string;
  createdOn: string;
  updatedOn: string;
}

export const targetSavingsService = {
  create: (data: {
    userId: number | string;
    walletId: number | string;
    currencyCode: string;
    goalName: string;
    description?: string;
    targetAmount: number;
    targetDate?: string;
    goalIcon?: string;
  }) => makeAuthenticatedRequest(API_URLS.SAVINGS.CREATE, 'POST', data),

  topUp: (id: number | string, data: {
    userId: number | string;
    walletId: number | string;
    amount: number;
  }) => makeAuthenticatedRequest(API_URLS.SAVINGS.TOPUP(id), 'POST', data),

  withdraw: (id: number | string, data: {
    userId: number | string;
    walletId: number | string;
  }) => makeAuthenticatedRequest(API_URLS.SAVINGS.WITHDRAW(id), 'POST', data),

  getByUserId: (userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.SAVINGS.BY_USER(userId), 'GET'),

  getById: (id: number | string, userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.SAVINGS.BY_ID(id, userId), 'GET'),

  cancel: (id: number | string, userId: number | string) =>
    makeAuthenticatedRequest(`${API_URLS.SAVINGS.CANCEL(id)}?userId=${userId}`, 'DELETE'),
};

export default targetSavingsService;
