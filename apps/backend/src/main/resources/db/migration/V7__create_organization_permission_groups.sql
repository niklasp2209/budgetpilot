CREATE TABLE organization_permission_groups (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_organization_permission_groups_org_name UNIQUE (organization_id, name)
);

ALTER TABLE organization_permission_groups
    ADD CONSTRAINT fk_organization_permission_groups_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

CREATE TABLE organization_permission_group_permissions (
    group_id UUID NOT NULL,
    permission VARCHAR(64) NOT NULL,
    PRIMARY KEY (group_id, permission)
);

ALTER TABLE organization_permission_group_permissions
    ADD CONSTRAINT fk_organization_permission_group_permissions_group
        FOREIGN KEY (group_id) REFERENCES organization_permission_groups (id) ON DELETE CASCADE;

CREATE TABLE organization_member_permission_groups (
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    group_id UUID NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (organization_id, user_id, group_id)
);

ALTER TABLE organization_member_permission_groups
    ADD CONSTRAINT fk_member_permission_groups_group
        FOREIGN KEY (group_id) REFERENCES organization_permission_groups (id) ON DELETE CASCADE;

ALTER TABLE organization_member_permission_groups
    ADD CONSTRAINT fk_member_permission_groups_membership
        FOREIGN KEY (organization_id, user_id)
        REFERENCES organization_memberships (organization_id, user_id)
        ON DELETE CASCADE;

CREATE INDEX idx_organization_permission_groups_org_id
    ON organization_permission_groups (organization_id);

CREATE INDEX idx_organization_member_permission_groups_member
    ON organization_member_permission_groups (organization_id, user_id);
