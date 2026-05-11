CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    period_start DATE NOT NULL,
    currency CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE budgets
    ADD CONSTRAINT fk_budgets_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE;

CREATE TABLE budget_items (
    id UUID PRIMARY KEY,
    budget_id UUID NOT NULL,
    category_id UUID NOT NULL,
    amount_cents BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

ALTER TABLE budget_items
    ADD CONSTRAINT fk_budget_items_budget
        FOREIGN KEY (budget_id) REFERENCES budgets (id) ON DELETE CASCADE;

ALTER TABLE budget_items
    ADD CONSTRAINT fk_budget_items_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT;

ALTER TABLE budget_items
    ADD CONSTRAINT uq_budget_items_budget_category UNIQUE (budget_id, category_id);
