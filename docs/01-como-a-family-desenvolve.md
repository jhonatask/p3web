# Como a Family desenvolve hoje — o resumo

Leitura de 5 minutos. É o mínimo pra você entender onde seu módulo se encaixa.

## Domínios

Os domínios da igreja estão no CNPJ da Family. A produção hoje ainda roda num domínio
herdado do fornecedor de infraestrutura, e existe um plano em andamento para migrar tudo
para o domínio próprio. Nada disso muda o seu trabalho — só o endereço final.

## Infraestrutura — e a parte que você precisa saber

Em **11/08/2026** a liderança da igreja separou formalmente:

- **Infra** — operada por um **fornecedor externo**
- **Desenvolvimento** — Lucas + os devs voluntários

**Nenhuma pessoa do time de Dev tem acesso ao servidor.** Nem SSH, nem leitura, nem o
Lucas. Publicar qualquer coisa é abrir um chamado com o fornecedor.

Isso não é desconfiança com ninguém — a porta é fechada igual para todos. E, na prática,
significa duas coisas para você:

1. **Deploy tem fila e prazo que não controlamos.** Você vai saber onde seu pedido está.
2. **Isso não te bloqueia.** Você desenvolve inteiro na sua máquina (ver
   `03-integrar-servico-java.md`). Publicar é a última etapa, não a primeira.

## Git

- Organização: **`Family-Church-Dev`**
- Este repositório é seu: você é dono do módulo do P3.
- Regra da casa: **branch + Pull Request**, nunca commit direto na `main`.
- Quem revisa hoje é o Lucas. Conforme o time cresce, passa a ter revisão entre pares.
- Segredo (senha, token, chave) **nunca** entra no Git. Nem como valor padrão no código.

## Banco de dados

- **PostgreSQL 17.6**, hospedado no **Supabase** (que é Postgres gerenciado + autenticação
  + API automática).
- O banco é organizado em **schemas**, um por domínio:
  - **`core`** — o núcleo: identidade, papéis, auditoria. É de todos, e ninguém edita por
    fora do contrato.
  - **`academy`** — o módulo da Family Academy (turmas, aulas, presença). É o exemplo de um
    módulo já plugado no núcleo.
  - **`p3`** — o seu, quando existir.
- Mudança de schema é **migration versionada em arquivo**, dentro do repositório. No seu
  caso, Flyway — que já é o que você usa. Ele cuida **só do schema `p3`**.

## A regra que organiza tudo

> **O núcleo responde "quem é a pessoa e o que ela pode fazer".
> O seu módulo responde todo o resto.**

Identidade, login, papel e permissão vêm do núcleo. Curso, turma, aula, presença,
avaliação, formatura, histórico — isso é seu, e ninguém encosta.

O detalhe de como fazer isso está em `02-nucleo-contrato.md` e `03-integrar-servico-java.md`.

## Padrões de desenvolvimento

Existe um documento em 7 blocos (segurança e dados, git e revisão, ambientes e publicação,
stack e low-code, design, documentação e continuidade, princípios). Está em **rascunho, sem
revisão de ninguém ainda** — e aceita Pull Request, inclusive o seu.

O único bloco que já vale como regra firme é o de **segurança e dados**, e ele se resume a:
segredo nunca no Git; dado de membro só em conta institucional da igreja; e nada de rodar
contra base real sem combinar. O resto é conversa.
