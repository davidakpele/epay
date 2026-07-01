// bankCollectionService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const bankCollectionService = {
    getDefaultHome: () => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.DEFAULT_HOME, 'GET');
    },
    
    create: (bankData) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.CREATE, 'POST', bankData);
    },
    
    getById: (id) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BY_ID(id), 'GET');
    },

    getByAccountNumber: (accountNumber) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BY_ACCOUNT_NUMBER(accountNumber), 'GET');
    },
    
    getByUserId: (userId) => {
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.GET_BY_USER_ID(userId), 'GET');
    },
    
    delete: (id) => {
        const request = { ids: [id] } 
        return makeAuthenticatedRequest(API_URLS.BANKCOLLECTIONLIST.DELETE(id), 'DELETE', request);
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