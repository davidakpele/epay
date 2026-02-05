// api/services/depositService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const depositService = {
    getAll: () => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.BASE, 'GET');
    },
    
    getById: (id) => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.BY_ID(id), 'GET');
    },
    
    create: (depositData) => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.BASE, 'POST', depositData);
    },
    
    update: (id, depositData) => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.BY_ID(id), 'PUT', depositData);
    },
    
    delete: (id) => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.BY_ID(id), 'DELETE');
    },
    
    getHistory: (userId) => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.HISTORY(userId), 'GET');
    },
    
    getStatus: (id) => {
        return makeAuthenticatedRequest(API_URLS.DEPOSIT.STATUS(id), 'GET');
    },
};

export default depositService;