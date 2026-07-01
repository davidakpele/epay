
import xhrClient from './xhrClient';
import authService from './services/authService';
import depositService from './services/depositService';
import userService from './services/userService';
import paymentService from './services/paymentService';
import walletService from './services/walletService';
import bankCollectionService from './services/bankCollectionList';
import withdrawService from './services/withdrawService';
import historyService from './services/historyService';
import configService from './services/configService';
import  beneficiaryService from './services/beneficiaryService';
import virtualCardService from './services/virtualCardService';
import investmentService from './services/investmentService';
import targetSavingsService from './services/targetSavingsService';
export * from './utils';
export * from './config';


export {
  xhrClient,
  authService,
  depositService,
  userService,
  paymentService,
  walletService,
  bankCollectionService,
  withdrawService,
  historyService,
  configService,
  beneficiaryService,
  virtualCardService,
  investmentService,
  targetSavingsService,
};

const api = {
  xhrClient,
  authService,
  depositService,
  userService,
  paymentService,
  walletService,
  bankCollectionService,
  withdrawService,
  historyService,
  configService,
  beneficiaryService,
  virtualCardService,
  investmentService,
  targetSavingsService,
};

export default api;