epay-server/                                     ← Root (Maven multi-module)
│
├── domain/                                      ← Pure Java — no Spring beans, shared everywhere
│   └── src/main/java/.../domain/
│       ├── common/
│       │   ├── response/                        ← ApiResponse, PageResponse, ErrorResponse
│       │   └── exception/                       ← DomainException, InsufficientFundsException...
│       ├── auth/
│       │   ├── dto/                             ← UserDTO, TenantDTO, JwtResponse
│       │   ├── input/                           ← LoginRequest, RegisterRequest, ForgotPasswordRequest
│       │   └── enums/                           ← Role, UserStatus, AccountStatus
│       ├── wallet/
│       │   ├── dto/                             ← WalletDTO, BalanceDTO, CurrencyBalanceDTO
│       │   ├── input/                           ← TransferRequest, CreatePinRequest, SwapRequest
│       │   └── enums/                           ← TransactionType, TransactionStatus
│       ├── deposit/
│       │   ├── dto/                             ← DepositDTO, DepositReceiptDTO
│       │   ├── input/                           ← InitiateDepositRequest, VerifyDepositRequest
│       │   └── enums/                           ← DepositStatus, DepositChannel
│       ├── withdraw/
│       │   ├── dto/                             ← WithdrawalDTO, BankTransferDTO
│       │   ├── input/                           ← WithdrawRequest, BankTransferRequest
│       │   └── enums/                           ← WithdrawalStatus, WithdrawalType
│       ├── virtual_card/
│       │   ├── dto/                             ← VirtualCardDTO, CardTransactionDTO
│       │   ├── input/                           ← CreateCardRequest, FundCardRequest, FreezeCardRequest
│       │   └── enums/                           ← CardStatus, CardProvider, CardType
│       ├── escrow/
│       │   ├── dto/                             ← EscrowDTO, EscrowTransactionDTO
│       │   ├── input/                           ← CreateEscrowRequest, ReleaseEscrowRequest, DisputeRequest
│       │   └── enums/                           ← EscrowStatus, EscrowType, DisputeOutcome
│       ├── beneficiary/
│       │   ├── dto/                             ← BeneficiaryDTO
│       │   ├── input/                           ← AddBeneficiaryRequest, UpdateBeneficiaryRequest
│       │   └── enums/                           ← BeneficiaryType (INTERNAL, BANK)
│       ├── blacklist/
│       │   ├── dto/                             ← BlacklistEntryDTO
│       │   ├── input/                           ← BlacklistRequest
│       │   └── enums/                           ← BlacklistType (ACCOUNT, IP, DEVICE, CARD)
│       ├── maintenance/
│       │   ├── dto/                             ← MaintenanceFeeDTO, MaintenanceScheduleDTO
│       │   ├── input/                           ← MaintenanceFeeConfigRequest
│       │   └── enums/                           ← MaintenanceFeeType, DeductionStatus
│       ├── currency/
│       │   ├── dto/                             ← SupportedCurrencyDTO, ExchangeRateDTO
│       │   └── input/                           ← CurrencyRequest
│       ├── notification/
│       │   ├── dto/                             ← NotificationDTO, EmailPayload
│       │   └── enums/                           ← NotificationChannel, NotificationType
│       ├── savings/
│       │   ├── dto/                             ← SavingsGoalDTO, SavingsTransactionDTO
│       │   └── input/                           ← CreateSavingsRequest, TopUpRequest
│       └── investment/
│           ├── dto/                             ← InvestmentPlanDTO, InvestmentDTO
│           └── input/                           ← CreateInvestmentRequest
│
├── common/                                      ← Shared Spring configs — imported by all modules
│   └── src/main/java/.../common/
│       ├── config/
│       │   ├── async/                           ← AsyncExecutorConfig
│       │   ├── cache/                           ← RedisConfig, CacheManagerConfig, HazelcastConfig
│       │   ├── cors/                            ← CorsAutoConfiguration
│       │   ├── logging/                         ← RequestLoggingFilter, AuditLogInterceptor
│       │   ├── object_mapper/                   ← JacksonConfig
│       │   ├── openapi/                         ← SwaggerConfig
│       │   ├── security/                        ← JwtFilter, SecurityFilterChain, JwtService
│       │   └── ratelimit/                       ← RedisRateLimitService, @RateLimited, RateLimitAspect
│       ├── constants/                           ← AppConstants, SecurityConstants, CacheKeys
│       ├── exception/                           ← GlobalExceptionHandler, RateLimitExceededException
│       ├── events/                              ← Internal domain events (ApplicationEventPublisher)
│       │   ├── DomainEvent.java                 ← Base event
│       │   ├── auth/                            ← UserRegisteredEvent, PasswordResetEvent
│       │   ├── wallet/                          ← WalletCreditedEvent, WalletDebitedEvent, SwapCompletedEvent
│       │   ├── deposit/                         ← DepositInitiatedEvent, DepositCompletedEvent
│       │   ├── withdraw/                        ← WithdrawalInitiatedEvent, WithdrawalCompletedEvent, WithdrawalFailedEvent
│       │   ├── virtual_card/                    ← CardCreatedEvent, CardFundedEvent, CardFrozenEvent
│       │   ├── escrow/                          ← EscrowCreatedEvent, EscrowReleasedEvent, EscrowDisputedEvent
│       │   ├── beneficiary/                     ← BeneficiaryAddedEvent, BeneficiaryRemovedEvent
│       │   ├── blacklist/                       ← AccountBlacklistedEvent, BlacklistRemovedEvent
│       │   └── maintenance/                     ← MaintenanceFeeDeductedEvent, MaintenanceFeeFailedEvent
│       └── util/                                ← IpExtractor, OtpGenerator, CurrencyFormatter, ReceiptBuilder
│
├── auth/                                        ← Authentication & user management
│   └── src/main/java/.../auth/
│       ├── controller/
│       │   ├── AuthController.java              ← /auth/**
│       │   ├── UserController.java              ← /user/**
│       │   └── AdminUserController.java         ← /admin/users/**
│       ├── domain/
│       │   └── entity/                          ← Users, UserRecord, VerificationToken, PasswordResetToken, UserTracer
│       ├── mapper/
│       ├── repository/
│       └── service/
│           ├── AuthenticationService.java       ← Login, register, verify, forgot-password, forgot-username
│           ├── UserService.java
│           ├── OtpService.java                  ← In-memory + Redis OTP store with cool-down
│           └── UserSessionService.java
│
├── wallet/                                      ← Wallet, balances, transfers, currency
│   └── src/main/java/.../wallet/
│       ├── controller/
│       │   ├── WalletController.java            ← /wallet/**
│       │   └── CurrencyController.java          ← /admin/currencies/**
│       ├── domain/
│       │   └── entity/                          ← Wallet, WalletSettings, SupportedCurrency, CurrencyBalance
│       ├── mapper/
│       ├── repository/
│       └── service/
│           ├── WalletService.java               ← Balance reads, credit, debit, refund
│           ├── WalletCacheService.java          ← Hazelcast/Redis warm-up
│           ├── TransferService.java             ← In-platform peer transfers
│           ├── SwapService.java                 ← Currency swap with exchange rate
│           └── CurrencyConfigService.java       ← Dynamic currency CRUD + seeder
│
├── deposit/                                     ← Deposits via payment gateways
│   └── src/main/java/.../deposit/
│       ├── controller/
│       │   ├── DepositController.java           ← /deposit/**
│       │   └── DepositWebhookController.java    ← /webhook/deposit/**
│       ├── domain/
│       │   └── entity/                          ← DepositTransaction, DepositReceipt
│       ├── gateway/
│       │   ├── DepositGateway.java              ← Interface
│       │   ├── PaystackDepositGateway.java
│       │   ├── FlutterwaveDepositGateway.java
│       │   └── DepositGatewayFactory.java
│       ├── mapper/
│       ├── repository/
│       └── service/
│           ├── DepositService.java              ← Initiate + verify + credit wallet on success
│           └── DepositReceiptService.java       ← PDF receipt generation
│
├── withdraw/                                    ← Withdrawals — in-house + bank payouts
│   └── src/main/java/.../withdraw/
│       ├── controller/
│       │   ├── WithdrawController.java          ← /withdrawals/**
│       │   └── WithdrawWebhookController.java   ← /webhook/withdraw/**
│       ├── domain/
│       │   └── entity/                          ← WithdrawalTransaction, BankTransferRecord
│       ├── gateway/
│       │   ├── PayoutGateway.java               ← Interface
│       │   ├── PaystackPayoutGateway.java
│       │   ├── FlutterwavePayoutGateway.java
│       │   └── PayoutGatewayFactory.java
│       ├── mapper/
│       ├── repository/
│       └── service/
│           ├── WithdrawService.java             ← In-house transfer (debit sender, credit receiver)
│           ├── BankTransferService.java         ← External bank payout via gateway
│           ├── WithdrawVerificationService.java ← PIN + balance + blacklist checks
│           └── RefundService.java               ← Failed payout reversal
│
├── virtual_card/                                ← Virtual debit card issuance and management
│   └── src/main/java/.../virtual_card/
│       ├── controller/
│       │   ├── VirtualCardController.java       ← /cards/**
│       │   └── CardWebhookController.java       ← /webhook/card/**
│       ├── domain/
│       │   └── entity/                          ← VirtualCard, CardTransaction, CardLimit
│       ├── provider/
│       │   ├── CardProvider.java                ← Interface
│       │   ├── SudoCardProvider.java
│       │   └── CardProviderFactory.java
│       ├── mapper/
│       ├── repository/
│       └── service/
│           ├── VirtualCardService.java          ← Create, list, freeze, terminate
│           ├── CardFundingService.java          ← Debit wallet → fund card
│           ├── CardTransactionService.java
│           └── CardLimitService.java
│
├── escrow/                                      ← Held funds pending release or dispute
│   └── src/main/java/.../escrow/
│       ├── controller/
│       │   ├── EscrowController.java            ← /escrow/**
│       │   └── EscrowAdminController.java       ← /admin/escrow/**
│       ├── domain/
│       │   └── entity/                          ← EscrowAccount, EscrowTransaction, EscrowDispute
│       ├── mapper/
│       ├── repository/
│       └── service/
│           ├── EscrowService.java               ← Create (lock funds), release, cancel
│           ├── EscrowDisputeService.java        ← Raise + arbitrate + outcome
│           └── EscrowExpiryScheduler.java       ← @Scheduled auto-release/cancel
│
├── beneficiary/                                 ← Saved payees for fast repeat transfers
│   └── src/main/java/.../beneficiary/
│       ├── controller/
│       │   └── BeneficiaryController.java       ← /beneficiaries/**
│       ├── domain/
│       │   └── entity/                          ← Beneficiary (userId, recipientUsername/accountNumber, bankCode, type)
│       ├── mapper/
│       ├── repository/
│       │   └── BeneficiaryRepository.java
│       └── service/
│           ├── BeneficiaryService.java          ← Add, update, delete, list, verify recipient exists
│           └── BeneficiaryVerificationService.java ← Calls bank-list/auth to confirm account is real
│
├── blacklist/                                   ← Fraud prevention — blocks accounts, IPs, devices, cards
│   └── src/main/java/.../blacklist/
│       ├── controller/
│       │   └── BlacklistAdminController.java    ← /admin/blacklist/** (add, remove, query)
│       ├── domain/
│       │   └── entity/                          ← BlacklistEntry (type, value, reason, addedBy, expiresAt)
│       ├── mapper/
│       ├── repository/
│       │   └── BlacklistRepository.java
│       └── service/
│           ├── BlacklistService.java            ← Add, remove, check (cached in Redis for sub-ms lookup)
│           └── BlacklistCacheService.java       ← Warms Redis on startup; @EventListener on BlacklistEntryAddedEvent
│
├── maintenance/                                 ← Periodic wallet maintenance fee deduction
│   └── src/main/java/.../maintenance/
│       ├── controller/
│       │   └── MaintenanceAdminController.java  ← /admin/maintenance/** (configure fee rules)
│       ├── domain/
│       │   └── entity/                          ← MaintenanceFeeConfig, MaintenanceDeductionLog
│       ├── mapper/
│       ├── repository/
│       │   ├── MaintenanceFeeConfigRepository.java
│       │   └── MaintenanceDeductionLogRepository.java
│       └── service/
│           ├── MaintenanceFeeConfigService.java ← CRUD fee rules (amount, currency, interval, target wallets)
│           ├── MaintenanceFeeService.java       ← Deduct fee from wallet, handle insufficient balance
│           └── MaintenanceFeeScheduler.java     ← @Scheduled — runs at configured interval, deducts per config rule
│
├── savings/                                     ← Target savings goals
│   └── src/main/java/.../savings/
│       ├── controller/
│       │   └── SavingsController.java           ← /savings/**
│       ├── domain/
│       │   └── entity/                          ← SavingsGoal, SavingsTransaction
│       ├── repository/
│       └── service/
│           ├── SavingsService.java
│           └── SavingsScheduler.java            ← @Scheduled auto-debit
│
├── investment/                                  ← Investment plans & maturity payouts
│   └── src/main/java/.../investment/
│       ├── controller/
│       │   └── InvestmentController.java        ← /investments/**
│       ├── domain/
│       │   └── entity/                          ← InvestmentPlan, Investment
│       ├── repository/
│       └── service/
│           ├── InvestmentService.java
│           └── InvestmentMaturityScheduler.java ← @Scheduled maturity processor
│
├── notification/                                ← All channels — email, SMS, push
│   └── src/main/java/.../notification/
│       ├── domain/
│       │   └── entity/                          ← NotificationLog
│       ├── listener/                            ← @EventListener — replaces RabbitMQ entirely
│       │   ├── AuthEventListener.java
│       │   ├── WalletEventListener.java
│       │   ├── DepositEventListener.java
│       │   ├── WithdrawEventListener.java
│       │   ├── VirtualCardEventListener.java
│       │   ├── EscrowEventListener.java
│       │   ├── BeneficiaryEventListener.java
│       │   ├── BlacklistEventListener.java      ← Alerts user/admin when their account is flagged
│       │   └── MaintenanceEventListener.java    ← Sends fee deduction receipt email
│       ├── provider/
│       │   ├── EmailProvider.java               ← Interface
│       │   ├── SmtpEmailProvider.java           ← JavaMailSender + Thymeleaf
│       │   ├── SmsProvider.java                 ← Interface
│       │   └── TwilioSmsProvider.java
│       ├── template/                            ← Thymeleaf HTML per notification type
│       │   ├── auth/
│       │   │   ├── forgot-password-otp.html
│       │   │   ├── forgot-username.html
│       │   │   ├── login-alert.html
│       │   │   └── account-security-alert.html
│       │   ├── wallet/
│       │   │   ├── credit-notification.html
│       │   │   ├── debit-notification.html
│       │   │   └── swap-notification.html
│       │   ├── deposit/
│       │   │   └── deposit-success.html
│       │   ├── withdraw/
│       │   │   ├── withdrawal-success.html
│       │   │   └── withdrawal-failed.html
│       │   ├── virtual_card/
│       │   │   ├── card-created.html
│       │   │   └── card-funded.html
│       │   ├── escrow/
│       │   │   ├── escrow-created.html
│       │   │   ├── escrow-released.html
│       │   │   └── escrow-dispute.html
│       │   ├── beneficiary/
│       │   │   └── beneficiary-added.html
│       │   ├── blacklist/
│       │   │   └── account-flagged.html
│       │   └── maintenance/
│       │       └── maintenance-fee-deducted.html
│       └── service/
│           ├── NotificationDispatcher.java
│           └── NotificationLogService.java
│
├── history/                                     ← Unified transaction history & audit trail
│   └── src/main/java/.../history/
│       ├── controller/
│       │   └── HistoryController.java           ← /history/**
│       ├── domain/
│       │   └── entity/                          ← TransactionHistory, AuditLog
│       ├── listener/
│       │   └── TransactionHistoryListener.java  ← Listens to ALL domain events, writes unified log
│       ├── repository/
│       └── service/
│           └── HistoryService.java
│
├── admin/                                       ← Admin dashboard API
│   └── src/main/java/.../admin/
│       ├── controller/
│       │   ├── AdminUserController.java         ← /admin/users/**
│       │   ├── AdminWalletController.java       ← /admin/wallets/**
│       │   ├── AdminDepositController.java      ← /admin/deposits/**
│       │   ├── AdminWithdrawController.java     ← /admin/withdrawals/**
│       │   ├── AdminCardController.java         ← /admin/cards/**
│       │   ├── AdminEscrowController.java       ← /admin/escrow/**
│       │   ├── AdminBeneficiaryController.java  ← /admin/beneficiaries/**
│       │   ├── AdminBlacklistController.java    ← /admin/blacklist/**
│       │   ├── AdminMaintenanceController.java  ← /admin/maintenance/**
│       │   └── AdminReportController.java       ← /admin/reports/**
│       └── service/
│           ├── AdminUserService.java
│           ├── AdminWalletService.java
│           ├── AdminPaymentService.java
│           └── ReportService.java
│
└── main/                                        ← Single Spring Boot entry point
    └── src/main/java/.../
        ├── EpayApplication.java                 ← @SpringBootApplication — scans all modules
        └── resources/
            └── application.yaml                 ← One config file shared by every module
