-- V3: Datos semilla de prueba (Usuarios y Técnicos de demostración)
-- Contraseña verificada con BCrypt para 'admin123': $2a$10$AvSkEwnGTKt3olQQWYp0TONjqoO0GTpYLvHs3rZ1Bmxe.OkfFByRi

INSERT INTO users (documento, nombre, correo, password, sede, area, rol, estado) VALUES
('1001', 'Administrador General', 'admin@helpflow.com', '$2a$10$AvSkEwnGTKt3olQQWYp0TONjqoO0GTpYLvHs3rZ1Bmxe.OkfFByRi', 'Medellin', 'TI Infraestructura', 'ROLE_ADMIN', 'ACTIVO'),
('1002', 'Carlos Perez', 'carlos.perez@helpflow.com', '$2a$10$AvSkEwnGTKt3olQQWYp0TONjqoO0GTpYLvHs3rZ1Bmxe.OkfFByRi', 'Medellin', 'Operaciones', 'ROLE_USUARIO', 'ACTIVO'),
('1003', 'Laura Gomez', 'laura.gomez@helpflow.com', '$2a$10$AvSkEwnGTKt3olQQWYp0TONjqoO0GTpYLvHs3rZ1Bmxe.OkfFByRi', 'Bogota', 'Finanzas', 'ROLE_USUARIO', 'ACTIVO'),
('1004', 'Usuario Inactivo', 'inactivo@helpflow.com', '$2a$10$AvSkEwnGTKt3olQQWYp0TONjqoO0GTpYLvHs3rZ1Bmxe.OkfFByRi', 'Cali', 'Logística', 'ROLE_USUARIO', 'INACTIVO');

INSERT INTO technicians (documento, nombre, correo, salario, fecha_afiliacion, especialidad, sede, estado, max_tickets_activos) VALUES
('TEC-001', 'Mateo Redes', 'mateo.redes@helpflow.com', 3500000.00, '2023-01-15', 'RED', 'Medellin', 'ACTIVO', 5),
('TEC-002', 'Sofia Hardware', 'sofia.hw@helpflow.com', 3200000.00, '2023-03-20', 'HARDWARE', 'Medellin', 'ACTIVO', 5),
('TEC-003', 'Andres Software', 'andres.sw@helpflow.com', 3800000.00, '2022-11-10', 'SOFTWARE', 'Bogota', 'ACTIVO', 5),
('TEC-004', 'Camila Accesos', 'camila.acc@helpflow.com', 3000000.00, '2024-02-01', 'ACCESOS', 'Bogota', 'ACTIVO', 5),
('TEC-014', 'Julian Soporte General', 'julian.soporte@helpflow.com', 3100000.00, '2023-08-14', 'OTRO', 'Medellin', 'ACTIVO', 5);

