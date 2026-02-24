-- Add migration script her
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'debt_status') THEN
        CREATE TYPE debt_status AS ENUM ('PENDING', 'PAID', 'OVERDUE');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'currency_type') THEN
        CREATE TYPE currency_type AS ENUM ('USD', 'EUR', 'NGN', 'GBP', 'JPY', 'AUD', 'CAD', 'CHF', 'CNY', 'INR');
    END IF;
END $$;

-- Create table
CREATE TABLE IF NOT EXISTS debt_collector (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount NUMERIC(20,2),
    due_amount NUMERIC(20,2),
    debt_status debt_status,
    description TEXT,
    currency_type currency_type NOT NULL,
    created_on TIMESTAMP DEFAULT NOW(),
    updated_on TIMESTAMP DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS wallet_maintenance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,            
    currency_type currency_type NOT NULL,
    balance NUMERIC(20,2) DEFAULT 0,    
    status debt_status DEFAULT 'PENDING', 
    last_charged TIMESTAMP NULL,         
    created_on TIMESTAMP DEFAULT NOW(),
    updated_on TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS maintenance_fee_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    currency_type currency_type NOT NULL,
    fee_amount NUMERIC(20,2) NOT NULL,
    status debt_status NOT NULL, 
    attempted_on TIMESTAMP DEFAULT NOW(),
    paid_on TIMESTAMP NULL,      
    reason TEXT NULL 
);

ALTER TABLE wallet_maintenance 
ADD CONSTRAINT unique_user_currency UNIQUE (user_id, currency_type);