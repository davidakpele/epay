import { User } from "../types";

export const USERS: User[] = [
  { id: "1", name: "David Alex",     email: "david.alex@email.com",    balance: "₦150,000", kyc: "Verified", status: "Active",    dateRegistered: "Apr 20, 2024", avatar: "DA" },
  { id: "2", name: "Sarah Williams", email: "sarah.williams@email.com", balance: "₦7,500",   kyc: "Pending",  status: "Suspended", dateRegistered: "Apr 18, 2024", avatar: "SW" },
  { id: "3", name: "Ayo Ade",        email: "ayo.ade@email.com",        balance: "₦300,000", kyc: "Verified", status: "Active",    dateRegistered: "Apr 15, 2024", avatar: "AA" },
  { id: "4", name: "Emma Johnson",   email: "emma.j@email.com",         balance: "₦50,000",  kyc: "Pending",  status: "Inactive",  dateRegistered: "Apr 14, 2024", avatar: "EJ" },
  { id: "5", name: "Michael Brown",  email: "michael.b@email.com",      balance: "₦75,000",  kyc: "Verified", status: "Inactive",  dateRegistered: "Apr 10, 2024", avatar: "MB" },
  { id: "6", name: "John Doe",       email: "john.doe@email.com",       balance: "₦10,000",  kyc: "Verified", status: "Active",    dateRegistered: "Apr 8, 2024",  avatar: "JD" },
  { id: "7", name: "Richard Clark",  email: "richard.c@email.com",      balance: "₦210,000", kyc: "Verified", status: "Active",    dateRegistered: "Apr 5, 2024",  avatar: "RC" },
  { id: "8", name: "Jessica Wong",   email: "jessica.w@email.com",      balance: "₦1,200",   kyc: "Verified", status: "Active",    dateRegistered: "Apr 2, 2024",  avatar: "JW" },
  { id: "9", name: "Tunde Bakare",   email: "tunde.b@email.com",        balance: "₦88,000",  kyc: "Verified", status: "Active",    dateRegistered: "Mar 30, 2024", avatar: "TB" },
  { id: "10", name: "Amaka Obi",      email: "amaka.o@email.com",        balance: "₦22,500",  kyc: "Pending",  status: "Active",    dateRegistered: "Mar 28, 2024", avatar: "AO" },
  { id: "11", name: "Chidi Nwosu",    email: "chidi.n@email.com",        balance: "₦500,000", kyc: "Verified", status: "Active",    dateRegistered: "Mar 25, 2024", avatar: "CN" },
  { id: "12", name: "Fatima Bello",   email: "fatima.b@email.com",       balance: "₦9,800",   kyc: "Rejected", status: "Suspended", dateRegistered: "Mar 22, 2024", avatar: "FB" },
  { id: "13", name: "Uche Eze",       email: "uche.e@email.com",         balance: "₦130,000", kyc: "Verified", status: "Active",    dateRegistered: "Mar 20, 2024", avatar: "UE" },
  { id: "14", name: "Grace Adeleke",  email: "grace.a@email.com",        balance: "₦45,000",  kyc: "Pending",  status: "Inactive",  dateRegistered: "Mar 18, 2024", avatar: "GA" },
  { id: "15", name: "Emeka Okafor",   email: "emeka.o@email.com",        balance: "₦670,000", kyc: "Verified", status: "Active",    dateRegistered: "Mar 15, 2024", avatar: "EO" },
];

export const TOTAL_USERS = 324;
export const USERS_PER_PAGE = 8;
export const TOTAL_PAGES = Math.ceil(TOTAL_USERS / USERS_PER_PAGE);