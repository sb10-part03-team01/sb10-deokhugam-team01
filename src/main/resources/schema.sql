CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    nickname   VARCHAR(20)  NOT NULL,
    password   VARCHAR(100) NOT NULL,

    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS books
(
    id             UUID PRIMARY KEY,
    title          VARCHAR(255)     NOT NULL,
    author         VARCHAR(100)     NOT NULL,
    description    TEXT             NOT NULL,
    publisher      VARCHAR(100)     NOT NULL,
    published_date DATE             NOT NULL,
    isbn           VARCHAR(20),
    thumbnail_url  VARCHAR(255),
    review_count   INTEGER          NOT NULL DEFAULT 0 CHECK (review_count >= 0),
    rating         DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),


    is_deleted     BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reviews
(
    id            UUID PRIMARY KEY,
    book_id       UUID             NOT NULL,
    user_id       UUID             NOT NULL,
    content       VARCHAR(1000)    NOT NULL,
    rating        DOUBLE PRECISION NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    like_count    INTEGER          NOT NULL DEFAULT 0 CHECK (like_count >= 0),
    comment_count INTEGER          NOT NULL DEFAULT 0 CHECK (comment_count >= 0),
    version       BIGINT           NOT NULL DEFAULT 0,

    is_deleted    BOOLEAN          NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reviews_books FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_reviews_book_user UNIQUE (book_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews (book_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews (user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews (created_at);
CREATE INDEX IF NOT EXISTS idx_reviews_rating_created_at ON reviews (rating, created_at);

CREATE TABLE IF NOT EXISTS comments
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

CREATE INDEX IF NOT EXISTS idx_comment_review_created_at_id ON comments (review_id, created_at, id);

CREATE TABLE IF NOT EXISTS notifications
(
    id           UUID PRIMARY KEY,
    review_id    UUID         NOT NULL,
    user_id      UUID         NOT NULL,
    content      VARCHAR(255) NOT NULL,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    confirmed_at TIMESTAMPTZ,

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_reviews FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_review_id ON notifications (review_id);

CREATE TABLE IF NOT EXISTS review_likes
(
    id         UUID PRIMARY KEY,
    review_id  UUID        NOT NULL,
    user_id    UUID        NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT fk_review_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_review_like_review_user UNIQUE (review_id, user_id)
);

CREATE TABLE IF NOT EXISTS popular_books
(
    id              UUID PRIMARY KEY,
    book_id         UUID             NOT NULL,
    period_type     VARCHAR(20)      NOT NULL CHECK (period_type IN
                                                     ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    calculated_date DATE             NOT NULL, -- 랭킹 산정 기준일 (시간은 필요 없으므로 DATE)
    rank            INTEGER          NOT NULL CHECK (rank > 0),
    score           DOUBLE PRECISION NOT NULL CHECK (score >= 0),
    rating          DOUBLE PRECISION NOT NULL CHECK (rating >= 0.0 AND rating <= 5.0),
    review_count    INTEGER          NOT NULL DEFAULT 0 CHECK (review_count >= 0),

    created_at      TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_popular_books_book FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT uk_popular_books_period_rank UNIQUE (period_type, calculated_date, rank),
    CONSTRAINT uk_popular_books_period_book UNIQUE (period_type, calculated_date, book_id)
);

CREATE INDEX IF NOT EXISTS idx_popular_books_book_id ON popular_books (book_id);

CREATE TABLE IF NOT EXISTS popular_reviews
(
    id              UUID PRIMARY KEY,
    review_id       UUID             NOT NULL,
    period_type     VARCHAR(20)      NOT NULL CHECK (period_type IN
                                                   ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    calculated_date DATE             NOT NULL,
    ranking         INTEGER          NOT NULL CHECK (ranking > 0),
    score           DOUBLE PRECISION NOT NULL CHECK (score >= 0),
    liked_count     INTEGER          NOT NULL DEFAULT 0 CHECK (liked_count >= 0),
    comment_count   INTEGER          NOT NULL DEFAULT 0 CHECK (comment_count >= 0),

    created_at      TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_popular_reviews_review FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    CONSTRAINT uk_popular_reviews_period_rank UNIQUE (period_type, calculated_date, ranking),
    CONSTRAINT uk_popular_reviews_period_review UNIQUE (period_type, calculated_date, review_id)
);

CREATE TABLE IF NOT EXISTS power_users
(
    id               UUID PRIMARY KEY,
    user_id          UUID             NOT NULL,
    period_type      VARCHAR(20)      NOT NULL CHECK (period_type IN
                                                      ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    calculated_date  TIMESTAMPTZ      NOT NULL,
    rank             BIGINT           NOT NULL CHECK (rank > 0),
    score            DOUBLE PRECISION NOT NULL CHECK (score >= 0),
    review_score_sum DOUBLE PRECISION NOT NULL DEFAULT 0,
    like_count       BIGINT           NOT NULL DEFAULT 0 CHECK (like_count >= 0),
    comment_count    BIGINT           NOT NULL DEFAULT 0 CHECK (comment_count >= 0),

    created_at       TIMESTAMPTZ      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_power_users_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_power_users_period_rank UNIQUE (period_type, calculated_date, rank),
    CONSTRAINT uk_power_users_period_user UNIQUE (period_type, calculated_date, user_id)
);
