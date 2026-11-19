-- Tablas base: empresas, roles, usuarios y categorías.
create table organizations (
    id                        uuid primary key,
    name                      varchar(150) not null,
    active                    boolean      not null default true,
    assignment_strategy_code  varchar(30)  not null default 'MANUAL',   -- MANUAL, ROUND_ROBIN o LEAST_LOADED
    created_at                timestamptz  not null default now()
);

create table roles (
    id    bigserial primary key,
    name  varchar(30) not null unique
);

-- Los cinco roles del sistema (coinciden con el enum RoleName)
insert into roles (name) values ('SUPER_ADMIN'), ('COMPANY_ADMIN'), ('SUPERVISOR'), ('AGENT'), ('REQUESTER');

create table users (
    id               uuid primary key,
    organization_id  uuid references organizations (id),   -- null solo para el Super Administrador
    email            varchar(200) not null unique,
    password_hash    varchar(100),                          -- null mientras el usuario está INVITED
    full_name        varchar(150) not null,
    status           varchar(20)  not null,
    created_at       timestamptz  not null default now()
);

create table user_roles (
    user_id  uuid   not null references users (id),
    role_id  bigint not null references roles (id),
    primary key (user_id, role_id)
);

create table categories (
    id               uuid primary key,
    organization_id  uuid         not null references organizations (id),
    name             varchar(100) not null
);
