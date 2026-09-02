// api/services/exchangeService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const exchangeService = {
  swap: (payload) => {
    return makeAuthenticatedRequest(
      API_URLS.EXCHANGE(payload.userId),
      "POST",
      payload,
    );
  },
};

export default exchangeService;
