import { HistoryFilterPayload } from "../types/utils";

const BASE_URL = 'http://localhost/api/v1';

export const SERVICE_URLS = {
  AUTH: BASE_URL,
  DEPOSIT: BASE_URL,
  USER: BASE_URL,
  PAYMENT: BASE_URL,
  WALLET: BASE_URL,
  BANKCOLLECTIONLIST: BASE_URL,
  WITHDRAW: BASE_URL,
  HISTORY: BASE_URL,
  BENEFICIARY: BASE_URL,
  VIRTUALCARD: BASE_URL,
} as const;

export const defaultHeaders: Record<string, string> = {
  'Content-Type': 'application/json',
};

export const API_URLS = {
  AUTH: {
    LOGIN: `${SERVICE_URLS.AUTH}/auth/login`,
    REGISTER: `${SERVICE_URLS.AUTH}/auth/register`,
    PROFILE: `${SERVICE_URLS.AUTH}/auth/profile`,
    LOGOUT: (id: string) => `${SERVICE_URLS.AUTH}/auth/logout?userId=${id}`,
    REFRESH_TOKEN: `${SERVICE_URLS.AUTH}/auth/refresh-token`,
    VERIFYTOKEN: (token: string) => `${SERVICE_URLS.AUTH}/auth/verify-otp-token?token=${token}`,
    VERIFYOTP: `${SERVICE_URLS.AUTH}/auth/verify-otp`,
    RESENDOTP: `${SERVICE_URLS.AUTH}/auth/resend-otp`,
    SEND_VERIFY_CODE: (identifier: string, method: 'email' | 'sms') =>`${SERVICE_URLS.AUTH}/auth/send-verify-code?identifier=${identifier}&method=${method}`,
    FORGOT_PASSWORD: `${SERVICE_URLS.AUTH}/auth/forgot-password`,
    RESET_PASSWORD: `${SERVICE_URLS.AUTH}/auth/reset-password`,
    FORGOT_USERNAME: `${SERVICE_URLS.AUTH}/auth/forgot-username`,
  },

  USER: {
    BASE: `${SERVICE_URLS.USER}/user`,
    BY_ID: (id: string | number) => `${SERVICE_URLS.USER}/user/${id}`,
    PROFILE: (id: string | number) => `${SERVICE_URLS.USER}/user/profile/${id}`,
    UPDATE_PASSWORD: `${SERVICE_URLS.USER}/user/settings/updatepassword`,
    PREFERENCES: `${SERVICE_URLS.USER}/user/preferences`,
    SENDACCOUNTSTATEMENT: (id: string | number) => `${SERVICE_URLS.USER}/receipt/generate-pdf/${id}`,
    UPDATE2FASTATUS: () => `${SERVICE_URLS.USER}/settings/enable-twofactor`,
    UPLOAD_PROFILE_IMAGE: (id: string | number) => `${SERVICE_URLS.USER}/settings/upload-profile-image/${id}`,
    REMOVE_PROFILE_IMAGE: (id: string | number) => `${SERVICE_URLS.USER}/settings/remove-profile-image/${id}`,
    DELETE_ACCOUNT: (id: string | number) => `${SERVICE_URLS.USER}/settings/delete-account/${id}`,
    UPLOAD_KYC_DOC: (id: string | number, docType: string) => `${SERVICE_URLS.USER}/user/${id}/kyc/upload?docType=${docType}`,
    GET_KYC_DOC:    (id: string | number, docType: string) => `${SERVICE_URLS.USER}/user/${id}/kyc/document?docType=${docType}`,
    SEND_RESET_LINK: (id: string | number) => `${SERVICE_URLS.USER}/user/${id}/send-reset-link`,
    SUSPEND_ACCOUNT: (id: string | number) => `${SERVICE_URLS.USER}/user/${id}/suspend`,
  },

  ACCOUNTSETTING:{
    BY_ID: (id: string | number) => `${SERVICE_URLS.USER}/settings/user/${id}`,
    UPDATE_NOTIFICATION_SETTINGS: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/notifications`,
    UPDATE_SESSION_TIMEOUT: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/session-timeout`,
    UPDATE_BIOMETRIC_STATUS: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/biometric`,
    UPDATE_PREFERENCES: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/preferences`,
  },

  DEPOSIT: {
    BASE: `${SERVICE_URLS.DEPOSIT}/deposit/initiate`,
    BY_ID: (id: string | number) => `${SERVICE_URLS.DEPOSIT}/deposit/${id}`,
    HISTORY: (userId: string | number) => `${SERVICE_URLS.DEPOSIT}/deposit/history/${userId}`,
    STATUS: (id: string | number) => `${SERVICE_URLS.DEPOSIT}/deposit/${id}/status`,
  },

  PAYMENT: {
    BASE: `${SERVICE_URLS.PAYMENT}/payments`,
    PROCESS: `${SERVICE_URLS.PAYMENT}/payments/process`,
    HISTORY: `${SERVICE_URLS.PAYMENT}/payments/history`,
    STATUS: (id: string | number) => `${SERVICE_URLS.PAYMENT}/payments/${id}/status`,
  },

  WALLET: {
    ID: (id: string | number) => `${SERVICE_URLS.WALLET}/wallet/${id}`,
    BY_USER_ID: (id: string | number) => `${SERVICE_URLS.WALLET}/wallet/userId/${id}`,
    CURRENCY: (id: string | number, currency: string) => `${SERVICE_URLS.WALLET}/wallet/${id}/${currency}`,
    CREATEWITHDRAWPIN: `${SERVICE_URLS.WALLET}/wallet/create/pin`,
    VERIFYPIN: `${SERVICE_URLS.WALLET}/wallet/verify/pin`,
    UPDATE_DEFAULT_CURRENCY: (id: string) => `${SERVICE_URLS.WALLET}/wallet/${id}/default/currency`,
  },

  BANKCOLLECTIONLIST: {
    BASE: `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank`,
    CREATE: `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/create`,
    CREATEVIRTUALCARD: `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/create/virtual-card`,
    GET_BY_ID: (id: string | number) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/details/${id}`,
    GET_BY_ACCOUNT_NUMBER: (accountNumber: string) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/accounts/${accountNumber}`,
    GET_BY_USER_ID: (userId: string | number) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/user/${userId}`,
    DELETE: (id: string | number) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/delete/${id}`,
    GET_BANK_LIST_API: `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/list`,
    VERIFY_USER_BANK_DETAILS: (accountNumber: string, bankCode: string) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/verify-user-bank-details?accountNumber=${accountNumber}&bankCode=${bankCode}`,
    VERIFY: (accountNumber: string, bankCode: string) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/user/bank?accountNumber=${accountNumber}&bankCode=${bankCode}`,
    DEFAULT_HOME: `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/`,
    GET_VIRTUAL_CARD_LIST_BY_USER_ID: (id: string | number) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/${id}/cards`,
  },

  TRANSFER: {
    BANKTRANSFER: `${SERVICE_URLS.WITHDRAW}/withdrawals/bank`,
    PLATFORMWITHDRAWS: `${SERVICE_URLS.WITHDRAW}/withdrawals/user`,
  },

  HISTORY: {
    ALL: (userId: string | number) => `${SERVICE_URLS.HISTORY}/history/user/${userId}`,
    DELETE: (id: string | number) => `${SERVICE_URLS.HISTORY}/history/${id}`,
    FILTERED: (userId: string | number, payload: HistoryFilterPayload) => `${SERVICE_URLS.HISTORY}/history/user/${userId}/filter?fromDate=${payload.startDate}&toDate=${payload.endDate}&transactionType=${payload.transactionType}&currency=${payload.currency}`,
    BENEFICIARY: (beneficiaryId: string | number) => `${SERVICE_URLS.HISTORY}/history/user/${beneficiaryId}`,
  },

  BENEFICIARY: {
    BASE: `${SERVICE_URLS.BENEFICIARY}/beneficiaries`,
    CREATE: `${SERVICE_URLS.BENEFICIARY}/beneficiaries/create`,
    GET_ALL: (userId: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiaries/${userId}/all`,
    GET_BY_ID: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiaries/${id}`,
    UPDATE: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiaries/${id}`,
    DELETE: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiaries/${id}`,
    VERIFY: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiaries/${id}/verify`,
    CHECK_IF_GOT_USER: (id: string | number, recipientUsername: string) => `${SERVICE_URLS.BENEFICIARY}/beneficiaries/${id}/username/${recipientUsername}`,
  },

  VIRTUALCARD: {
    GET_CARDS_BY_USER_ID: (id: string | number) => `${SERVICE_URLS.VIRTUALCARD}/virtual-cards/user/${id}`,
    CREATECARD: `${SERVICE_URLS.VIRTUALCARD}/virtual-cards`,
  },

  INVESTMENT: {
    PLANS: `${BASE_URL}/investments/plans`,
    CALCULATE: `${BASE_URL}/investments/calculate`,
    CREATE: `${BASE_URL}/investments/create`,
    BY_USER: (userId: string | number) => `${BASE_URL}/investments/user/${userId}`,
    BY_ID: (id: string | number, userId: string | number) => `${BASE_URL}/investments/${id}?userId=${userId}`,
  },

  SAVINGS: {
    CREATE: `${BASE_URL}/savings/create`,
    TOPUP: (id: string | number) => `${BASE_URL}/savings/${id}/topup`,
    WITHDRAW: (id: string | number) => `${BASE_URL}/savings/${id}/withdraw`,
    BY_USER: (userId: string | number) => `${BASE_URL}/savings/user/${userId}`,
    BY_ID: (id: string | number, userId: string | number) => `${BASE_URL}/savings/${id}?userId=${userId}`,
    CANCEL: (id: string | number) => `${BASE_URL}/savings/${id}`,
  },
};