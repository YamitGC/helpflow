-- V1: Creación de tablas principales para usuarios, técnicos, tickets e historiales

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    sede VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE technicians (
    id BIGSERIAL PRIMARY KEY,
    documento VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    salario NUMERIC(12, 2) NOT NULL,
    fecha_afiliacion DATE NOT NULL,
    especialidad VARCHAR(50) NOT NULL,
    sede VARCHAR(100) NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ACTIVO',
    max_tickets_activos INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    prioridad VARCHAR(50) NOT NULL,
    sede VARCHAR(100) NOT NULL,
    activo_afectado VARCHAR(150),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_sla TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    fecha_primera_respuesta TIMESTAMP WITHOUT TIME ZONE,
    fecha_resolucion TIMESTAMP WITHOUT TIME ZONE,
    estado VARCHAR(50) NOT NULL DEFAULT 'ABIERTO',
    solicitante_id BIGINT NOT NULL,
    tecnico_id BIGINT,
    CONSTRAINT fk_tickets_solicitante FOREIGN KEY (solicitante_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_tecnico FOREIGN KEY (tecnico_id) REFERENCES technicians(id) ON DELETE SET NULL
);

CREATE TABLE ticket_histories (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    usuario_accion_id BIGINT,
    usuario_accion_nombre VARCHAR(150),
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    observaciones TEXT,
    fecha_cambio TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_histories_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

