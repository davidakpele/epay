export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterCredentials extends LoginCredentials {
  name: string;
  confirmPassword: string;
}


export interface Toast {
  id: number;
  message: string;
  type: 'warning' | 'success';
  exiting: boolean;
}

export type Country = {
  name: string;
  code: string;
  abbr: string;
  abbr3?: string; 
};