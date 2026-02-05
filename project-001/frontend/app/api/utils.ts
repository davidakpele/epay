import xhrClient from './xhrClient';
import { defaultHeaders } from './config';
import { StorageData, UserData, UserNotification, UserWallet } from '../types/dto';

// --- Auth Helpers ---

export const getAuthToken = (): string | null => {
  return localStorage.getItem('authToken');
};

export const getToken = (): string | null => {
  const userToken = localStorage.getItem('data');
  if (!userToken) return null;
  try {
    const appData: StorageData = JSON.parse(userToken);
    return appData.user?._jwt_?.jwt || null;
  } catch {
    return null;
  }
};

export const getAuthHeader = (): Record<string, string> => {
  const token = getToken();
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

export const setAuthToken = (payload: Partial<UserData> & { token?: string } = {}) => {
  const {
    token = "",
    username = "",
    userId = "",
    email = "",
    referral_link = "",
    referral_username = "",
    twoFactorAuthEnabled = false,
    is_verify = false,
    fullname = "",
    country = "",
    state = "",
    city = "",
    dob = "",
    gender = "",
    telephone = "",
    isCompleteProfile = false,
    sessionId
  } = payload;

  const nameParts = fullname.trim().split(" ");
  const firstName = nameParts[0] || "";
  const lastName = nameParts.length > 1 ? nameParts.slice(1).join(" ") : "";

  const data: StorageData = {
    app: {
      alignment: "right",
      color: "#000"
    },
    user: {
      hasConversations: false,
      locale: window.location.href,
      referral_link,
      referral_username,
      is_verify,
      twoFactorAuthEnabled,
      username,
      userId,
      email,
      firstName,
      lastName,
      telephone,
      country,
      state,
      city,
      gender,
      dob,
      isCompleteProfile,
      sessionId,
      userFullName: fullname,
      _jwt_: { jwt: token }
    } as any 
  };

  const secret_data = JSON.stringify(data);
  localStorage.setItem("data", secret_data);
  sessionStorage.setItem("data", secret_data);

  document.cookie = `data=${encodeURIComponent(secret_data)}; path=/; secure; samesite=None`;
  document.cookie = `jwt=${token}; path=/; secure; samesite=None`;

  window.location.replace("/dashboard");
};

export const removeAuthToken = () => {
  localStorage.removeItem('data');
  sessionStorage.removeItem('data');
  document.cookie = `data=; path=/; secure; samesite=None; expires=Thu, 01 Jan 1970 00:00:00 GMT`;
  document.cookie = `jwt=; path=/; secure; samesite=None; expires=Thu, 01 Jan 1970 00:00:00 GMT`;
  window.location.replace('/');
};

// --- Getters ---

const getFromStorage = <K extends keyof UserData>(key: K): UserData[K] | null => {
  try {
    const data = localStorage.getItem('data');
    if (!data) return null;
    const appData: StorageData = JSON.parse(data);
    return appData.user ? appData.user[key] : null;
  } catch {
    return null;
  }
};

export const getUsername = () => getFromStorage('username');
export const getSessionId = () => getFromStorage('sessionId');
export const getUserFullName = () => getFromStorage('userFullName');
export const getUserEmail = () => getFromStorage('email');
export const getUserId = () => getFromStorage('userId');
export const getUserIsVerified = () => getFromStorage('is_verify');
export const getActiveWallet = () => getFromStorage('active_wallet');
export const getFiat = () => getFromStorage('fiat');
export const getUserWalletId = () => getFromStorage('wallet_id');
export const getUserIsSetTransfer = () => getFromStorage('isPinSet');
export const getReferenceLink = () => getFromStorage('referral_link');
export const getReferenceUsername = () => getFromStorage('referral_username');
export const getNotificationContainer = () => getFromStorage('notifications');
export const getWalletList = () => getFromStorage('wallet');

export const getWallet = (currency: string): UserWallet | null => {
  const wallets = getWalletList();
  return wallets?.find((w) => w.currency_code === currency) || null;
};

// --- Setters / Updaters ---

const updateStorage = (updater: (user: UserData) => void) => {
  let existingData: StorageData = {};
  try {
    const storedData = localStorage.getItem('data');
    if (storedData) existingData = JSON.parse(storedData);
  } catch (e) {
    console.error("Failed to parse data", e);
  }

  if (!existingData.user) existingData.user = {};
  updater(existingData.user);

  const updatedData = JSON.stringify(existingData);
  localStorage.setItem('data', updatedData);
  sessionStorage.setItem('data', updatedData);
  document.cookie = `data=${encodeURIComponent(updatedData)}; path=/; secure; samesite=None`;
};

export const setActiveWallet = (currency: string) => {
  updateStorage((user) => { user.active_wallet = currency; });
};

export const setWalletContainer = (wallet: UserWallet[], isPinSet: boolean, id: string | number) => {
  updateStorage((user) => {
    user.wallet = wallet;
    user.wallet_id = id;
    user.isPinSet = isPinSet;
  });
};

export const setFiat = (currency: string) => {
  updateStorage((user) => { user.fiat = currency; });
};

export const updateProfileDetails = (
  firstName: string,
  lastName: string,
  gender: string,
  telephone: string,
  dob: string,
  email: string,
  isCompleteProfile: boolean
) => {
  updateStorage((user) => {
    user.email = email;
    user.firstName = firstName;
    user.lastName = lastName;
    user.gender = gender;
    user.telephone = telephone;
    user.dob = dob;
    user.isCompleteProfile = isCompleteProfile;
    user.userFullName = `${firstName} ${lastName}`;
  });
};

export const updateCompleteProfileDetails = (
  firstName: string,
  lastName: string,
  gender: string,
  telephone: string,
  dob: string,
  email: string,
  country: string,
  state: string,
  city: string,
  isCompleteProfile: boolean
) => {
  updateStorage((user) => {
    user.email = email;
    user.firstName = firstName;
    user.lastName = lastName;
    user.gender = gender;
    user.telephone = telephone;
    user.dob = dob;
    user.isCompleteProfile = isCompleteProfile;
    user.userFullName = `${firstName} ${lastName}`;
    user.country = country || '';
    user.state = state || '';
    user.city = city || '';
  });
};

export const updateNotificationContainer = (notificationData: Omit<UserNotification, 'date'> & { date?: string }) => {
  updateStorage((user) => {
    if (!user.notifications) user.notifications = [];
    user.notifications.push({
      type: notificationData.type,
      description: notificationData.description,
      date: notificationData.date || new Date().toISOString()
    });
    if (user.notifications.length > 50) {
      user.notifications = user.notifications.slice(-50);
    }
  });
};

export const setUserTransferPin = (pin: boolean | string) => {
  updateStorage((user) => { user.isPinSet = pin; });
};

export const clearAllNotifications = () => {
  updateStorage((user) => { user.notifications = []; });
};

// --- Utilities ---

export const uuidv4 = (): string => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0;
    const v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
};

  export const makeAuthenticatedRequest = <T = any>(
    url: string, 
    method: string, 
    data: any = null, 
    customHeaders: Record<string, string> = {}
  ) => {
    const headers: Record<string, string> = { 
      ...getAuthHeader(), 
      ...customHeaders 
    };
    
    if (!(data instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }
    
    return xhrClient<T>(url, method, headers, data);
  };


export const makePublicRequest = <T = any>(
  url: string, 
  method: string, 
  data: any = null, 
  customHeaders: Record<string, string> = {}
) => {
  const headers: Record<string, string> = { 
    ...customHeaders 
  };
  
  if (!(data instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }
  
  return xhrClient<T>(url, method, headers, data);
};

export const formatAmount = (amount: number | string | null | undefined): string => {
  if (amount === null || amount === undefined) return "0.00";
  const amountStr = amount.toString().replace(/,/g, '');
  const number = parseFloat(amountStr);
  if (isNaN(number)) return "0.00";
  return number.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
};

export const getUserDetails = (): UserData | null => {
  try {
    const userData = localStorage.getItem("data");
    if (!userData) return null;
    const appData: StorageData = JSON.parse(userData);
    return appData.user || null;
  } catch {
    return null;
  }
};

export const capitalizeFirstLetter = (str: string): string => {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1);
};

export const formatDateToDDMMYYYY = (dateString: string): string => {
  if (!dateString) return '';
  const parts = dateString.split('-');
  if (parts.length !== 3) return dateString;
  const [year, month, day] = parts;
  return `${day}/${month}/${year}`;
};

export const calculateProfileCompletion = (): number => {
  const user = getUserDetails();
  if (!user) return 0;

  const fields: { key: keyof UserData; weight: number }[] = [
    { key: 'firstName', weight: 1 },
    { key: 'lastName', weight: 1 },
    { key: 'email', weight: 1 },
    { key: 'gender', weight: 1 },
    { key: 'telephone', weight: 1 },
    { key: 'dob', weight: 1 },
    { key: 'country', weight: 1 },
    { key: 'state', weight: 0.5 },
    { key: 'city', weight: 0.5 },
    { key: 'userId', weight: 1 },
    { key: 'username', weight: 1 }
  ];

  let totalWeight = 0;
  let completedWeight = 0;

  fields.forEach(field => {
    totalWeight += field.weight;
    const value = user[field.key];
    if (value !== null && value !== undefined && value !== '') {
      completedWeight += field.weight;
    }
  });

  const percentage = totalWeight > 0 ? Math.round((completedWeight / totalWeight) * 100) : 0;
  return Math.min(100, Math.max(0, percentage));
};

// Update user profile image in storage
export const updateProfileImageInStorage = (imageUrl: string) => {
  let existingData: any = {};
  try {
    const storedData = localStorage.getItem('data');
    if (storedData) existingData = JSON.parse(storedData);
  } catch (e) {
    console.error("Failed to parse data", e);
  }
  
  // Update the photo field in user records
  if (!existingData.user) existingData.user = {};
  if (!existingData.user.records) existingData.user.records = [];
  if (existingData.user) {
    existingData.user.photo = imageUrl;
  }
  
  const updatedData = JSON.stringify(existingData);
  localStorage.setItem('data', updatedData);
  sessionStorage.setItem('data', updatedData);
  document.cookie = `data=${encodeURIComponent(updatedData)}; path=/; secure; samesite=None`;
};
