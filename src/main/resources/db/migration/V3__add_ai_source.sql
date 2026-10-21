-- Future Pathway · V3 — record which engine produced each AI artefact.
ALTER TABLE skill_analysis ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'gemini';
ALTER TABLE roadmap        ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'gemini';
