CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    amount      INTEGER     NOT NULL,
    description VARCHAR(255),
    date        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
