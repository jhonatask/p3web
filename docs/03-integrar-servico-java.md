# Integrar um serviço Java ao núcleo da Family

O `02-nucleo-contrato.md` foi escrito para módulos que rodam **dentro** do app Next.js do
Painel. A API dele é TypeScript, então não te serve. Este documento é o complemento: como um
**serviço separado** (Spring Boot, no seu caso) conversa com o núcleo.

---

## O modelo em três frases

1. Quem emite o token de login é o **Supabase Auth**, não o seu serviço.
2. Seu serviço **valida** esse token e descobre quem é a pessoa.
3. Permissão você **pergunta ao núcleo** — não decide por conta própria.

---

## 1. Autenticação: pare de emitir, passe a validar

Hoje seu `JwtService` emite o token e o `JwtAuthenticationFilter` valida o que você mesmo
emitiu. A mudança é: **o token passa a vir do Supabase Auth**, e você só valida.

- A biblioteca continua a mesma (`jjwt`).
- O que muda é a **chave**: em vez do seu segredo, você usa o **JWT secret do projeto
  Supabase** (Dashboard → Settings → API → JWT Settings).
- Projetos Supabase mais novos assinam com chave assimétrica e publicam um JWKS. **Confirme
  qual é o regime do nosso projeto antes de implementar** — muda de segredo compartilhado
  (HS256) para chave pública (RS256/ES256), e a configuração do resolvedor de chave é
  diferente. Isso é item da nossa conversa.

### O que vem dentro do token

A claim que importa é o **`sub`** — o id do usuário no Supabase Auth. Ele é o elo:

```
token.sub  ==  core.pessoa.auth_user_id  ->  core.pessoa.id
```

Com o `pessoa_id` em mãos, você tem a identidade. O resto do seu domínio referencia esse id.

## 2. Descobrir os papéis

Papel na Family é `(pessoa, papel, escopo)` — o escopo é polimórfico, então
`('servo', 'ministerio', 'p3')` já cabe no modelo hoje, sem mudar nada no núcleo.

Duas formas de consultar, e a escolha é assunto da nossa conversa:

**a) Perguntando ao banco** (mais simples, e o núcleo continua sendo a única verdade):

```sql
select a.papel, a.scope_type, a.scope_id
from core.atribuicao a
join core.pessoa p on p.id = a.pessoa_id
where p.auth_user_id = :sub
  and a.revogado_em is null;
```

**b) Usando a função pronta**, quando você só quer um sim/não:

```sql
select core.tem_papel('servo', 'ministerio', 'p3');
```

> ⚠️ `core.tem_papel()` resolve a pessoa a partir da **sessão autenticada do Postgres**. Se
> seu Spring conecta por JDBC com um usuário fixo, não existe sessão — então a opção (a),
> filtrando por `auth_user_id`, é a que funciona no seu caso. Vale confirmarmos juntos.

## 3. RLS: um aviso importante

O núcleo protege as tabelas com **RLS** (segurança em nível de linha), e ela é aplicada com
base em quem está autenticado. **Uma conexão JDBC com usuário privilegiado ignora RLS.**

Ou seja: no seu serviço, a autorização precisa acontecer **na aplicação** — você consulta os
papéis e decide. Não é errado, é o normal para serviço fora do Supabase; só precisa ser uma
decisão consciente, e vamos registrar isso como ADR.

## 4. Seu schema, seu Flyway

- Seu módulo vive no schema **`p3`**. O Flyway continua seu e cuida só dele.
- Toda tabela sua que fala de gente referencia `core.pessoa`:

```sql
create schema if not exists p3;

create table p3.participante (
  id                    uuid primary key default gen_random_uuid(),
  pessoa_id             uuid not null unique references core.pessoa (id),
  campus                text,
  consentimento_lgpd_em timestamptz,
  criado_em             timestamptz not null default now()
);
```

O **`unique` no `pessoa_id`** é a linha que garante *uma pessoa, um participante*. É ela que
impede a identidade duplicada de voltar pela porta dos fundos.

- Dependência de ordem: o schema `core` precisa existir antes do seu Flyway rodar. No
  ambiente local isso já vem pronto (próxima seção).

## 5. Rodando na sua máquina, sem depender de ninguém

O núcleo inteiro sobe local, com dados fictícios, em dois comandos. Você aponta o
`spring.datasource` do seu serviço para esse Postgres e desenvolve normalmente.

```bash
git clone https://github.com/Family-Church-Dev/painel-family.git
cd painel-family
npx supabase start      # 1a vez baixa ~4 GB de imagens
npx supabase db reset   # aplica as migrations + seed ficticio
```

Isso já foi testado do zero: **34 migrations aplicam limpas e o seed carrega** 5 pessoas
fictícias com papéis, 2 professores, 2 turmas, 3 alunos e aulas. Nenhum dado real.

O `supabase start` imprime as portas e chaves locais. As que te interessam:

| O quê | Onde |
|---|---|
| Postgres (seu `spring.datasource`) | `postgresql://postgres:postgres@127.0.0.1:54322/postgres` |
| Painel visual das tabelas | `http://127.0.0.1:54323` |
| Caixa de e-mail local (confirmação, reset) | `http://127.0.0.1:54324` |
| Segredo do JWT local | impresso como `JWT_SECRET` |

> As chaves do ambiente local são públicas e iguais para todo mundo — são padrão do Supabase
> CLI. Não confunda com as de produção.

## 6. O que nunca fazer

1. **Criar pessoa por fora do núcleo.** Se o P3 cadastra alguém que a igreja ainda não
   conhece, isso entra pelo núcleo. Como exatamente, é o que vamos definir juntos.
2. **Emitir token próprio.** Dois emissores = dois logins para o mesmo membro.
3. **Guardar papel no seu banco.** Ele muda no núcleo e o seu fica velho sem ninguém notar.
4. **Rodar contra dados reais de membro.** Desenvolvimento é sempre com o seed fictício.
5. **Commitar segredo**, nem como valor padrão no código.

## 7. O que pedir, e o que resolver sozinho

**Resolve sozinho:** tudo dentro do schema `p3`, o domínio inteiro (curso, turma, aula,
presença, avaliação, formatura, histórico), suas migrations, sua arquitetura, suas
bibliotecas.

**Pede:** papel novo no catálogo do núcleo (é um insert, sai rápido), qualquer mudança em
`core.*`, e publicação em produção.
