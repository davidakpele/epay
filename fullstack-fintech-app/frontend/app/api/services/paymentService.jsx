// api/services/paymentService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const paymentService = {
    process: (paymentData) => {
        return makeAuthenticatedRequest(API_URLS.PAYMENT.PROCESS, 'POST', paymentData);
    },
    
    getHistory: () => {
        return makeAuthenticatedRequest(API_URLS.PAYMENT.HISTORY, 'GET');
    },
    
    getStatus: (id) => {
        return makeAuthenticatedRequest(API_URLS.PAYMENT.STATUS(id), 'GET');
    },
};

export default paymentService;