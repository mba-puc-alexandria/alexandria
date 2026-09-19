CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status ENUM('TRIALING', 'ACTIVE', 'PAST_DUE', 'EXPIRED', 'CANCELED') NOT NULL,
    trial_ends_at DATETIME NULL,
    current_period_ends_at DATETIME NULL,
    mp_payment_id BIGINT NULL,
    mp_customer_id VARCHAR(255) NULL,
    mp_card_id VARCHAR(255) NULL,
    last_payment_status VARCHAR(64) NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME NULL,
    scheduled_payment_id VARCHAR(255) NULL,
    payment_scheduled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_subscriptions_user UNIQUE (user_id),
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- The original V0 schema predates the User.role enum that Hibernate used to
-- create implicitly. Keep a clean Flyway installation compatible with it.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER';

CREATE INDEX idx_subscriptions_status_trial_end ON subscriptions(status, trial_ends_at);
CREATE INDEX idx_subscriptions_status_period_end ON subscriptions(status, current_period_ends_at);
CREATE INDEX idx_subscriptions_status_retry_at ON subscriptions(status, next_retry_at);
CREATE INDEX idx_subscriptions_mp_payment_id ON subscriptions(mp_payment_id);
