CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE organizations
    ADD CONSTRAINT uq_organizations_slug UNIQUE (slug);

ALTER TABLE organizations
    ADD CONSTRAINT fk_organizations_created_by
        FOREIGN KEY (created_by) REFERENCES users (id);

CREATE TABLE organization_memberships (
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(32) NOT NULL,
    PRIMARY KEY (organization_id, user_id)
);

ALTER TABLE organization_memberships
    ADD CONSTRAINT fk_memberships_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

ALTER TABLE organization_memberships
    ADD CONSTRAINT fk_memberships_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
