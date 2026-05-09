CREATE TABLE jd_extraction_cache (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    jd_hash          CHAR(64)    NOT NULL UNIQUE,
    extracted_json   JSONB       NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_accessed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_jd_cache_hash ON jd_extraction_cache(jd_hash);
