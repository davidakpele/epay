// api/services/walletService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const walletService = {
    
    getById: (id) => {
        return makeAuthenticatedRequest(API_URLS.WALLET.ID(id), 'GET');
    },

    getByCurrency: (id, currency) => {
        return makeAuthenticatedRequest(API_URLS.WALLET.CURRENCY(id, currency), 'GET');
    },
    
    getByUserId: (id, token) => {
        return makeAuthenticatedRequest(API_URLS.WALLET.BY_USER_ID(id, token), 'GET');
    },

    createTransferPin: (data) => { 
        return makeAuthenticatedRequest(API_URLS.WALLET.CREATEWITHDRAWPIN, 'POST', data);
    },

    verifyPin: (data) => { 
        return makeAuthenticatedRequest(API_URLS.WALLET.VERIFYPIN, 'POST', data);
    },

    updateDefaultCurrency: (data)=>{
        return makeAuthenticatedRequest(API_URLS.WALLET.UPDATE_DEFAULT_CURRENCY, 'PUT', data);
    },
    
};

export default walletService;