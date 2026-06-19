CREATE TABLE videos (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users (id),
    youtube_url      VARCHAR(512) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    result_file_path VARCHAR(1024),
    failure_reason   VARCHAR(1024),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_videos_user_id ON videos (user_id);
CREATE INDEX idx_videos_status ON videos (status);
