-- Historico de conclusoes (cursos/eventos) do participante. Cada conclusao e
-- uma linha ligada ao participante por CPF (participante_id), nao uma coluna
-- nova por curso - isso permite que cursos futuros (Turma 16, outros
-- ministerios) entrem sem alterar o schema. "data" fica nullable porque,
-- quando origem=CADASTRO, nao ha data exata de conclusao informada pelo
-- homem, apenas a declaracao de que ja concluiu. Reconciliar linhas
-- origem=CADASTRO com futuras linhas origem=CHAMADA e escopo da proxima
-- entrega (chamada), nao implementado aqui.
create table historico_item (
    id uuid not null,
    participante_id uuid not null,
    item varchar(255) not null,
    tipo varchar(10) not null,
    turma varchar(255),
    status varchar(20) not null,
    data date,
    origem varchar(10) not null,
    constraint pk_historico_item primary key (id),
    constraint fk_historico_item_participante foreign key (participante_id) references participante (id)
);
