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
-- create implicitly. MySQL 8.0 does not support ADD COLUMN IF NOT EXISTS, so
-- build the statement dynamically to support both a clean schema and an
-- existing schema created by earlier versions of the application.
SET @role_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'role'
);
SET @role_column_statement := IF(
    @role_column_exists = 0,
    "ALTER TABLE users ADD COLUMN role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER'",
    'SELECT 1'
);
PREPARE add_role_column FROM @role_column_statement;
EXECUTE add_role_column;
DEALLOCATE PREPARE add_role_column;

CREATE INDEX idx_subscriptions_status_trial_end ON subscriptions(status, trial_ends_at);
CREATE INDEX idx_subscriptions_status_period_end ON subscriptions(status, current_period_ends_at);
CREATE INDEX idx_subscriptions_status_retry_at ON subscriptions(status, next_retry_at);
CREATE INDEX idx_subscriptions_mp_payment_id ON subscriptions(mp_payment_id);
