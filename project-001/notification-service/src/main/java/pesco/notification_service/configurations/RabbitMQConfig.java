package pesco.notification_service.configurations;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Constants for exchange and queues
    public static final String WALLET_EXCHANGE = "wallet.notifications";
    public static final String AUTH_EXCHANGE = "auth.notifications";
    public static final String ACCOUNT_EXCHANGE = "account.notifications"; 

    // Auth & User Queues
    public static final String ACCOUNT_VERIFICATION_QUEUE = "account.verification";
    public static final String USER_OTP_QUEUE = "user.otp";
    public static final String RESET_PASSWORD_QUEUE = "password.reset"; 

    // Wallet Queues
    public static final String CREDIT_WALLET_QUEUE = "credit.wallet";
    public static final String DEBIT_WALLET_QUEUE = "debit.wallet";
    public static final String DEPOSIT_WALLET_QUEUE = "wallet.deposit";
    public static final String MAINTENANCE_DEDUCTION_QUEUE = "maintenance.wallet";
    public static final String SWAP_WALLET_QUEUE = "swap.wallet";

    // Account Queues - NEW SECTION
    public static final String ACCOUNT_STATEMENT_QUEUE = "account.statement"; 
    public static final String BLOCK_USER_WALLET_QUEUE = "block.user.wallet";

    // Routing keys
    public static final String ROUTING_KEY_ACCOUNT_VERIFICATION = "auth.account";
    public static final String ROUTING_KEY_ACCOUNT_USER_OTP = "auth.user";
    public static final String ROUTING_KEY_RESET_PASSWORD = "auth.password"; 

    public static final String ROUTING_KEY_CREDIT_WALLET = "wallet.credit";
    public static final String ROUTING_KEY_DEBIT_WALLET = "wallet.debit";
    public static final String ROUTING_KEY_DEPOSIT_WALLET = "wallet.deposit";
    public static final String ROUTING_KEY_WALLET_MAINTENANCE_SERVICE_DEDUCTION = "wallet.deduction-fee";
    public static final String ROUTING_KEY_ACCOUNT_STATEMENT = "account.statement";
    public static final String ROUTING_KEY_SWAP_WALLET = "wallet.swap";
    public static final String ROUTING_KEY_BLOCK_USER_WALLET = "wallet.block-user";

    // Declare the exchanges
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE);
    }

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE);
    }

    @Bean
    public TopicExchange accountExchange() { 
        return new TopicExchange(ACCOUNT_EXCHANGE);
    }

    // Declare the queues
    @Bean
    public Queue accountVerificationQueue() {
        return new Queue(ACCOUNT_VERIFICATION_QUEUE);
    }

    @Bean
    public Queue userOTPQueue() {
        return new Queue(USER_OTP_QUEUE);
    }

    @Bean
    public Queue resetPasswordQueue() {
        return new Queue(RESET_PASSWORD_QUEUE);
    }

    @Bean
    public Queue creditWalletQueue() {
        return new Queue(CREDIT_WALLET_QUEUE);
    }

    @Bean
    public Queue debitWalletQueue() {
        return new Queue(DEBIT_WALLET_QUEUE);
    }

    @Bean
    public Queue depositWalletQueue() {
        return new Queue(DEPOSIT_WALLET_QUEUE);
    }
    
    @Bean
    public Queue walletMaintenanceDeductionQueue() {
        return new Queue(MAINTENANCE_DEDUCTION_QUEUE);
    }

    @Bean
    public Queue swapWalletQueue() {
        return new Queue(SWAP_WALLET_QUEUE);
    }

    @Bean
    public Queue accountStatementQueue() {
        return new Queue(ACCOUNT_STATEMENT_QUEUE);
    }

    @Bean
    public Queue blockUserWalletQueue() {
        return new Queue(BLOCK_USER_WALLET_QUEUE);
    }   

    // Bindings for Auth Exchange
    @Bean
    public Binding accountVerificationBinding(Queue accountVerificationQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(accountVerificationQueue).to(authExchange).with(ROUTING_KEY_ACCOUNT_VERIFICATION);
    }

    @Bean
    public Binding userOTPBinding(Queue userOTPQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userOTPQueue).to(authExchange).with(ROUTING_KEY_ACCOUNT_USER_OTP);
    }

    @Bean
    public Binding resetPasswordBinding(Queue resetPasswordQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(resetPasswordQueue).to(authExchange).with(ROUTING_KEY_RESET_PASSWORD);
    }

    // Bindings for Wallet Exchange
    @Bean
    public Binding creditWalletBinding(Queue creditWalletQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(creditWalletQueue).to(walletExchange).with(ROUTING_KEY_CREDIT_WALLET);
    }

    @Bean
    public Binding debitWalletBinding(Queue debitWalletQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(debitWalletQueue).to(walletExchange).with(ROUTING_KEY_DEBIT_WALLET);
    }

    @Bean
    public Binding depositWalletBinding(Queue depositWalletQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(depositWalletQueue).to(walletExchange).with(ROUTING_KEY_DEPOSIT_WALLET);
    }

    @Bean
    public Binding walletMaintenanceBinding(Queue walletMaintenanceDeductionQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(walletMaintenanceDeductionQueue).to(walletExchange).with(ROUTING_KEY_WALLET_MAINTENANCE_SERVICE_DEDUCTION);
    }

    // NEW: Binding for Account Statement to Account Exchange
    @Bean
    public Binding accountStatementBinding(Queue accountStatementQueue, TopicExchange accountExchange) {
        return BindingBuilder.bind(accountStatementQueue).to(accountExchange).with(ROUTING_KEY_ACCOUNT_STATEMENT);
    }

    @Bean
    public Binding swapWalletBinding(Queue swapWalletQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(swapWalletQueue).to(walletExchange).with(ROUTING_KEY_SWAP_WALLET);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
    
    @Bean
    public Binding blockUserWalletBinding(Queue blockUserWalletQueue, TopicExchange walletExchange) {
        return BindingBuilder.bind(blockUserWalletQueue).to(walletExchange).with(ROUTING_KEY_BLOCK_USER_WALLET);
    }
}