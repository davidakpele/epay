import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';


export const withdrawService = { 
    withdrawToBank: (data) => {
        return makeAuthenticatedRequest(API_URLS.TRANSFER.BANKTRANSFER, 'POST', data);
    },

    transferToUser: (data) => {
        return makeAuthenticatedRequest(API_URLS.TRANSFER.PLATFORMWITHDRAWS, 'POST', data);
    },

    
};
export default withdrawService;
    