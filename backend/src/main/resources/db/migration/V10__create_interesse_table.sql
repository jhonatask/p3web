-- Mesma regra de pre-requisito de check-in e unicidade por aluno+aula de
-- avaliacao (uk_interesse_aluno_aula), validada na aplicacao.
create table interesse (
    id uuid not null,
    aluno_id uuid not null,
    aula_id uuid not null,
    gc_homens boolean not null default false,
    proximo_awake boolean not null default false,
    discipulado boolean not null default false,
    constraint pk_interesse primary key (id),
    constraint fk_interesse_aluno foreign key (aluno_id) references aluno (id),
    constraint fk_interesse_aula foreign key (aula_id) references aula (id),
    constraint uk_interesse_aluno_aula unique (aluno_id, aula_id)
);
