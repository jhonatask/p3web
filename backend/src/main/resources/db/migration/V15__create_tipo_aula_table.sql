-- Catalogo de Tipo de Aula (Modulo + numero + tema), compartilhado entre
-- todas as turmas de um curso - e o que faz a reposicao valer entre turmas
-- (repor e fazer presenca no mesmo tipo_aula em outra turma). Sem professor
-- aqui: professor muda por turma/data, entao fica na sessao (aula), nao no
-- catalogo. numero=0 e a aula introdutoria, fora da regra de formatura.
-- gen_random_uuid() e nativo do PostgreSQL 16 (sem extensao), mesmo padrao
-- usado no seed de V13.
create table tipo_aula (
    id uuid not null,
    curso_id uuid not null,
    modulo varchar(20) not null,
    numero integer not null,
    tema varchar(255) not null,
    ordem integer not null,
    constraint pk_tipo_aula primary key (id),
    constraint fk_tipo_aula_curso foreign key (curso_id) references curso (id),
    constraint uk_tipo_aula_curso_numero unique (curso_id, numero)
);

-- Garante o curso "Escola de Hombridade" (pode ja existir, criado via API
-- antes desta migration rodar - nesse caso so aproveita o id existente).
insert into curso (id, nome, descricao, ativo)
select gen_random_uuid(), 'Escola de Hombridade',
       'Tres modulos (Plataforma, Protecao, Provisao) de tres aulas cada, mais aula introdutoria',
       true
where not exists (select 1 from curso where nome = 'Escola de Hombridade');

insert into tipo_aula (id, curso_id, modulo, numero, tema, ordem)
select gen_random_uuid(), c.id, v.modulo, v.numero, v.tema, v.ordem
from curso c
cross join (values
    ('INTRODUTORIO', 0, 'Aula introdutoria', 0),
    ('PLATAFORMA', 1, 'Identidade', 1),
    ('PLATAFORMA', 2, 'Carater', 2),
    ('PLATAFORMA', 3, 'Confianca', 3),
    ('PROTECAO', 4, 'Hombridade', 4),
    ('PROTECAO', 5, 'Paternidade', 5),
    ('PROTECAO', 6, 'Sacerdocio', 6),
    ('PROVISAO', 7, 'Mentalidade', 7),
    ('PROVISAO', 8, 'Prosperidade', 8),
    ('PROVISAO', 9, 'O Plano', 9)
) as v(modulo, numero, tema, ordem)
where c.nome = 'Escola de Hombridade';
