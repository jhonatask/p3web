-- E-mail do cadastro base: faltava desde a V11 original. Nullable porque
-- cadastros ja existentes nao tem esse dado e nao ha como fazer backfill -
-- a obrigatoriedade para cadastros novos/atualizados fica no Bean Validation
-- de ParticipanteCadastroRequest, mesmo padrao ja usado para os demais
-- campos obrigatorios do formulario.
alter table participante add column email varchar(255);
