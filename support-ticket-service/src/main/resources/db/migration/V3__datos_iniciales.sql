-- V3: Datos semilla de prueba (Usuarios y Técnicos de demostración)
-- Contraseña cifrada con BCrypt para 'admin123' y 'user123': $2a$10$wT5gQ9/yD7iM7.lFw6bLwOP8gL9m9.7UuE576B3U1xJ6aJ9xXmC1m

INSERT INTO users (documento, nombre, correo, password, sede, area, rol, estado) VALUES
('1001', 'Administrador General', 'admin@helpflow.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtSDzxJTXGZZ88vI4WvyQ9rO.6tM2.', 'Medellin', 'TI Infraestructura', 'ROLE_ADMIN', 'ACTIVO'),
('1002', 'Carlos Perez', 'carlos.perez@helpflow.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtSDzxJTXGZZ88vI4WvyQ9rO.6tM2.', 'Medellin', 'Operaciones', 'ROLE_USUARIO', 'ACTIVO'),
('1003', 'Laura Gomez', 'laura.gomez@helpflow.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtSDzxJTXGZZ88vI4WvyQ9rO.6tM2.', 'Bogota', 'Finanzas', 'ROLE_USUARIO', 'ACTIVO'),
('1004', 'Usuario Inactivo', 'inactivo@helpflow.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtSDzxJTXGZZ88vI4WvyQ9rO.6tM2.', 'Cali', 'Logística', 'ROLE_USUARIO', 'INACTIVO');

INSERT INTO technicians (documento, nombre, correo, salario, fecha_afiliacion, especialidad, sede, estado, max_tickets_activos) VALUES
('TEC-001', 'Mateo Redes', 'mateo.redes@helpflow.com', 3500000.00, '2023-01-15', 'RED', 'Medellin', 'ACTIVO', 5),
('TEC-002', 'Sofia Hardware', 'sofia.hw@helpflow.com', 3200000.00, '2023-03-20', 'HARDWARE', 'Medellin', 'ACTIVO', 5),
('TEC-003', 'Andres Software', 'andres.sw@helpflow.com', 3800000.00, '2022-11-10', 'SOFTWARE', 'Bogota', 'ACTIVO', 5),
('TEC-004', 'Camila Accesos', 'camila.acc@helpflow.com', 3000000.00, '2024-02-01', 'ACCESOS', 'Bogota', 'ACTIVO', 5),
('TEC-014', 'Julian Soporte General', 'julian.soporte@helpflow.com', 3100000.00, '2023-08-14', 'OTRO', 'Medellin', 'ACTIVO', 5);

