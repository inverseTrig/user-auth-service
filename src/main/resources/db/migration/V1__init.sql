CREATE TABLE users
(
    id         BIGINT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP    NULL,

    CONSTRAINT users_email_key UNIQUE (email)
);

COMMENT ON TABLE users IS '사용자';
COMMENT ON COLUMN users.id IS 'ID';
COMMENT ON COLUMN users.email IS '이메일';
COMMENT ON COLUMN users.password IS '암호화된 비밀번호';
COMMENT ON COLUMN users.name IS '이름';
COMMENT ON COLUMN users.role IS '권한';
COMMENT ON COLUMN users.created_at IS '생성일시';
COMMENT ON COLUMN users.updated_at IS '수정일시';
COMMENT ON COLUMN users.deleted_at IS '삭제일시';

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE refresh_tokens
(
    id         BIGINT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(512) NOT NULL,
    family_id  BIGINT       NOT NULL,
    is_revoked BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,

    CONSTRAINT refresh_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT refresh_tokens_token_key UNIQUE (token)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (family_id);

COMMENT ON TABLE refresh_tokens IS '리프레시 토큰';
COMMENT ON COLUMN refresh_tokens.id IS 'ID';
COMMENT ON COLUMN refresh_tokens.user_id IS '사용자 ID';
COMMENT ON COLUMN refresh_tokens.token IS '리프레시 토큰';
COMMENT ON COLUMN refresh_tokens.family_id IS '토큰 패밀리 ID (토큰 재사용 탐지용)';
COMMENT ON COLUMN refresh_tokens.is_revoked IS '폐기 여부';
COMMENT ON COLUMN refresh_tokens.expires_at IS '만료일시';
COMMENT ON COLUMN refresh_tokens.created_at IS '생성일시';
COMMENT ON COLUMN refresh_tokens.updated_at IS '수정일시';

-- INITIAL ADMIN USER
-- Password: secure_password
INSERT INTO users
VALUES (731276600279109,
        'admin@example.com',
        '$2a$10$zG1xeffcDTrQ3Hdh9jEKAupm6rMjsSRwYK6cNsV/yslcE2Wd3wVea',
        'Admin',
        'ADMIN',
        now(),
        now(),
        null);
