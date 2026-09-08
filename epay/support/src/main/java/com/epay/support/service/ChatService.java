package com.epay.support.service;

import com.epay.domain.support.dto.ChatResponseDTO;
import com.epay.domain.support.entity.ChatMessage;
import com.epay.domain.support.entity.SupportArticle;
import com.epay.domain.support.entity.SupportFaq;
import com.epay.domain.support.input.ChatRequest;
import com.epay.domain.support.repository.ChatMessageRepository;
import com.epay.domain.support.repository.SupportArticleRepository;
import com.epay.domain.support.repository.SupportFaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository    chatMessageRepository;
    private final SupportArticleRepository articleRepository;
    private final SupportFaqRepository     faqRepository;


    private static final List<String> GREETINGS =
            List.of("hi", "hello", "hey", "good morning", "good afternoon",
                    "good evening", "howdy", "what's up", "sup");

    private static final String GREETING_REPLY =
            "Hi there! I'm ePay's support assistant. I can help you with wallets, " +
            "transfers, cards, KYC, bills, investments and more. What can I help you with today?";

    private static final List<String> THANKS_WORDS =
            List.of("thank", "thanks", "thank you", "thx", "cheers");

    private static final String THANKS_REPLY =
            "You're welcome! Is there anything else I can help you with? " +
            "If you need further assistance, feel free to open a support ticket.";

    private static final List<String> GOODBYE_WORDS =
            List.of("bye", "goodbye", "see you", "cya", "later", "ok bye");

    private static final String GOODBYE_REPLY =
            "Thanks for reaching out! Have a great day. " +
            "If you ever need help again, I'm always here.";

    @Transactional
    public ChatResponseDTO chat(ChatRequest req) {
        String sessionId = req.getSessionId().trim();
        String userText  = req.getMessage().trim();
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .userId(req.getUserId())
                .role("user")
                .content(userText)
                .build());

        String reply = generateReply(userText, sessionId);
        chatMessageRepository.save(ChatMessage.builder()
                .sessionId(sessionId)
                .userId(req.getUserId())
                .role("assistant")
                .content(reply)
                .build());

        log.info("[Chat] session={} userId={} query=\"{}\"",
                sessionId, req.getUserId(),
                userText.length() > 60 ? userText.substring(0, 60) + "…" : userText);

        return ChatResponseDTO.builder()
                .sessionId(sessionId)
                .role("assistant")
                .reply(reply)
                .build();
    }

    private String generateReply(String userMessage, String sessionId) {
        String normalised = userMessage.toLowerCase().trim();
        if (isChitChat(normalised, GREETINGS))  return GREETING_REPLY;
        if (isChitChat(normalised, THANKS_WORDS)) return THANKS_REPLY;
        if (isChitChat(normalised, GOODBYE_WORDS)) return GOODBYE_REPLY;

        Set<String> queryTokens = tokenise(normalised);
        if (queryTokens.isEmpty()) return noAnswerReply();
        List<SupportFaq> faqs = faqRepository.findByActiveTrueOrderByCategoryAscSortOrderAsc();
        ScoredFaq bestFaq = faqs.stream()
                .map(f -> new ScoredFaq(f, scoreFaq(f, queryTokens, normalised)))
                .filter(s -> s.score > 0)
                .max(Comparator.comparingDouble(s -> s.score))
                .orElse(null);

        List<SupportArticle> articles = articleRepository.findByActiveTrueOrderByCreatedAtDesc();
        ScoredArticle bestArticle = articles.stream()
                .map(a -> new ScoredArticle(a, scoreArticle(a, queryTokens, normalised)))
                .filter(s -> s.score > 0)
                .max(Comparator.comparingDouble(s -> s.score))
                .orElse(null);

        double faqScore     = bestFaq     != null ? bestFaq.score     : 0;
        double articleScore = bestArticle != null ? bestArticle.score : 0;

        if (faqScore == 0 && articleScore == 0) {
            return noAnswerReply();
        }

        if (faqScore >= articleScore) {
            return formatFaqReply(bestFaq.faq);
        } else {
            return formatArticleReply(bestArticle.article);
        }
    }

    private double scoreFaq(SupportFaq faq, Set<String> queryTokens, String rawQuery) {
        double score = 0;

        Set<String> questionTokens = tokenise(faq.getQuestion().toLowerCase());
        Set<String> categoryTokens = tokenise(faq.getCategory().toLowerCase());
        Set<String> answerTokens   = tokenise(faq.getAnswer().toLowerCase());

        score += intersection(queryTokens, questionTokens) * 3.0;
        score += intersection(queryTokens, categoryTokens) * 1.0;
        score += intersection(queryTokens, answerTokens)   * 1.0;

        if (faq.getQuestion().toLowerCase().contains(rawQuery) ||
            rawQuery.contains(faq.getQuestion().toLowerCase().substring(0,
                Math.min(15, faq.getQuestion().length())))) {
            score += 5;
        }

        return score;
    }

    private double scoreArticle(SupportArticle art, Set<String> queryTokens, String rawQuery) {
        double score = 0;

        Set<String> titleTokens   = tokenise(art.getTitle().toLowerCase());
        Set<String> contentTokens = tokenise(art.getContent().toLowerCase());
        Set<String> catTokens     = tokenise(art.getCategory().toLowerCase());

        score += intersection(queryTokens, titleTokens)   * 4.0;
        score += intersection(queryTokens, contentTokens) * 1.0;
        score += intersection(queryTokens, catTokens)     * 1.0;

        if (art.getSummary() != null) {
            Set<String> sumTokens = tokenise(art.getSummary().toLowerCase());
            score += intersection(queryTokens, sumTokens) * 2.0;
        }

        return score;
    }

    private String formatFaqReply(SupportFaq faq) {
        return faq.getAnswer() +
               "\n\nIf this didn't fully answer your question, feel free to ask again " +
               "or open a support ticket and our team will assist you.";
    }

    private String formatArticleReply(SupportArticle article) {
        String body = article.getContent();
        if (body.length() > 600) {
            body = body.substring(0, 597) + "…";
        }
        return "**" + article.getTitle() + "**\n\n" + body +
               "\n\nFor the full article, tap the Articles tab. " +
               "Still have questions? Open a support ticket and we'll help you out.";
    }

    private String noAnswerReply() {
        return "I don't have a specific answer for that in my knowledge base yet. " +
               "Here are a few things you can do:\n" +
               "• Browse the FAQs tab for common questions\n" +
               "• Check the Articles tab for step-by-step guides\n" +
               "• Open a support ticket and our team will get back to you within 1 business day";
    }


    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "it", "in", "on", "at", "to", "do",
            "for", "of", "and", "or", "but", "not", "my", "i", "me",
            "we", "you", "how", "what", "when", "where", "why", "can",
            "will", "be", "been", "was", "are", "this", "that", "with",
            "have", "has", "had", "if", "so", "up", "by", "as", "from"
    );

    private Set<String> tokenise(String text) {
        return Arrays.stream(text.split("[^a-z0-9]+"))
                .filter(t -> t.length() > 2 && !STOP_WORDS.contains(t))
                .collect(Collectors.toSet());
    }

    private long intersection(Set<String> a, Set<String> b) {
        return a.stream().filter(b::contains).count();
    }

    private boolean isChitChat(String normalised, List<String> phrases) {
        for (String phrase : phrases) {
            if (normalised.equals(phrase) || normalised.startsWith(phrase + " ") ||
                normalised.endsWith(" " + phrase)) {
                return true;
            }
        }
        return false;
    }

    private record ScoredFaq(SupportFaq faq, double score) {}
    private record ScoredArticle(SupportArticle article, double score) {}
}
