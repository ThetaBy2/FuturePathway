-- ═══════════════════════════════════════════════════════════════════
-- Future Pathway · V1 — initial schema (PostgreSQL 15+)
-- NOTE: `app_user` is an addition to §3 of the spec: one login table for all
-- four roles (STUDENT / COLLEGE / COMPANY / ADMIN) so JWT auth is unified.
-- STUDENT.name/email/password_hash from the spec live on app_user; the
-- student row holds the academic profile (1:1 via student.user_id).
-- ═══════════════════════════════════════════════════════════════════

-- ── Users & organisations ────────────────────────────────────────────
CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL
                  CHECK (role IN ('STUDENT','COLLEGE','COMPANY','ADMIN')),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE college (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT REFERENCES app_user (id) ON DELETE SET NULL UNIQUE,
    name              VARCHAR(160) NOT NULL,
    address           VARCHAR(500),
    tpo_contact_email VARCHAR(160),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE company (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT REFERENCES app_user (id) ON DELETE SET NULL UNIQUE,
    name             VARCHAR(160) NOT NULL,
    industry_domain  VARCHAR(120),
    website          VARCHAR(255),
    hr_contact_email VARCHAR(160),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Student profile ──────────────────────────────────────────────────
CREATE TABLE student (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE UNIQUE,
    college_id        BIGINT REFERENCES college (id) ON DELETE SET NULL,
    roll_no           VARCHAR(40) NOT NULL UNIQUE,
    branch            VARCHAR(80) NOT NULL,
    batch_year        INT         NOT NULL,
    cgpa              DECIMAL(4,2) CHECK (cgpa IS NULL OR (cgpa >= 0 AND cgpa <= 10)),
    backlog_count     INT         NOT NULL DEFAULT 0 CHECK (backlog_count >= 0),
    cv_url            VARCHAR(500),
    profile_photo_url VARCHAR(500),
    resume_text       TEXT, -- plain-text CV supplied by the student; input for Gemini
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Skills ───────────────────────────────────────────────────────────
CREATE TABLE skill (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    category    VARCHAR(20)  NOT NULL CHECK (category IN ('TECHNICAL','SOFT')),
    description VARCHAR(1000),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE student_skill (
    id                BIGSERIAL PRIMARY KEY,
    student_id        BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    skill_id          BIGINT NOT NULL REFERENCES skill (id) ON DELETE CASCADE,
    proficiency_level INT    NOT NULL CHECK (proficiency_level BETWEEN 1 AND 5),
    source            VARCHAR(20) NOT NULL DEFAULT 'SELF_DECLARED'
                      CHECK (source IN ('SELF_DECLARED','GEMINI_DERIVED','VERIFIED')),
    verified_by       VARCHAR(160),
    verified_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, skill_id)
);

CREATE TABLE project (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    tech_stack  VARCHAR(500),
    repo_link   VARCHAR(500),
    duration    VARCHAR(100),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE certificate (
    id                  BIGSERIAL PRIMARY KEY,
    student_id          BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    title               VARCHAR(200) NOT NULL,
    issuing_org         VARCHAR(200),
    issue_date          DATE,
    credential_url      VARCHAR(500),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Gemini outputs ───────────────────────────────────────────────────
CREATE TABLE skill_analysis (
    id            BIGSERIAL PRIMARY KEY,
    student_id    BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    strong_skills JSONB  NOT NULL DEFAULT '[]',  -- [{skill, level, evidence}]
    weak_skills   JSONB  NOT NULL DEFAULT '[]',  -- [{skill, gap, suggestion}]
    summary_text  TEXT,
    generated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE roadmap (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    target_role VARCHAR(160) NOT NULL,
    milestones  JSONB  NOT NULL DEFAULT '[]',    -- [{week, title, tasks[], resources[]}]
    generated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Jobs & applications ──────────────────────────────────────────────
CREATE TABLE job (
    id                BIGSERIAL PRIMARY KEY,
    company_id        BIGINT NOT NULL REFERENCES company (id) ON DELETE CASCADE,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    job_domain        VARCHAR(120),
    min_cgpa          DECIMAL(4,2) CHECK (min_cgpa IS NULL OR (min_cgpa >= 0 AND min_cgpa <= 10)),
    max_backlogs      INT CHECK (max_backlogs IS NULL OR max_backlogs >= 0),
    eligible_branches JSONB NOT NULL DEFAULT '[]',   -- ["CSE","IT", ...]; [] = all
    package_lpa       DECIMAL(6,2) CHECK (package_lpa IS NULL OR package_lpa >= 0),
    deadline          DATE,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE job_skill (
    job_id               BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    skill_id             BIGINT NOT NULL REFERENCES skill (id) ON DELETE CASCADE,
    required_proficiency INT    NOT NULL DEFAULT 3 CHECK (required_proficiency BETWEEN 1 AND 5),
    PRIMARY KEY (job_id, skill_id)
);

CREATE TABLE application (
    id               BIGSERIAL PRIMARY KEY,
    student_id       BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    job_id           BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE,
    status           VARCHAR(20) NOT NULL DEFAULT 'APPLIED'
                     CHECK (status IN ('APPLIED','SHORTLISTED','INTERVIEWED','SELECTED','REJECTED')),
    match_score      DECIMAL(5,2) CHECK (match_score IS NULL OR (match_score >= 0 AND match_score <= 100)),
    match_reasoning  TEXT,  -- Gemini's explanation of the match score
    applied_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (student_id, job_id)
);

-- ── Interviews ───────────────────────────────────────────────────────
CREATE TABLE interview_process (
    id                 BIGSERIAL PRIMARY KEY,
    job_id             BIGINT NOT NULL REFERENCES job (id) ON DELETE CASCADE UNIQUE,
    rounds             JSONB  NOT NULL DEFAULT '[]',  -- [{round:"Aptitude"}, ...]
    selection_criteria JSONB  NOT NULL DEFAULT '{}',  -- {min_cgpa, branches[], max_backlogs}
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE interview_slot (
    id                   BIGSERIAL PRIMARY KEY,
    interview_process_id BIGINT NOT NULL REFERENCES interview_process (id) ON DELETE CASCADE,
    student_id           BIGINT NOT NULL REFERENCES student (id) ON DELETE CASCADE,
    round_name           VARCHAR(120) NOT NULL,
    scheduled_at         TIMESTAMPTZ NOT NULL,
    venue_or_link        VARCHAR(500),
    status               VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                         CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED')),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ── Market trends ────────────────────────────────────────────────────
CREATE TABLE trending_skill (
    id           BIGSERIAL PRIMARY KEY,
    skill_id     BIGINT NOT NULL REFERENCES skill (id) ON DELETE CASCADE UNIQUE,
    demand_score DECIMAL(6,2) NOT NULL DEFAULT 0,
    source       VARCHAR(40)  NOT NULL DEFAULT 'JOB_POSTINGS',
    insight      TEXT,  -- Gemini market commentary (scheduler-generated)
    last_updated TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ── Indexes on frequently queried columns ────────────────────────────
CREATE INDEX idx_student_branch      ON student (branch);
CREATE INDEX idx_student_batch       ON student (batch_year);
CREATE INDEX idx_student_college     ON student (college_id);
CREATE INDEX idx_student_skill_sid   ON student_skill (student_id);
CREATE INDEX idx_student_skill_kid   ON student_skill (skill_id);
CREATE INDEX idx_project_student     ON project (student_id);
CREATE INDEX idx_certificate_student ON certificate (student_id);
CREATE INDEX idx_cert_status         ON certificate (verification_status);
CREATE INDEX idx_job_company         ON job (company_id);
CREATE INDEX idx_job_domain          ON job (job_domain);
CREATE INDEX idx_job_active          ON job (active);
CREATE INDEX idx_job_deadline        ON job (deadline);
CREATE INDEX idx_application_student ON application (student_id);
CREATE INDEX idx_application_job     ON application (job_id);
CREATE INDEX idx_application_status  ON application (status);
CREATE INDEX idx_slot_process        ON interview_slot (interview_process_id);
CREATE INDEX idx_slot_student        ON interview_slot (student_id);
CREATE INDEX idx_slot_time           ON interview_slot (scheduled_at);
CREATE INDEX idx_analysis_student    ON skill_analysis (student_id);
CREATE INDEX idx_roadmap_student     ON roadmap (student_id);
CREATE INDEX idx_trending_score      ON trending_skill (demand_score DESC);
