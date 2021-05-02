CREATE TABLE IF NOT EXISTS translation
(
    entry_id   INT,
    language   VARCHAR(2),
    revision   INT,
    title      VARCHAR(255),
    content    TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (entry_id, language, revision)
);