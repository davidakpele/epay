// bankCollectionService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const bankCollectionService = {
    // Get default home endpoint
    getDefaultHome: () => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.DEFAULT_HOME, 'GET');
    },
    
    // Create a new bank collection
    create: (bankData) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.CREATE, 'POST', bankData);
    },
    
    // Get bank collection by ID
    getById: (id) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BY_ID(id), 'GET');
    },
    
    // Get bank collection by account number
    getByAccountNumber: (accountNumber) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BY_ACCOUNT_NUMBER(accountNumber), 'GET');
    },
    
    // Get bank collections by user ID
    getByUserId: (userId) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BY_USER_ID(userId), 'GET');
    },
    
    // Delete bank collection by ID
    delete: (id) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.DELETE(id), 'DELETE');
    },

    getBankList: () => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BANK_LIST_API, 'GET');
    },

    getUserBanks: (accountNumber, bankCode) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.VERIFY_USER_BANK_DETAILS(accountNumber, bankCode), 'GET');
    },

    verifyUserBanks: (accountNumber, bankCode) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.VERIFY(accountNumber, bankCode), 'GET');
    },

    createVirtualCard: (payload) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.CREATEVIRTUALCARD, 'POST', payload);
    },

    getUserVirtualCards: (userId) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_VIRTUAL_CARD_LIST_BY_USER_ID(userId), 'GET');
    },
};

export default bankCollectionService;