import { makeAuthenticatedRequest } from '../utils.ts';
import { API_URLS } from '../config.ts';

export const virtualCardService = {

  fetchUserVirtualCardsByUserId: (id) => {
    return makeAuthenticatedRequest(API_URLS.VIRTUALCARD.GET_CARDS_BY_USER_ID(id), 'GET');
  },

  createCard: (payload) => {
    return makeAuthenticatedRequest(API_URLS.VIRTUALCARD.CREATECARD, 'POST', payload);
  },

};

export default virtualCardService;