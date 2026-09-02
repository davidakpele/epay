export interface DSTVOption {
  name: string;
  price: number;
  value: string;
}

export const DSTV_OPTIONS: DSTVOption[] = [
  { name: 'DStv Indian', price: 15645.00, value: 'indian' },
  { name: 'DStv Padi', price: 4620.00, value: 'padi' },
  { name: 'DStv Compact', price: 19950.00, value: 'compact' },
  { name: 'DStv Confam', price: 11550.00, value: 'confam' },
  { name: 'DStv Premium + French', price: 72450.00, value: 'premium-french' },
  { name: 'DStv Premium W/Afr + Asian Bouquet E36', price: 53025.00, value: 'premium-wafr-asian' },
  { name: 'DStv Yanga', price: 6300.00, value: 'yanga' },
  { name: 'DStv Compact Plus', price: 31500.00, value: 'compact-plus' },
  { name: 'DStv Premium', price: 46725.00, value: 'premium' },
];

export interface GOTVOption {
  name: string;
  price: number;
  value: string;
}

export const GOTV_OPTIONS: GOTVOption[] = [
  { name: 'GOtv Supa Plus Bouquet', price: 17640.00, value: 'supa-plus' },
  { name: 'GOtv Jinja', price: 4095.00, value: 'jinja' },
  { name: 'GOtv Jolli', price: 6090.00, value: 'jolli' },
  { name: 'GOtv Supa', price: 11970.00, value: 'supa' },
  { name: 'GOtv Max', price: 8925.00, value: 'max' },
  { name: 'GOtv Smallie-monthly', price: 1995.00, value: 'smallie' },
];

export interface StartimeOption {
  name: string;
  price: number;
  value: string;
}

export const STARTIME_OPTIONS: StartimeOption[] = [
  { name: 'Classic (Dish) - Weekly', price: 2625.00, value: 'classic-dish-weekly' },
  { name: 'Classic (Dish) - Monthly', price: 7770.00, value: 'classic-dish-monthly' },
  { name: 'Classic (Antenna) - Monthly', price: 6300.00, value: 'classic-antenna-monthly' },
  { name: 'Nova (Dish) - Monthly', price: 2205.00, value: 'nova-dish-monthly' },
  { name: 'Startimes Chinese (Dish) - Monthly', price: 22050.00, value: 'chinese-dish-monthly' },
  { name: 'Super (Antenna) - Weekly', price: 3360.00, value: 'super-antenna-weekly' },
  { name: 'Super (Antenna) - Monthly', price: 9975.00, value: 'super-antenna-monthly' },
  { name: 'Basic (Antenna) - Monthly', price: 4200.00, value: 'basic-antenna-monthly' },
  { name: 'Nova (Antenna) - Monthly', price: 2205.00, value: 'nova-antenna-monthly' },
  { name: 'Basic (Dish) - Monthly', price: 5355.00, value: 'basic-dish-monthly' },
  { name: 'Nova (Antenna) - Weekly', price: 735.00, value: 'nova-antenna-weekly' },
  { name: 'Classic (Antenna) - Weekly', price: 2100.00, value: 'classic-antenna-weekly' },
  { name: 'Super (Dish) - Weekly', price: 3465.00, value: 'super-dish-weekly' },
  { name: 'Super (Dish) - Monthly', price: 10290.00, value: 'super-dish-monthly' },
  { name: 'Basic (Dish) - Weekly', price: 1785.00, value: 'basic-dish-weekly' },
  { name: 'Nova (Dish) - Weekly', price: 735.00, value: 'nova-dish-weekly' },
  { name: 'Basic (Antenna) - Weekly', price: 1470.00, value: 'basic-antenna-weekly' },
  { name: 'Global (Dish) - Monthly', price: 22050.00, value: 'global-dish-monthly' },
  { name: 'Global (Dish) - Weekly', price: 7350.00, value: 'global-dish-weekly' },
];


export const CableProviders = [
    {
        id: 'dstv',
        name: 'DSTV',
        logo: '/assets/images/dstv.jpg'
    },
    {
        id: 'Gotv',
        name: 'Gotv',
        logo: '/assets/images/gotv.png'
    },
    {
        id: 'startimes',
        name: 'Start Times',
        logo: '/assets/images/startimes.png'
    },
]