// api/services/paymentService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const historyService = {
    
    getHistory: (id) => {
        return makeAuthenticatedRequest(API_URLS.HISTORY.ALL(id), 'GET');
    },

    deleteTransaction : (id) => {
        return makeAuthenticatedRequest(API_URLS.HISTORY.DELETE(id), 'DELETE');
    },

    getFilteredHistory: (id, data) => {
        return makeAuthenticatedRequest(API_URLS.HISTORY.FILTERED(id, data), 'GET');
    }

};

export default historyService;