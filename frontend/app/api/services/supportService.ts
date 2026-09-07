import { makeAuthenticatedRequest, makePublicRequest } from '../utils';
import { API_URLS } from '../config';

export interface ChatRequestPayload {
  sessionId: string;
  userId?: number | string | null;
  message: string;
}

export interface CreateTicketPayload {
  userId: number | string;
  subject: string;
  description: string;
  category: string;
  priority?: string;
  relatedTransactionId?: string | null;
}

export const supportService = {

  /** Send a message to the AI support bot — no auth required. */
  chat: (payload: ChatRequestPayload) =>
    makePublicRequest(API_URLS.SUPPORT.CHAT, 'POST', payload),

  /** Fetch all active help articles — no auth required. */
  getArticles: () =>
    makePublicRequest(API_URLS.SUPPORT.ARTICLES, 'GET'),

  /** Fetch a single article by slug — no auth required. */
  getArticle: (slug: string) =>
    makePublicRequest(API_URLS.SUPPORT.ARTICLE(slug), 'GET'),

  /** Fetch all FAQs grouped by category — no auth required. */
  getFaqs: () =>
    makePublicRequest(API_URLS.SUPPORT.FAQS, 'GET'),

  /** Create a support ticket — requires USER auth. */
  createTicket: (payload: CreateTicketPayload) =>
    makeAuthenticatedRequest(API_URLS.SUPPORT.CREATE_TICKET, 'POST', payload),

  /** Get paginated tickets for a user — requires USER auth. */
  getUserTickets: (userId: number | string, page = 0, size = 10) =>
    makeAuthenticatedRequest(
      `${API_URLS.SUPPORT.USER_TICKETS(userId)}?page=${page}&size=${size}`,
      'GET',
    ),

  /** Get a single ticket by reference — requires USER auth. */
  getTicket: (reference: string, userId: number | string) =>
    makeAuthenticatedRequest(API_URLS.SUPPORT.TICKET(reference, userId), 'GET'),
};

export default supportService;
