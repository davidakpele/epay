// api/services/investmentService.js
import { API_URLS } from '../config.ts';
import { makeAuthenticatedRequest } from '../utils.ts';

export const investmentService = {

  /**
   * GET /investments/plans
   * Returns all available plan types with their rates.
   */
  getPlans: () => {
    return makeAuthenticatedRequest(API_URLS.INVESTMENT.PLANS, 'GET');
  },

  /**
   * POST /investments/calculate
   * Preview returns before committing. Call on every amount/duration change (debounced).
   * @param {{ principal: number, duration: string, currencyCode: string }} data
   */
  calculate: (data) => {
    return makeAuthenticatedRequest(API_URLS.INVESTMENT.CALCULATE, 'POST', data);
  },

  /**
   * POST /investments/create
   * Deducts principal from wallet and locks the investment.
   * @param {{ userId: number|string, walletId: number|string, principal: number, duration: string, currencyCode: string }} data
   */
  create: (data) => {
    return makeAuthenticatedRequest(API_URLS.INVESTMENT.CREATE, 'POST', data);
  },

  /**
   * GET /investments/user/{userId}
   * Returns all investments for the authenticated user.
   * @param {string|number} userId
   */
  getByUserId: (userId) => {
    return makeAuthenticatedRequest(API_URLS.INVESTMENT.BY_USER_ID(userId), 'GET');
  },

  /**
   * GET /investments/{id}?userId={userId}
   * Returns a single investment (ownership enforced server-side).
   * @param {string|number} id
   * @param {string|number} userId
   */
  getById: (id, userId) => {
    return makeAuthenticatedRequest(API_URLS.INVESTMENT.BY_ID(id, userId), 'GET');
  },
};

export default investmentService;
