CREATE TABLE organizations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    legal_name      VARCHAR(160) NOT NULL,
    tax_id          VARCHAR(40)  NOT NULL UNIQUE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    ticket_counter  INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(40) NOT NULL UNIQUE,
    display_name    VARCHAR(80) NOT NULL
);

CREATE TABLE user_accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    email           VARCHAR(180) NOT NULL,
    full_name       VARCHAR(160) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'INVITED',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_email_per_org UNIQUE (organization_id, email)
);

CREATE TABLE user_roles (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id  UUID NOT NULL REFERENCES user_accounts(id),
    role_id  UUID NOT NULL REFERENCES roles(id),
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE service_categories (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID NOT NULL REFERENCES organizations(id),
    name             VARCHAR(120) NOT NULL,
    default_priority VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
);

CREATE TABLE sla_policies (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL REFERENCES organizations(id),
    priority            VARCHAR(20) NOT NULL,
    first_response_minutes INTEGER  NOT NULL,
    resolution_minutes     INTEGER  NOT NULL,
    CONSTRAINT uq_sla_priority_per_org UNIQUE (organization_id, priority)
);

CREATE TABLE tickets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    ticket_number   INTEGER NOT NULL,
    category_id     UUID NOT NULL REFERENCES service_categories(id),
    requester_id    UUID NOT NULL REFERENCES user_accounts(id),
    assignee_id     UUID     NULL REFERENCES user_accounts(id),
    subject         VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    priority        VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    due_at          TIMESTAMPTZ  NULL,
    resolved_at     TIMESTAMPTZ  NULL,
    CONSTRAINT uq_ticket_number_per_org UNIQUE (organization_id, ticket_number)
);

CREATE TABLE ticket_comments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id   UUID NOT NULL REFERENCES tickets(id),
    author_id   UUID NOT NULL REFERENCES user_accounts(id),
    body        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ticket_status_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id    UUID NOT NULL REFERENCES tickets(id),
    from_status  VARCHAR(20) NULL,
    to_status    VARCHAR(20) NOT NULL,
    changed_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices en claves foráneas de consulta frecuente
CREATE INDEX idx_tickets_org_status  ON tickets (organization_id, status);
CREATE INDEX idx_tickets_assignee    ON tickets (assignee_id);
CREATE INDEX idx_tickets_requester   ON tickets (requester_id);
CREATE INDEX idx_tickets_category    ON tickets (category_id);
CREATE INDEX idx_comments_ticket     ON ticket_comments (ticket_id);
CREATE INDEX idx_status_hist_ticket  ON ticket_status_history (ticket_id);
CREATE INDEX idx_user_accounts_org   ON user_accounts (organization_id);

-- Supabase expone "public" por API: activar RLS (el backend no se ve afectado)
ALTER TABLE organizations          ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_accounts          ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles             ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_categories     ENABLE ROW LEVEL SECURITY;
ALTER TABLE sla_policies           ENABLE ROW LEVEL SECURITY;
ALTER TABLE tickets                ENABLE ROW LEVEL SECURITY;
ALTER TABLE ticket_comments        ENABLE ROW LEVEL SECURITY;
ALTER TABLE ticket_status_history  ENABLE ROW LEVEL SECURITY;