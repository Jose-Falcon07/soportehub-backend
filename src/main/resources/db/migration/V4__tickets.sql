create table tickets (
    id               uuid primary key,
    organization_id  uuid          not null references organizations (id),
    number           integer       not null,
    requester_id     uuid          not null references users (id),
    category_id      uuid          not null references categories (id),
    assignee_id      uuid          references users (id),
    subject          varchar(200)  not null,
    description      varchar(4000) not null,
    status           varchar(30)   not null,
    priority         varchar(20)   not null,
    due_at           timestamptz   not null,
    resolved_at      timestamptz,
    created_at       timestamptz   not null,
    unique (organization_id, number)
);
create index if not exists idx_tickets_org_status on tickets (organization_id, status);

create table ticket_comments (
    id               uuid primary key,
    organization_id  uuid          not null references organizations (id),
    ticket_id        uuid          not null references tickets (id),
    author_id        uuid          not null references users (id),
    body             varchar(4000) not null,
    internal_note    boolean       not null default false,
    created_at       timestamptz   not null
);
create index idx_ticket_comments_ticket on ticket_comments (ticket_id);

-- Historial de auditoría: solo se AGREGAN filas, nunca se actualizan ni se borran (regla RN-06)
create table ticket_status_history (
    id               uuid primary key,
    organization_id  uuid         not null references organizations (id),
    ticket_id        uuid         not null references tickets (id),
    from_status      varchar(30),
    to_status        varchar(30)  not null,
    changed_by       uuid         not null references users (id),
    reason           varchar(500),
    changed_at       timestamptz  not null
);
create index idx_ticket_history_ticket on ticket_status_history (ticket_id);
