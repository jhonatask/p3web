-- Tabela do curso/programa (ex: "Escola de Hombridade"), ao qual as turmas
-- serao vinculadas em uma migration futura.
-- O UUID e gerado pela aplicacao (Hibernate @UuidGenerator), nao pelo banco.
create table curso (
    id uuid not null,
    nome varchar(255) not null,
    descricao varchar(1000),
    ativo boolean not null default true,
    constraint pk_curso primary key (id),
    constraint uk_curso_nome unique (nome)
);
