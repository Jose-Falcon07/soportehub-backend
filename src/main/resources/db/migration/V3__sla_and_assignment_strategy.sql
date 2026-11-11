alter table organizations
    add column assignment_strategy_code varchar(30) not null default 'MANUAL';

create table sla_policies (
    id                uuid primary key,
    organization_id   uuid        not null references organizations (id),
    priority          varchar(20) not null,
    hours_to_resolve  integer     not null,
    unique (organization_id, priority)
);
