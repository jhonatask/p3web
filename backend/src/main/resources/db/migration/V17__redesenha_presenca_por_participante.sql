-- Presenca passa a ser identificada por Participante (nao mais Aluno, que so
-- podia pertencer a uma turma para sempre - incompativel com reposicao em
-- outra turma) e ganha a turma da sessao explicitamente (pode ser diferente
-- da turma de origem do participante, no caso de reposicao) e a origem
-- (CHAMADA normal vs REPOSICAO). A tabela ja esta vazia (limpa em V16),
-- entao a troca de aluno_id por participante_id nao precisa de reconciliacao
-- de dados.
alter table presenca drop constraint fk_presenca_aluno;
alter table presenca drop constraint uk_presenca_aluno_aula;
alter table presenca drop column aluno_id;

alter table presenca
    add column participante_id uuid,
    add column turma_id uuid,
    add column origem varchar(20);

alter table presenca add constraint fk_presenca_participante foreign key (participante_id) references participante (id);
alter table presenca add constraint fk_presenca_turma foreign key (turma_id) references turma (id);
alter table presenca alter column participante_id set not null;
alter table presenca alter column turma_id set not null;
alter table presenca alter column origem set not null;

alter table presenca add constraint uk_presenca_participante_aula unique (participante_id, aula_id);
