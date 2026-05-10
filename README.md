# autoApply

A Spring Boot backend that helps job seekers optimize their resumes for ATS (Applicant Tracking Systems). Upload a resume, paste a job description, and get an AI-powered keyword analysis plus a scored breakdown of how well your resume matches the role.

## Features

- **Auth** — Email/password signup & login, Google OAuth2, GitHub OAuth2. All methods issue a JWT for stateless API access.
- **Resume Upload** — PDF upload stored in Supabase Storage. Parsed asynchronously into structured JSON (contact info, skills, experience, education).
- **JD Keyword Extraction** — Paste any job description; an AI model extracts roles, technical skills, soft skills, must-have keywords, and experience requirements. Results are cached by content hash to avoid redundant AI calls.
- **ATS Scoring** — Scores your resume against a job description across 6 categories (keyword match, formatting, action verbs, quantification, contact completeness, length/density) out of 100 points with actionable recommendations.
- **Multi-AI Provider** — Switch between OpenAI, Anthropic (Claude), and Google Gemini by changing one environment variable.

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 17, Spring Boot 4.0.6 |
| Security | Spring Security 6, JWT (JJWT 0.12.6), OAuth2 |
| Database | PostgreSQL (Supabase), Spring Data JPA, Flyway |
| Session | Spring Session JDBC |
| Storage | Supabase Storage (REST API via WebClient) |
| AI | Spring AI 2.0.0-M5 — OpenAI / Anthropic / Gemini |
| PDF Parsing | Spring AI PDF Document Reader, Apache Tika |
| Build | Gradle |

## Project Structure

```
src/main/java/com/autoapply/
├── config/               # Security, WebClient, AI provider factory, AppProperties
├── controller/           # REST endpoints
├── dto/                  # Request / response DTOs
├── entity/               # JPA entities
├── exception/            # Global exception handler
├── repository/           # Spring Data repositories
├── security/             # JWT filter
├── service/
│   ├── auth/             # JWT, local auth, OAuth2/OIDC user services
│   ├── ats/              # ATS scoring engine + 6 scorer implementations
│   ├── jd/               # JD extraction service + JdKeywords model
│   └── resume/           # Upload, async parsing, Supabase storage
└── util/                 # HashUtil, TextNormalizer
```

## API Endpoints

All endpoints except auth require `Authorization: Bearer <token>`.

### Auth
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/auth/providers` | No | List available sign-in methods |
| POST | `/api/v1/auth/register` | No | Email + password signup → returns JWT |
| POST | `/api/v1/auth/login` | No | Email + password login → returns JWT |
| GET | `/oauth2/authorization/google` | No | Start Google OAuth2 flow (browser) |
| GET | `/oauth2/authorization/github` | No | Start GitHub OAuth2 flow (browser) |
| GET | `/api/v1/auth/me` | Yes | Get current user |
| POST | `/api/v1/auth/logout` | Yes | Invalidate session |

### User Profile
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/users/profile` | Get profile (target roles, skills, locations) |
| PUT | `/api/v1/users/profile` | Update profile |

### Resumes
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/resumes/upload` | Upload PDF (`multipart/form-data`, field: `file`). Returns immediately with `parseStatus: PENDING`; poll after ~3s for `PARSED`. |
| GET | `/api/v1/resumes` | List all resumes |
| GET | `/api/v1/resumes/{id}` | Get resume by ID (includes `parsedJson` once parsed) |
| DELETE | `/api/v1/resumes/{id}` | Delete resume |

### JD Extraction
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/jd/extract` | Extract structured keywords from a job description. Cached by content hash. |

**Request body:**
```json
{ "jdText": "We are hiring a Senior Java Engineer..." }
```

**Response:**
```json
{
  "roles": ["Senior Java Engineer"],
  "technicalSkills": ["Java", "Spring Boot", "AWS", "Docker"],
  "softSkills": ["Agile", "communication", "teamwork"],
  "mustHaveKeywords": ["CI/CD pipelines"],
  "niceToHaveKeywords": ["Kubernetes"],
  "resumeKeywords": ["led", "optimized"],
  "experienceRequired": "5+ years",
  "cachedResult": false
}
```

### ATS Scoring
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/ats/score` | Score a resume against a job description |
| GET | `/api/v1/ats/scores` | List all scores for current user |
| GET | `/api/v1/ats/scores/{id}` | Get score by ID |

**Request body:**
```json
{ "resumeId": "<uuid>", "jdText": "We are hiring..." }
```

**Response:**
```json
{
  "totalScore": 72,
  "maxScore": 100,
  "categoryBreakdown": [
    { "category": "KEYWORD_MATCH", "rawScore": 28, "maxScore": 35, "recommendation": "..." },
    { "category": "FORMATTING",    "rawScore": 16, "maxScore": 20, "recommendation": "..." },
    { "category": "ACTION_VERBS",  "rawScore": 10, "maxScore": 15, "recommendation": "..." },
    { "category": "QUANTIFICATION","rawScore":  6, "maxScore": 10, "recommendation": "..." },
    { "category": "CONTACT",       "rawScore":  7, "maxScore": 10, "recommendation": "..." },
    { "category": "LENGTH_DENSITY","rawScore":  5, "maxScore": 10, "recommendation": "..." }
  ]
}
```

## Setup

### 1. Prerequisites

- Java 17+
- Gradle (or use the wrapper `./gradlew`)
- A [Supabase](https://supabase.com) project
- An API key for at least one AI provider (OpenAI, Anthropic, or Gemini)
- Google and/or GitHub OAuth2 app credentials (optional — only needed for OAuth login)

### 2. Supabase Setup

1. Create a new Supabase project.
2. In **Project Settings → Database**, note your host, port, and password.
3. In **Project Settings → API**, copy:
   - `anon` public key → `SUPABASE_ANON_KEY`
   - `service_role` key → `SUPABASE_SERVICE_ROLE_KEY`
   - Both keys start with `eyJ...` (they are JWTs).
4. In **Storage**, create a bucket named `resumes` (or any name — set via `SUPABASE_STORAGE_BUCKET`).
5. Run the Flyway migrations on first startup by setting `FLYWAY_ENABLED=true` (then set it back to `false`).

### 3. Environment Variables

Create a `.env` file or export these in your shell. **Never commit secrets to git.**

```bash
# ── Database ────────────────────────────────────────────────────────────
SUPABASE_DB_HOST=db.xxxxxxxxxxxx.supabase.co
SUPABASE_DB_PASSWORD=your_db_password

# ── Supabase Storage ─────────────────────────────────────────────────────
SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
SUPABASE_SERVICE_ROLE_KEY=eyJ...   # service_role JWT from Project Settings → API
SUPABASE_ANON_KEY=eyJ...           # anon JWT from Project Settings → API

# ── JWT ──────────────────────────────────────────────────────────────────
# Generate: openssl rand -base64 64
JWT_SECRET=your_long_random_secret

# ── AI Provider (choose one) ─────────────────────────────────────────────
AI_PROVIDER=openai                 # openai | anthropic | gemini

OPENAI_API_KEY=sk-...              # platform.openai.com/api-keys
# ANTHROPIC_API_KEY=sk-ant-...     # console.anthropic.com/settings/keys
# GEMINI_API_KEY=AIza...           # aistudio.google.com/app/apikey

# ── OAuth2 (optional — needed only for Google/GitHub login) ──────────────
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

### 4. Run

```bash
./gradlew bootRun
```

The API is available at `http://localhost:8080`.

### 5. First-time DB Migration

On first run (empty database), set `FLYWAY_ENABLED=true` to create all tables:

```bash
FLYWAY_ENABLED=true ./gradlew bootRun
```

After tables are created you can set it back to `false`.

## OAuth2 Flow (Browser → JWT)

OAuth2 login is browser-initiated. After completing the consent screen, the server responds with JSON containing a JWT you can use for all subsequent API calls:

```json
{ "token": "eyJ...", "userId": "uuid", "email": "user@example.com" }
```

Paste this token into Postman (or any client) as a Bearer token.

## Postman Collection

Import `autoApply.postman_collection.json` from the project root. The collection:
- Uses `{{baseUrl}}` (default `http://localhost:8080`) and `{{token}}` variables.
- Auto-saves the JWT from any login/register response into `{{token}}`.
- All authenticated requests automatically send `Authorization: Bearer {{token}}`.

## AI Provider Details

| Provider | `AI_PROVIDER` | Model default | Key env var |
|---|---|---|---|
| OpenAI (default) | `openai` | `gpt-4o-mini` | `OPENAI_API_KEY` |
| Anthropic (Claude) | `anthropic` | `claude-sonnet-4-6` | `ANTHROPIC_API_KEY` |
| Google Gemini | `gemini` | `gemini-2.0-flash` | `GEMINI_API_KEY` |

Override the model with `OPENAI_MODEL`, `ANTHROPIC_MODEL`, or `GEMINI_MODEL`.

## JD Extraction Cache

Keyword extraction results are cached in PostgreSQL keyed by a SHA-256 hash of the normalized job description text. Identical or trivially different JDs (extra spaces, different casing) return the cached result instantly without calling the AI. The response includes `"cachedResult": true/false`.
