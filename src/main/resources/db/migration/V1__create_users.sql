CREATE TABLE if not exists users (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    provider            VARCHAR(50)  NOT NULL,
    provider_id         VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    name                VARCHAR(255),
    avatar_url          VARCHAR(500),
    target_roles        TEXT[]       NOT NULL DEFAULT '{}',
    years_experience    SMALLINT,
    preferred_locations TEXT[]       NOT NULL DEFAULT '{}',
    preferred_skills    TEXT[]       NOT NULL DEFAULT '{}',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_login_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_provider UNIQUE (provider, provider_id),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

CREATE INDEX if not exists idx_users_email  ON users(email);
