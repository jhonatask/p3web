-- Tabela da turma, vinculada a um curso (ex: "Escola de Hombridade - Campus 85
-- - Turma 13"). O token_cadastro e um codigo curto e unico usado para montar o
-- link publico de cadastro do aluno (ex: app.com/cadastro/<token_cadastro>).
-- O UUID e gerado pela aplicacao (Hibernate @UuidGenerator), nao pelo banco.
create table turma (
    id uuid not null,
    curso_id uuid not null,
    nome varchar(255) not null,
    modulo_atual varchar(255),
    token_cadastro varchar(20) not null,
    status varchar(20) not null,
    constraint pk_turma primary key (id),
    constraint fk_turma_curso foreign key (curso_id) references curso (id),
    constraint uk_turma_token_cadastro unique (token_cadastro)
);
