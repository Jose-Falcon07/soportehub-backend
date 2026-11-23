-- Índice que sostiene la bandeja del agente (empresa + agente + estado + vencimiento).
create index idx_tickets_inbox on tickets (organization_id, assignee_id, status, due_at);
