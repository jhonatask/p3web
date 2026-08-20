-- Reset de dados da chamada anterior (Fase 2/3, feita antes do mapa de
-- execucao detalhado da Escola de Hombridade existir): o modelo antigo de
-- Aula (numero/modulo soltos por turma, sem tipo_aula) nao tem mapeamento
-- limpo para o catalogo tipo_aula novo. Este e um refactor de dominio
-- aprovado (nao uma migracao de dados de producao), entao limpamos as
-- tabelas dependentes em vez de tentar reconciliar dados de teste.
-- interesse e removido por completo aqui (feature aposentada - as perguntas
-- de intencao que ele capturava ja sao do cadastro inicial, nao da chamada).
drop table interesse;

delete from avaliacao;
delete from presenca;
delete from aula;

-- Aula passa a ser a "sessao": turma + tipo_aula (catalogo) + data + status
-- (AGENDADA/ATIVA/ENCERRADA, sincronizado pela agenda da turma). Sem mais
-- janela de horario nem qr_token por aula - o QR code passa a ser estatico,
-- por turma (reaproveita turma.token_cadastro).
alter table aula
    add column tipo_aula_id uuid,
    add column status varchar(20) not null default 'AGENDADA',
    add column encerrada_manualmente boolean not null default false;

alter table aula add constraint fk_aula_tipo_aula foreign key (tipo_aula_id) references tipo_aula (id);
alter table aula alter column tipo_aula_id set not null;

alter table aula drop constraint uk_aula_qr_token;
alter table aula drop column numero;
alter table aula drop column modulo;
alter table aula drop column janela_abertura;
alter table aula drop column janela_fechamento;
alter table aula drop column qr_token;

alter table aula add constraint uk_aula_turma_tipo_aula unique (turma_id, tipo_aula_id);
