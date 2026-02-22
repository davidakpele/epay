export const filters = [
  { id: 'all', label: 'All Services' },
  { id: 'airtime', label: 'Airtime' },
  { id: 'data', label: 'Data' },
  { id: 'tv', label: 'Cable TV' },
  { id: 'electricity', label: 'Electricity' },
  { id: 'betting', label: 'Betting' },
  { id: 'shopping', label: 'Shopping' },
];

export const services = [
  {
    id: 1,
    name: 'Airtime',
    category: 'airtime',
    color: 'green',
    href: '/services/bills/airtime'       // ← add this
  },
  {
    id: 2,
    name: 'Betting',
    category: 'betting',
    color: 'blue',
    href: '/services/bills/betting'       // ← add this
  },
  {
    id: 3,
    name: 'Cable TV',
    category: 'tv',
    color: 'cyan',
    href: '/services/bills/cabletv'      // ← add this
  },
  {
    id: 4,
    name: 'Data',
    category: 'data',
    color: 'black',
    href: '/services/bills/data'          // ← add this
  },
  {
    id: 5,
    name: 'Shopping',
    category: 'shopping',
    color: 'purple',
    href: '/services/bills/shopping'          // ← add this
  },
  {
    id: 6,
    name: 'Electricity',
    category: 'electricity',
    color: 'green',
    href: '/services/bills/electricity'   // ← add this
  }
];

// Network Providers Data
export const providers = [
  {
    id: 'airtel',
    name: 'Airtel',
    logo: './assets/images/airtel.png'
  },
  {
    id: '9mobile',
    name: '9mobile',
    logo: './assets/images/9mobile.jpg'
  },
  {
    id: 'glo',
    name: 'Glo',
    logo: './assets/images/glo.jpg'
  },
  {
    id: 'mtn',
    name: 'MTN',
    logo: './assets/images/mtn.png'
  },
  {
    id: 'smile',
    name: 'Smile',
    logo: './assets/images/smile.png'
  }
];


