CREATE TABLE ats_scores (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id            UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resume_id          UUID        NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    jd_hash            CHAR(64)    NOT NULL,
    jd_text_snippet    VARCHAR(500),
    total_score        SMALLINT    NOT NULL,
    max_score          SMALLINT    NOT NULL DEFAULT 100,
    category_breakdown JSONB       NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ats_scores_user_id   ON ats_scores(user_id);
CREATE INDEX idx_ats_scores_resume_id ON ats_scores(resume_id);
