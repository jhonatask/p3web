-- Extrai nome/cpf/celular de aluno para a nova entidade participante (base
-- unica por CPF, compartilhada entre ministerios). Aluno passa a ser so o
-- registro de matricula na Escola de Hombridade, referenciando participante
-- por FK. gen_random_uuid() e nativo do PostgreSQL 16 (sem extensao). Este
-- INSERT...SELECT/UPDATE funciona tanto com 0 quanto com N linhas existentes
-- em aluno.
alter table aluno add column participante_id uuid;

insert into participante (id, nome, cpf, celular)
select gen_random_uuid(), nome, cpf, celular from aluno;

update aluno a
set participante_id = p.id
from participante p
where p.cpf = a.cpf;

alter table aluno alter column participante_id set not null;

alter table aluno
    add constraint fk_aluno_participante foreign key (participante_id) references participante (id);

-- Preserva o comportamento atual (uk_aluno_cpf era unico globalmente: uma
-- pessoa so podia ser aluno de uma turma na vida toda). A constraint
-- equivalente pos-refactor e unique em aluno.participante_id.
alter table aluno add constraint uk_aluno_participante unique (participante_id);

alter table aluno drop constraint uk_aluno_cpf;

alter table aluno drop column nome;
alter table aluno drop column cpf;
alter table aluno drop column celular;
