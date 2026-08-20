-- Prova de consentimento LGPD do cadastro base, no mesmo espirito do que ja
-- existe em aluno.consentimento_lgpd/consentimento_lgpd_timestamp. Faltava em
-- participante: o formulario ja exigia aceiteLgpd=true, mas o timestamp do
-- aceite nao era persistido em lugar nenhum.
alter table participante
    add column consentimento_lgpd boolean,
    add column consentimento_lgpd_timestamp timestamp;
