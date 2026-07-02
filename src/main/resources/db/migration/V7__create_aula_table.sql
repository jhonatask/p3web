-- Tabela da aula, vinculada a uma turma. Cada aula recebe um qr_token unico,
-- gerado pela aplicacao no momento da criacao, usado para montar o QR code
-- dinamico exibido em sala e validado pelo endpoint publico de check-in
-- (/api/publico/checkin). A janela de abertura/fechamento delimita o periodo
-- em que o check-in por QR code (ou marcacao manual) e aceito para essa aula.
-- O UUID e gerado pela aplicacao (Hibernate @UuidGenerator), nao pelo banco.
create table aula (
    id uuid not null,
    turma_id uuid not null,
    numero integer not null,
    modulo varchar(255),
    data date not null,
    janela_abertura timestamp not null,
    janela_fechamento timestamp not null,
    qr_token varchar(20) not null,
    constraint pk_aula primary key (id),
    constraint fk_aula_turma foreign key (turma_id) references turma (id),
    constraint uk_aula_qr_token unique (qr_token)
);
