CREATE TABLE users
(
    id         BIGINT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP NULL,

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
