-- Đảm bảo mỗi user chỉ có tối đa 1 bank account là default
-- Partial index: chỉ index các row có is_default = true → enforce uniqueness cho những row đó
CREATE UNIQUE INDEX uq_user_bank_accounts_default
    ON user_bank_accounts (user_id)
    WHERE is_default = true;
