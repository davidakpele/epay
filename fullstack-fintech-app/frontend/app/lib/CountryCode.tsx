// app/lib/CountryCode.ts

export interface CountryCode {
  name: string;
  code: string; 
  dialCode: string;
  flag: string;
  iso2: string;
  iso3?: string;
  minLength?: number;
  maxLength?: number;
  format?: string;
  placeholder?: string;
}

export const countryCodes: CountryCode[] = [
  // Africa
  { name: 'Nigeria', code: '+234', dialCode: '+234', flag: '🇳🇬', iso2: 'NG', minLength: 10, maxLength: 10, placeholder: '802 345 6789' },
  { name: 'Ghana', code: '+233', dialCode: '+233', flag: '🇬🇭', iso2: 'GH', minLength: 9, maxLength: 9, placeholder: '23 456 7890' },
  { name: 'Kenya', code: '+254', dialCode: '+254', flag: '🇰🇪', iso2: 'KE', minLength: 9, maxLength: 9, placeholder: '712 345 678' },
  { name: 'South Africa', code: '+27', dialCode: '+27', flag: '🇿🇦', iso2: 'ZA', minLength: 9, maxLength: 9, placeholder: '82 345 6789' },
  { name: 'Egypt', code: '+20', dialCode: '+20', flag: '🇪🇬', iso2: 'EG', minLength: 10, maxLength: 10, placeholder: '100 234 5678' },
  { name: 'Morocco', code: '+212', dialCode: '+212', flag: '🇲🇦', iso2: 'MA', minLength: 9, maxLength: 9, placeholder: '612 345 678' },
  { name: 'Tanzania', code: '+255', dialCode: '+255', flag: '🇹🇿', iso2: 'TZ', minLength: 9, maxLength: 9, placeholder: '712 345 678' },
  { name: 'Uganda', code: '+256', dialCode: '+256', flag: '🇺🇬', iso2: 'UG', minLength: 9, maxLength: 9, placeholder: '712 345 678' },
  { name: 'Algeria', code: '+213', dialCode: '+213', flag: '🇩🇿', iso2: 'DZ', minLength: 9, maxLength: 9, placeholder: '551 234 567' },
  { name: 'Ethiopia', code: '+251', dialCode: '+251', flag: '🇪🇹', iso2: 'ET', minLength: 9, maxLength: 9, placeholder: '911 234 567' },
  { name: 'Rwanda', code: '+250', dialCode: '+250', flag: '🇷🇼', iso2: 'RW', minLength: 9, maxLength: 9, placeholder: '781 234 567' },
  { name: 'Senegal', code: '+221', dialCode: '+221', flag: '🇸🇳', iso2: 'SN', minLength: 9, maxLength: 9, placeholder: '77 123 4567' },
  { name: 'Cameroon', code: '+237', dialCode: '+237', flag: '🇨🇲', iso2: 'CM', minLength: 9, maxLength: 9, placeholder: '671 234 567' },
  { name: 'Ivory Coast', code: '+225', dialCode: '+225', flag: '🇨🇮', iso2: 'CI', minLength: 8, maxLength: 8, placeholder: '07 123 456' },
  { name: 'Angola', code: '+244', dialCode: '+244', flag: '🇦🇴', iso2: 'AO', minLength: 9, maxLength: 9, placeholder: '923 456 789' },
  { name: 'Mozambique', code: '+258', dialCode: '+258', flag: '🇲🇿', iso2: 'MZ', minLength: 9, maxLength: 9, placeholder: '82 123 4567' },
  { name: 'Zambia', code: '+260', dialCode: '+260', flag: '🇿🇲', iso2: 'ZM', minLength: 9, maxLength: 9, placeholder: '95 123 4567' },
  { name: 'Zimbabwe', code: '+263', dialCode: '+263', flag: '🇿🇼', iso2: 'ZW', minLength: 9, maxLength: 9, placeholder: '71 234 5678' },
  { name: 'Botswana', code: '+267', dialCode: '+267', flag: '🇧🇼', iso2: 'BW', minLength: 8, maxLength: 8, placeholder: '71 234 567' },
  { name: 'Namibia', code: '+264', dialCode: '+264', flag: '🇳🇦', iso2: 'NA', minLength: 9, maxLength: 9, placeholder: '81 234 5678' },
  { name: 'Mauritius', code: '+230', dialCode: '+230', flag: '🇲🇺', iso2: 'MU', minLength: 8, maxLength: 8, placeholder: '5 123 4567' },
  { name: 'Seychelles', code: '+248', dialCode: '+248', flag: '🇸🇨', iso2: 'SC', minLength: 7, maxLength: 7, placeholder: '2 512 345' },
  
  // North America
  { name: 'United States', code: '+1', dialCode: '+1', flag: '🇺🇸', iso2: 'US', minLength: 10, maxLength: 10, placeholder: '(123) 456-7890' },
  { name: 'Canada', code: '+1', dialCode: '+1', flag: '🇨🇦', iso2: 'CA', minLength: 10, maxLength: 10, placeholder: '(123) 456-7890' },
  { name: 'Mexico', code: '+52', dialCode: '+52', flag: '🇲🇽', iso2: 'MX', minLength: 10, maxLength: 10, placeholder: '55 1234 5678' },
  
  // Caribbean (with NANP - same +1 code)
  { name: 'Jamaica', code: '+1-876', dialCode: '+1876', flag: '🇯🇲', iso2: 'JM', minLength: 10, maxLength: 10, placeholder: '876 123 4567' },
  { name: 'Bahamas', code: '+1-242', dialCode: '+1242', flag: '🇧🇸', iso2: 'BS', minLength: 10, maxLength: 10, placeholder: '242 123 4567' },
  { name: 'Barbados', code: '+1-246', dialCode: '+1246', flag: '🇧🇧', iso2: 'BB', minLength: 10, maxLength: 10, placeholder: '246 123 4567' },
  { name: 'Dominican Republic', code: '+1-809', dialCode: '+1809', flag: '🇩🇴', iso2: 'DO', minLength: 10, maxLength: 10, placeholder: '809 123 4567' },
  { name: 'Puerto Rico', code: '+1-787', dialCode: '+1787', flag: '🇵🇷', iso2: 'PR', minLength: 10, maxLength: 10, placeholder: '787 123 4567' },
  { name: 'Trinidad & Tobago', code: '+1-868', dialCode: '+1868', flag: '🇹🇹', iso2: 'TT', minLength: 10, maxLength: 10, placeholder: '868 123 4567' },
  
  // Europe
  { name: 'United Kingdom', code: '+44', dialCode: '+44', flag: '🇬🇧', iso2: 'GB', minLength: 10, maxLength: 10, placeholder: '7911 123456' },
  { name: 'France', code: '+33', dialCode: '+33', flag: '🇫🇷', iso2: 'FR', minLength: 9, maxLength: 9, placeholder: '6 12 34 56 78' },
  { name: 'Germany', code: '+49', dialCode: '+49', flag: '🇩🇪', iso2: 'DE', minLength: 10, maxLength: 11, placeholder: '151 234 5678' },
  { name: 'Italy', code: '+39', dialCode: '+39', flag: '🇮🇹', iso2: 'IT', minLength: 10, maxLength: 10, placeholder: '312 345 6789' },
  { name: 'Spain', code: '+34', dialCode: '+34', flag: '🇪🇸', iso2: 'ES', minLength: 9, maxLength: 9, placeholder: '612 345 678' },
  { name: 'Netherlands', code: '+31', dialCode: '+31', flag: '🇳🇱', iso2: 'NL', minLength: 9, maxLength: 9, placeholder: '6 12345678' },
  { name: 'Sweden', code: '+46', dialCode: '+46', flag: '🇸🇪', iso2: 'SE', minLength: 9, maxLength: 9, placeholder: '70 123 45 67' },
  { name: 'Norway', code: '+47', dialCode: '+47', flag: '🇳🇴', iso2: 'NO', minLength: 8, maxLength: 8, placeholder: '412 34 567' },
  { name: 'Denmark', code: '+45', dialCode: '+45', flag: '🇩🇰', iso2: 'DK', minLength: 8, maxLength: 8, placeholder: '12 34 56 78' },
  { name: 'Finland', code: '+358', dialCode: '+358', flag: '🇫🇮', iso2: 'FI', minLength: 9, maxLength: 10, placeholder: '40 123 4567' },
  { name: 'Switzerland', code: '+41', dialCode: '+41', flag: '🇨🇭', iso2: 'CH', minLength: 9, maxLength: 9, placeholder: '76 123 45 67' },
  { name: 'Belgium', code: '+32', dialCode: '+32', flag: '🇧🇪', iso2: 'BE', minLength: 9, maxLength: 9, placeholder: '471 23 45 67' },
  { name: 'Portugal', code: '+351', dialCode: '+351', flag: '🇵🇹', iso2: 'PT', minLength: 9, maxLength: 9, placeholder: '912 345 678' },
  { name: 'Ireland', code: '+353', dialCode: '+353', flag: '🇮🇪', iso2: 'IE', minLength: 9, maxLength: 9, placeholder: '83 123 4567' },
  { name: 'Greece', code: '+30', dialCode: '+30', flag: '🇬🇷', iso2: 'GR', minLength: 10, maxLength: 10, placeholder: '691 234 5678' },
  { name: 'Poland', code: '+48', dialCode: '+48', flag: '🇵🇱', iso2: 'PL', minLength: 9, maxLength: 9, placeholder: '512 345 678' },
  { name: 'Czech Republic', code: '+420', dialCode: '+420', flag: '🇨🇿', iso2: 'CZ', minLength: 9, maxLength: 9, placeholder: '601 234 567' },
  { name: 'Hungary', code: '+36', dialCode: '+36', flag: '🇭🇺', iso2: 'HU', minLength: 9, maxLength: 9, placeholder: '30 123 4567' },
  { name: 'Austria', code: '+43', dialCode: '+43', flag: '🇦🇹', iso2: 'AT', minLength: 10, maxLength: 10, placeholder: '660 123 4567' },
  { name: 'Russia', code: '+7', dialCode: '+7', flag: '🇷🇺', iso2: 'RU', minLength: 10, maxLength: 10, placeholder: '912 345 67 89' },
  { name: 'Ukraine', code: '+380', dialCode: '+380', flag: '🇺🇦', iso2: 'UA', minLength: 9, maxLength: 9, placeholder: '50 123 45 67' },
  
  // Asia
  { name: 'India', code: '+91', dialCode: '+91', flag: '🇮🇳', iso2: 'IN', minLength: 10, maxLength: 10, placeholder: '98765 43210' },
  { name: 'China', code: '+86', dialCode: '+86', flag: '🇨🇳', iso2: 'CN', minLength: 11, maxLength: 11, placeholder: '139 1234 5678' },
  { name: 'Japan', code: '+81', dialCode: '+81', flag: '🇯🇵', iso2: 'JP', minLength: 10, maxLength: 10, placeholder: '90 1234 5678' },
  { name: 'South Korea', code: '+82', dialCode: '+82', flag: '🇰🇷', iso2: 'KR', minLength: 9, maxLength: 10, placeholder: '10 1234 5678' },
  { name: 'Singapore', code: '+65', dialCode: '+65', flag: '🇸🇬', iso2: 'SG', minLength: 8, maxLength: 8, placeholder: '9123 4567' },
  { name: 'Malaysia', code: '+60', dialCode: '+60', flag: '🇲🇾', iso2: 'MY', minLength: 9, maxLength: 10, placeholder: '12 345 6789' },
  { name: 'Indonesia', code: '+62', dialCode: '+62', flag: '🇮🇩', iso2: 'ID', minLength: 10, maxLength: 12, placeholder: '812 3456 7890' },
  { name: 'Thailand', code: '+66', dialCode: '+66', flag: '🇹🇭', iso2: 'TH', minLength: 9, maxLength: 9, placeholder: '81 234 5678' },
  { name: 'Vietnam', code: '+84', dialCode: '+84', flag: '🇻🇳', iso2: 'VN', minLength: 9, maxLength: 10, placeholder: '91 234 5678' },
  { name: 'Philippines', code: '+63', dialCode: '+63', flag: '🇵🇭', iso2: 'PH', minLength: 10, maxLength: 10, placeholder: '912 345 6789' },
  { name: 'Pakistan', code: '+92', dialCode: '+92', flag: '🇵🇰', iso2: 'PK', minLength: 10, maxLength: 10, placeholder: '300 1234567' },
  { name: 'Bangladesh', code: '+880', dialCode: '+880', flag: '🇧🇩', iso2: 'BD', minLength: 10, maxLength: 10, placeholder: '1712 345678' },
  { name: 'Sri Lanka', code: '+94', dialCode: '+94', flag: '🇱🇰', iso2: 'LK', minLength: 9, maxLength: 9, placeholder: '71 234 5678' },
  { name: 'Nepal', code: '+977', dialCode: '+977', flag: '🇳🇵', iso2: 'NP', minLength: 10, maxLength: 10, placeholder: '984 123 4567' },
  
  // Middle East
  { name: 'UAE', code: '+971', dialCode: '+971', flag: '🇦🇪', iso2: 'AE', minLength: 9, maxLength: 9, placeholder: '50 123 4567' },
  { name: 'Saudi Arabia', code: '+966', dialCode: '+966', flag: '🇸🇦', iso2: 'SA', minLength: 9, maxLength: 9, placeholder: '50 123 4567' },
  { name: 'Qatar', code: '+974', dialCode: '+974', flag: '🇶🇦', iso2: 'QA', minLength: 8, maxLength: 8, placeholder: '3312 3456' },
  { name: 'Kuwait', code: '+965', dialCode: '+965', flag: '🇰🇼', iso2: 'KW', minLength: 8, maxLength: 8, placeholder: '5123 4567' },
  { name: 'Bahrain', code: '+973', dialCode: '+973', flag: '🇧🇭', iso2: 'BH', minLength: 8, maxLength: 8, placeholder: '3600 1234' },
  { name: 'Oman', code: '+968', dialCode: '+968', flag: '🇴🇲', iso2: 'OM', minLength: 8, maxLength: 8, placeholder: '9212 3456' },
  { name: 'Jordan', code: '+962', dialCode: '+962', flag: '🇯🇴', iso2: 'JO', minLength: 9, maxLength: 9, placeholder: '79 123 4567' },
  { name: 'Israel', code: '+972', dialCode: '+972', flag: '🇮🇱', iso2: 'IL', minLength: 9, maxLength: 9, placeholder: '50 123 4567' },
  { name: 'Turkey', code: '+90', dialCode: '+90', flag: '🇹🇷', iso2: 'TR', minLength: 10, maxLength: 10, placeholder: '532 123 4567' },
  
  // Oceania
  { name: 'Australia', code: '+61', dialCode: '+61', flag: '🇦🇺', iso2: 'AU', minLength: 9, maxLength: 9, placeholder: '412 345 678' },
  { name: 'New Zealand', code: '+64', dialCode: '+64', flag: '🇳🇿', iso2: 'NZ', minLength: 9, maxLength: 9, placeholder: '21 123 4567' },
  { name: 'Fiji', code: '+679', dialCode: '+679', flag: '🇫🇯', iso2: 'FJ', minLength: 7, maxLength: 7, placeholder: '701 2345' },
  { name: 'Papua New Guinea', code: '+675', dialCode: '+675', flag: '🇵🇬', iso2: 'PG', minLength: 7, maxLength: 7, placeholder: '7012 3456' },
  
  // South America
  { name: 'Brazil', code: '+55', dialCode: '+55', flag: '🇧🇷', iso2: 'BR', minLength: 10, maxLength: 11, placeholder: '11 91234 5678' },
  { name: 'Argentina', code: '+54', dialCode: '+54', flag: '🇦🇷', iso2: 'AR', minLength: 10, maxLength: 10, placeholder: '11 2345 6789' },
  { name: 'Colombia', code: '+57', dialCode: '+57', flag: '🇨🇴', iso2: 'CO', minLength: 10, maxLength: 10, placeholder: '300 123 4567' },
  { name: 'Chile', code: '+56', dialCode: '+56', flag: '🇨🇱', iso2: 'CL', minLength: 9, maxLength: 9, placeholder: '9 1234 5678' },
  { name: 'Peru', code: '+51', dialCode: '+51', flag: '🇵🇪', iso2: 'PE', minLength: 9, maxLength: 9, placeholder: '987 654 321' },
  { name: 'Venezuela', code: '+58', dialCode: '+58', flag: '🇻🇪', iso2: 'VE', minLength: 10, maxLength: 10, placeholder: '412 123 4567' },
  { name: 'Ecuador', code: '+593', dialCode: '+593', flag: '🇪🇨', iso2: 'EC', minLength: 9, maxLength: 9, placeholder: '98 123 4567' },
  { name: 'Bolivia', code: '+591', dialCode: '+591', flag: '🇧🇴', iso2: 'BO', minLength: 8, maxLength: 8, placeholder: '712 34567' },
  { name: 'Paraguay', code: '+595', dialCode: '+595', flag: '🇵🇾', iso2: 'PY', minLength: 9, maxLength: 9, placeholder: '981 123 456' },
  { name: 'Uruguay', code: '+598', dialCode: '+598', flag: '🇺🇾', iso2: 'UY', minLength: 8, maxLength: 8, placeholder: '91 234 567' },
];

export const getPhonePlaceholder = (countryCode: CountryCode): string => {
  return countryCode.placeholder || 'Enter phone number';
};

export const isValidPhoneForCountry = (phone: string, countryCode: CountryCode): boolean => {
  const digitsOnly = phone.replace(/\D/g, '');
  const length = digitsOnly.length;
  
  if (countryCode.minLength && length < countryCode.minLength) return false;
  if (countryCode.maxLength && length > countryCode.maxLength) return false;
  return true;
};

export const getMaxPhoneLength = (countryCode: CountryCode): number => {
  return countryCode.maxLength || 15;
};

export const formatPhoneNumberByCountry = (value: string, countryCode: CountryCode): string => {
  const digits = value.replace(/\D/g, '');

  if (countryCode.iso2 === 'US' || countryCode.iso2 === 'CA') {
    if (digits.length <= 3) return digits;
    if (digits.length <= 6) return `(${digits.slice(0,3)}) ${digits.slice(3)}`;
    return `(${digits.slice(0,3)}) ${digits.slice(3,6)}-${digits.slice(6,10)}`;
  }
  
  return digits;
};