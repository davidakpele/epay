import { ContactMethod, FAQCategory, QuickLink } from "../types/utils";
import {
 FileText, AlertCircle,
  HelpCircle, Zap, Shield, CreditCard, Smartphone,
  Phone,
  Mail,
  MessageCircle
} from 'lucide-react';
  // FAQs data
export const faqs: FAQCategory[] = [
    {
      category: 'Account',
      icon: <Smartphone />,
      questions: [
        {
          q: 'How do I reset my password?',
          a: 'Go to Settings > Security > Change Password. Enter your current password and choose a new one. You\'ll receive a confirmation email once updated.'
        },
        {
          q: 'Can I have multiple accounts?',
          a: 'Yes, you can create multiple accounts with different email addresses. However, each account must be verified separately.'
        },
        {
          q: 'How do I verify my account?',
          a: 'Complete your KYC verification by uploading a valid ID and selfie. The process takes 24-48 hours. You\'ll receive an email once approved.'
        }
      ]
    },
    {
      category: 'Payments',
      icon: <CreditCard />,
      questions: [
        {
          q: 'Why was my payment declined?',
          a: 'Payments can be declined due to insufficient funds, incorrect card details, or security restrictions. Check your payment method and try again.'
        },
        {
          q: 'How long do transfers take?',
          a: 'Internal transfers are instant. Bank transfers take 1-3 business days. International transfers may take up to 5 business days.'
        },
        {
          q: 'What are the transaction limits?',
          a: 'Daily limits vary by account level: Basic ($1,000), Verified ($10,000), Premium ($50,000). Contact support to increase limits.'
        }
      ]
    },
    {
      category: 'Security',
      icon: <Shield />,
      questions: [
        {
          q: 'How do I enable two-factor authentication?',
          a: 'Go to Settings > Security > Two-Factor Authentication. Scan the QR code with your authenticator app and enter the verification code.'
        },
        {
          q: 'What should I do if my account is compromised?',
          a: 'Immediately contact support via live chat or call our emergency hotline. We\'ll freeze your account and help secure it.'
        },
        {
          q: 'Are my transactions encrypted?',
          a: 'Yes, all transactions use bank-level 256-bit SSL encryption. Your data is stored in secure, encrypted databases.'
        }
      ]
    }
];

// Quick links data
export const quickLinks: QuickLink[] = [
    { icon: <Zap />, label: 'Getting Started Guide', href: '#' },
    { icon: <CreditCard />, label: 'Payment Methods', href: '#' },
    { icon: <Shield />, label: 'Security Guidelines', href: '#' },
    { icon: <FileText />, label: 'Terms of Service', href: '#' },
    { icon: <HelpCircle />, label: 'Privacy Policy', href: '#' },
    { icon: <AlertCircle />, label: 'Report an Issue', href: '#' }
];

export const contactMethods: ContactMethod[] = [
    {
      icon: <MessageCircle size={28} />,
      title: 'Live Chat',
      description: 'Chat with our support team',
      availability: 'Available 24/7',
      action: 'Start Chat',
      color: '#10b981'
    },
    {
      icon: <Mail size={28} />,
      title: 'Email Support',
      description: 'Send us an email',
      availability: 'Response within 24 hours',
      action: 'Send Email',
      color: '#3b82f6'
    },
    {
      icon: <Phone size={28} />,
      title: 'Phone Support',
      description: 'Call our support line',
      availability: 'Mon-Fri, 9AM-6PM',
      action: 'Call Now',
      color: '#8b5cf6'
    },
    {
      icon: <FileText size={28} />,
      title: 'Help Center',
      description: 'Browse our documentation',
      availability: 'Self-service resources',
      action: 'Visit Help Center',
      color: '#f59e0b'
    }
];
