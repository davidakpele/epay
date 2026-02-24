import { makePublicRequest, makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const beneficiaryService = {
    getBeneficiaries: () => {
        return makeAuthenticatedRequest(API_URLS.BENEFICIARY.LIST, 'GET');
    },

    getAllByUserId: (id) => {
        return makeAuthenticatedRequest(API_URLS.BENEFICIARY.GET_ALL(id), 'GET');
    },
    
    saveBeneficiary: (beneficiaryData) => {
        return makeAuthenticatedRequest(API_URLS.BENEFICIARY.CREATE, 'POST', beneficiaryData);
    },

    updateBeneficiary: (beneficiaryId, beneficiaryData) => {
        return makeAuthenticatedRequest(API_URLS.BENEFICIARY.UPDATE(beneficiaryId), 'PUT', beneficiaryData);
    },

    deleteBeneficiary: (beneficiaryId) => {
        return makeAuthenticatedRequest(API_URLS.BENEFICIARY.DELETE(beneficiaryId), 'DELETE');
    },

    verifyBeneficiary: (beneficiaryId) => {
        return makeAuthenticatedRequest(API_URLS.BENEFICIARY.VERIFY(beneficiaryId), 'POST');
    },
};

export default beneficiaryService;