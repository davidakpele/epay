package com.epay.support.seeder;

import com.epay.domain.support.entity.SupportArticle;
import com.epay.domain.support.entity.SupportFaq;
import com.epay.domain.support.repository.SupportArticleRepository;
import com.epay.domain.support.repository.SupportFaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Runs once on every startup.
 * Seeds articles and FAQs into the database if they do not already exist.
 * Uses slug uniqueness for articles and category presence for FAQs as guards.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class SupportDataSeeder implements ApplicationRunner {

    private final SupportArticleRepository articleRepository;
    private final SupportFaqRepository     faqRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedArticles();
        seedFaqs();
    }

    // ── Articles ──────────────────────────────────────────────────────────────

    private void seedArticles() {
        List<ArticleSeed> seeds = List.of(
            new ArticleSeed("how-to-generate-account-statement", "How to generate account statement",
                "Go to History > Download Statement. Choose your date range and format (PDF or CSV), then tap Download. " +
                "Statements are available for the last 12 months. The PDF is suitable for bank or visa applications.",
                "Generate your account statement as PDF or CSV in seconds.", "Getting Started"),

            new ArticleSeed("how-to-buy-airtime", "How to buy airtime on ePay",
                "Navigate to Services > Airtime. Select your network (MTN, Airtel, Glo, 9mobile), enter the phone number " +
                "and amount, confirm with your Transfer PIN. Airtime is delivered instantly. A transaction receipt is sent " +
                "to your email.",
                "Top up any Nigerian network instantly from your ePay wallet.", "Services"),

            new ArticleSeed("setting-up-transfer-pin", "Setting up your transfer PIN",
                "Go to Settings > Security > Transfer PIN. Enter your account password to verify your identity, then set " +
                "a 4-digit PIN. You will be asked to confirm the PIN. This PIN is required for all outgoing transfers, " +
                "withdrawals, and bill payments. Keep it secret — ePay staff will never ask for your PIN.",
                "Secure all outgoing transactions with a 4-digit Transfer PIN.", "Security"),

            new ArticleSeed("currency-exchange-explained", "Currency exchange explained",
                "ePay supports 10+ currencies. Go to Exchange, select the source and target currency, enter the amount, " +
                "and confirm. Rates are updated in real-time from our liquidity providers. A small spread is applied. " +
                "The converted funds are credited to your target-currency wallet immediately.",
                "Swap between currencies at real-time rates directly in your wallet.", "Wallets"),

            new ArticleSeed("how-to-create-virtual-card", "How to create a virtual card",
                "Go to Cards > Create Card. Select the currency wallet to link (USD or NGN), choose a card name, and " +
                "confirm with your Transfer PIN. A card creation fee is charged from your selected wallet. Each wallet " +
                "can hold one virtual card. The card is ready for online payments instantly.",
                "Create a USD or NGN virtual card for online purchases.", "Cards"),

            new ArticleSeed("how-to-fund-wallet", "How to fund your ePay wallet",
                "Tap Deposit on your dashboard. Choose a funding method — Paystack (card or bank transfer) or direct " +
                "bank transfer to your dedicated account number. Paystack deposits reflect within seconds. " +
                "Bank transfers may take 5–30 minutes depending on your bank.",
                "Fund your wallet via card, Paystack, or direct bank transfer.", "Wallets"),

            new ArticleSeed("how-to-transfer-to-another-user", "How to transfer to another ePay user",
                "Tap Withdraw > ePay User. Enter the recipient's ePay username, verify their name, enter the amount, " +
                "and confirm with your Transfer PIN. Internal transfers are instant and free. You can save the recipient " +
                "as a beneficiary to send again quickly.",
                "Send money to any ePay user instantly and for free.", "Transfers"),

            new ArticleSeed("kyc-verification-guide", "KYC verification guide",
                "KYC (Know Your Customer) is required to increase your transaction limits. Go to Settings > KYC. " +
                "Tier 1 requires a valid government ID (NIN, BVN, or passport). Tier 2 requires a utility bill or " +
                "proof of address. Upload clear photos of your documents. Approval typically takes 1–3 business days.",
                "Complete KYC to unlock higher transaction limits.", "Account"),

            new ArticleSeed("otc-trading-guide", "OTC Trading guide",
                "OTC (Over-the-Counter) trading lets you exchange large amounts directly without affecting the open " +
                "market rate. Minimum OTC trade is typically $10,000 equivalent. Contact our support team via live chat " +
                "to initiate an OTC trade and receive a custom quote.",
                "Trade large amounts at custom rates via OTC.", "Advanced"),

            new ArticleSeed("what-to-do-if-transaction-failed", "What to do if a transaction failed",
                "If a transaction shows 'Failed' but your balance was debited, do not panic — funds are usually " +
                "reversed within 1–24 hours automatically. If the reversal does not arrive, open a support ticket " +
                "with the transaction reference number. Go to History, find the transaction, and tap 'Report Issue'.",
                "Steps to take if a payment or transfer fails.", "Troubleshooting")
        );

        int seeded = 0;
        for (ArticleSeed s : seeds) {
            if (!articleRepository.existsBySlug(s.slug)) {
                articleRepository.save(SupportArticle.builder()
                        .slug(s.slug)
                        .title(s.title)
                        .content(s.content)
                        .summary(s.summary)
                        .category(s.category)
                        .active(true)
                        .build());
                seeded++;
            }
        }
        if (seeded > 0) log.info("[SupportSeeder] ✓ Seeded {} article(s).", seeded);
        else            log.info("[SupportSeeder] Articles already seeded — skipping.");
    }

    // ── FAQs ──────────────────────────────────────────────────────────────────

    private void seedFaqs() {
        // Guard: if any FAQ row exists, skip entirely
        if (faqRepository.count() > 0) {
            log.info("[SupportSeeder] FAQs already seeded — skipping.");
            return;
        }

        List<SupportFaq> faqs = List.of(

            // ── Account & Login ───────────────────────────────────────────────
            faq("Account & Login", 1, "How do I reset my password?",
                "Go to the login page and click 'Forgot Password'. Enter your registered email address and we will " +
                "send you a reset link valid for 15 minutes. If you do not receive the email, check your spam folder " +
                "or contact support."),
            faq("Account & Login", 2, "Why is my account locked?",
                "Accounts are locked after 5 consecutive failed login attempts. Wait 30 minutes for an automatic " +
                "unlock, or contact our support team with your email to expedite the process."),
            faq("Account & Login", 3, "How do I enable two-factor authentication (2FA)?",
                "Go to Settings > Security > Two-Factor Authentication. Scan the QR code with an authenticator app " +
                "(Google Authenticator or Authy) and enter the 6-digit code to confirm. 2FA adds an extra layer of " +
                "protection to your account."),
            faq("Account & Login", 4, "Can I change my username?",
                "Usernames cannot be changed once set, as they are used as your unique ePay identifier for receiving " +
                "transfers. If you have a compelling reason, contact support and we will review your request."),

            // ── Wallets & Balances ────────────────────────────────────────────
            faq("Wallets & Balances", 1, "How do I check my wallet balance?",
                "Your wallet balance is displayed on your Dashboard. For each currency, the balance card shows " +
                "available, pending, and total balance. Tap a wallet card to see a full breakdown and recent " +
                "transactions for that currency."),
            faq("Wallets & Balances", 2, "Why does my balance show as pending?",
                "A pending balance means funds have been received but are undergoing a brief security hold. This " +
                "typically clears within 30 minutes for internal transfers and 1–3 hours for bank deposits. If it " +
                "has been longer than 24 hours, please contact support."),
            faq("Wallets & Balances", 3, "How do I add a new currency wallet?",
                "ePay automatically creates wallets for all supported currencies when you register. If a currency " +
                "wallet is missing, go to Wallets > Add Currency, select the currency, and confirm. No fee is " +
                "charged for creating a currency wallet."),
            faq("Wallets & Balances", 4, "What currencies does ePay support?",
                "ePay currently supports NGN (Nigerian Naira), USD (US Dollar), EUR (Euro), GBP (British Pound), " +
                "CAD (Canadian Dollar), AUD (Australian Dollar), and several more. Check the Exchange page for the " +
                "full up-to-date list."),

            // ── Transfers & Withdrawals ───────────────────────────────────────
            faq("Transfers & Withdrawals", 1, "How long does a bank withdrawal take?",
                "Bank withdrawals are processed within 1–3 business hours during weekdays (8AM–5PM WAT). Requests " +
                "submitted after hours or on weekends are processed the next business day. You will receive an email " +
                "notification once the transfer is complete."),
            faq("Transfers & Withdrawals", 2, "Is there a fee for internal transfers?",
                "No. Transfers between ePay users are completely free regardless of the amount or currency. Bank " +
                "withdrawals may attract a small processing fee depending on your account tier and the amount."),
            faq("Transfers & Withdrawals", 3, "What is the maximum transfer limit?",
                "Transfer limits depend on your KYC tier. Tier 0 (unverified): ₦50,000/day. Tier 1: ₦500,000/day. " +
                "Tier 2: ₦5,000,000/day. Contact support for higher limits for business accounts."),
            faq("Transfers & Withdrawals", 4, "Why was my withdrawal rejected?",
                "Withdrawals can be rejected due to incorrect bank details, an unverified account, insufficient " +
                "balance after fees, or a security hold on your account. Check the rejection reason in your History, " +
                "correct the issue, and retry. Contact support if the problem persists."),

            // ── Cards ─────────────────────────────────────────────────────────
            faq("Cards", 1, "How do I freeze my virtual card?",
                "Go to Cards, select the card, and tap 'Freeze Card'. The card will be blocked immediately for new " +
                "transactions. You can unfreeze it at any time from the same screen. Freezing does not cancel the card."),
            faq("Cards", 2, "My virtual card was declined online. Why?",
                "Common reasons: insufficient card balance, 3D Secure verification failed, the merchant does not " +
                "accept virtual cards, or the card is frozen. Ensure the billing address matches your ePay profile " +
                "and that the CVV entered is correct."),
            faq("Cards", 3, "Can I create multiple virtual cards?",
                "You can have one virtual card per currency wallet. For example, one USD card and one NGN card. " +
                "Creating a second card for the same currency requires terminating the existing one first. A card " +
                "creation fee applies for each new card."),

            // ── KYC & Verification ────────────────────────────────────────────
            faq("KYC & Verification", 1, "What documents do I need for KYC?",
                "Tier 1 KYC requires one of: National ID (NIN), Bank Verification Number (BVN), International " +
                "Passport, or Driver's License. Tier 2 additionally requires a recent utility bill or bank " +
                "statement (within the last 3 months) as proof of address."),
            faq("KYC & Verification", 2, "How long does KYC approval take?",
                "KYC reviews typically take 1–3 business days. You will receive an email notification once your " +
                "documents are approved or if additional information is required. You can check your KYC status " +
                "at any time in Settings > KYC."),
            faq("KYC & Verification", 3, "My KYC was rejected. What should I do?",
                "Rejections are usually due to blurry images, expired documents, or a name mismatch with your " +
                "ePay profile. Review the rejection reason in Settings > KYC, correct the issue, and resubmit. " +
                "Ensure photos are clear, well-lit, and show all four corners of the document."),

            // ── Bills & Services ──────────────────────────────────────────────
            faq("Bills & Services", 1, "How do I pay electricity bills?",
                "Go to Services > Electricity. Select your disco (EKEDC, IKEDC, etc.), enter your meter number, " +
                "select prepaid or postpaid, enter the amount, and confirm with your Transfer PIN. Your token is " +
                "displayed on screen and sent to your email."),
            faq("Bills & Services", 2, "What happens if I pay a wrong meter number?",
                "Payments to wrong meter numbers cannot be reversed as they are processed immediately with the " +
                "electricity provider. Always double-check the meter number before confirming. If you made an " +
                "error, contact the electricity provider directly with your transaction reference."),
            faq("Bills & Services", 3, "Which cable TV providers does ePay support?",
                "ePay supports DSTV, GOtv, and Startimes. Go to Services > Cable TV, select your provider, enter " +
                "your decoder/smartcard number, choose a subscription plan, and confirm payment. Renewal is instant."),

            // ── Security ──────────────────────────────────────────────────────
            faq("Security", 1, "I suspect my account has been compromised. What should I do?",
                "Immediately: (1) Change your password in Settings > Security. (2) Enable 2FA if not already done. " +
                "(3) Review recent transactions in History. (4) Contact support via live chat with subject 'Account " +
                "Compromised' so we can place a temporary hold while investigating."),
            faq("Security", 2, "Does ePay ever ask for my PIN or password?",
                "No. ePay staff will never ask for your Transfer PIN, login password, or OTP. If anyone contacts " +
                "you claiming to be ePay support and asks for these details, it is a scam. Report it immediately " +
                "via support."),
            faq("Security", 3, "How do I change my Transfer PIN?",
                "Go to Settings > Security > Change Transfer PIN. Enter your current PIN, then your new 4-digit " +
                "PIN twice to confirm. If you have forgotten your current PIN, you can reset it by verifying your " +
                "identity with your account password and OTP.")
        );

        faqRepository.saveAll(faqs);
        log.info("[SupportSeeder] ✓ Seeded {} FAQ(s).", faqs.size());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SupportFaq faq(String category, int order, String question, String answer) {
        return SupportFaq.builder()
                .category(category)
                .sortOrder(order)
                .question(question)
                .answer(answer)
                .authorName("ePay Support Team")
                .active(true)
                .build();
    }

    private record ArticleSeed(String slug, String title, String content, String summary, String category) {}
}
