-- Base de la Fase 1 (ajústala si tu equipo ya tiene sus migraciones V1).
create table organizations (
    id          uuid primary key,
    name        varchar(150) not null,
    active      boolean      not null default true,
    created_at  timestamptz  not null default now()
);

create table users (
    id               uuid primary key,
    organization_id  uuid references organizations (id),   -- null solo para el Super Administrador
    email            varchar(200) not null unique,
    password_hash    varchar(100),                          -- null mientras el usuario está INVITED
    full_name        varchar(150) not null,
    status           varchar(20)  not null,
    created_at       timestamptz  not null default now()
);

create table categories (
    id               uuid primary key,
    organization_id  uuid         not null references organizations (id),
    name             varchar(100) not null
);
