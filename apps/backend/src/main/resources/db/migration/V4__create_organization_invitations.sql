CREATE TABLE organization_invitations (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    invited_email VARCHAR(320) NOT NULL,
    role VARCHAR(32) NOT NULL,
    token VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    declined_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE organization_invitations
    ADD CONSTRAINT uq_organization_invitations_token UNIQUE (token);

ALTER TABLE organization_invitations
    ADD CONSTRAINT fk_organization_invitations_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

ALTER TABLE organization_invitations
    ADD CONSTRAINT fk_organization_invitations_created_by
        FOREIGN KEY (created_by) REFERENCES users (id);
