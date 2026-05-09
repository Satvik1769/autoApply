CREATE TABLE resumes (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    original_name VARCHAR(500)  NOT NULL,
    storage_url   VARCHAR(1000) NOT NULL,
    storage_path  VARCHAR(500)  NOT NULL,
    file_size     BIGINT        NOT NULL,
    parsed_json   JSONB,
    parse_status  VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_resumes_user_id    ON resumes(user_id);
CREATE INDEX idx_resumes_parsed_json ON resumes USING gin(parsed_json);
