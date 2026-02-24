export interface LoginResponse {
  success: boolean;
  message: string;
  token?: string;
  userId: string;
  username: string;
  email: string;
  emreferral_linkil: string;
  referral_username: string;
  twoFactorAuthEnabled: boolean;
  is_verify: boolean;
  fullname: string;
  lastname: string;
  country: string;
  state: string;
  city: string;
  dob: string;
  gender: string;
  telephone: string;
  isCompleteProfile: boolean;
  sessionId: string;
}

export interface ApiError {
  error: string;
  message: string;
  details: string;
}

