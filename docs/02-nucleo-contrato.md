# Contrato do Núcleo — Painel da Family
## v0.1 · 06/07/2026

Este documento é a **única interface** entre o núcleo e os módulos (regra 2 dos
Briefings). Módulos consomem o núcleo por aqui; **não** acessam `core.*` direto
nem reimplementam auth/identidade. Mudanças neste contrato exigem atualização
deste doc + aviso à outra frente.

---

## 1. Identidade e acesso — modelo

- **Conta** (Supabase Auth): e-mail/senha primário + Google OAuth, vinculação por
  e-mail verificado (ADR-003).
- **Pessoa** (`core.pessoa`): identidade única do domínio. `auth_user_id` é
  **nullable** — Pessoas importadas (ex.: alunos vindos do Bitrix) existem sem
  conta e são **reivindicadas automaticamente** no primeiro login com o mesmo
  e-mail **confirmado**.
- **Atribuição** (`core.atribuicao`): `(pessoa, papel, escopo)` acumuláveis.
  Escopo polimórfico: `scope_type` (`'turma'`, `'ministerio'`, `'rede'`, ...) +
  `scope_id` (**text** — acomoda uuid/int/ids Bitrix). `NULL/NULL` = global.
- **Fiel-first**: todo cadastro ganha `fiel` automaticamente (trigger).
  Qualquer outro papel só existe por concessão de admin — sempre auditada.
- Revogação é **soft** (`revogado_em`): histórico preservado; helpers e RLS só
  consideram atribuições ativas.

## 2. API TypeScript (server-side) — `@/core/contrato`

```ts
import { getSessaoPessoa, temPapel, escoposDoPapel } from '@/core/contrato';

const sessao = await getSessaoPessoa();
// null => sem sessão válida (redirecione para /entrar)
// { pessoa: Pessoa, atribuicoes: Atribuicao[] } => atribuições ATIVAS

temPapel(sessao.atribuicoes, 'professor');                                  // em qualquer escopo
temPapel(sessao.atribuicoes, 'professor', { scopeType: 'turma', scopeId }); // na turma
escoposDoPapel(sessao.atribuicoes, 'professor'); // [{ scopeType: 'turma', scopeId: '...' }]
```

Tipos em `@/core/tipos` (`Pessoa`, `Atribuicao`, `PapelCodigo`, `SessaoPessoa`).

## 3. API SQL (RLS e RPCs) — para policies de módulos

| Função | Uso |
|---|---|
| `core.pessoa_do_usuario()` | `uuid` da Pessoa do usuário logado (para policies) |
| `core.tem_papel(papel, scope_type, scope_id)` | `boolean` — atribuição ativa? Escopo `NULL` = qualquer |
| `core.is_admin()` | atalho para `core.tem_papel('admin')` |
| `core.conceder_papel(pessoa, papel, scope_type, scope_id)` | **RPC** (só admin) — concede e audita |
| `core.revogar_papel(atribuicao)` | **RPC** (só admin) — revoga (soft) e audita |

Exemplo de policy de módulo (Academy):

```sql
create policy professor_le_sua_turma on academy.turma
  for select to authenticated
  using (core.tem_papel('professor', 'turma', id::text));
```

## 4. Regras para módulos

1. **Schema próprio** (ex.: `academy`), migrations no diretório único
   `supabase/migrations/` (timestamp no nome evita colisão entre frentes).
2. FK para Pessoa: `references core.pessoa (id)`. Nunca criar Pessoa por fora —
   se o módulo importa gente (sync Bitrix), a criação de Pessoa entra por
   função do núcleo (a definir quando o sync existir — registrar necessidade
   aqui antes de implementar).
3. RLS deny-by-default em TODA tabela nova; policies via `core.tem_papel`.
4. Fluxos públicos (ex.: formulário QR de presença) ou privilegiados: rota
   server-side própria do módulo; `SUPABASE_SERVICE_ROLE_KEY` só no servidor.
5. Registrar o módulo na casca: adicionar entrada em `src/core/modulos.ts`
   (`acesso` + `comoDestravar`). É a única edição permitida em `src/core/` —
   qualquer outra necessidade vira pedido à frente do núcleo.
6. Auditoria: eventos sensíveis do módulo gravam em `core.auditoria` via
   inserts server-side (service role) — nunca do cliente.

## 5. Rotas do núcleo (não colidir)

`/`, `/entrar`, `/registrar`, `/recuperar-senha`, `/atualizar-senha`,
`/auth/confirm`, `/auth/callback`, `/painel`. Módulos montam em rota própria
(ex.: `/academy/...`).

## 6. Critério de pronto da Frente A (Briefings) — ✅ concluído 14/07/2026

- [x] Estrutura núcleo/módulos + fronteiras (este contrato)
- [x] Migration `core` (Pessoa, Papel, Atribuição, auditoria, RLS, fiel-first)
      + endurecimento (0002) + grants service_role (0003)
- [x] Telas login/registro/recuperação (pt-BR, mobile-first)
- [x] Casca com visibilidade+bloqueio
- [x] Projeto Supabase provisionado e migrations aplicadas (sa-east-1)
- [x] Google OAuth configurado + vinculação por e-mail **testada e segura** (R-10)
- [x] Testes automatizados (Vitest): RLS, fiel-first, caso Amanda, travas de admin,
      open redirect — `npm test`, 13 verdes
- [x] Repositório no GitHub (`Family-Church-Dev/painel-family`, privado)

**Pendente antes do handoff completo:** as 2 primitivas da seção 4 do
`handoff-modulo-academy.md` (criar Pessoa sem login / conceder papel professor),
que dependem do modelo de permissões que o Lucas está definindo.
