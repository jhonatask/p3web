-- Usuario admin semente para permitir testar o login localmente, ja que esta
-- tarefa nao inclui endpoint de cadastro de admin (isso e escopo da Fase 4 do
-- roadmap, "Gerenciar acessos"). Credenciais documentadas no README do backend
-- e destinadas apenas a ambiente local/dev.
insert into usuario_admin (id, nome, email, senha, papel)
values (
    'ca287c71-0602-4690-a88a-b8047c997723',
    'Administrador Local',
    'admin@hombridade.local',
    '$2a$10$YFXXFbWexPwXlA3s4NyfuOU6/43Av6Kd4iNFtMSmjZRpLjrg3X2zO',
    'ADMIN'
);
