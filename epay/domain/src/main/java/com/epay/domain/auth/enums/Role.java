package com.epay.domain.auth.enums;

/**
 * System roles — ordered from most to least privileged.
 *
 * SUPER_USER       — full system access, manages all admin/staff accounts
 * ADMIN            — manages users, wallets, transactions; cannot create SUPER_USER
 * CUSTOMER_SERVICE — read access + ticket management, wallet freeze/unfreeze, account block/unblock
 * EDITOR           — read-only content/config access, cannot touch financial data
 * USER             — standard customer
 */
public enum Role {
    SUPER_USER,
    ADMIN,
    CUSTOMER_SERVICE,
    EDITOR,
    USER
}
