-- V2: Índices para optimizar consultas frecuentes, relaciones, asignaciones y SLA

CREATE INDEX idx_tickets_solicitante ON tickets(solicitante_id);
CREATE INDEX idx_tickets_tecnico ON tickets(tecnico_id);
CREATE INDEX idx_tickets_estado ON tickets(estado);
CREATE INDEX idx_tickets_sla ON tickets(fecha_limite_sla);
CREATE INDEX idx_tickets_categoria ON tickets(categoria);
CREATE INDEX idx_tickets_prioridad ON tickets(prioridad);

CREATE INDEX idx_histories_ticket ON ticket_histories(ticket_id);
CREATE INDEX idx_technicians_especialidad ON technicians(especialidad);
CREATE INDEX idx_technicians_sede ON technicians(sede);
CREATE INDEX idx_users_correo ON users(correo);

