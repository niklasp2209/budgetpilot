ALTER TABLE organizations
    ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'EUR';
