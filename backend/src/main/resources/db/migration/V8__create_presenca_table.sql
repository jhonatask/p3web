-- Tabela de presenca, separada de avaliacao/interesse (fases futuras) para
-- que o registro de presenca nunca dependa do aluno responder qualquer
-- pesquisa. Um aluno so pode ter uma presenca por aula (uk_presenca_aluno_aula).
-- validado_por referencia o usuario_admin (professor) que fez a marcacao
-- manual; fica nulo quando o metodo e QRCODE (check-in feito pelo proprio
-- aluno). O UUID e gerado pela aplicacao (Hibernate @UuidGenerator).
create table presenca (
    id uuid not null,
    aluno_id uuid not null,
    aula_id uuid not null,
    timestamp_checkin timestamp not null,
    metodo varchar(20) not null,
    validado_por uuid,
    constraint pk_presenca primary key (id),
    constraint fk_presenca_aluno foreign key (aluno_id) references aluno (id),
    constraint fk_presenca_aula foreign key (aula_id) references aula (id),
    constraint fk_presenca_validado_por foreign key (validado_por) references usuario_admin (id),
    constraint uk_presenca_aluno_aula unique (aluno_id, aula_id)
);
