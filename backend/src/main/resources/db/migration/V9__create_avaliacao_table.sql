-- Um aluno so pode avaliar uma aula em que ja fez check-in (validado na
-- aplicacao via presenca.aluno_id+aula_id) e apenas uma vez por aula
-- (uk_avaliacao_aluno_aula). O UUID e gerado pela aplicacao (Hibernate @UuidGenerator).
create table avaliacao (
    id uuid not null,
    aluno_id uuid not null,
    aula_id uuid not null,
    nota integer not null,
    didatica varchar(20) not null,
    sugestao text,
    nps integer not null,
    constraint pk_avaliacao primary key (id),
    constraint fk_avaliacao_aluno foreign key (aluno_id) references aluno (id),
    constraint fk_avaliacao_aula foreign key (aula_id) references aula (id),
    constraint uk_avaliacao_aluno_aula unique (aluno_id, aula_id),
    constraint ck_avaliacao_nota check (nota between 0 and 10),
    constraint ck_avaliacao_nps check (nps between 0 and 10)
);
