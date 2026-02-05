import { CheckCircle, Clock, CreditCard, FileText, Plus, Send, Settings } from "lucide-react";
import { BalanceCardProps } from "../types/utils";

export const balanceCards: BalanceCardProps[] = [
    {
      title: 'Total Balance',
      amount: '$4,998,406.00',
      subtitle: 'from last month',
      icon: <CreditCard size={24} />,
      isPrimary: true,
      trend: '+2.5%'
    },
    {
      title: 'Available Balance',
      amount: '$4,998,406.00',
      subtitle: 'Ready for transactions',
      icon: <CheckCircle size={24} />,
    },
    {
      title: 'Pending Balance',
      amount: '$1,594.00',
      subtitle: 'Processing transactions',
      icon: <Clock size={24} />,
    }
  ];

export  const accountInfo = {
    accountName: 'Nezer Techy',
    accountType: 'Savings',
    accountNumber: '5001320096',
    swiftCode: 'NEBANK'
  };

export  const cardDetails = {
    balance: '$0.00',
    cardholderName: 'NEZER TECHY',
    expiryDate: '09/28',
    cardNumber: '5122********8111'
  };

export  const quickActions = [
    { id: 1, title: 'Send Money', subtitle: 'Wire Transfer', icon: <Send size={24} />, color: 'primary' },
    { id: 2, title: 'Add Deposit', subtitle: 'Fund account', icon: <Plus size={24} />, color: 'secondary' },
    { id: 3, title: 'Settings', subtitle: 'Account Settings', icon: <Settings size={24} />, color: 'tertiary' },
    { id: 4, title: 'Statements', subtitle: 'Reports', icon: <FileText size={24} />, color: 'quaternary' }
  ];