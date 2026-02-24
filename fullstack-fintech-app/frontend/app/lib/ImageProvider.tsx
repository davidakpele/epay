export const BettingProviders = [
    { id: '1xbet',    name: '1xbet',    logo: '/assets/images/onexbet.png' },
    { id: 'bangbet',  name: 'BangBet',  logo: '/assets/images/bangbet.jpeg' },
    { id: 'bet9ja',   name: 'Bet9ja',   logo: '/assets/images/bet9ja.jpeg' },
    { id: 'betking',  name: 'BetKing',  logo: '/assets/images/betking.png' },
    { id: 'betland',  name: 'BetLand',  logo: '/assets/images/betland.jpeg' },
    { id: 'betlion',  name: 'BetLion',  logo: '/assets/images/betlion.png' },
    { id: 'betway',   name: 'Betway',   logo: '/assets/images/betway.jpeg' },
    { id: 'cloudbet', name: 'CloudBet', logo: '/assets/images/cloudbet.jpeg' },
    { id: 'naijabet', name: 'NaijaBet', logo: '/assets/images/naijabet.png' },
    { id: 'nairabet', name: 'NairaBet', logo: '/assets/images/nairabet.png' },
    { id: 'superbet', name: 'SuperBet', logo: '/assets/images/supabet.jpeg' },
]

export const shoppingProviders = [
    { id: 'jumia',      name: 'Jumia',      logo: '/assets/images/jumia.png' },
    { id: 'aliexpress', name: 'AliExpress', logo: '/assets/images/aliexpress.png' },
    { id: 'amazon',     name: 'Amazon',     logo: '/assets/images/amazon.jpg' },
    { id: 'jiji',       name: 'Jiji',       logo: '/assets/images/jiji.jpg' },
    { id: 'ebay',       name: 'eBay',       logo: '/assets/images/ebay.png' },
]

export const BILLS_HERO_IMAGE = {
  src: '/assets/images/bills-hero.png',
  alt: 'Pay Bills Illustration',
};

export const getBillServiceImage = (name: string) => {
  const lowerName = name.toLowerCase();
  if (lowerName.includes('electricity')) return { src: '/assets/images/electricity_bill.png', alt: 'Electricity' };
  if (lowerName.includes('internet') || lowerName.includes('data')) return { src: '/assets/images/wifi.png', alt: 'Data' };
  if (lowerName.includes('tv') || lowerName.includes('cable')) return { src: '/assets/images/cabletv-banner.png', alt: 'Cable TV' };
  if (lowerName.includes('airtime')) return { src: '/assets/images/airtime-banner.png', alt: 'Airtime' };
  if (lowerName.includes('betting')) return { src: '/assets/images/betting-background.png', alt: 'Betting' };
  return { src: '/assets/images/shopping-banner.png', alt: 'Service' };
};