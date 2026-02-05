// api/services/authService.js
import { makePublicRequest, makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const authService = {
    login: (credentials) => {
        return makePublicRequest(API_URLS.AUTH.LOGIN, 'POST', credentials);
    },
    
    register: (userData) => {
        return makePublicRequest(API_URLS.AUTH.REGISTER, 'POST', userData);
    },
    
    getProfile: () => {
        return makeAuthenticatedRequest(API_URLS.AUTH.PROFILE, 'GET');
    },
    
    logout: (sessionId) => {
        return makePublicRequest(API_URLS.AUTH.LOGOUT(sessionId), 'POST');
    },
    
    refreshToken: () => {
        return makePublicRequest(API_URLS.AUTH.REFRESH_TOKEN, 'POST');
    },

    verifyToken: (token) => {
        return makePublicRequest(API_URLS.AUTH.VERIFYTOKEN(token), 'GET');
    },

    resendOtp: (token) => {
        return makePublicRequest(API_URLS.AUTH.RESENDOTP(token), 'POST');
    },

    verifyOtp: (payload) => {
        return makePublicRequest(API_URLS.AUTH.VERIFYOTP, 'POST', payload);
    },

    sendVerifyCode: (identifier, method) => {
        return makePublicRequest(API_URLS.AUTH.SEND_VERIFY_CODE(identifier, method), 'POST');
    },

    resendVerificationCode: (token)=>{

    },

    verifyTwoFactorAuth:(token, verificationCode)=>{

    }
};

export default authService;