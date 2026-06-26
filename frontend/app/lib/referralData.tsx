import { ReferredUser } from "../types/utils";

export const referralData: ReferredUser[] = [
    {
      id: '1',
      username: 'john_doe',
      joinedDate: '2024-01-15',
      totalEarningsFromUser: 1500.50,
      transactions: [
        { id: 'tx1', type: 'Airtime', amount: 2000, commission: 50, date: '2024-01-16' },
        { id: 'tx2', type: 'Data', amount: 5000, commission: 150, date: '2024-01-20' },
      ]
    },
    {
      id: '2',
      username: 'sarah_smith',
      joinedDate: '2024-02-10',
      totalEarningsFromUser: 850.00,
      transactions: [
        { id: 'tx3', type: 'Cable TV', amount: 12000, commission: 300, date: '2024-02-12' },
      ]
    }
  ];