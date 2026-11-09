-- Consultores fictícios para desenvolvimento e demonstração (não roda em produção, ver FLYWAY_LOCATIONS).
-- Senha de todos: datalens123 (hash BCrypt)
INSERT INTO consultores (nome, email, senha_hash, area, meta) VALUES
    ('Ana Souza',      'ana.souza@exemplo.com',      '$2a$10$DRBzOYbQbWjk1wQDq77qT.hm2TeCfaUj/SgJ2dDWDeK7Vey2IAH/S', 'Indústria',   50000.00),
    ('Bruno Lima',     'bruno.lima@exemplo.com',     '$2a$10$DRBzOYbQbWjk1wQDq77qT.hm2TeCfaUj/SgJ2dDWDeK7Vey2IAH/S', 'Comércio',    40000.00),
    ('Carla Mendes',   'carla.mendes@exemplo.com',   '$2a$10$DRBzOYbQbWjk1wQDq77qT.hm2TeCfaUj/SgJ2dDWDeK7Vey2IAH/S', 'Serviços',    45000.00),
    ('Diego Ferreira', 'diego.ferreira@exemplo.com', '$2a$10$DRBzOYbQbWjk1wQDq77qT.hm2TeCfaUj/SgJ2dDWDeK7Vey2IAH/S', 'Agronegócio', 30000.00);
