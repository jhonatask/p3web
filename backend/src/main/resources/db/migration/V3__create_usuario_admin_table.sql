-- Tabela do usuario administrativo (Professor/Admin/Visualizador), usada para
-- autenticacao via Spring Security + JWT.
-- O vinculo com Turma ("turmas_vinculadas") sera adicionado em uma migration
-- futura, quando a entidade Turma existir.
-- O UUID e gerado pela aplicacao (Hibernate @UuidGenerator), nao pelo banco.
create table usuario_admin (
    id uuid not null,
    nome varchar(255) not null,
    email varchar(255) not null,
    senha varchar(255) not null,
    papel varchar(20) not null,
    constraint pk_usuario_admin primary key (id),
    constraint uk_usuario_admin_email unique (email)
);
