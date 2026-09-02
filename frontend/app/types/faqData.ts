
//  faqData.ts  –  ePay FAQ dataset

export interface FaqAnswer {
  title: string;
  user: string;
  postedTime: string;
  content: string;
}

export interface FaqChild {
  id: string;
  question: string;
  answer: FaqAnswer;
}

export interface FaqParent {
  id: string;
  category: string;
  children: FaqChild[];
}

export const faqData: FaqParent[] = [
  // ── P1: Verification ──────────────────────────────────────
  {
    id: 'p1',
    category: 'Verification',
    children: [
      {
        id: 'p1-c1',
        question: 'Can I use my NIN instead of BVN for verification please?',
        answer: {
          title: 'Can I use my NIN instead of BVN for verification please?',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'Boss, we understand how much you really want to get your account verified but BVN is the only accepted means of verification. Kindly visit your bank closest to you for your BVN.',
        },
      },
      {
        id: 'p1-c2',
        question:
          'My account shows duplicate on verification tab that is the error message and up till now kyc level is 50%',
        answer: {
          title:
            'My account shows duplicate on verification tab that is the error message and up till now kyc level is 50%',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'Boss, this suggests that you may have previously verified another account using the same BVN you\'re attempting to verify for this account. Please note that you cannot use the same BVN details for two ePay accounts. However, you can escalate this issue to the customer care representative, the issue will be looked into for confirmation. Thank you for your patience.',
        },
      },
    ],
  },

  // ── P2: OTP ───────────────────────────────────────────────
  {
    id: 'p2',
    category: 'OTP',
    children: [
      {
        id: 'p2-c1',
        question: "I'll like to use just google authenticator to receive OTP",
        answer: {
          title: "I'll like to use just google authenticator to receive OTP",
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'To set up Google Authentication for withdrawals, follow these steps:\n\n1. Click on "Menu" from the dashboard.\n2. Select "Settings."\n3. Under settings, choose "Security."\n4. Select "Withdrawal Authentication."\n5. Choose "Google Authentication," the second option on the list.\n6. Copy the setup keys and add them to your Google Authenticator app.',
        },
      },
      {
        id: 'p2-c2',
        question:
          "I'm really disappointed in this app today I have been trying to withdraw my fund from it for the last 2 hours but they won't send code to my gmail",
        answer: {
          title:
            "I'm really disappointed — OTP not being sent to my Gmail",
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            "We sincerely apologise boss. May I confirm if you have checked your spam or promotions folder? I would also like to clarify that sometimes there may be a delay in receiving your OTP due to network issues. Please note that attempting to request the OTP multiple times may not result in receiving it faster. We kindly ask for your patience as the OTP will eventually appear. Alternatively, you can opt for the Google Authenticator option.",
        },
      },
      {
        id: 'p2-c3',
        question: 'I am not getting OTP code.',
        answer: {
          title: 'I am not getting OTP code.',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'Apologies for the delay in getting the OTP. This might be a temporary glitch with OTP delivery. Kindly check your spam folder — it could be there. If it is not, please wait for about 30 minutes before trying again.',
        },
      },
      {
        id: 'p2-c4',
        question: 'Can I get the OTP on my mobile number?',
        answer: {
          title: 'Can I get the OTP on my mobile number?',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content: 'You can only receive your code in your email boss.',
        },
      },
    ],
  },

  // ── P3: Deposit ───────────────────────────────────────────
  {
    id: 'p3',
    category: 'Deposit',
    children: [
      {
        id: 'p3-c1',
        question: 'Hello, I deposited money and it is still pending',
        answer: {
          title: 'Hello, I deposited money and it is still pending',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'Our sincere apologies chief. After conducting our check, we gathered that the session for the transaction has already expired, but rest assured that the money will be reversed in a timely manner as the transaction was not successful.',
        },
      },
      {
        id: 'p3-c2',
        question:
          'I dey try log to my union bank wey I register with but I no fit login and I wan deposit buy BTC, na only my Opay dey available like this',
        answer: {
          title: 'Cannot login to registered bank for deposit',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            "Ah boss, try fix am so you go fit deposit your money buy coin! If you get another bank wey carry your BVN details, you still fit use that one self, cos you no go fit use that Opay boss.",
        },
      },
      {
        id: 'p3-c3',
        question: 'What is the charge fee for depositing in naira?',
        answer: {
          title: 'What is the charge fee for depositing in naira?',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'Chief, for every ₦5,000 naira that you deposit, a transfer fee of ₦100 applies. So depositing ₦20,000 will attract a transfer fee of ₦300.',
        },
      },
      {
        id: 'p3-c4',
        question: 'What is the minimum amount that I can deposit in my ePay wallet?',
        answer: {
          title: 'What is the minimum amount that I can deposit in my ePay wallet?',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content: 'The minimum amount you can deposit is ₦1,000.',
        },
      },
      {
        id: 'p3-c5',
        question: 'Hey chief, I deposited money and it says fraud deposit',
        answer: {
          title: 'I deposited money and it says fraud deposit',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'You must have made the deposit from an unconventional bank. It takes 14 working days for a payment labelled as fraud deposit to be refunded.',
        },
      },
      {
        id: 'p3-c6',
        question: 'I am trying to add Opay to make deposit',
        answer: {
          title: 'I am trying to add Opay to make deposit',
          user: 'Admin',
          postedTime: 'Published 26 days ago',
          content:
            'Oh, deposits can only be made from conventional Nigerian banks.',
        },
      },
    ],
  },


  // ── ADD MORE PARENTS BELOW ─────────────────────────────────
    {
        id: 'p4',
        category: 'Transfers',
        children: [
        {
            id: 'p4-c1',
            question: 'How do I transfer funds?',
            answer: {
            title: 'How do I transfer funds?',
            user: 'Admin',
            postedTime: 'Published today',
            content: 'Go to Wallet > Transfer, enter the recipient details and amount, then confirm with your PIN.',
            },
        },
        ],
    },

    {
        id: 'p5',
        category: 'Sign up',
        children: [
        {
            id: 'p5-c1',
            question: 'How do I sign up?',
            answer: {
            title: 'How do I sign up?',
            user: 'Admin',
            postedTime: 'Published today',
            content: 'Go to the ePay app or website and tap on "Sign Up". Enter your details and follow the instructions.',
            },
        },
        ],
    },
  
    {
        id: 'p6',
        category: 'Refer and Earn',
        children: [
            {
                id: 'p6-c1',
                question: 'How can I refer people on this App? I want my friend to use my referral code',
                answer: {
                    title: 'How can I refer people on this App? I want my friend to use my referral code',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Please note that your username can be used as your referral code. You can also find your referral link when you click on menu on your dashboard, you will find the refer and earn icon. Click on refer and earn to view and copy your referral link.',
                },
            },
            {
                id: 'p6-c2',
                question: 'No 1k referral bonus yet',
                answer: {
                    title: 'No 1k referral bonus yet',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Thank you for the referral boss. Please be aware that you only earn ePay when the people you refer trade at least $100 worth of trade',
                },
            },
        ],
    },

    {
        id: 'p7',
        category: 'Security',
        children: [
            {
                id: 'p7-c1',
                question:"My friend's ePay account was locked he has been trying to log in but still locked when this happens when do you reopen the app",
                answer: {
                    title: "My friend's ePay account was locked he has been trying to log in but still locked when this happens when do you reopen the app",
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: "Thank you for contacting ePay support, boss. Please assist with your friend's username. Your friends account is most likely temporarily restricted due to repeated order creations. Once the restriction time elapses, your friend will be able to gain access into his account",
                },
            },
            {
                id: 'p7-c2',
                question: 'I forgot my dashboard password',
                answer: {
                    title: 'I forgot my dashboard password',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: "To reset your password, click on forgot password, if you cannot see the forgot password option, click on switch account, there you will find the forgot password option.",
                },
            },

            {
                id: 'p7-c3',
                question: 'How can I link my ePay app to authentication app I lost the old authentication app?',
                answer: {
                    title: 'How can I link my ePay app to authentication app I lost the old authentication app?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'To set up Google Authentication for withdrawals, follow these steps: Click on "Menu" from the dashboard. :\n\n1. Click on "Menu" from the dashboard. from the dashboard.\n2. Select "Settings."\n3. Under settings, choose "Security."\n4. Select "Withdrawal Authentication."\n5. Choose "Google Authentication," the second option on the list.\n6. Copy the setup keys and add them to your Google Authenticator app.',
                },
            },
        ],
    },

    {
        id: 'p8',
        category: 'Bill payments',
        children: [
            {
                id: 'p7-c1',
                question: 'I made a payment to my prepaid meter, got debited, but didn’t receive the token code—only purchase notification',
                answer: {
                    title: 'I made a payment to my prepaid meter, got debited, but didn’t receive the token code—only purchase notification',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Apologies for the delay and inconvenience. This will be looked into by the team in charge and the token code will be sent to your mail manually. Thank you for your patience',
                },
            },
            {
                id: 'p7-c2',
                question: 'What are the features available on your bill payments?',
                answer: {
                    title: 'What are the features available on your bill payments?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'ePay allows you to pay for various bills such as data and airtime top-ups, electricity bills, and cable subscriptions.',
                },
            },

            {
                id: 'p7-c3',
                question: 'I have been trying to use the bill payment features but it is not allowing me.',
                answer: {
                    title: 'I have been trying to use the bill payment features but it is not allowing me.',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Kindly note that before you can be allowed to use the bill payment features, your ePay account must be Verified.',
                },
            },
            {
                id: 'p7-c4',
                question: "I recharged my mobile line from my account and it hasn't been delivered to my line yet?",
                answer: {
                    title: "I recharged my mobile line from my account and it hasn't been delivered to my line yet?",
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Sorry about the delay boss. The delay with your bill payment is most likely from your network provider, once the process is completed, your number will be credited',
                },
            },
            {
                id: 'p7-c4',
                question: "Hello please why are all my electrical bill payments showing pending?",
                answer: {
                    title: "Hello please why are all my electrical bill payments showing pending?",
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'We sincerely apologise for the delay, but please be informed that the delay could be as a result of instability in network, we kindly ask you to remain patient as it will be successful soon, or you will get a refund if it fails eventually.',
                },
            },
        ],
    },

    {
        id: 'p9',
        category: 'Virtual card',
        children: [
            {
                id: 'p9-c1',
                question: 'Please my card has been blocked after funding',
                answer: {
                    title: 'Please my card has been blocked after funding',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'We sincerely apologise as the card was blocked because the system already detected multiple failed transaction, but not to worry your fund will be refunded back to your wallet',
                },
            },
            {
                id: 'p9-c2',
                question: 'How can I freeze my card because am suspecting some unauthorized transactions on my card?',
                answer: {
                    title: 'How can I freeze my card because am suspecting some unauthorized transactions on my card?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'TO FREEZE YOUR VIRTUAL CARD\n\n1.On your main dashboard, kindly click on virtual card \n\n2.Click on your type of card type \n\n3.Follow the prompts thereafter',
                },
            },
            {
                id: 'p9-c3',
                question: 'what is my daily limit for card spending?',
                answer: {
                    title: 'what is my daily limit for card spending?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Your daily limit for card spending is $10000 and the good part is that you can spend it at once.',
                },
            },
            {
                id: 'p9-c4',
                question: 'Can I use the $1 that is automatically added to my card?',
                answer: {
                    title: 'Can I use the $1 that is automatically added to my card?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Absolutely Boss, but please note that before you can be eligible to use the fund, you need to atleast fund your card first before the fund can be used.',
                },
            },

            {
                id: 'p9-c5',
                question: 'Hello, I have a problem funding my card, it keeps pending for long?',
                answer: {
                    title: 'Hello, I have a problem funding my card, it keeps pending for long?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'We sincerely apologize for this delay with your card funding. Please be assured that this is a temporary network glitch and it will be rectified. It is either the funding goes through successfully or you get a refund of the amount in your wallet.',
                },
            },

             {
                id: 'p9-c6',
                question: 'Hello, my card was blocked and i have some funds in it, are they gone?',
                answer: {
                    title: 'Hello, my card was blocked and i have some funds in it, are they gone?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'We are sorry to hear that your virtual card has being blocked, kindly note that the balance on the card will be automatically added to your Naira or crypto wallet.',
                },
            },

            {
                id: 'p9-c7',
                question: 'What is the validity period of my card?',
                answer: {
                    title: 'What is the validity period of my card?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Chief, your card is valid for two years from the date of issuance. You can check the expiration date on your card details page in the app.',
                },
            },

            {
                id: 'p9-c8',
                question: 'Hello, I tried adding my card on amazon and I keep getting an error response that my payment card is declined, why is that?',
                answer: {
                    title: 'Hello, I tried adding my card on amazon and I keep getting an error response that my payment card is declined, why is that?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: "Kindly assist us with your username while we look into it, and we strongly recommend that you reach out to the amazon's customer service for further assistance",
                },
            },

            {
                id: 'p9-c9',
                question: 'What is the minimum that I can fund my dollar card with?',
                answer: {
                    title: 'What is the minimum that I can fund my dollar card with?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'The minimum you can fund your card with is $5',
                },
            },

             {
                id: 'p9-c10',
                question: 'How much is the creation card fee for your dollar card?',
                answer: {
                    title: 'How much is the creation card fee for your dollar card?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'The creation card fee for your dollar card is $5.',
                },
            },

             {
                id: 'p9-c11',
                question: 'Can I create two cards at a go?',
                answer: {
                    title: 'Can I create two cards at a go?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Certainly chief, you are at liberty to create as many cards as you want?',
                },
            },

            {
                id: 'p9-c12',
                question: 'Can I create multiple cards for different currencies wallet like USD and EUR?',
                answer: {
                    title: 'Can I create multiple cards for different currencies wallet like USD and EUR?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Yes. You can create multiple cards for different currency wallets. For instance, you can have a card linked to your USD wallet and another card linked to your EUR wallet but Note that you cannot link two cards to the same currency wallet and creation fee applies for each card you create.',
                },
            },

            {
                id: 'p9-c13',
                question: 'Hey, why is my dollar card Blocked?',
                answer: {
                    title: 'Hey, why is my dollar card Blocked?',
                    user: 'Admin',
                    postedTime: 'Published today',
                    content: 'Your dollar card may be blocked for several reasons, such as suspicious activity or non-compliance with our terms of service. One of the reasons is the reason your card was blocked is because you have attempted using your card for multiple failed transactions and immediately the system detects this it auomatically blocks your card.',
                },
            },
        ],
    },
];
