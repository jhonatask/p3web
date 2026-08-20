-- Cadastro base do homem: base unica por CPF, reaproveitada por todos os
-- ministerios (nao presa a uma turma/curso especifico). Apenas nome/cpf sao
-- NOT NULL: o cadastro simplificado feito pelo fluxo de Aluno (Escola de
-- Hombridade) cria um registro minimo aqui, sem os demais dados do formulario
-- completo de cadastro base. A obrigatoriedade dos demais campos e aplicada
-- via Bean Validation no ParticipanteCadastroRequest, nao como constraint de
-- banco. Endereco + geo (latitude/longitude, sempre calculados no servidor
-- via Geocoding, nunca aceitos do cliente) ficam embutidos como colunas
-- simples (relacao 1:1 real, @Embeddable). O UUID e gerado pela aplicacao
-- (Hibernate @UuidGenerator).
create table participante (
    id uuid not null,
    nome varchar(255) not null,
    cpf varchar(11) not null,
    celular varchar(20),
    data_nascimento date,
    estado_civil varchar(20),
    tem_filhos boolean,
    quantidade_filhos integer,
    cep varchar(9),
    rua varchar(255),
    numero varchar(20),
    complemento varchar(255),
    bairro varchar(255),
    cidade varchar(255),
    uf varchar(2),
    ponto_referencia varchar(255),
    latitude double precision,
    longitude double precision,
    tempo_na_family varchar(20),
    batizado boolean,
    serve_ministerio boolean,
    qual_ministerio_serve varchar(255),
    instagram varchar(255),
    facebook varchar(255),
    deseja_servir text,
    habilidades text,
    participa_gc boolean,
    constraint pk_participante primary key (id),
    constraint uk_participante_cpf unique (cpf)
);

-- Multipla escolha "Quais GCs" (GC de Casais / GC de Homens): tabela de
-- elemento simples (Set<String> via @ElementCollection), nao enum fixo -
-- mesma razao ja usada em turma.modulo_atual: as opcoes sao especificas de
-- hoje e tendem a mudar.
create table participante_gc (
    participante_id uuid not null,
    gc varchar(255) not null,
    constraint fk_participante_gc_participante foreign key (participante_id) references participante (id)
);
