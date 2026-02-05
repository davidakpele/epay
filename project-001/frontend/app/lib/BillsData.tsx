import { Service } from "../types/utils";
import { 
  Smartphone, 
  Wifi, 
  Tv, 
  Lightbulb, 
  CreditCard, 
  Hospital, 
  Trophy, 
  RefreshCw, 
  Plane, 
  ShoppingBag,
  Search
} from 'lucide-react';


export const filters = [
  { id: 'all', label: 'All Services' },
  { id: 'airtime', label: 'Airtime' },
  { id: 'data', label: 'Data' },
  { id: 'tv', label: 'Cable TV' },
  { id: 'electricity', label: 'Electricity' },
  { id: 'betting', label: 'Betting' },
  { id: 'epin', label: 'E-Pin' },
];

export const services = [
  {
    id: 1,
    name: 'Airtime',
    category: 'airtime',
    color: 'green'
  },
  {
    id: 2,
    name: 'Betting',
    category: 'betting',
    color: 'blue'
  },
  {
    id: 3,
    name: 'Cable TV',
    category: 'tv',
    color: 'cyan'
  },
  {
    id: 4,
    name: 'Data',
    category: 'data',
    color: 'black'
  },
  {
    id: 5,
    name: 'EPin',
    category: 'epin',
    color: 'purple'
  },
  {
    id: 6,
    name: 'Electricity',
    category: 'electricity',
    color: 'orange'
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