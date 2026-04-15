-- ==========================================
-- 1. 핵심 도메인 테이블
-- ==========================================

CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    nickname   VARCHAR(20)  NOT NULL,
    password   VARCHAR(255) NOT NULL,

    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE books
(
    id             UUID PRIMARY KEY,
    title          VARCHAR(255)  NOT NULL,
    author         VARCHAR(100)  NOT NULL,
    description    TEXT          NOT NULL,
    publisher      VARCHAR(100)  NOT NULL,
    published_date DATE          NOT NULL,
    isbn           VARCHAR(20) UNIQUE,
    thumbnail_url  VARCHAR(255),
    review_count   INTEGER       NOT NULL DEFAULT 0 CHECK (review_count >= 0),
    rating         NUMERIC(3, 2) NOT NULL DEFAULT 0.00 CHECK (rating >= 0.0 AND rating <= 5.0),


    is_deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reviews
(
    id            UUID PRIMARY KEY,
    book_id       UUID          NOT NULL,
    user_id       UUID          NOT NULL,
    content       VARCHAR(1000) NOT NULL,
    rating        NUMERIC(3, 2) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    liked_count   INTEGER       NOT NULL DEFAULT 0 CHECK (liked_count >= 0),
    comment_count INTEGER       NOT NULL DEFAULT 0 CHECK (comment_count >= 0),

    is_deleted    BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_books FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_reviews_book_user UNIQUE (book_id, user_id)
);

CREATE TABLE comments
(
    id         UUID PRIMARY KEY,
    review_id  UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    content    VARCHAR(500) NOT NULL,

    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comments_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE notifications
(
    id         UUID PRIMARY KEY,
    review_id  UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    content    VARCHAR(255) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==========================================
-- 2. 좋아요 여부 기록
-- ==========================================

CREATE TABLE liked_reviews
(
    id         UUID PRIMARY KEY,
    review_id  UUID        NOT NULL,
    user_id    UUID        NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_liked_reviews_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_liked_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_liked_reviews_user_review UNIQUE (review_id, user_id) -- 1인 1리뷰 1좋아요 보장 💡
);

-- ==========================================
-- 3. 통계 및 랭킹 테이블
-- ==========================================

CREATE TABLE popular_books
(
    id              UUID PRIMARY KEY,
    book_id         UUID           NOT NULL,
    period_type     VARCHAR(20)    NOT NULL CHECK (period_type IN
                                                   ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY',
                                                    'ALL_TIME')),
    calculated_date DATE           NOT NULL, -- 랭킹 산정 기준일 (시간은 필요 없으므로 DATE)
    rank            INTEGER        NOT NULL CHECK (rank > 0),
    score           NUMERIC(10, 2) NOT NULL CHECK (score >= 0),
    rating          NUMERIC(3, 2)  NOT NULL CHECK (rating >= 0.0 AND rating <= 5.0),
    review_count    INTEGER        NOT NULL DEFAULT 0 CHECK (review_count >= 0),

    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_popular_books_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT uk_popular_books_period_rank UNIQUE (period_type, calculated_date, rank),
    CONSTRAINT uk_popular_books_period_book UNIQUE (period_type, calculated_date, book_id)
);

CREATE TABLE popular_reviews
(
    id              UUID PRIMARY KEY,
    review_id       UUID           NOT NULL,
    period_type     VARCHAR(20)    NOT NULL CHECK (period_type IN
                                                   ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY',
                                                    'ALL_TIME')),
    calculated_date DATE           NOT NULL,
    rank            INTEGER        NOT NULL CHECK (rank > 0),
    score           NUMERIC(10, 2) NOT NULL CHECK (score >= 0),
    liked_count     INTEGER        NOT NULL DEFAULT 0 CHECK (liked_count >= 0),
    comment_count   INTEGER        NOT NULL DEFAULT 0 CHECK (comment_count >= 0),

    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_popular_reviews_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT uk_popular_reviews_period_rank UNIQUE (period_type, calculated_date, rank),
    CONSTRAINT uk_popular_reviews_period_review UNIQUE (period_type, calculated_date, review_id)
);

CREATE TABLE power_users
(
    id              UUID PRIMARY KEY,
    user_id         UUID           NOT NULL,
    period_type     VARCHAR(20)    NOT NULL CHECK (period_type IN
                                                   ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY',
                                                    'ALL_TIME')),
    calculated_date DATE           NOT NULL,
    rank            INTEGER        NOT NULL CHECK (rank > 0),
    score           NUMERIC(10, 2) NOT NULL CHECK (score >= 0),

    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_power_users_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_power_users_period_rank UNIQUE (period_type, calculated_date, rank),
    CONSTRAINT uk_power_users_period_user UNIQUE (period_type, calculated_date, user_id)
);
