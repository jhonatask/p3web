-- ============================================================================
-- PAINEL DA FAMILY - Migration 0001: schema core (identidade e acesso)
-- Nucleo_Login_Registro_v0.1 / ADR-003 / ADR-004
-- Regras centrais:
--   * Pessoa e a identidade unica do dominio; auth_user_id NULLABLE (pessoas
--     importadas podem existir sem conta e serem reivindicadas depois).
--   * Papel 'fiel' e o unico auto-atribuido (signup aberto). Demais papeis
--     sao concedidos por admin e auditados.
--   * RLS deny-by-default em todas as tabelas.
--   * Reivindicacao de Pessoa por e-mail SO ocorre com e-mail CONFIRMADO
--     (fecha o vetor de pre-account takeover — achado R-10).
-- ============================================================================

create schema if not exists core;

grant usage on schema core to authenticated, service_role;

-- ---------------------------------------------------------------------------
-- PESSOA
-- ---------------------------------------------------------------------------
create table core.pessoa (
  id            uuid primary key default gen_random_uuid(),
  auth_user_id  uuid unique references auth.users (id) on delete set null,
  nome          text not null,
  email         text,
  criado_em     timestamptz not null default now(),
  atualizado_em timestamptz not null default now()
);

-- e-mail unico ignorando caixa; multiplos NULL permitidos (importados sem e-mail)
create unique index pessoa_email_unico on core.pessoa (lower(email)) where email is not null;

-- ---------------------------------------------------------------------------
-- PAPEL (catalogo; tabela em vez de enum para ser extensivel sem migration)
-- ---------------------------------------------------------------------------
create table core.papel (
  codigo    text primary key,
  nome      text not null,
  descricao text
);

insert into core.papel (codigo, nome, descricao) values
  ('fiel',        'Fiel',           'Papel inicial de todo cadastro. Acesso a eventos, promocoes e servicos abertos.'),
  ('membro',      'Membro',         'Membro da Family Church.'),
  ('servo',       'Servo',          'Servo de um ministerio (escopo: ministerio).'),
  ('lider',       'Lider',          'Lider (escopo: ministerio ou GC).'),
  ('professor',   'Professor',      'Professor da Academy (escopo: turma).'),
  ('aluno',       'Aluno',          'Aluno da Academy (escopo: turma).'),
  ('admin',       'Administrador',  'Administracao do Painel da Family.'),
  ('pastor',      'Pastor',         'Pastor.'),
  ('pastor_rede', 'Pastor de Rede', 'Pastor de rede (escopo: rede).');

-- ---------------------------------------------------------------------------
-- ATRIBUICAO papel+escopo (escopo polimorfico; revogacao preserva historico)
-- ---------------------------------------------------------------------------
create table core.atribuicao (
  id            uuid primary key default gen_random_uuid(),
  pessoa_id     uuid not null references core.pessoa (id) on delete cascade,
  papel         text not null references core.papel (codigo),
  scope_type    text,  -- ex.: 'turma', 'ministerio', 'rede'; NULL = global
  scope_id      text,  -- id no dominio do modulo (text acomoda uuid/int/ids Bitrix)
  concedido_por uuid references core.pessoa (id),
  concedido_em  timestamptz not null default now(),
  revogado_em   timestamptz,
  constraint escopo_completo check ((scope_type is null) = (scope_id is null))
);

-- uma atribuicao ATIVA por (pessoa, papel, escopo); historico revogado nao bloqueia
create unique index atribuicao_ativa_unica
  on core.atribuicao (pessoa_id, papel, coalesce(scope_type, ''), coalesce(scope_id, ''))
  where revogado_em is null;

create index atribuicao_pessoa on core.atribuicao (pessoa_id) where revogado_em is null;

-- ---------------------------------------------------------------------------
-- AUDITORIA (append-only; leitura so por service_role/admin — RNF-N02)
-- ---------------------------------------------------------------------------
create table core.auditoria (
  id            bigint generated always as identity primary key,
  ocorrido_em   timestamptz not null default now(),
  actor_auth_id uuid,           -- auth.uid() do ator; NULL = sistema/trigger
  acao          text not null,  -- 'pessoa.provisionada', 'pessoa.reivindicada', 'atribuicao.concedida', ...
  entidade      text not null,
  entidade_id   text,
  detalhe       jsonb
);

revoke update, delete on core.auditoria from authenticated, anon;

-- ---------------------------------------------------------------------------
-- Helpers de autorizacao
-- ---------------------------------------------------------------------------
create or replace function core.pessoa_do_usuario()
returns uuid
language sql stable security definer set search_path = ''
as $$
  select id from core.pessoa where auth_user_id = (select auth.uid())
$$;

create or replace function core.tem_papel(p_papel text, p_scope_type text default null, p_scope_id text default null)
returns boolean
language sql stable security definer set search_path = ''
as $$
  select exists (
    select 1
    from core.atribuicao a
    where a.pessoa_id = core.pessoa_do_usuario()
      and a.papel = p_papel
      and a.revogado_em is null
      and (p_scope_type is null or (a.scope_type = p_scope_type and a.scope_id = p_scope_id))
  )
$$;

create or replace function core.is_admin()
returns boolean
language sql stable security definer set search_path = ''
as $$ select core.tem_papel('admin') $$;

-- ---------------------------------------------------------------------------
-- Provisionamento fiel-first (trigger em auth.users)
--   INSERT com e-mail ja confirmado (ex.: OAuth Google)  -> provisiona
--   UPDATE quando email_confirmed_at passa de NULL -> ts  -> provisiona
--   Reivindica Pessoa pre-existente pelo e-mail (confirmado) sem conta.
--   Falha do provisionamento e FAIL-LOUD de proposito no piloto: melhor
--   bloquear um signup que criar conta sem Pessoa.
-- ---------------------------------------------------------------------------
create or replace function core.provisionar_usuario(p_user_id uuid, p_email text, p_nome text)
returns void
language plpgsql security definer set search_path = ''
as $$
declare
  v_pessoa uuid;
  v_acao   text;
begin
  -- idempotente: ja provisionado
  select id into v_pessoa from core.pessoa where auth_user_id = p_user_id;
  if v_pessoa is not null then
    return;
  end if;

  -- reivindicacao por e-mail confirmado (Pessoa importada sem conta)
  select id into v_pessoa
  from core.pessoa
  where lower(email) = lower(p_email) and auth_user_id is null;

  if v_pessoa is not null then
    update core.pessoa
    set auth_user_id = p_user_id, atualizado_em = now()
    where id = v_pessoa;
    v_acao := 'pessoa.reivindicada';
  else
    insert into core.pessoa (auth_user_id, nome, email)
    values (p_user_id, coalesce(nullif(trim(p_nome), ''), split_part(p_email, '@', 1)), p_email)
    returning id into v_pessoa;
    v_acao := 'pessoa.provisionada';
  end if;

  -- fiel: unico papel auto-atribuido
  insert into core.atribuicao (pessoa_id, papel)
  values (v_pessoa, 'fiel')
  on conflict do nothing;

  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
  values (p_user_id, v_acao, 'pessoa', v_pessoa::text, jsonb_build_object('origem', 'auth'));
end;
$$;

create or replace function core.handle_auth_user_insert()
returns trigger
language plpgsql security definer set search_path = ''
as $$
begin
  if new.email_confirmed_at is not null then
    perform core.provisionar_usuario(
      new.id, new.email,
      coalesce(new.raw_user_meta_data ->> 'nome', new.raw_user_meta_data ->> 'full_name', new.raw_user_meta_data ->> 'name')
    );
  end if;
  return new;
end;
$$;

create or replace function core.handle_auth_user_confirm()
returns trigger
language plpgsql security definer set search_path = ''
as $$
begin
  if old.email_confirmed_at is null and new.email_confirmed_at is not null then
    perform core.provisionar_usuario(
      new.id, new.email,
      coalesce(new.raw_user_meta_data ->> 'nome', new.raw_user_meta_data ->> 'full_name', new.raw_user_meta_data ->> 'name')
    );
  end if;
  return new;
end;
$$;

create trigger core_on_auth_user_created
  after insert on auth.users
  for each row execute function core.handle_auth_user_insert();

create trigger core_on_auth_user_confirmed
  after update on auth.users
  for each row execute function core.handle_auth_user_confirm();

-- ---------------------------------------------------------------------------
-- Concessao/revogacao de papel (RF-N07) — sempre via RPC, sempre auditada
-- ---------------------------------------------------------------------------
create or replace function core.conceder_papel(p_pessoa uuid, p_papel text, p_scope_type text default null, p_scope_id text default null)
returns uuid
language plpgsql security definer set search_path = ''
as $$
declare v_id uuid;
begin
  if not core.is_admin() then
    raise exception 'apenas admin pode conceder papeis';
  end if;
  if p_papel = 'fiel' then
    raise exception 'fiel e atribuido automaticamente no cadastro';
  end if;

  insert into core.atribuicao (pessoa_id, papel, scope_type, scope_id, concedido_por)
  values (p_pessoa, p_papel, p_scope_type, p_scope_id, core.pessoa_do_usuario())
  returning id into v_id;

  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
  values ((select auth.uid()), 'atribuicao.concedida', 'atribuicao', v_id::text,
          jsonb_build_object('pessoa', p_pessoa, 'papel', p_papel, 'scope_type', p_scope_type, 'scope_id', p_scope_id));
  return v_id;
end;
$$;

create or replace function core.revogar_papel(p_atribuicao uuid)
returns void
language plpgsql security definer set search_path = ''
as $$
begin
  if not core.is_admin() then
    raise exception 'apenas admin pode revogar papeis';
  end if;

  update core.atribuicao set revogado_em = now()
  where id = p_atribuicao and revogado_em is null;

  if not found then
    raise exception 'atribuicao inexistente ou ja revogada';
  end if;

  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id)
  values ((select auth.uid()), 'atribuicao.revogada', 'atribuicao', p_atribuicao::text);
end;
$$;

-- ---------------------------------------------------------------------------
-- RLS deny-by-default (RNF-N01)
-- ---------------------------------------------------------------------------
alter table core.pessoa     enable row level security;
alter table core.papel      enable row level security;
alter table core.atribuicao enable row level security;
alter table core.auditoria  enable row level security;

-- catalogo de papeis: leitura para autenticados
create policy papel_select on core.papel
  for select to authenticated using (true);

-- pessoa: ve e edita (nome) a propria; admin ve todas
create policy pessoa_select_propria on core.pessoa
  for select to authenticated
  using (auth_user_id = (select auth.uid()) or core.is_admin());

create policy pessoa_update_propria on core.pessoa
  for update to authenticated
  using (auth_user_id = (select auth.uid()))
  with check (auth_user_id = (select auth.uid()));

-- atribuicao: ve as proprias; admin ve todas. Escrita SO via RPCs acima.
create policy atribuicao_select_propria on core.atribuicao
  for select to authenticated
  using (pessoa_id = core.pessoa_do_usuario() or core.is_admin());

-- auditoria: nenhuma policy para authenticated => invisivel (service_role bypassa)

-- grants de tabela (RLS restringe as linhas)
grant select, update (nome, atualizado_em) on core.pessoa to authenticated;
grant select on core.papel to authenticated;
grant select on core.atribuicao to authenticated;
grant execute on function core.conceder_papel, core.revogar_papel, core.tem_papel, core.pessoa_do_usuario, core.is_admin to authenticated;

-- anon: nada alem de usage (deny-by-default)

-- ============================================================================
-- PAINEL DA FAMILY - Migration 0002: endurecimento de seguranca
-- Origem: Revisao de Seguranca 14/07/2026 (achados #2, #7, #11, #12, #16).
-- Nao altera a migration 0001 (ja aplicada); corrige por cima.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- #2 [ALTA] provisionar_usuario (SECURITY DEFINER, escreve identidade) estava
--   executavel por PUBLIC via PostgREST RPC. Revogar de todos os roles de
--   requisicao; os triggers (owner) mantem execute implicito.
--   + postura padrao: nenhuma funcao nova em core e executavel por PUBLIC.
-- ---------------------------------------------------------------------------
revoke execute on function core.provisionar_usuario(uuid, text, text) from public;
revoke execute on function core.handle_auth_user_insert() from public;
revoke execute on function core.handle_auth_user_confirm() from public;
alter default privileges in schema core revoke execute on functions from public;

-- ---------------------------------------------------------------------------
-- #16 [BAIXA] Nome vem de raw_user_meta_data (controlado pelo usuario no signup
--   publico). Sanitizar na fronteira: remover caracteres de controle e limitar
--   tamanho. Recria provisionar_usuario identica a 0001 + sanitizacao do nome.
-- ---------------------------------------------------------------------------
create or replace function core.provisionar_usuario(p_user_id uuid, p_email text, p_nome text)
returns void
language plpgsql security definer set search_path = ''
as $$
declare
  v_pessoa uuid;
  v_acao   text;
  v_nome   text;
begin
  select id into v_pessoa from core.pessoa where auth_user_id = p_user_id;
  if v_pessoa is not null then
    return;
  end if;

  -- sanitiza: tira controle, colapsa espacos, limita a 120 chars
  v_nome := left(
    btrim(regexp_replace(coalesce(p_nome, ''), '[[:cntrl:]]+', ' ', 'g')),
    120
  );
  if v_nome = '' then
    v_nome := split_part(p_email, '@', 1);
  end if;

  select id into v_pessoa
  from core.pessoa
  where lower(email) = lower(p_email) and auth_user_id is null;

  if v_pessoa is not null then
    update core.pessoa
    set auth_user_id = p_user_id, atualizado_em = now()
    where id = v_pessoa;
    v_acao := 'pessoa.reivindicada';
  else
    insert into core.pessoa (auth_user_id, nome, email)
    values (p_user_id, v_nome, p_email)
    returning id into v_pessoa;
    v_acao := 'pessoa.provisionada';
  end if;

  insert into core.atribuicao (pessoa_id, papel)
  values (v_pessoa, 'fiel')
  on conflict do nothing;

  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
  values (p_user_id, v_acao, 'pessoa', v_pessoa::text, jsonb_build_object('origem', 'auth'));
end;
$$;

-- re-revoga apos o create or replace (o replace restaura o default PUBLIC)
revoke execute on function core.provisionar_usuario(uuid, text, text) from public;

-- ---------------------------------------------------------------------------
-- #11 [MEDIA] Papel 'admin' concedido COM escopo virava admin GLOBAL (is_admin
--   ignora escopo). Duas travas: (a) is_admin exige linha admin SEM escopo;
--   (b) conceder_papel proibe 'admin' com escopo.
-- ---------------------------------------------------------------------------
create or replace function core.is_admin()
returns boolean
language sql stable security definer set search_path = ''
as $$
  select exists (
    select 1 from core.atribuicao a
    where a.pessoa_id = core.pessoa_do_usuario()
      and a.papel = 'admin'
      and a.revogado_em is null
      and a.scope_type is null
  )
$$;

create or replace function core.conceder_papel(p_pessoa uuid, p_papel text, p_scope_type text default null, p_scope_id text default null)
returns uuid
language plpgsql security definer set search_path = ''
as $$
declare v_id uuid;
begin
  if not core.is_admin() then
    raise exception 'apenas admin pode conceder papeis';
  end if;
  if p_papel = 'fiel' then
    raise exception 'fiel e atribuido automaticamente no cadastro';
  end if;
  if p_papel = 'admin' and p_scope_type is not null then
    raise exception 'admin e sempre global (nao aceita escopo)';
  end if;

  insert into core.atribuicao (pessoa_id, papel, scope_type, scope_id, concedido_por)
  values (p_pessoa, p_papel, p_scope_type, p_scope_id, core.pessoa_do_usuario())
  returning id into v_id;

  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
  values ((select auth.uid()), 'atribuicao.concedida', 'atribuicao', v_id::text,
          jsonb_build_object('pessoa', p_pessoa, 'papel', p_papel, 'scope_type', p_scope_type, 'scope_id', p_scope_id));
  return v_id;
end;
$$;

revoke execute on function core.is_admin() from public;
revoke execute on function core.conceder_papel(uuid, text, text, text) from public;
grant execute on function core.is_admin() to authenticated;
grant execute on function core.conceder_papel(uuid, text, text, text) to authenticated;

-- ---------------------------------------------------------------------------
-- #7 [ALTA] Helper para RLS por-linha nos MODULOS sem reconsultar a pessoa a
--   cada linha. Retorna o conjunto de escopos uma vez (STABLE) -> o planner
--   avalia como subconsulta unica.
--   Uso no modulo: using ( turma_id in (select scope_id from core.escopos_do_papel('professor')) )
-- ---------------------------------------------------------------------------
create or replace function core.escopos_do_papel(p_papel text)
returns table (scope_id text)
language sql stable security definer set search_path = ''
as $$
  select a.scope_id
  from core.atribuicao a
  where a.pessoa_id = core.pessoa_do_usuario()
    and a.papel = p_papel
    and a.revogado_em is null
    and a.scope_id is not null
$$;

revoke execute on function core.escopos_do_papel(text) from public;
grant execute on function core.escopos_do_papel(text) to authenticated;

-- ---------------------------------------------------------------------------
-- #12 [MEDIA] Indice para a consulta reversa 'quem tem papel X no escopo Y'
--   (ex.: listar alunos/professores de uma turma) — evita seq scan.
-- ---------------------------------------------------------------------------
create index if not exists atribuicao_por_escopo
  on core.atribuicao (papel, scope_type, scope_id)
  where revogado_em is null;

-- ============================================================================
-- PAINEL DA FAMILY - Migration 0005: primitiva de convite/claim de identidade
-- Atende o Pedido 1 de docs/pedidos-modulo-academy-ao-nucleo.md (decisoes do
-- Lucas 15/07). Mecanismo GENERICO do nucleo (Academy e o 1o cliente).
-- Design completo: docs/design-convite-claim.md
--
-- Seguranca:
--  * token guardado so como sha256 (o cru so aparece no retorno da emissao);
--  * prova (2o fator, ex.: 4 digitos do CPF) guardada so como sha256(salt||prova);
--    o valor cru NUNCA entra em tabela, log ou auditoria; rate limit + erro generico;
--  * SQL dinamico restrito ao registro core.vinculo_tipo (identificadores %I,
--    valores parametrizados) — sem injecao;
--  * RLS deny-by-default; escrita so via estas RPCs SECURITY DEFINER.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Registro (whitelist) de tabelas externas vinculaveis a uma Pessoa.
-- ----------------------------------------------------------------------------
create table if not exists core.vinculo_tipo (
  tipo          text primary key,
  schema_nome   text not null,
  tabela        text not null,
  pk_coluna     text not null,
  pessoa_coluna text not null,
  descricao     text,
  criado_em     timestamptz not null default now()
);

-- ----------------------------------------------------------------------------
-- Convites
-- ----------------------------------------------------------------------------
create table if not exists core.convite (
  id             uuid primary key default gen_random_uuid(),
  token_hash     text not null unique,
  payload        jsonb not null,
  requer_prova   boolean not null default false,
  prova_hash     text,
  prova_salt     text,
  status         text not null default 'pendente' check (status in ('pendente','aceito','revogado')),
  expira_em      timestamptz not null,
  tentativas     integer not null default 0,
  max_tentativas integer not null default 5,
  emitido_por    uuid references core.pessoa (id),
  aceito_por     uuid references core.pessoa (id),
  criado_em      timestamptz not null default now(),
  aceito_em      timestamptz,
  revogado_em    timestamptz
);

create index if not exists convite_pendente on core.convite (status) where status = 'pendente';

alter table core.vinculo_tipo enable row level security;
alter table core.convite enable row level security;
-- sem policy para authenticated/anon: invisiveis fora das RPCs (service_role herda
-- DML via default privileges da migration 0003).

-- ----------------------------------------------------------------------------
-- registrar_vinculo_tipo (admin) — registra um tipo vinculavel
-- ----------------------------------------------------------------------------
create or replace function core.registrar_vinculo_tipo(
  p_tipo text, p_schema text, p_tabela text, p_pk_coluna text, p_pessoa_coluna text, p_descricao text default null
)
returns void
language plpgsql security definer set search_path = ''
as $$
begin
  if not core.is_admin() then
    raise exception 'apenas admin pode registrar tipo de vinculo';
  end if;
  -- valida que a tabela/colunas existem (evita registrar alvo inexistente)
  if not exists (
    select 1 from information_schema.columns
    where table_schema = p_schema and table_name = p_tabela and column_name = p_pessoa_coluna
  ) or not exists (
    select 1 from information_schema.columns
    where table_schema = p_schema and table_name = p_tabela and column_name = p_pk_coluna
  ) then
    raise exception 'tabela/coluna inexistente para o tipo de vinculo';
  end if;

  insert into core.vinculo_tipo (tipo, schema_nome, tabela, pk_coluna, pessoa_coluna, descricao)
  values (p_tipo, p_schema, p_tabela, p_pk_coluna, p_pessoa_coluna, p_descricao)
  on conflict (tipo) do update
    set schema_nome = excluded.schema_nome, tabela = excluded.tabela,
        pk_coluna = excluded.pk_coluna, pessoa_coluna = excluded.pessoa_coluna,
        descricao = excluded.descricao;
end;
$$;

-- ----------------------------------------------------------------------------
-- emitir_convite (admin) -> retorna o token CRU uma unica vez
-- ----------------------------------------------------------------------------
create or replace function core.emitir_convite(
  p_payload jsonb,
  p_expira_em timestamptz default now() + interval '7 days',
  p_prova text default null
)
returns text
language plpgsql security definer set search_path = ''
as $$
declare
  v_token   text;
  v_hash    text;
  v_salt    text;
  v_phash   text;
  v_elem    jsonb;
  v_papel   text;
  v_st      text;
  v_sid     text;
  v_tipo    text;
begin
  if not core.is_admin() then
    raise exception 'apenas admin pode emitir convite';
  end if;

  -- validade: nunca no passado, teto de 30 dias (evita convite eterno se o
  -- modulo chamador passar uma data errada — cicatriz de bug de TZ)
  if p_expira_em <= now() then
    raise exception 'expira_em nao pode estar no passado';
  end if;
  if p_expira_em > now() + interval '30 days' then
    raise exception 'validade maxima do convite e 30 dias';
  end if;

  -- valida papeis
  if jsonb_typeof(p_payload -> 'papeis') is distinct from 'array' then
    raise exception 'payload.papeis deve ser um array';
  end if;
  for v_elem in select * from jsonb_array_elements(p_payload -> 'papeis') loop
    v_papel := v_elem ->> 'papel';
    v_st := v_elem ->> 'scope_type';
    v_sid := v_elem ->> 'scope_id';
    if v_papel is null or not exists (select 1 from core.papel where codigo = v_papel) then
      raise exception 'papel invalido no payload: %', v_papel;
    end if;
    if v_papel in ('admin', 'fiel') then
      raise exception 'papel % nao pode ser concedido por convite', v_papel;
    end if;
    if (v_st is null) <> (v_sid is null) then
      raise exception 'scope_type e scope_id devem vir juntos';
    end if;
  end loop;

  -- valida vinculos (todos os tipos precisam estar registrados)
  if p_payload ? 'vinculos' and jsonb_typeof(p_payload -> 'vinculos') is distinct from 'array' then
    raise exception 'payload.vinculos deve ser um array';
  end if;
  for v_elem in select * from jsonb_array_elements(coalesce(p_payload -> 'vinculos', '[]'::jsonb)) loop
    v_tipo := v_elem ->> 'tipo';
    if v_tipo is null or not exists (select 1 from core.vinculo_tipo where tipo = v_tipo) then
      raise exception 'tipo de vinculo nao registrado: %', v_tipo;
    end if;
    if (v_elem ->> 'pk') is null then
      raise exception 'vinculo sem pk';
    end if;
  end loop;

  v_token := encode(extensions.gen_random_bytes(24), 'hex');
  v_hash := encode(extensions.digest(v_token, 'sha256'), 'hex');

  if p_prova is not null then
    v_salt := encode(extensions.gen_random_bytes(16), 'hex');
    v_phash := encode(extensions.digest(v_salt || p_prova, 'sha256'), 'hex');
  end if;

  insert into core.convite (token_hash, payload, requer_prova, prova_hash, prova_salt, expira_em, emitido_por)
  values (v_hash, p_payload, p_prova is not null, v_phash, v_salt, p_expira_em, core.pessoa_do_usuario());

  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
  values ((select auth.uid()), 'convite.emitido', 'convite', v_hash,
          jsonb_build_object('requer_prova', p_prova is not null,
                             'papeis', p_payload -> 'papeis',
                             'vinculos', p_payload -> 'vinculos'));  -- SEM a prova

  return v_token;
end;
$$;

-- ----------------------------------------------------------------------------
-- validar_convite (authenticated) — nao consome; so info de exibicao
-- ----------------------------------------------------------------------------
create or replace function core.validar_convite(p_token text)
returns jsonb
language plpgsql security definer set search_path = ''
as $$
declare
  v_hash text;
  v_c    core.convite;
begin
  v_hash := encode(extensions.digest(p_token, 'sha256'), 'hex');
  select * into v_c from core.convite where token_hash = v_hash;

  if not found or v_c.status <> 'pendente' or v_c.expira_em < now() then
    return jsonb_build_object('valido', false, 'motivo', 'invalido');
  end if;

  return jsonb_build_object(
    'valido', true,
    'requer_prova', v_c.requer_prova,
    'descricao', coalesce(v_c.payload ->> 'descricao', 'Convite do Painel da Family'),
    'prova_label', v_c.payload ->> 'prova_label'
  );
end;
$$;

-- ----------------------------------------------------------------------------
-- aceitar_convite (authenticated) — transacao unica, atomica
-- ----------------------------------------------------------------------------
create or replace function core.aceitar_convite(p_token text, p_prova text default null)
returns jsonb
language plpgsql security definer set search_path = ''
as $$
declare
  v_hash    text;
  v_c       core.convite;
  v_pessoa  uuid;
  v_elem    jsonb;
  v_tipo    text;
  v_pk      text;
  vt        core.vinculo_tipo;
  v_existe  boolean;
  v_rows    integer;
  v_cnt     integer := 0;
begin
  v_pessoa := core.pessoa_do_usuario();
  if v_pessoa is null then
    raise exception 'sessao invalida (faca login)';
  end if;

  v_hash := encode(extensions.digest(p_token, 'sha256'), 'hex');
  select * into v_c from core.convite where token_hash = v_hash for update;

  if not found or v_c.status <> 'pendente' or v_c.expira_em < now() then
    return jsonb_build_object('ok', false, 'motivo', 'invalido');
  end if;

  -- 2o fator (prova) — rate limited + auditado (sem NUNCA tocar a prova crua)
  if v_c.requer_prova then
    if v_c.tentativas >= v_c.max_tentativas then
      insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id)
      values ((select auth.uid()), 'convite.bloqueado', 'convite', v_c.id::text);
      return jsonb_build_object('ok', false, 'motivo', 'bloqueado');
    end if;
    if p_prova is null
       or encode(extensions.digest(v_c.prova_salt || p_prova, 'sha256'), 'hex') <> v_c.prova_hash then
      update core.convite set tentativas = tentativas + 1 where id = v_c.id;
      insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id)
      values ((select auth.uid()), 'convite.prova_falha', 'convite', v_c.id::text);
      return jsonb_build_object('ok', false, 'motivo', 'prova_invalida');  -- generico
    end if;
  end if;

  -- Aplicacao ATOMICA. O UPDATE guardado (`... and (pessoa is null or pessoa = eu)`)
  -- é o ponto de serializacao: sob concorrencia, quem chega depois nao casa o WHERE
  -- e recebe conflito (elimina o TOCTOU da checagem-antes-de-escrever). Erros
  -- intencionais viram retorno limpo via subtransacao (sqlstate customizado);
  -- erros reais propagam para a server action logar.
  begin
    for v_elem in select * from jsonb_array_elements(coalesce(v_c.payload -> 'vinculos', '[]'::jsonb)) loop
      v_tipo := v_elem ->> 'tipo';
      v_pk := v_elem ->> 'pk';
      select * into vt from core.vinculo_tipo where tipo = v_tipo;
      if not found then
        raise exception 'tipo de vinculo nao registrado: %', v_tipo;
      end if;
      execute format(
        'update %I.%I set %I = $1 where %I::text = $2 and (%I is null or %I = $1)',
        vt.schema_nome, vt.tabela, vt.pessoa_coluna, vt.pk_coluna, vt.pessoa_coluna, vt.pessoa_coluna)
        using v_pessoa, v_pk;
      get diagnostics v_rows = row_count;
      if v_rows = 0 then
        -- 0 linhas: ou o registro nao existe, ou ja e de OUTRA pessoa
        execute format('select exists(select 1 from %I.%I where %I::text = $1)', vt.schema_nome, vt.tabela, vt.pk_coluna)
          into v_existe using v_pk;
        if v_existe then
          raise exception using errcode = 'PF001';  -- conflito
        else
          raise exception using errcode = 'PF002';  -- vinculo inexistente
        end if;
      end if;
      insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
      values ((select auth.uid()), 'vinculo.aplicado', 'convite', v_c.id::text,
              jsonb_build_object('tipo', v_tipo, 'pk', v_pk, 'pessoa', v_pessoa));
    end loop;

    -- concede os papeis do payload (autorizado pelo token, nao por is_admin).
    -- so conta/audita quando REALMENTE insere (on conflict do nothing) — trilha veridica.
    for v_elem in select * from jsonb_array_elements(coalesce(v_c.payload -> 'papeis', '[]'::jsonb)) loop
      insert into core.atribuicao (pessoa_id, papel, scope_type, scope_id, concedido_por)
      values (v_pessoa, v_elem ->> 'papel', v_elem ->> 'scope_type', v_elem ->> 'scope_id', v_c.emitido_por)
      on conflict do nothing;
      get diagnostics v_rows = row_count;
      if v_rows > 0 then
        v_cnt := v_cnt + 1;
        insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
        values ((select auth.uid()), 'atribuicao.concedida_por_convite', 'atribuicao', v_c.id::text,
                jsonb_build_object('papel', v_elem ->> 'papel', 'scope_type', v_elem ->> 'scope_type',
                                   'scope_id', v_elem ->> 'scope_id', 'convite', v_c.id));
      end if;
    end loop;

    update core.convite set status = 'aceito', aceito_por = v_pessoa, aceito_em = now() where id = v_c.id;
    insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id, detalhe)
    values ((select auth.uid()), 'convite.aceito', 'convite', v_c.id::text,
            jsonb_build_object('pessoa', v_pessoa));
  exception
    when sqlstate 'PF001' then return jsonb_build_object('ok', false, 'motivo', 'conflito');
    when sqlstate 'PF002' then return jsonb_build_object('ok', false, 'motivo', 'vinculo_inexistente');
  end;

  return jsonb_build_object('ok', true, 'papeis', v_cnt,
                            'descricao', coalesce(v_c.payload ->> 'descricao', 'Acesso liberado'));
end;
$$;

-- ----------------------------------------------------------------------------
-- revogar_convite (admin)
-- ----------------------------------------------------------------------------
create or replace function core.revogar_convite(p_convite uuid)
returns void
language plpgsql security definer set search_path = ''
as $$
begin
  if not core.is_admin() then
    raise exception 'apenas admin pode revogar convite';
  end if;
  update core.convite set status = 'revogado', revogado_em = now()
  where id = p_convite and status = 'pendente';
  if not found then
    raise exception 'convite inexistente ou nao pendente';
  end if;
  insert into core.auditoria (actor_auth_id, acao, entidade, entidade_id)
  values ((select auth.uid()), 'convite.revogado', 'convite', p_convite::text);
end;
$$;

-- ----------------------------------------------------------------------------
-- Grants: RPCs executaveis por authenticated (guard interno onde e admin-only).
-- emitir/registrar/revogar tem guard is_admin() dentro. validar/aceitar sao do
-- fluxo do usuario logado.
-- ----------------------------------------------------------------------------
revoke execute on function core.registrar_vinculo_tipo(text, text, text, text, text, text) from public;
revoke execute on function core.emitir_convite(jsonb, timestamptz, text) from public;
revoke execute on function core.validar_convite(text) from public;
revoke execute on function core.aceitar_convite(text, text) from public;
revoke execute on function core.revogar_convite(uuid) from public;

grant execute on function core.registrar_vinculo_tipo(text, text, text, text, text, text) to authenticated;
grant execute on function core.emitir_convite(jsonb, timestamptz, text) to authenticated;
grant execute on function core.validar_convite(text) to authenticated;
grant execute on function core.aceitar_convite(text, text) to authenticated;
grant execute on function core.revogar_convite(uuid) to authenticated;

-- ============================================================================
-- PAINEL DA FAMILY - Migration 0007: leitura para o painel de admin
-- O painel /admin lista pessoas, convites e auditoria. Escrita (conceder papel,
-- emitir/revogar convite) ja passa pelas RPCs SECURITY DEFINER com guard
-- is_admin() — o app NAO usa service_role. Falta so a LEITURA: core.convite,
-- core.vinculo_tipo e core.auditoria nao tinham policy p/ authenticated
-- (invisiveis). Aqui: select SO para admin (RLS filtra as linhas) + grant de
-- tabela (RLS continua sendo a barreira real; nao-admin ve 0 linhas).
--   core.pessoa e core.atribuicao ja sao lidas por admin (policies da 0001).
-- ============================================================================

create policy convite_admin_le on core.convite
  for select to authenticated using (core.is_admin());

create policy vinculo_tipo_admin_le on core.vinculo_tipo
  for select to authenticated using (core.is_admin());

create policy auditoria_admin_le on core.auditoria
  for select to authenticated using (core.is_admin());

grant select on core.convite to authenticated;
grant select on core.vinculo_tipo to authenticated;
grant select on core.auditoria to authenticated;
