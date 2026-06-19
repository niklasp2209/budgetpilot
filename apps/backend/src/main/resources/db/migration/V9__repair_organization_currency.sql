ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) DEFAULT 'EUR';

UPDATE organizations
SET currency = 'EUR'
WHERE currency IS NULL;

ALTER TABLE organizations
    ALTER COLUMN currency SET DEFAULT 'EUR';

ALTER TABLE organizations
    ALTER COLUMN currency SET NOT NULL;
