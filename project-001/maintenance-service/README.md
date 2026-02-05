# Maintenance Service

## Overview

The Maintenance Service is a background service responsible for automatically collecting maintenance fees from users based on their transaction activities. It runs periodic cycles to calculate and charge fees for users who have performed specific types of transactions within the last 30 days.

## Purpose

This service ensures that users who actively use the platform for financial transactions contribute to maintenance costs through a small percentage fee on their transaction volumes.

## Key Features

### 🎯 Transaction-Based Fee Calculation
- **Fee Trigger**: Charges are based on user transaction activities
- **Applicable Transactions**: WITHDRAW, TRANSFER, SWAP, DEBITED, EXCHANGE
- **Calculation Method**: 0.5% of total transaction volume per currency
- **Time Window**: Last 30 days of transaction history

### 💰 Fee Structure
- **Rate**: 0.5% of total transaction volume
- **Minimum Fee**: $0.01 (or equivalent in other currencies)
- **Currency Support**: USD, EUR, NGN, GBP, JPY, AUD, CAD, CHF, CNY, INR
- **Frequency**: Once every 30 days per currency

### 🔄 Processing Logic
1. **User Eligibility**: Only active users with transaction history
2. **Balance Verification**: Sufficient wallet balance required
3. **Duplicate Prevention**: 30-day cooldown between charges
4. **Multi-Currency Support**: Processes each currency separately

## Service Architecture

### External Dependencies
- **User Service**: Fetches user profiles and contact information
- **Wallet Service**: Deducts fees and manages balances
- **History Service**: Retrieves 30-day transaction history
- **Revenue Service**: Records fee transactions for accounting
- **Notification Service**: Sends success notifications to users

### Data Flow
1. Fetch all active users
2. Retrieve user wallet balances
3. Get 30-day transaction history
4. Calculate fees per currency
5. Process deductions (if eligible)
6. Record revenue
7. Send notifications (success only)

## Business Rules

### Eligibility Criteria
- User must have performed chargeable transactions in last 30 days
- Minimum transaction volume to trigger fee calculation
- Sufficient wallet balance to cover the fee
- No previous charge in the last 30 days for same currency

### Failure Handling
- **Insufficient Balance**: Marks as overdue, no notification sent
- **Service Failures**: Automatic rollback of partial transactions
- **Network Issues**: Retry logic with timeouts
- **Data Consistency**: Database transactions ensure atomic operations

## Notification System

### Success Notifications Only
- **When**: Only after successful fee collection
- **Content**: Transaction summary, fee details, balance changes
- **Frequency**: Once per successful charge
- **No Spam**: No failure or reminder notifications

### Notification Content
- User details (name, email)
- Transaction summary (total spent, fee amount)
- Balance information (before and after)
- Currency and timestamp

## Security & Compliance

### Data Protection
- Secure API communication between services
- No storage of sensitive financial data
- Audit trails for all fee transactions

### Financial Integrity
- Atomic operations ensure no double-charging
- Rollback mechanisms for partial failures
- Comprehensive logging for audit purposes

## Monitoring & Logging

### Key Metrics
- Number of users processed
- Success/failure rates per currency
- Total revenue collected
- Service response times

### Alerting
- Service downtime
- High failure rates
- Revenue recording failures
- Wallet service issues

## Deployment

### Environment Requirements
- Database connection for state management
- Access to all dependent microservices
- Sufficient memory for processing large user batches
- Network connectivity to external services

### Scaling Considerations
- Designed for batch processing
- Can be scaled horizontally for large user bases
- Database connection pooling for efficiency

## Maintenance Windows

- **Best Time**: Low-traffic periods
- **Frequency**: Monthly cycles
- **Duration**: Depends on user base size
- **Monitoring**: Real-time progress tracking

This service plays a crucial role in platform sustainability by ensuring fair contribution from active users while maintaining excellent user experience through transparent and infrequent notifications.