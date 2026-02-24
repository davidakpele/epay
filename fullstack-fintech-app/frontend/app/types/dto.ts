export interface UserNotification {
  type: string;
  description: string;
  date: string;
}

export interface UserWallet {
  currency_code: string;
  balance: number;
  [key: string]: any;
}

export interface UserData {
  token?: string;
  username?: string;
  userId?: string | number;
  email?: string;
  referral_link?: string;
  referral_username?: string;
  twoFactorAuthEnabled?: boolean;
  is_verify?: boolean;
  fullname?: string;
  userFullName?: string;
  firstName?: string;
  lastName?: string;
  country?: string;
  state?: string;
  city?: string;
  dob?: string;
  gender?: string;
  telephone?: string;
  isCompleteProfile?: boolean;
  sessionId?: string;
  active_wallet?: string;
  wallet?: UserWallet[];
  wallet_id?: string | number;
  isPinSet?: boolean | string;
  fiat?: string;
  notifications?: UserNotification[];
  _jwt_?: { jwt: string };
}

export interface StorageData {
  app?: {
    alignment: string;
    color: string;
  };
  user?: UserData;
}
