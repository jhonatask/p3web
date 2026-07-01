-- Tabela do aluno. O cadastro e feito uma unica vez pelo link publico da
-- turma (identificada pelo token_cadastro), sem autenticacao. O CPF e unico
-- globalmente (nao apenas por turma), pois o aluno pertence a uma unica
-- turma (turma_id e FK simples). O CPF e armazenado em texto puro nesta
-- fase - ver README para a justificativa dessa decisao.
-- O UUID e gerado pela aplicacao (Hibernate @UuidGenerator), nao pelo banco.
create table aluno (
    id uuid not null,
    nome varchar(255) not null,
    cpf varchar(11) not null,
    celular varchar(20) not null,
    campus varchar(255) not null,
    turma_id uuid not null,
    data_cadastro timestamp not null,
    consentimento_lgpd boolean not null,
    consentimento_lgpd_timestamp timestamp,
    constraint pk_aluno primary key (id),
    constraint fk_aluno_turma foreign key (turma_id) references turma (id),
    constraint uk_aluno_cpf unique (cpf)
);
