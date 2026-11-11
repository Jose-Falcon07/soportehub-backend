-- Relación muchos a muchos entre usuarios y roles (P1 la usa para el login, P2 para las invitaciones).
create table user_roles (
    user_id  uuid        not null references users (id),
    role     varchar(30) not null,
    primary key (user_id, role)
);
