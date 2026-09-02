// api/services/userService.js
import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const userService = {
    getAll: () => {
        return makeAuthenticatedRequest(API_URLS.USER.BASE, 'GET');
    },
    
    getById: (id) => {
        return makeAuthenticatedRequest(API_URLS.USER.BY_ID(id), 'GET');
    },
    
    updateProfile: (userData, id) => {
        return makeAuthenticatedRequest(API_URLS.USER.PROFILE(id), 'PUT', userData);
    },

    updateUserPassword:(userData) => {
        return makeAuthenticatedRequest(API_URLS.USER.UPDATE_PASSWORD, 'PUT', userData);
    },
    

    sendAccountStatement:(id, email, statement) => {
        return makeAuthenticatedRequest(API_URLS.USER.SENDACCOUNTSTATEMENT(id, email), 'POST', statement);
    },

    update2FAStatus: (userId, twoFARequest) => {
        return makeAuthenticatedRequest(API_URLS.USER.UPDATE2FASTATUS(), 'POST', twoFARequest);
    },

    uploadProfileImage: (id, formData) => {
        return makeAuthenticatedRequest(API_URLS.USER.UPLOAD_PROFILE_IMAGE(id), 'POST', formData);
    },

    changePassword: (userId, passwordData) => {
        return makeAuthenticatedRequest(API_URLS.USER.UPDATE_PASSWORD(), 'PUT', passwordData);
    },

    removeProfileImage: (userId) => {
        return makeAuthenticatedRequest(API_URLS.USER.REMOVE_PROFILE_IMAGE(userId), 'DELETE');
    },

    deleteAccount: (userId) =>{ 
        return makeAuthenticatedRequest(API_URLS.USER.DELETE_ACCOUNT(userId), 'DELETE');
    }
    
};

export default userService;