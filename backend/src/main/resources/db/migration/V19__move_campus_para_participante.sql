-- Campus passa a viver no cadastro base (Participante), nao mais em Aluno -
-- e o unico jeito de a chamada mostrar "Ola, Fulano" sem reperguntar campus,
-- ja que ele passa a vir do cadastro (por CPF), nao mais de um formulario de
-- matricula por turma. Backfill a partir dos dados atuais de aluno antes de
-- a coluna sair de la. Aluno tambem perde o proprio LGPD (o consentimento
-- que vale e o de Participante, ja gravado no cadastro base) e vira so a
-- matricula (participante x turma de origem), criada automaticamente na
-- primeira chamada normal - por isso data_cadastro vira data_matricula.
alter table participante add column campus varchar(255);

update participante p
set campus = a.campus
from aluno a
where a.participante_id = p.id;

alter table aluno drop column campus;
alter table aluno drop column consentimento_lgpd;
alter table aluno drop column consentimento_lgpd_timestamp;
alter table aluno rename column data_cadastro to data_matricula;
