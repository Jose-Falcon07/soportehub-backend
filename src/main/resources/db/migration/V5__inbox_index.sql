-- Índice que sostiene la bandeja del agente (empresa + agente + estado + vencimiento).
-- Sin índice, CQRS solo ordena el código: no acelera nada.
create index idx_tickets_inbox on tickets (organization_id, assignee_id, status, due_at);
