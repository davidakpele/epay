# Enterprise Transaction History & Audit Design

## Overview

This document defines the transaction history and audit strategy for the payment platform.

The primary goals are:

- Maintain **one source of truth** for every transaction.
- Avoid duplicating transaction data every time a status changes.
- Provide a clean transaction timeline for frontend applications.
- Support enterprise auditing and compliance.
- Keep reads fast while maintaining a complete history of status progression.

---

# Problem

A common implementation stores one transaction history record every time the status changes.

Example:

```
Transaction
----------------------
Amount = ₦100,000
Reference = REF123
Status = INITIATED

↓

Insert another row

Amount = ₦100,000
Reference = REF123
Status = PENDING

↓

Insert another row

Amount = ₦100,000
Reference = REF123
Status = PROCESSING

↓

Insert another row

Amount = ₦100,000
Reference = REF123
Status = DELIVERED
```

Although this creates an immutable ledger, it duplicates almost every column.

Large payment systems may have millions of transactions, making this approach expensive in storage and maintenance.

---

# Proposed Architecture

Instead of duplicating the entire transaction, the system is divided into two responsibilities.

```
                    Transaction
                          │
        ┌─────────────────┴─────────────────┐
        │                                   │
 Status Timeline JSON                 Audit Log
 (Business Progress)            (Security & Compliance)
```

---

# 1. Transaction

The Transaction table stores the business data.

Exactly **one row exists per transaction**.

Example fields:

```
Transaction

id
transactionId
reference
userId
walletId
amount
fee
tax
currency
balance
currentStatus
statusTimeline (JSON)
createdAt
updatedAt
```

The transaction itself is updated only when necessary.

The `statusTimeline` contains the complete progression of the payment.

---

# 2. Status Timeline

The timeline represents the lifecycle of a transaction.

Instead of storing multiple history rows, we store one JSON object.

Example

```json
{
  "INITIATED": {
    "timestamp": "2026-07-23T09:00:00Z",
    "actor": "SYSTEM",
    "message": "Transaction created"
  },

  "PENDING": {
    "timestamp": "2026-07-23T09:00:02Z",
    "actor": "SYSTEM",
    "message": "Waiting for gateway"
  },

  "VALIDATED": {
    "timestamp": "2026-07-23T09:00:03Z",
    "actor": "RULE_ENGINE",
    "message": "Validation successful"
  },

  "PROCESSING": {
    "timestamp": "2026-07-23T09:00:05Z",
    "actor": "PAYSTACK",
    "message": "Gateway processing"
  },

  "PROCESSED": {
    "timestamp": "2026-07-23T09:00:08Z",
    "actor": "PAYSTACK",
    "message": "Gateway approved"
  },

  "SETTLED": {
    "timestamp": "2026-07-23T09:00:10Z",
    "actor": "LEDGER",
    "message": "Ledger updated"
  },

  "DELIVERED": {
    "timestamp": "2026-07-23T09:00:13Z",
    "actor": "BANK",
    "message": "Recipient credited"
  }
}
```

Only completed statuses exist.

No null values.

No duplicated transaction information.

---

# Current Status

For searching and filtering, keep a dedicated column.

```
currentStatus = DELIVERED
```

Examples

```
SELECT *
FROM transaction
WHERE current_status='PENDING';
```

No JSON parsing is required.

---

# Transaction Status Enum

```java
public enum TransactionStatus {

    INITIATED,

    PENDING,

    VALIDATED,

    PROCESSING,

    PROCESSED,

    SETTLED,

    DELIVERED,

    FAILED,

    CANCELLED,

    REVERSED,

    DISPUTED,

    EXPIRED
}
```

---

# Status Event

```java
public class StatusEvent {

    private LocalDateTime timestamp;

    private String actor;

    private String message;

}
```

---

# Status Timeline

```java
public class StatusTimeline {

    private LinkedHashMap<TransactionStatus, StatusEvent> statuses;

}
```

Using `LinkedHashMap` preserves insertion order, ensuring the timeline is returned in the same sequence it occurred.

---

# Updating Status

Example

Current

```
INITIATED
```

↓

Gateway responds

```
PENDING
```

↓

Validation passes

```
VALIDATED
```

↓

Gateway starts processing

```
PROCESSING
```

The JSON becomes

```json
{
  "INITIATED": {...},
  "PENDING": {...},
  "VALIDATED": {...},
  "PROCESSING": {...}
}
```

Only one JSON object is updated.

No duplicate transaction records are created.

---

# Audit Log

The audit log serves a different purpose.

It records **who changed what and when**, rather than storing another copy of the transaction.

Example table

```
AuditLog

id
transactionId
action
performedBy
previousStatus
newStatus
ipAddress
deviceId
reason
createdAt
```

Example records

```
CREATE

Status:
NULL → INITIATED
```

```
STATUS_CHANGE

INITIATED → PENDING
```

```
STATUS_CHANGE

PENDING → PROCESSING
```

```
STATUS_CHANGE

PROCESSING → DELIVERED
```

Unlike the transaction table, the audit log is **append-only**.

Audit records are never updated or deleted.

---

# Why Separate Audit From Transaction?

Transaction answers:

> What is the current state of this payment?

Audit answers:

> Who changed it?
>
> When was it changed?
>
> What was the previous state?
>
> From which device?
>
> From which IP address?

These are two different responsibilities.

Keeping them separate produces a much cleaner design.

---

# Fetch Transaction History

Example response

```json
{
  "success": true,
  "message": "Transaction history retrieved successfully.",
  "data": {
    "transactionId": "TXN-20260723-000001",
    "reference": "REF-9F8D3A2B",
    "transactionType": "BANK_TRANSFER",
    "debitCredit": "DEBIT",

    "user": {
      "id": 1001,
      "walletId": 5001,
      "accountHolder": "David Ray"
    },

    "recipient": {
      "userId": 2002,
      "walletId": 6002,
      "accountHolder": "John Doe"
    },

    "amount": {
      "gross": 100000.00,
      "fee": 100.00,
      "tax": 7.50,
      "net": 99892.50,
      "currency": "NGN",
      "symbol": "₦"
    },

    "balance": {
      "previous": 350000.00,
      "available": 250000.00,
      "running": 250000.00
    },

    "currentStatus": "DELIVERED",

    "statusTimeline": {
      "INITIATED": {
        "timestamp": "2026-07-23T09:00:00Z",
        "actor": "SYSTEM",
        "message": "Transaction created"
      },

      "PENDING": {
        "timestamp": "2026-07-23T09:00:01Z",
        "actor": "SYSTEM",
        "message": "Waiting for gateway"
      },

      "VALIDATED": {
        "timestamp": "2026-07-23T09:00:02Z",
        "actor": "RULE_ENGINE",
        "message": "Validation successful"
      },

      "PROCESSING": {
        "timestamp": "2026-07-23T09:00:05Z",
        "actor": "PAYSTACK",
        "message": "Gateway processing"
      },

      "PROCESSED": {
        "timestamp": "2026-07-23T09:00:08Z",
        "actor": "PAYSTACK",
        "message": "Gateway approved"
      },

      "SETTLED": {
        "timestamp": "2026-07-23T09:00:10Z",
        "actor": "LEDGER",
        "message": "Ledger updated"
      },

      "DELIVERED": {
        "timestamp": "2026-07-23T09:00:13Z",
        "actor": "BANK",
        "message": "Recipient credited"
      }
    },

    "createdAt": "2026-07-23T09:00:00Z",
    "completedAt": "2026-07-23T09:00:13Z"
  }
}
```

---

# Benefits

- One transaction record per payment.
- No duplicated amounts, balances, references, or customer information.
- Status progression is stored in a compact JSON timeline.
- Fast filtering using `currentStatus`.
- Simple timeline rendering for frontend applications.
- Immutable audit records for security, compliance, and investigations.
- Clear separation of business data and audit data.
- Scales better than storing a complete transaction row for every status transition.