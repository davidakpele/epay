const BASE_URL = 'http://localhost:8292/api';

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
} as const;

export const defaultHeaders: Record<string, string> = {
  'Content-Type': 'application/json',
};

export interface HistoryFilterPayload {
  startDate: string;
  endDate: string;
  transactionType: string;
  currency: string;
}

export const API_URLS = {
  AUTH: {
    LOGIN: `${SERVICE_URLS.AUTH}/auth/login`,
    REGISTER: `${SERVICE_URLS.AUTH}/auth/register`,
    PROFILE: `${SERVICE_URLS.AUTH}/auth/profile`,
    LOGOUT: (sessionId: string) => `${SERVICE_URLS.AUTH}/auth/logout?sessionId=${sessionId}`,
    REFRESH_TOKEN: `${SERVICE_URLS.AUTH}/auth/refresh-token`,
    VERIFYTOKEN: (token: string) => `${SERVICE_URLS.AUTH}/auth/verify-otp-token?token=${token}`,
    VERIFYOTP: `${SERVICE_URLS.AUTH}/auth/verify-otp`,
    RESENDOTP: `${SERVICE_URLS.AUTH}/auth/resend-otp`,
    SEND_VERIFY_CODE: (identifier: string, method: 'email' | 'sms') =>
      `${SERVICE_URLS.AUTH}/auth/send-verify-code?identifier=${identifier}&method=${method}`,
  },

  USER: {
    BASE: `${SERVICE_URLS.USER}/user`,
    BY_ID: (id: string | number) => `${SERVICE_URLS.USER}/user/${id}`,
    PROFILE: (id: string | number) => `${SERVICE_URLS.USER}/user/profile/${id}`,
    UPDATE_PASSWORD: `${SERVICE_URLS.USER}/user/settings/updatepassword`,
    PREFERENCES: `${SERVICE_URLS.USER}/user/preferences`,
    SENDACCOUNTSTATEMENT: (id: string | number) => `${SERVICE_URLS.USER}/receipt/generate-pdf/${id}`,
    UPDATE2FASTATUS: () => `${SERVICE_URLS.USER}/user/settings/enable-twofactor`,
    UPLOAD_PROFILE_IMAGE: (id: string | number) => `${SERVICE_URLS.USER}/settings/upload-profile-image/${id}`,
    REMOVE_PROFILE_IMAGE: (id: string | number) => `${SERVICE_URLS.USER}/settings/remove-profile-image/${id}`,
    DELETE_ACCOUNT: (id: string | number) => `${SERVICE_URLS.USER}/settings/delete-account/${id}`,
  },

  ACCOUNTSETTING:{
    BY_ID: (id: string | number) => `${SERVICE_URLS.USER}/settings/user/${id}`,
    UPDATE_NOTIFICATION_SETTINGS: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/notifications`,
    UPDATE_SESSION_TIMEOUT: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/session-timeout`,
    UPDATE_BIOMETRIC_STATUS: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/biometric`,
    UPDATE_PREFERENCES: (id: string | number) => `${SERVICE_URLS.USER}/settings/${id}/preferences`,
  },

  DEPOSIT: {
    BASE: `${SERVICE_URLS.DEPOSIT}/deposit/create`,
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
    GET_BY_USER_ID: (userId: string | number) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/users/${userId}`,
    DELETE: (id: string | number) => `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/delete/${id}`,
    GET_BANK_LIST_API: `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/bank-list`,
    VERIFY_USER_BANK_DETAILS: (accountNumber: string, bankCode: string) => 
      `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/verify-user-bank-details?accountNumber=${accountNumber}&bankCode=${bankCode}`,
    VERIFY: (accountNumber: string, bankCode: string) => 
      `${SERVICE_URLS.BANKCOLLECTIONLIST}/bank/user/bank?accountNumber=${accountNumber}&bankCode=${bankCode}`,
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
    BASE: `${SERVICE_URLS.BENEFICIARY}/beneficiary`,
    CREATE: `${SERVICE_URLS.BENEFICIARY}/beneficiary/create`,
    GET_ALL: (userId: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiary/all/${userId}`,
    GET_BY_ID: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiary/${id}`,
    UPDATE: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiary/${id}`,
    DELETE: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiary/${id}`,
    VERIFY: (id: string | number) => `${SERVICE_URLS.BENEFICIARY}/beneficiary/${id}/verify`,
  },
};