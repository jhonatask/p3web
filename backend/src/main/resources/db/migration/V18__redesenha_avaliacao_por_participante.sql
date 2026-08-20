-- Mesma troca de aluno_id por participante_id feita em presenca (V17), pelo
-- mesmo motivo. deseja_servir/habilidades ficam nulas na maioria das aulas -
-- so sao preenchidas nas aulas 03/06/09 (3a aula de cada modulo) e, quando
-- vem preenchidas, fazem write-back direto em participante (ver
-- AvaliacaoServiceImpl).
alter table avaliacao drop constraint fk_avaliacao_aluno;
alter table avaliacao drop constraint uk_avaliacao_aluno_aula;
alter table avaliacao drop column aluno_id;

alter table avaliacao
    add column participante_id uuid,
    add column deseja_servir text,
    add column habilidades text;

alter table avaliacao add constraint fk_avaliacao_participante foreign key (participante_id) references participante (id);
alter table avaliacao alter column participante_id set not null;
alter table avaliacao add constraint uk_avaliacao_participante_aula unique (participante_id, aula_id);
